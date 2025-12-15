package com.github.reygnn.hocschwiiz.domain.usecase.home

import app.cash.turbine.test
import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class GetDailyStreakUseCaseTest {

    private lateinit var progressRepository: ProgressRepository
    private lateinit var useCase: GetDailyStreakUseCase

    private val fixedZone = ZoneId.of("Europe/Zurich")
    private val today = LocalDate.of(2025, 6, 15) // A Sunday

    @Before
    fun setup() {
        progressRepository = mockk()
        useCase = GetDailyStreakUseCase(progressRepository)
    }

    @Test
    fun `returns 0 when no progress exists`() = runTest {
        every { progressRepository.getAllProgress() } returns flowOf(emptyList())

        useCase(today, fixedZone).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns 0 when no lastPracticed timestamps exist`() = runTest {
        val progressWithoutTimestamps = listOf(
            LearningProgress("word_1", correctCount = 1, wrongCount = 0, lastPracticed = null),
            LearningProgress("word_2", correctCount = 2, wrongCount = 1, lastPracticed = null)
        )
        every { progressRepository.getAllProgress() } returns flowOf(progressWithoutTimestamps)

        useCase(today, fixedZone).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns 1 when practiced today only`() = runTest {
        val todayTimestamp = dateToMillis(today)
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(LearningProgress("word_1", lastPracticed = todayTimestamp))
        )

        useCase(today, fixedZone).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns 1 when practiced yesterday only and not today yet`() = runTest {
        val yesterdayTimestamp = dateToMillis(today.minusDays(1))
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(LearningProgress("word_1", lastPracticed = yesterdayTimestamp))
        )

        useCase(today, fixedZone).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns 2 when practiced today and yesterday`() = runTest {
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(
                LearningProgress("word_1", lastPracticed = dateToMillis(today)),
                LearningProgress("word_2", lastPracticed = dateToMillis(today.minusDays(1)))
            )
        )

        useCase(today, fixedZone).test {
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns 0 when last practice was 2 days ago`() = runTest {
        val twoDaysAgo = dateToMillis(today.minusDays(2))
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(LearningProgress("word_1", lastPracticed = twoDaysAgo))
        )

        useCase(today, fixedZone).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns correct streak for 5 consecutive days`() = runTest {
        val progressList = (0L..4L).map { daysAgo ->
            LearningProgress("word_$daysAgo", lastPracticed = dateToMillis(today.minusDays(daysAgo)))
        }
        every { progressRepository.getAllProgress() } returns flowOf(progressList)

        useCase(today, fixedZone).test {
            assertEquals(5, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `handles gap in streak correctly`() = runTest {
        // Practiced today, yesterday, and 4 days ago (gap at day 3)
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(
                LearningProgress("word_1", lastPracticed = dateToMillis(today)),
                LearningProgress("word_2", lastPracticed = dateToMillis(today.minusDays(1))),
                LearningProgress("word_3", lastPracticed = dateToMillis(today.minusDays(4)))
            )
        )

        useCase(today, fixedZone).test {
            // Only counts today + yesterday = 2
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `counts multiple practices on same day as one day`() = runTest {
        // Multiple practices today
        val todayMorning = today.atTime(LocalTime.of(9, 0))
            .atZone(fixedZone).toInstant().toEpochMilli()
        val todayEvening = today.atTime(LocalTime.of(20, 0))
            .atZone(fixedZone).toInstant().toEpochMilli()

        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(
                LearningProgress("word_1", lastPracticed = todayMorning),
                LearningProgress("word_2", lastPracticed = todayEvening)
            )
        )

        useCase(today, fixedZone).test {
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `handles long streak correctly`() = runTest {
        // 30 day streak
        val progressList = (0L..29L).map { daysAgo ->
            LearningProgress("word_$daysAgo", lastPracticed = dateToMillis(today.minusDays(daysAgo)))
        }
        every { progressRepository.getAllProgress() } returns flowOf(progressList)

        useCase(today, fixedZone).test {
            assertEquals(30, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `streak starts from yesterday when not practiced today`() = runTest {
        // 3 day streak ending yesterday
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(
                LearningProgress("word_1", lastPracticed = dateToMillis(today.minusDays(1))),
                LearningProgress("word_2", lastPracticed = dateToMillis(today.minusDays(2))),
                LearningProgress("word_3", lastPracticed = dateToMillis(today.minusDays(3)))
            )
        )

        useCase(today, fixedZone).test {
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ignores old practice when recent streak exists`() = runTest {
        // Practiced today, yesterday, and way back (should ignore old)
        every { progressRepository.getAllProgress() } returns flowOf(
            listOf(
                LearningProgress("word_1", lastPracticed = dateToMillis(today)),
                LearningProgress("word_2", lastPracticed = dateToMillis(today.minusDays(1))),
                LearningProgress("word_3", lastPracticed = dateToMillis(today.minusDays(100)))
            )
        )

        useCase(today, fixedZone).test {
            assertEquals(2, awaitItem())
            awaitComplete()
        }
    }

    private fun dateToMillis(date: LocalDate): Long {
        return date.atTime(LocalTime.NOON)
            .atZone(fixedZone)
            .toInstant()
            .toEpochMilli()
    }
}