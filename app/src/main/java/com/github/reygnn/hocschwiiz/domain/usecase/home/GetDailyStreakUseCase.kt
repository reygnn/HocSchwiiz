package com.github.reygnn.hocschwiiz.domain.usecase.home

import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Calculates the current daily learning streak.
 *
 * A streak counts consecutive days where the user practiced at least once.
 * The streak is preserved until midnight - if the user practiced yesterday
 * but not yet today, the streak still counts.
 *
 * Examples:
 * - Practiced today only → 1
 * - Practiced today and yesterday → 2
 * - Practiced yesterday only (not today yet) → 1
 * - Last practice was 2 days ago → 0
 */
class GetDailyStreakUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {

    operator fun invoke(
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Flow<Int> {
        return progressRepository.getAllProgress().map { progressList ->
            calculateStreak(progressList.mapNotNull { it.lastPracticed }, today, zoneId)
        }
    }

    private fun calculateStreak(
        timestamps: List<Long>,
        today: LocalDate,
        zoneId: ZoneId
    ): Int {
        if (timestamps.isEmpty()) return 0

        // Convert timestamps to distinct dates, sorted descending
        val practiceDates = timestamps
            .map { millis ->
                Instant.ofEpochMilli(millis)
                    .atZone(zoneId)
                    .toLocalDate()
            }
            .distinct()
            .sortedDescending()

        val mostRecentPractice = practiceDates.first()
        val yesterday = today.minusDays(1)

        // Streak is broken if last practice was before yesterday
        if (mostRecentPractice < yesterday) {
            return 0
        }

        // Count consecutive days backwards from most recent practice
        var streak = 0
        var expectedDate = mostRecentPractice

        for (date in practiceDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (date < expectedDate) {
                // Gap in dates - streak ends here
                break
            }
            // date > expectedDate shouldn't happen with sorted list, but ignore if it does
        }

        return streak
    }
}