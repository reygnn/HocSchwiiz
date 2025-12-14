package com.github.reygnn.hocschwiiz.domain.model

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: List<QuizQuestion>,
    val quizType: QuizType,
    val durationMillis: Long = 0L
) {
    val scorePercent: Int
        get() = if (totalQuestions > 0) {
            (correctAnswers * 100) / totalQuestions
        } else 0

    val isPerfect: Boolean
        get() = correctAnswers == totalQuestions

    val isGood: Boolean
        get() = scorePercent >= 80
}