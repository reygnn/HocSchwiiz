package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubmitAnswerUseCaseTest {

    private lateinit var progressRepository: ProgressRepository
    private lateinit var useCase: SubmitAnswerUseCase

    private val testTimestamp = 1700000000000L

    @Before
    fun setup() {
        progressRepository = mockk(relaxed = true)
        useCase = SubmitAnswerUseCase(progressRepository)
    }

    // region New Word Tests

    @Test
    fun `new word with correct answer creates progress with correctCount 1 and streak 1`() = runTest {
        // Given: Word has no existing progress
        coEvery { progressRepository.getProgress("word_001") } returns null

        // When
        useCase("word_001", isCorrect = true, timestamp = testTimestamp)

        // Then
        val slot = slot<LearningProgress>()
        coVerify { progressRepository.saveProgress(capture(slot)) }

        with(slot.captured) {
            assertEquals("word_001", wordId)
            assertEquals(1, correctCount)
            assertEquals(0, wrongCount)
            assertEquals(1, streak)
            assertEquals(testTimestamp, lastPracticed)
        }
    }

    @Test
    fun `new word with wrong answer creates progress with wrongCount 1 and streak 0`() = runTest {
        // Given
        coEvery { progressRepository.getProgress("word_001") } returns null

        // When
        useCase("word_001", isCorrect = false, timestamp = testTimestamp)

        // Then
        val slot = slot<LearningProgress>()
        coVerify { progressRepository.saveProgress(capture(slot)) }

        with(slot.captured) {
            assertEquals("word_001", wordId)
            assertEquals(0, correctCount)
            assertEquals(1, wrongCount)
            assertEquals(0, streak)
            assertEquals(testTimestamp, lastPracticed)
        }
    }

    // endregion

    // region Existing Word Tests

    @Test
    fun `existing word with correct answer increments correctCount and streak`() = runTest {
        // Given: Word has existing progress
        val existing = LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            lastPracticed = 1600000000000L,
            streak = 3
        )
        coEvery { progressRepository.getProgress("word_001") } returns existing

        // When
        useCase("word_001", isCorrect = true, timestamp = testTimestamp)

        // Then
        val slot = slot<LearningProgress>()
        coVerify { progressRepository.saveProgress(capture(slot)) }

        with(slot.captured) {
            assertEquals("word_001", wordId)
            assertEquals(6, correctCount)  // 5 + 1
            assertEquals(2, wrongCount)    // unchanged
            assertEquals(4, streak)        // 3 + 1
            assertEquals(testTimestamp, lastPracticed)
        }
    }

    @Test
    fun `existing word with wrong answer increments wrongCount and resets streak to 0`() = runTest {
        // Given: Word has existing progress with a streak
        val existing = LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            lastPracticed = 1600000000000L,
            streak = 7  // High streak to verify reset
        )
        coEvery { progressRepository.getProgress("word_001") } returns existing

        // When
        useCase("word_001", isCorrect = false, timestamp = testTimestamp)

        // Then
        val slot = slot<LearningProgress>()
        coVerify { progressRepository.saveProgress(capture(slot)) }

        with(slot.captured) {
            assertEquals("word_001", wordId)
            assertEquals(5, correctCount)  // unchanged
            assertEquals(3, wrongCount)    // 2 + 1
            assertEquals(0, streak)        // RESET!
            assertEquals(testTimestamp, lastPracticed)
        }
    }

    // endregion

    // region Timestamp Tests

    @Test
    fun `lastPracticed is always updated regardless of answer correctness`() = runTest {
        // Given
        val oldTimestamp = 1600000000000L
        val existing = LearningProgress(
            wordId = "word_001",
            correctCount = 1,
            wrongCount = 1,
            lastPracticed = oldTimestamp,
            streak = 0
        )
        coEvery { progressRepository.getProgress("word_001") } returns existing

        // When - wrong answer
        useCase("word_001", isCorrect = false, timestamp = testTimestamp)

        // Then
        val slot = slot<LearningProgress>()
        coVerify { progressRepository.saveProgress(capture(slot)) }
        assertEquals(testTimestamp, slot.captured.lastPracticed)
    }

    // endregion

    // region Edge Cases

    @Test
    fun `streak builds correctly over multiple correct answers`() = runTest {
        // Simulate 3 correct answers in sequence
        var currentProgress: LearningProgress? = null

        coEvery { progressRepository.getProgress("word_001") } answers { currentProgress }
        coEvery { progressRepository.saveProgress(any()) } answers {
            currentProgress = firstArg()
        }

        // First correct
        useCase("word_001", isCorrect = true, timestamp = testTimestamp)
        assertEquals(1, currentProgress?.streak)

        // Second correct
        useCase("word_001", isCorrect = true, timestamp = testTimestamp + 1000)
        assertEquals(2, currentProgress?.streak)

        // Third correct
        useCase("word_001", isCorrect = true, timestamp = testTimestamp + 2000)
        assertEquals(3, currentProgress?.streak)
        assertEquals(3, currentProgress?.correctCount)
        assertEquals(0, currentProgress?.wrongCount)
    }

    @Test
    fun `streak resets and rebuilds correctly`() = runTest {
        // Simulate: correct, correct, wrong, correct
        var currentProgress: LearningProgress? = null

        coEvery { progressRepository.getProgress("word_001") } answers { currentProgress }
        coEvery { progressRepository.saveProgress(any()) } answers {
            currentProgress = firstArg()
        }

        useCase("word_001", isCorrect = true, timestamp = testTimestamp)
        assertEquals(1, currentProgress?.streak)

        useCase("word_001", isCorrect = true, timestamp = testTimestamp + 1000)
        assertEquals(2, currentProgress?.streak)

        useCase("word_001", isCorrect = false, timestamp = testTimestamp + 2000)
        assertEquals(0, currentProgress?.streak)  // Reset!

        useCase("word_001", isCorrect = true, timestamp = testTimestamp + 3000)
        assertEquals(1, currentProgress?.streak)  // Starts fresh

        // Final counts
        assertEquals(3, currentProgress?.correctCount)
        assertEquals(1, currentProgress?.wrongCount)
    }

    @Test
    fun `different words have independent progress`() = runTest {
        coEvery { progressRepository.getProgress(any()) } returns null

        val savedProgress = mutableListOf<LearningProgress>()
        coEvery { progressRepository.saveProgress(any()) } answers {
            savedProgress.add(firstArg())
        }

        // When
        useCase("word_001", isCorrect = true, timestamp = testTimestamp)
        useCase("word_002", isCorrect = false, timestamp = testTimestamp)

        // Then
        assertEquals(2, savedProgress.size)
        assertEquals("word_001", savedProgress[0].wordId)
        assertEquals(1, savedProgress[0].correctCount)
        assertEquals("word_002", savedProgress[1].wordId)
        assertEquals(1, savedProgress[1].wrongCount)
    }

    // endregion
}