package com.github.reygnn.hocschwiiz.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetCategoriesUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetWordCountUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.words.SearchWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val wordCounts: Map<Category, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<Word> = emptyList(),
    val isSearching: Boolean = false,
    val dialect: Dialect = Dialect.AARGAU,
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getWordCountUseCase: GetWordCountUseCase,
    private val searchWordsUseCase: SearchWordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    // Eigener Flow für Search-Query um Debouncing zu ermöglichen
    private val _searchQuery = MutableStateFlow("")

    init {
        // 1. Daten laden basierend auf Dialekt
        viewModelScope.launch {
            preferencesRepository.selectedDialect.flatMapLatest { dialect ->
                combine(
                    getCategoriesUseCase(dialect),
                    getWordCountUseCase.byCategory(dialect)
                ) { categories, counts ->
                    Triple(dialect, categories, counts)
                }
            }.collect { (dialect, categories, counts) ->
                _uiState.update {
                    it.copy(
                        dialect = dialect,
                        categories = categories,
                        wordCounts = counts,
                        isLoading = false
                    )
                }
            }
        }

        // 2. Suche beobachten
        setupSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        _searchQuery
            .debounce(300) // Warte 300ms nach Tippen
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    // Wir brauchen den aktuellen Dialekt für die Suche
                    val dialect = _uiState.value.dialect
                    searchWordsUseCase(query, dialect)
                }
            }
            .onEach { results ->
                _uiState.update {
                    it.copy(searchResults = results, isSearching = _searchQuery.value.isNotBlank())
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        onSearchQueryChanged("")
    }
}