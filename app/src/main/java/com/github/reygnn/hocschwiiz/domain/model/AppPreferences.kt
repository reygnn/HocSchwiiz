package com.github.reygnn.hocschwiiz.domain.model

data class AppPreferences(
    val selectedDialect: Dialect = Dialect.AARGAU,
    val quizQuestionCount: Int = 10,
    val preferredQuizType: QuizType = QuizType.MIXED,
    val showVietnamese: Boolean = true,
    val darkMode: DarkMode = DarkMode.SYSTEM
) {
    companion object {
        const val MIN_QUIZ_QUESTIONS = 5
        const val MAX_QUIZ_QUESTIONS = 20
        const val DEFAULT_QUIZ_QUESTIONS = 10
    }
}