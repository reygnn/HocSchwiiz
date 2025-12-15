package com.github.reygnn.hocschwiiz.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.QuizQuestion
import com.github.reygnn.hocschwiiz.domain.model.QuizResult
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.usecase.quiz.CheckAnswerUseCase
import com.github.reygnn.hocschwiiz.domain.usecase.quiz.GenerateQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isAnswerCorrect: Boolean? = null, // null = noch nicht geantwortet
    val showFeedback: Boolean = false,    // true = Zeige Rot/Grün und disable Buttons
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val quizResult: QuizResult? = null, // Wird am Ende gesetzt für Navigation
    val answeredQuestions: List<QuizQuestion> = emptyList()
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val progress: Float
        get() = if (questions.isNotEmpty()) (currentQuestionIndex + 1) / questions.size.toFloat() else 0f
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val checkAnswerUseCase: CheckAnswerUseCase,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Argumente aus Navigation
    private val categoryArg: String? = savedStateHandle["category"] // Name der Category oder null
    private val quizTypeArg: String = savedStateHandle["quizType"] ?: QuizType.MIXED.name

    private var startTimeMillis: Long = 0L

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Einstellungen lesen
            val dialect = preferencesRepository.selectedDialect.first()
            val count = preferencesRepository.quizQuestionCount.first()

            val category = categoryArg?.let {
                if (it != "null") Category.valueOf(it) else null
            }
            val quizType = QuizType.valueOf(quizTypeArg)

            // UseCase aufrufen
            val questions = generateQuizUseCase(
                questionCount = count,
                quizType = quizType,
                dialect = dialect,
                category = category,
                prioritizeWeak = true
            )

            startTimeMillis = System.currentTimeMillis()

            _uiState.update {
                it.copy(
                    questions = questions,
                    isLoading = false,
                    currentQuestionIndex = 0,
                    isFinished = false,
                    quizResult = null,
                    answeredQuestions = emptyList()
                )
            }
        }
    }

    fun submitAnswer(answer: String) {
        val currentState = _uiState.value
        if (currentState.showFeedback || currentState.isFinished) return

        viewModelScope.launch {
            val currentQ = currentState.currentQuestion ?: return@launch

            // 1. Check Answer (UseCase update Progress im Hintergrund)
            val checkedQuestion = checkAnswerUseCase(currentQ, answer)
            val isCorrect = checkedQuestion.answeredCorrectly == true

            _uiState.update {
                it.copy(
                    selectedAnswer = answer,
                    isAnswerCorrect = isCorrect,
                    showFeedback = true,
                    answeredQuestions = it.answeredQuestions + checkedQuestion
                )
            }

            // 3. Kurze Pause für Feedback (1.5 Sekunden)
            delay(1500)

            // 4. Nächste Frage oder Finish
            advanceToNextQuestion()
        }
    }

    private fun advanceToNextQuestion() {
        _uiState.update { state ->
            val nextIndex = state.currentQuestionIndex + 1
            if (nextIndex < state.questions.size) {
                // Nächste Frage
                state.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerCorrect = null,
                    showFeedback = false
                )
            } else {
                // Quiz beendet -> Result berechnen
                val answered = state.answeredQuestions
                val correct = answered.count { it.answeredCorrectly == true }
                val wrong = answered.filter { it.answeredCorrectly == false }

                val result = QuizResult(
                    totalQuestions = answered.size,
                    correctAnswers = correct,
                    wrongAnswers = wrong,
                    quizType = QuizType.valueOf(quizTypeArg),
                    durationMillis = System.currentTimeMillis() - startTimeMillis
                )

                state.copy(
                    isFinished = true,
                    showFeedback = false,
                    quizResult = result
                )
            }
        }
    }
}