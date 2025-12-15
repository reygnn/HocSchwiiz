package com.github.reygnn.hocschwiiz.domain.usecase.home

import java.time.LocalTime
import javax.inject.Inject

/**
 * Returns a Swiss German greeting based on the current time of day.
 *
 * Time ranges:
 * - 05:00 - 11:59 → "Guete Morge!"
 * - 12:00 - 17:59 → "Guete Tag!"
 * - 18:00 - 04:59 → "Guete Aabig!"
 */
class GetTimeBasedGreetingUseCase @Inject constructor() {

    operator fun invoke(currentTime: LocalTime = LocalTime.now()): String {
        return when (currentTime.hour) {
            in 5..11 -> "Guete Morge!"
            in 12..17 -> "Guete Tag!"
            else -> "Guete Aabig!"
        }
    }
}