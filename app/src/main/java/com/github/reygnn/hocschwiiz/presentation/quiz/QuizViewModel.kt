package com.github.reygnn.hocschwiiz.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.QuizQuestion
import com.github.reygnn.hocschwiiz.domain.model.QuizResult
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.progress.GetWeakWordsUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.quiz.GenerateQuizUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.quiz.SubmitAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val showFeedback: Boolean = false,
    val isAnswerCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val quizResult: QuizResult? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val progress: Float
        get() = if (questions.isEmpty()) 0f
        else (currentQuestionIndex + 1).toFloat() / questions.size
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val getWeakWordsUseCase: GetWeakWordsUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Navigation args - null or "all" means all categories
    private val categoryArg: String? = savedStateHandle["categoryId"]
    private val quizTypeArg: String? = savedStateHandle["quizType"]

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Track answered questions for result
    private val answeredQuestions = mutableListOf<QuizQuestion>()

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState(isLoading = true)
            answeredQuestions.clear()

            try {
                val dialect = preferencesRepository.selectedDialect.first()
                val questionCount = preferencesRepository.quizQuestionCount.first()

                // Quiz type from nav arg or settings
                val quizType = quizTypeArg
                    ?.takeIf { it != "FROM_SETTINGS" }
                    ?.let {
                        try {
                            QuizType.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                    ?: preferencesRepository.preferredQuizType.first()

                // Category - null or "all" means all categories
                val categoryId = categoryArg?.takeIf { it != "all" && it != "null" }

                val preSelectedWords: List<Word>? = if (categoryArg == "WEAK_WORDS") {
                    getWeakWordsUseCase(dialect).first()
                } else null

                val questions = generateQuizUseCase(
                    categoryId = categoryId?.takeIf { it != "WEAK_WORDS" },
                    dialect = dialect,
                    questionCount = questionCount,
                    quizType = quizType,
                    preSelectedWords = preSelectedWords
                )

                _uiState.value = QuizUiState(
                    questions = questions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = QuizUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to generate quiz"
                )
            }
        }
    }

    fun submitAnswer(answer: String) {
        val state = _uiState.value
        if (state.showFeedback) return

        val currentQuestion = state.currentQuestion ?: return
        val isCorrect = answer == currentQuestion.correctAnswer

        // Track answered question
        answeredQuestions.add(currentQuestion.copy(answeredCorrectly = isCorrect))

        viewModelScope.launch {
            submitAnswerUseCase(currentQuestion.word.id, isCorrect)
        }

        _uiState.value = state.copy(
            selectedAnswer = answer,
            showFeedback = true,
            isAnswerCorrect = isCorrect
        )

        // Auto-advance after delay
        viewModelScope.launch {
            delay(1200)
            advanceToNext()
        }
    }

    private fun advanceToNext() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex >= state.questions.size) {
            // Quiz finished
            val correctCount = answeredQuestions.count { it.answeredCorrectly == true }
            val wrongQuestions = answeredQuestions.filter { it.answeredCorrectly == false }

            _uiState.value = state.copy(
                isFinished = true,
                quizResult = QuizResult(
                    correctAnswers = correctCount,
                    totalQuestions = state.questions.size,
                    wrongAnswers = wrongQuestions
                )
            )
        } else {
            _uiState.value = state.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                showFeedback = false,
                isAnswerCorrect = null
            )
        }
    }
}