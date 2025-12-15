package com.github.reygnn.hocschwiiz.presentation.wordlist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Gender
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.words.GetWordsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WordListUiState(
    val category: Category? = null,
    val words: List<Word> = emptyList(),
    val showVietnamese: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Hier kommt das Argument aus der Navigation an
    private val getWordsByCategoryUseCase: GetWordsByCategoryUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    // Argument-Key muss mit dem in Screen.kt übereinstimmen
    private val categoryName: String? = savedStateHandle["categoryName"]

    init {
        loadData()
    }

    private fun loadData() {
        val category = Category.values().find { it.name == categoryName }

        if (category == null) {
            _uiState.update { it.copy(isLoading = false) } // Error State eigentlich
            return
        }

        // Wir kombinieren Dialekt, Vietnamesisch-Setting und die Wörter
        viewModelScope.launch {
            combine(
                preferencesRepository.selectedDialect,
                preferencesRepository.showVietnamese
            ) { dialect, showVietnamese ->
                Pair(dialect, showVietnamese)
            }.flatMapLatest { (dialect, showVietnamese) ->
                getWordsByCategoryUseCase(category, dialect).map { words ->
                    Triple(words, showVietnamese, category)
                }
            }.collect { (words, showVi, cat) ->
                _uiState.update {
                    it.copy(
                        words = words,
                        showVietnamese = showVi,
                        category = cat,
                        isLoading = false
                    )
                }
            }
        }
    }
}
