package com.github.reygnn.hocschwiiz.domain.model

/**
 * Result of a completed quiz.
 */
data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val wrongAnswers: List<QuizQuestion>
) {
    val scorePercent: Int
        get() = if (totalQuestions > 0) {
            (correctAnswers * 100) / totalQuestions
        } else 0

    val isPerfect: Boolean
        get() = correctAnswers == totalQuestions && totalQuestions > 0

    val isGood: Boolean
        get() = scorePercent >= 70
}