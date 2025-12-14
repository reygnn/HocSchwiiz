package com.github.reygnn.hocschwiiz.domain.usecase.progress

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.util.TimeProvider
import com.github.reygnn.hocschwiiz.fakes.FakeProgressRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class UpdateProgressUseCaseTest {

    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var useCase: UpdateProgressUseCase

    @Before
    fun setup() {
        progressRepository = FakeProgressRepository()
        timeProvider = FakeTimeProvider()
        useCase = UpdateProgressUseCase(progressRepository, timeProvider)
    }

    // ==================== New word ====================

    @Test
    fun `creates new progress for unknown word with correct answer`() = runTest {
        useCase("word_001", correct = true)

        val progress = progressRepository.getProgress("word_001")
        assertNotNull(progress)
        assertEquals(1, progress!!.correctCount)
        assertEquals(0, progress.wrongCount)
        assertEquals(1, progress.streak)
    }

    @Test
    fun `creates new progress for unknown word with wrong answer`() = runTest {
        useCase("word_001", correct = false)

        val progress = progressRepository.getProgress("word_001")
        assertNotNull(progress)
        assertEquals(0, progress!!.correctCount)
        assertEquals(1, progress.wrongCount)
        assertEquals(0, progress.streak)
    }

    // ==================== Existing word ====================

    @Test
    fun `increments correctCount for correct answer`() = runTest {
        progressRepository.setProgress("word_001", LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            streak = 3
        ))

        useCase("word_001", correct = true)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(6, progress!!.correctCount)
        assertEquals(2, progress.wrongCount)
    }

    @Test
    fun `increments wrongCount for wrong answer`() = runTest {
        progressRepository.setProgress("word_001", LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            streak = 3
        ))

        useCase("word_001", correct = false)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(5, progress!!.correctCount)
        assertEquals(3, progress.wrongCount)
    }

    // ==================== Streak ====================

    @Test
    fun `increments streak on correct answer`() = runTest {
        progressRepository.setProgress("word_001", LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            streak = 3
        ))

        useCase("word_001", correct = true)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(4, progress!!.streak)
    }

    @Test
    fun `resets streak on wrong answer`() = runTest {
        progressRepository.setProgress("word_001", LearningProgress(
            wordId = "word_001",
            correctCount = 5,
            wrongCount = 2,
            streak = 10
        ))

        useCase("word_001", correct = false)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(0, progress!!.streak)
    }

    @Test
    fun `streak starts at 1 for new word with correct answer`() = runTest {
        useCase("word_001", correct = true)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(1, progress!!.streak)
    }

    @Test
    fun `streak is 0 for new word with wrong answer`() = runTest {
        useCase("word_001", correct = false)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(0, progress!!.streak)
    }

    // ==================== Timestamp ====================

    @Test
    fun `updates lastPracticed timestamp`() = runTest {
        timeProvider.currentTime = 1000L

        useCase("word_001", correct = true)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(1000L, progress!!.lastPracticed)
    }

    @Test
    fun `updates lastPracticed on each practice`() = runTest {
        timeProvider.currentTime = 1000L
        useCase("word_001", correct = true)

        timeProvider.currentTime = 2000L
        useCase("word_001", correct = false)

        val progress = progressRepository.getProgress("word_001")
        assertEquals(2000L, progress!!.lastPracticed)
    }

    // ==================== Multiple words ====================

    @Test
    fun `handles multiple words independently`() = runTest {
        useCase("word_001", correct = true)
        useCase("word_002", correct = false)
        useCase("word_001", correct = true)

        val progress1 = progressRepository.getProgress("word_001")
        val progress2 = progressRepository.getProgress("word_002")

        assertEquals(2, progress1!!.correctCount)
        assertEquals(0, progress1.wrongCount)
        assertEquals(2, progress1.streak)

        assertEquals(0, progress2!!.correctCount)
        assertEquals(1, progress2.wrongCount)
        assertEquals(0, progress2.streak)
    }
}

/**
 * Fake TimeProvider for testing.
 */
class FakeTimeProvider : TimeProvider {
    var currentTime: Long = 0L

    override fun nowMillis(): Long = currentTime
}