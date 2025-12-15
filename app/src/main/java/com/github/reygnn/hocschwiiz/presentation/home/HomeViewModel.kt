package com.github.reygnn.hocschwiiz.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.home.GetCategoryProgressUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.home.GetDailyStreakUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.home.GetTimeBasedGreetingUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.home.GetWordOfTheDayUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.home.WordOfDayResult
import com.github.reygnn.hocschwiiz.domain.usecase.progress.GetProgressStatsUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.progress.GetWeakWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val getProgressStatsUseCase: GetProgressStatsUseCase,
    private val getWeakWordsUseCase: GetWeakWordsUseCase,
    private val getTimeBasedGreetingUseCase: GetTimeBasedGreetingUseCase,
    private val getWordOfTheDayUseCase: GetWordOfTheDayUseCase,
    private val getDailyStreakUseCase: GetDailyStreakUseCase,
    private val getCategoryProgressUseCase: GetCategoryProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Greeting is not reactive (doesn't need to update while screen is open)
        val greeting = getTimeBasedGreetingUseCase()

        // Combine dialect-independent flows
        val baseFlows = combine(
            getProgressStatsUseCase(),
            getDailyStreakUseCase(),
            getWeakWordsUseCase.count()
        ) { stats, dailyStreak, weakCount ->
            Triple(stats, dailyStreak, weakCount)
        }

        // Dialect-dependent flows need flatMapLatest
        preferencesRepository.selectedDialect
            .flatMapLatest { dialect ->
                combine(
                    baseFlows,
                    getWordOfTheDayUseCase(dialect),
                    getCategoryProgressUseCase(dialect)
                ) { (stats, dailyStreak, weakCount), wordOfDayResult, categoryProgress ->
                    val wordOfDay = when (wordOfDayResult) {
                        is WordOfDayResult.Success -> wordOfDayResult.word
                        is WordOfDayResult.NoWords -> null
                    }

                    HomeUiState(
                        dialect = dialect,
                        greeting = greeting,
                        stats = stats,
                        dailyStreak = dailyStreak,
                        weakWordCount = weakCount,
                        wordOfDay = wordOfDay,
                        categoryProgress = categoryProgress,
                        isLoading = false
                    )
                }
            }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }
}