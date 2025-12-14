package com.github.reygnn.hocschwiiz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningProgressTest {

    @Test
    fun `totalAttempts - zero when new`() {
        val progress = LearningProgress(wordId = "test_001")
        assertEquals(0, progress.totalAttempts)
    }

    @Test
    fun `totalAttempts - sum of correct and wrong`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 5,
            wrongCount = 3
        )
        assertEquals(8, progress.totalAttempts)
    }

    @Test
    fun `successRate - zero when no attempts`() {
        val progress = LearningProgress(wordId = "test_001")
        assertEquals(0f, progress.successRate)
    }

    @Test
    fun `successRate - 100 percent when all correct`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 10,
            wrongCount = 0
        )
        assertEquals(1f, progress.successRate)
    }

    @Test
    fun `successRate - 0 percent when all wrong`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 0,
            wrongCount = 5
        )
        assertEquals(0f, progress.successRate)
    }

    @Test
    fun `successRate - 50 percent when half correct`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 5,
            wrongCount = 5
        )
        assertEquals(0.5f, progress.successRate)
    }

    @Test
    fun `successRate - calculated correctly`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 3,
            wrongCount = 7
        )
        assertEquals(0.3f, progress.successRate)
    }

    @Test
    fun `isWeak - false when no attempts`() {
        val progress = LearningProgress(wordId = "test_001")
        assertFalse(progress.isWeak)
    }

    @Test
    fun `isWeak - false when less than 3 attempts`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 0,
            wrongCount = 2
        )
        assertFalse(progress.isWeak)
    }

    @Test
    fun `isWeak - false when success rate above 50 percent`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 4,
            wrongCount = 3
        )
        assertFalse(progress.isWeak)
    }

    @Test
    fun `isWeak - true when 3 plus attempts and below 50 percent`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 1,
            wrongCount = 4
        )
        assertTrue(progress.isWeak)
    }

    @Test
    fun `isWeak - false when exactly 50 percent`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 2,
            wrongCount = 2
        )
        assertFalse(progress.isWeak)
    }

    @Test
    fun `isWeak - edge case at boundary`() {
        val progress = LearningProgress(
            wordId = "test_001",
            correctCount = 1,
            wrongCount = 2  // 3 attempts, 33% success
        )
        assertTrue(progress.isWeak)
    }

    @Test
    fun `streak preserved in copy`() {
        val progress = LearningProgress(
            wordId = "test_001",
            streak = 5
        )
        val updated = progress.copy(correctCount = 1)
        assertEquals(5, updated.streak)
    }

    @Test
    fun `default values`() {
        val progress = LearningProgress(wordId = "test_001")
        assertEquals(0, progress.correctCount)
        assertEquals(0, progress.wrongCount)
        assertEquals(null, progress.lastPracticed)
        assertEquals(0, progress.streak)
    }
}