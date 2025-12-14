package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.QuizQuestion
import com.github.reygnn.hocschwiiz.domain.usecase.progress.UpdateProgressUseCase
import javax.inject.Inject

/**
 * Check if a quiz answer is correct and update progress.
 */
class CheckAnswerUseCase @Inject constructor(
    private val updateProgressUseCase: UpdateProgressUseCase
) {
    /**
     * Check the answer and update learning progress.
     *
     * @param question The quiz question
     * @param selectedAnswer The user's selected answer
     * @return Updated question with answeredCorrectly set
     */
    suspend operator fun invoke(
        question: QuizQuestion,
        selectedAnswer: String
    ): QuizQuestion {
        val isCorrect = selectedAnswer == question.correctAnswer

        // Update progress in database
        updateProgressUseCase(question.word.id, isCorrect)

        return question.copy(answeredCorrectly = isCorrect)
    }
}