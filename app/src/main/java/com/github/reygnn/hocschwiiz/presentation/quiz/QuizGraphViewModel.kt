package com.github.reygnn.hocschwiiz.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizGraphUiState(
    val quizType: QuizType = QuizType.MIXED,
    val questionCount: Int = AppPreferences.DEFAULT_QUIZ_QUESTIONS,
    val showVietnamese: Boolean = true,
    val selectedCategory: Category? = null, // null = Alle Kategorien
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class QuizGraphViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizGraphUiState())
    val uiState: StateFlow<QuizGraphUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                val dialect = preferencesRepository.selectedDialect.first()
                val quizType = preferencesRepository.preferredQuizType.first()
                val questionCount = preferencesRepository.quizQuestionCount.first()
                val showVietnamese = preferencesRepository.showVietnamese.first()
                val categories = wordRepository.getCategories(dialect).first()

                _uiState.value = QuizGraphUiState(
                    quizType = quizType,
                    questionCount = questionCount,
                    showVietnamese = showVietnamese,
                    selectedCategory = null, // Default: Alle Kategorien
                    categories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Fallback to defaults on error
                _uiState.value = QuizGraphUiState(isLoading = false)
            }
        }
    }

    fun setQuizType(type: QuizType) {
        _uiState.value = _uiState.value.copy(quizType = type)
        viewModelScope.launch {
            preferencesRepository.setPreferredQuizType(type)
        }
    }

    fun setQuestionCount(count: Int) {
        val clampedCount = count.coerceIn(
            AppPreferences.MIN_QUIZ_QUESTIONS,
            AppPreferences.MAX_QUIZ_QUESTIONS
        )
        _uiState.value = _uiState.value.copy(questionCount = clampedCount)
        viewModelScope.launch {
            preferencesRepository.setQuizQuestionCount(clampedCount)
        }
    }

    fun setShowVietnamese(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVietnamese = show)
        viewModelScope.launch {
            preferencesRepository.setShowVietnamese(show)
        }
    }

    fun setCategory(category: Category?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * Returns the category ID for navigation.
     * null means "all categories"
     */
    fun getSelectedCategoryId(): String? = _uiState.value.selectedCategory?.id
}