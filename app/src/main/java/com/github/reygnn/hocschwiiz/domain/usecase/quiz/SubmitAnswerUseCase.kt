package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Record a quiz answer and update learning progress.
 *
 * Updates correctCount/wrongCount, lastPracticed timestamp, and streak.
 * Streak resets to 0 on wrong answer, increments on correct answer.
 */
class SubmitAnswerUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    /**
     * @param wordId The word that was answered
     * @param isCorrect Whether the answer was correct
     * @param timestamp Optional timestamp for testing (defaults to current time)
     */
    suspend operator fun invoke(
        wordId: String,
        isCorrect: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = progressRepository.getProgress(wordId)

        val updated = if (existing != null) {
            existing.copy(
                correctCount = existing.correctCount + if (isCorrect) 1 else 0,
                wrongCount = existing.wrongCount + if (isCorrect) 0 else 1,
                lastPracticed = timestamp,
                streak = if (isCorrect) existing.streak + 1 else 0
            )
        } else {
            LearningProgress(
                wordId = wordId,
                correctCount = if (isCorrect) 1 else 0,
                wrongCount = if (isCorrect) 0 else 1,
                lastPracticed = timestamp,
                streak = if (isCorrect) 1 else 0
            )
        }

        progressRepository.saveProgress(updated)
    }
}