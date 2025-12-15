package com.github.reygnn.hocschwiiz.presentation.home

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.usecase.home.CategoryProgress
import com.github.reygnn.hocschwiiz.domain.usecase.progress.ProgressStats

data class HomeUiState(
    val dialect: Dialect = Dialect.AARGAU,
    val greeting: String = "Grüezi!",
    val stats: ProgressStats? = null,
    val dailyStreak: Int = 0,
    val weakWordCount: Int = 0,
    val wordOfDay: Word? = null,
    val categoryProgress: List<CategoryProgress> = emptyList(),
    val isLoading: Boolean = true
)