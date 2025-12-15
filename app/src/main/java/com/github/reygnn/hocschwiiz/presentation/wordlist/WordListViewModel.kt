package com.github.reygnn.hocschwiiz.presentation.wordlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetWordsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val category: Category? = null,
    val words: List<Word> = emptyList(),
    val showVietnamese: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWordsByCategoryUseCase: GetWordsByCategoryUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val categoryId: String? = savedStateHandle["categoryId"]

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init {
        loadWords()
        observePreferences()
    }

    private fun loadWords() {
        viewModelScope.launch {
            try {
                val dialect = preferencesRepository.selectedDialect.first()

                if (categoryId != null) {
                    getWordsByCategoryUseCase(categoryId, dialect).collect { words ->
                        // Get category from first word (they're all the same category)
                        val category = words.firstOrNull()?.category

                        _uiState.value = _uiState.value.copy(
                            category = category,
                            words = words,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = WordListUiState(
                        isLoading = false,
                        error = "No category specified"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = WordListUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.showVietnamese.collect { show ->
                _uiState.value = _uiState.value.copy(showVietnamese = show)
            }
        }
    }
}