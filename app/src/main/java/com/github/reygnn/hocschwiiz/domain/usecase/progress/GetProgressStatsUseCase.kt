package com.github.reygnn.hocschwiiz.domain.usecase.progress

import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Statistics about learning progress.
 */
data class ProgressStats(
    val practicedWordCount: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val maxStreak: Int
) {
    val totalAttempts: Int
        get() = totalCorrect + totalWrong

    val overallSuccessRate: Float
        get() = if (totalAttempts > 0) {
            totalCorrect.toFloat() / totalAttempts
        } else 0f
}

/**
 * Get overall learning statistics.
 */
class GetProgressStatsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<ProgressStats> {
        return combine(
            progressRepository.getPracticedWordCount(),
            progressRepository.getTotalCorrectCount(),
            progressRepository.getTotalWrongCount(),
            progressRepository.getMaxStreak()
        ) { practiced, correct, wrong, streak ->
            ProgressStats(
                practicedWordCount = practiced,
                totalCorrect = correct,
                totalWrong = wrong,
                maxStreak = streak
            )
        }
    }
}