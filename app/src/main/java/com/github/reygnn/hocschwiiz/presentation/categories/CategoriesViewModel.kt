package com.github.reygnn.hocschwiiz.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetCategoriesUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetWordCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val wordCounts: Map<Category, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<Word> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getWordCountUseCase: GetWordCountUseCase,
    private val wordRepository: WordRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadCategories()
        observeSearch()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val dialect = preferencesRepository.selectedDialect.first()

                combine(
                    getCategoriesUseCase(dialect),
                    getWordCountUseCase.byCategory(dialect)
                ) { categories, wordCounts ->
                    _uiState.value.copy(
                        categories = categories,
                        wordCounts = wordCounts,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = CategoriesUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            searchResults = emptyList()
                        )
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    private suspend fun performSearch(query: String) {
        try {
            val dialect = preferencesRepository.selectedDialect.first()
            wordRepository.search(query, dialect).collect { results ->
                _uiState.value = _uiState.value.copy(
                    isSearching = true,
                    searchResults = results
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQueryFlow.value = query
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false,
            searchResults = emptyList()
        )
        searchQueryFlow.value = ""
    }

    fun refresh() {
        _uiState.value = CategoriesUiState(isLoading = true)
        loadCategories()
    }
}