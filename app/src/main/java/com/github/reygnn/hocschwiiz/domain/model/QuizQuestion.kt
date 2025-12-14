package com.github.reygnn.hocschwiiz.domain.model

data class QuizQuestion(
    val word: Word,
    val quizType: QuizType,
    val questionText: String,
    val correctAnswer: String,
    val options: List<String>,
    val answeredCorrectly: Boolean? = null
)