package com.github.reygnn.hocschwiiz.fakes

import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake repository for testing preferences functionality.
 */
class FakePreferencesRepository : PreferencesRepository {

    private val _preferences = MutableStateFlow(AppPreferences())

    var shouldThrowError = false
    var errorMessage = "Test error"

    // ==================== Test data setup ====================

    fun setPreferences(prefs: AppPreferences) {
        _preferences.value = prefs
    }

    fun getCurrentPreferences(): AppPreferences = _preferences.value

    // ==================== Repository implementation ====================

    override val preferences: Flow<AppPreferences> = _preferences

    override val selectedDialect: Flow<Dialect> = _preferences.map { it.selectedDialect }

    override suspend fun setDialect(dialect: Dialect) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        _preferences.update { it.copy(selectedDialect = dialect) }
    }

    override val quizQuestionCount: Flow<Int> = _preferences.map { it.quizQuestionCount }

    override suspend fun setQuizQuestionCount(count: Int) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        val validCount = count.coerceIn(
            AppPreferences.MIN_QUIZ_QUESTIONS,
            AppPreferences.MAX_QUIZ_QUESTIONS
        )
        _preferences.update { it.copy(quizQuestionCount = validCount) }
    }

    override val preferredQuizType: Flow<QuizType> = _preferences.map { it.preferredQuizType }

    override suspend fun setPreferredQuizType(quizType: QuizType) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        _preferences.update { it.copy(preferredQuizType = quizType) }
    }

    override val showVietnamese: Flow<Boolean> = _preferences.map { it.showVietnamese }

    override suspend fun setShowVietnamese(show: Boolean) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        _preferences.update { it.copy(showVietnamese = show) }
    }

    override val darkMode: Flow<DarkMode> = _preferences.map { it.darkMode }

    override suspend fun setDarkMode(mode: DarkMode) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        _preferences.update { it.copy(darkMode = mode) }
    }

    override suspend fun resetToDefaults() {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        _preferences.value = AppPreferences()
    }
}