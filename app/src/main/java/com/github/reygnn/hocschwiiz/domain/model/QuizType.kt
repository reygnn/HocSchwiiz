package com.github.reygnn.hocschwiiz.domain.model

enum class QuizType(val displayName: String) {
    GERMAN_TO_SWISS("DE → CH"),
    SWISS_TO_GERMAN("CH → DE"),
    SWISS_TO_VIETNAMESE("CH → VN"),
    VIETNAMESE_TO_SWISS("VN → CH"),
    GERMAN_TO_VIETNAMESE("DE → VN"),
    VIETNAMESE_TO_GERMAN("VN → DE"),
    MIXED("Gemischt")
}