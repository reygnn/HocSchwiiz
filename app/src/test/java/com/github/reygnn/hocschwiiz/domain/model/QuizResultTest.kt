package com.github.reygnn.hocschwiiz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizResultTest {

    private fun createResult(
        total: Int,
        correct: Int,
        wrongCount: Int = 0
    ) = QuizResult(
        totalQuestions = total,
        correctAnswers = correct,
        wrongAnswers = emptyList(),
        quizType = QuizType.MIXED
    )

    @Test
    fun `scorePercent - 0 when no questions`() {
        val result = createResult(total = 0, correct = 0)
        assertEquals(0, result.scorePercent)
    }

    @Test
    fun `scorePercent - 100 when all correct`() {
        val result = createResult(total = 10, correct = 10)
        assertEquals(100, result.scorePercent)
    }

    @Test
    fun `scorePercent - 0 when all wrong`() {
        val result = createResult(total = 10, correct = 0)
        assertEquals(0, result.scorePercent)
    }

    @Test
    fun `scorePercent - calculated correctly`() {
        val result = createResult(total = 10, correct = 8)
        assertEquals(80, result.scorePercent)
    }

    @Test
    fun `scorePercent - rounds down`() {
        val result = createResult(total = 3, correct = 1)
        assertEquals(33, result.scorePercent)  // 33.33... -> 33
    }

    @Test
    fun `scorePercent - rounds correctly for 2 of 3`() {
        val result = createResult(total = 3, correct = 2)
        assertEquals(66, result.scorePercent)  // 66.66... -> 66
    }

    @Test
    fun `isPerfect - true when all correct`() {
        val result = createResult(total = 10, correct = 10)
        assertTrue(result.isPerfect)
    }

    @Test
    fun `isPerfect - false when one wrong`() {
        val result = createResult(total = 10, correct = 9)
        assertFalse(result.isPerfect)
    }

    @Test
    fun `isPerfect - true when empty quiz`() {
        val result = createResult(total = 0, correct = 0)
        assertTrue(result.isPerfect)
    }

    @Test
    fun `isGood - true when 80 percent`() {
        val result = createResult(total = 10, correct = 8)
        assertTrue(result.isGood)
    }

    @Test
    fun `isGood - true when above 80 percent`() {
        val result = createResult(total = 10, correct = 9)
        assertTrue(result.isGood)
    }

    @Test
    fun `isGood - false when below 80 percent`() {
        val result = createResult(total = 10, correct = 7)
        assertFalse(result.isGood)
    }

    @Test
    fun `isGood - true when perfect`() {
        val result = createResult(total = 10, correct = 10)
        assertTrue(result.isGood)
    }

    @Test
    fun `isGood - false when 79 percent`() {
        val result = createResult(total = 100, correct = 79)
        assertFalse(result.isGood)
    }

    @Test
    fun `isGood - edge case rounding`() {
        // 8/10 = 80% -> isGood
        val result = createResult(total = 10, correct = 8)
        assertTrue(result.isGood)
    }

    @Test
    fun `durationMillis default is 0`() {
        val result = createResult(total = 10, correct = 8)
        assertEquals(0L, result.durationMillis)
    }

    @Test
    fun `durationMillis preserved`() {
        val result = QuizResult(
            totalQuestions = 10,
            correctAnswers = 8,
            wrongAnswers = emptyList(),
            quizType = QuizType.MIXED,
            durationMillis = 45000L
        )
        assertEquals(45000L, result.durationMillis)
    }
}