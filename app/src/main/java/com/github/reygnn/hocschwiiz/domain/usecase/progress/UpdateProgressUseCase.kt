package com.github.reygnn.hocschwiiz.domain.usecase.progress

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import com.github.reygnn.hocschwiiz.domain.util.TimeProvider
import javax.inject.Inject

/**
 * Update learning progress after answering a quiz question.
 */
class UpdateProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val timeProvider: TimeProvider
) {
    /**
     * Update progress for a word.
     *
     * @param wordId The word ID
     * @param correct Whether the answer was correct
     */
    suspend operator fun invoke(wordId: String, correct: Boolean) {
        val current = progressRepository.getProgress(wordId)
        val now = timeProvider.nowMillis()

        val updated = if (current == null) {
            // First time practicing this word
            LearningProgress(
                wordId = wordId,
                correctCount = if (correct) 1 else 0,
                wrongCount = if (correct) 0 else 1,
                lastPracticed = now,
                streak = if (correct) 1 else 0
            )
        } else {
            current.copy(
                correctCount = current.correctCount + if (correct) 1 else 0,
                wrongCount = current.wrongCount + if (correct) 0 else 1,
                lastPracticed = now,
                streak = if (correct) current.streak + 1 else 0
            )
        }

        progressRepository.saveProgress(updated)
    }
}