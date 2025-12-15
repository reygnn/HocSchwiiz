package com.github.reygnn.hocschwiiz.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val isLoading: Boolean = true,
    val showResetProgressDialog: Boolean = false,
    val showResetPreferencesDialog: Boolean = false,
    val totalWordsLearned: Int = 0,
    val showResetSuccessMessage: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadProgressStats()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.preferences
                .catch { /* Graceful degradation - use defaults */ }
                .collect { prefs ->
                    _uiState.update {
                        it.copy(preferences = prefs, isLoading = false)
                    }
                }
        }
    }

    private fun loadProgressStats() {
        viewModelScope.launch {
            try {
                progressRepository.getTotalWordsLearned()
                    .collect { count ->
                        _uiState.update { it.copy(totalWordsLearned = count) }
                    }
            } catch (_: Exception) {
                // Ignore - stats are optional
            }
        }
    }

    // ========== Dialect ==========

    fun setDialect(dialect: Dialect) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDialect(dialect)
            } catch (_: Exception) {
                // Could show error toast
            }
        }
    }

    // ========== Quiz Settings ==========

    fun setQuizQuestionCount(count: Int) {
        val validCount = count.coerceIn(
            AppPreferences.MIN_QUIZ_QUESTIONS,
            AppPreferences.MAX_QUIZ_QUESTIONS
        )
        viewModelScope.launch {
            try {
                preferencesRepository.setQuizQuestionCount(validCount)
            } catch (_: Exception) {
                // Could show error toast
            }
        }
    }

    fun setPreferredQuizType(quizType: QuizType) {
        viewModelScope.launch {
            try {
                preferencesRepository.setPreferredQuizType(quizType)
            } catch (_: Exception) {
                // Could show error toast
            }
        }
    }

    // ========== Display Settings ==========

    fun setShowVietnamese(show: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setShowVietnamese(show)
            } catch (_: Exception) {
                // Could show error toast
            }
        }
    }

    fun setDarkMode(mode: DarkMode) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDarkMode(mode)
            } catch (_: Exception) {
                // Could show error toast
            }
        }
    }

    // ========== Reset Dialogs ==========

    fun showResetProgressDialog() {
        _uiState.update { it.copy(showResetProgressDialog = true) }
    }

    fun dismissResetProgressDialog() {
        _uiState.update { it.copy(showResetProgressDialog = false) }
    }

    fun confirmResetProgress() {
        viewModelScope.launch {
            try {
                progressRepository.resetAllProgress()
                _uiState.update {
                    it.copy(
                        showResetProgressDialog = false,
                        totalWordsLearned = 0,
                        showResetSuccessMessage = true
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(showResetProgressDialog = false) }
            }
        }
    }

    fun showResetPreferencesDialog() {
        _uiState.update { it.copy(showResetPreferencesDialog = true) }
    }

    fun dismissResetPreferencesDialog() {
        _uiState.update { it.copy(showResetPreferencesDialog = false) }
    }

    fun confirmResetPreferences() {
        viewModelScope.launch {
            try {
                preferencesRepository.resetToDefaults()
                _uiState.update { it.copy(showResetPreferencesDialog = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(showResetPreferencesDialog = false) }
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update { it.copy(showResetSuccessMessage = false) }
    }
}