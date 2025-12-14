package com.github.reygnn.hocschwiiz.domain.model

data class LearningProgress(
    val wordId: String,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastPracticed: Long? = null,
    val streak: Int = 0
) {
    val totalAttempts: Int
        get() = correctCount + wrongCount

    val successRate: Float
        get() = if (totalAttempts > 0) {
            correctCount.toFloat() / totalAttempts
        } else 0f

    val isWeak: Boolean
        get() = totalAttempts >= 3 && successRate < 0.5f
}