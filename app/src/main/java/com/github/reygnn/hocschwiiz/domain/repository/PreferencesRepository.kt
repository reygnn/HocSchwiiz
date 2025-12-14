package com.github.reygnn.hocschwiiz.domain.repository

import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app preferences.
 * Implementation uses DataStore for persistence.
 */
interface PreferencesRepository {

    /**
     * Get all preferences as a Flow.
     * Emits new value whenever any preference changes.
     */
    val preferences: Flow<AppPreferences>

    /**
     * Get the currently selected dialect.
     */
    val selectedDialect: Flow<Dialect>

    /**
     * Update the selected dialect.
     */
    suspend fun setDialect(dialect: Dialect)

    /**
     * Get the quiz question count.
     */
    val quizQuestionCount: Flow<Int>

    /**
     * Update the quiz question count.
     */
    suspend fun setQuizQuestionCount(count: Int)

    /**
     * Get the preferred quiz type.
     */
    val preferredQuizType: Flow<QuizType>

    /**
     * Update the preferred quiz type.
     */
    suspend fun setPreferredQuizType(quizType: QuizType)

    /**
     * Get whether Vietnamese translations should be shown.
     */
    val showVietnamese: Flow<Boolean>

    /**
     * Update Vietnamese visibility.
     */
    suspend fun setShowVietnamese(show: Boolean)

    /**
     * Get the dark mode setting.
     */
    val darkMode: Flow<DarkMode>

    /**
     * Update dark mode setting.
     */
    suspend fun setDarkMode(mode: DarkMode)

    /**
     * Reset all preferences to defaults.
     */
    suspend fun resetToDefaults()
}