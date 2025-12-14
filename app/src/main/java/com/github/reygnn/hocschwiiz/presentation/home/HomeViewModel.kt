package com.github.reygnn.hocschwiiz.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.progress.GetProgressStatsUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.progress.GetWeakWordsUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.progress.ProgressStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val dialect: Dialect = Dialect.AARGAU,
    val stats: ProgressStats? = null,
    val weakWordCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val getProgressStatsUseCase: GetProgressStatsUseCase,
    private val getWeakWordsUseCase: GetWeakWordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Wir kombinieren Flows, damit bei jeder Änderung (Dialekt oder Stats)
        // die UI aktualisiert wird.
        combine(
            preferencesRepository.selectedDialect,
            getProgressStatsUseCase(),
            // Hier ein kleiner Trick: Wenn WeakWordsCount keinen Flow hat,
            // müssten wir ihn triggern. Laut Briefing ist count() aber ein Flow<Int>.
            // Achtung: WeakWords hängen evtl. nicht vom Dialekt ab, wenn sie global gespeichert sind,
            // aber laut Signatur braucht invoke(dialect) den Dialekt.
            // count() hat keine Parameter in deiner Signatur, ich gehe davon aus es ist global oder holt sich Dialekt intern.
            // Falls count() Parameter braucht, müssten wir flatMapLatest nutzen.
            getWeakWordsUseCase.count()
        ) { dialect, stats, weakCount ->
            HomeUiState(
                dialect = dialect,
                stats = stats,
                weakWordCount = weakCount,
                isLoading = false
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }
}