package com.github.reygnn.hocschwiiz.domain.model

data class Word(
    val id: String,
    val german: String,
    val swiss: String,
    val vietnamese: String,
    val category: Category,
    val dialect: Dialect,
    val gender: Gender? = null,
    val altSpellings: List<String> = emptyList(),
    val notes: String? = null,
    val examples: List<String> = emptyList()
)