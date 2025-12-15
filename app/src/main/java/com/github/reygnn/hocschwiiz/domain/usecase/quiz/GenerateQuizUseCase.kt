package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizQuestion
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GenerateQuizUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * Generate quiz questions.
     *
     * @param categoryId Category ID to filter words, or null for all categories
     * @param dialect Dialect to use
     * @param questionCount Number of questions to generate
     * @param quizType Type of quiz (determines question/answer format)
     */
    suspend operator fun invoke(
        categoryId: String? = null,
        dialect: Dialect,
        questionCount: Int,
        quizType: QuizType,
        preSelectedWords: List<Word>? = null
    ): List<QuizQuestion> {
        // Get words for quiz
        val words = preSelectedWords ?: if (categoryId != null) {
            wordRepository.getByCategory(categoryId, dialect).first()
        } else {
            wordRepository.getAll(dialect).first()
        }

        if (words.isEmpty()) return emptyList()

        // Shuffle and take requested count
        val selectedWords = words.shuffled().take(questionCount)

        return selectedWords.map { word ->
            // Bei MIXED: zufälligen konkreten QuizType auswählen
            val effectiveQuizType = if (quizType == QuizType.MIXED) {
                listOf(
                    QuizType.GERMAN_TO_SWISS,
                    QuizType.SWISS_TO_GERMAN,
                    QuizType.SWISS_TO_VIETNAMESE,
                    QuizType.VIETNAMESE_TO_SWISS,
                    QuizType.GERMAN_TO_VIETNAMESE,
                    QuizType.VIETNAMESE_TO_GERMAN
                ).random()
            } else {
                quizType
            }

            // Get distractors from same category if possible
            val distractorAnswers = getDistractors(
                correctWord = word,
                allWords = words,
                quizType = effectiveQuizType
            )

            val correctAnswer = getAnswer(word, effectiveQuizType)
            val questionText = getQuestionText(word, effectiveQuizType)

            QuizQuestion(
                word = word,
                quizType = effectiveQuizType,
                questionText = questionText,
                correctAnswer = correctAnswer,
                options = (distractorAnswers + correctAnswer).shuffled()
            )
        }
    }

    private fun getDistractors(
        correctWord: Word,
        allWords: List<Word>,
        quizType: QuizType
    ): List<String> {
        // Try to get distractors from same category first
        val sameCategoryWords = allWords.filter {
            it.category.id == correctWord.category.id && it.id != correctWord.id
        }
        val otherWords = allWords.filter {
            it.category.id != correctWord.category.id && it.id != correctWord.id
        }

        val potentialDistractors = (sameCategoryWords.shuffled() + otherWords.shuffled())
            .take(3)

        return potentialDistractors.map { word -> getAnswer(word, quizType) }
    }

    private fun getQuestionText(word: Word, quizType: QuizType): String {
        return when (quizType) {
            QuizType.SWISS_TO_GERMAN -> word.swiss
            QuizType.GERMAN_TO_SWISS -> word.german
            QuizType.SWISS_TO_VIETNAMESE -> word.swiss
            QuizType.VIETNAMESE_TO_SWISS -> word.vietnamese
            QuizType.GERMAN_TO_VIETNAMESE -> word.german
            QuizType.VIETNAMESE_TO_GERMAN -> word.vietnamese
            QuizType.MIXED -> error("MIXED should be resolved before calling getQuestionText")
        }
    }

    private fun getAnswer(word: Word, quizType: QuizType): String {
        return when (quizType) {
            QuizType.SWISS_TO_GERMAN -> word.german
            QuizType.GERMAN_TO_SWISS -> word.swiss
            QuizType.SWISS_TO_VIETNAMESE -> word.vietnamese
            QuizType.VIETNAMESE_TO_SWISS -> word.swiss
            QuizType.GERMAN_TO_VIETNAMESE -> word.vietnamese
            QuizType.VIETNAMESE_TO_GERMAN -> word.german
            QuizType.MIXED -> error("MIXED should be resolved before calling getAnswer")
        }
    }
}