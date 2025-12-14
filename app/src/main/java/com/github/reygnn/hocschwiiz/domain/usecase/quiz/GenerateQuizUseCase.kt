package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizQuestion
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Generates quiz questions with intelligent distractor selection.
 */
class GenerateQuizUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) {
    /**
     * Generate quiz questions.
     *
     * @param questionCount Number of questions to generate
     * @param quizType The type of quiz (translation direction)
     * @param dialect The dialect to use
     * @param category Optional category filter
     * @param prioritizeWeak Whether to prioritize words the user struggles with
     * @return List of quiz questions
     */
    suspend operator fun invoke(
        questionCount: Int,
        quizType: QuizType,
        dialect: Dialect,
        category: Category? = null,
        prioritizeWeak: Boolean = true
    ): List<QuizQuestion> {
        // Get words based on category filter
        val allWords = if (category != null) {
            wordRepository.getByCategory(category, dialect).first()
        } else {
            wordRepository.getAll(dialect).first()
        }

        if (allWords.size < MIN_WORDS_FOR_QUIZ) {
            return emptyList()
        }

        // Select words for quiz
        val selectedWords = selectWords(allWords, questionCount, prioritizeWeak)

        // Generate questions
        return selectedWords.map { word ->
            createQuestion(word, quizType, allWords)
        }
    }

    private suspend fun selectWords(
        allWords: List<Word>,
        count: Int,
        prioritizeWeak: Boolean
    ): List<Word> {
        if (!prioritizeWeak) {
            return allWords.shuffled().take(count)
        }

        val weakWordIds = progressRepository.getWeakWordIds().first().toSet()
        val weakWords = allWords.filter { it.id in weakWordIds }
        val otherWords = allWords.filter { it.id !in weakWordIds }

        // Prioritize weak words but mix in others
        return (weakWords.shuffled() + otherWords.shuffled())
            .distinct()
            .take(count)
    }

    private fun createQuestion(
        word: Word,
        quizType: QuizType,
        allWords: List<Word>
    ): QuizQuestion {
        // Resolve MIXED to a specific type
        val actualType = if (quizType == QuizType.MIXED) {
            QuizType.entries
                .filter { it != QuizType.MIXED }
                .random()
        } else {
            quizType
        }

        val (questionText, correctAnswer) = getQuestionAndAnswer(word, actualType)
        val wrongOptions = generateDistractors(word, actualType, allWords)
        val options = (wrongOptions + correctAnswer).shuffled()

        return QuizQuestion(
            word = word,
            quizType = actualType,
            questionText = questionText,
            correctAnswer = correctAnswer,
            options = options
        )
    }

    private fun getQuestionAndAnswer(word: Word, quizType: QuizType): Pair<String, String> {
        return when (quizType) {
            QuizType.GERMAN_TO_SWISS -> word.german to word.swiss
            QuizType.SWISS_TO_GERMAN -> word.swiss to word.german
            QuizType.SWISS_TO_VIETNAMESE -> word.swiss to word.vietnamese
            QuizType.VIETNAMESE_TO_SWISS -> word.vietnamese to word.swiss
            QuizType.GERMAN_TO_VIETNAMESE -> word.german to word.vietnamese
            QuizType.VIETNAMESE_TO_GERMAN -> word.vietnamese to word.german
            QuizType.MIXED -> word.german to word.swiss // Fallback, shouldn't happen
        }
    }

    private fun generateDistractors(
        word: Word,
        quizType: QuizType,
        allWords: List<Word>
    ): List<String> {
        val correctAnswer = getAnswerField(word, quizType)

        // Prefer words from same category (harder distractors)
        val sameCategoryWords = allWords.filter {
            it.id != word.id && it.category == word.category
        }
        val otherWords = allWords.filter {
            it.id != word.id && it.category != word.category
        }

        // Get wrong options, preferring same category
        return (sameCategoryWords.shuffled() + otherWords.shuffled())
            .map { getAnswerField(it, quizType) }
            .filter { it != correctAnswer }
            .distinct()
            .take(DISTRACTOR_COUNT)
    }

    private fun getAnswerField(word: Word, quizType: QuizType): String {
        return when (quizType) {
            QuizType.GERMAN_TO_SWISS -> word.swiss
            QuizType.SWISS_TO_GERMAN -> word.german
            QuizType.SWISS_TO_VIETNAMESE -> word.vietnamese
            QuizType.VIETNAMESE_TO_SWISS -> word.swiss
            QuizType.GERMAN_TO_VIETNAMESE -> word.vietnamese
            QuizType.VIETNAMESE_TO_GERMAN -> word.german
            QuizType.MIXED -> word.swiss
        }
    }

    companion object {
        const val MIN_WORDS_FOR_QUIZ = 4 // Need at least 1 correct + 3 wrong
        const val DISTRACTOR_COUNT = 3
    }
}