package com.github.reygnn.hocschwiiz.presentation.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.reygnn.hocschwiiz.domain.model.QuizResult
import com.github.reygnn.hocschwiiz.presentation.quiz.components.QuestionCard
import com.github.reygnn.hocschwiiz.presentation.quiz.components.AnswerButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onQuizFinished: (QuizResult) -> Unit, // Callback wenn fertig
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Verhindern von versehentlichem Zurückgehen während des Quiz
    BackHandler(enabled = !uiState.isFinished) {
        // Optional: Dialog anzeigen "Willst du wirklich abbrechen?"
        onBack()
    }

    // Navigation Trigger
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            // Hier müssten wir eigentlich das Result aus dem VM holen oder berechnen
            // Einfachheitshalber navigieren wir hier nur,
            // idealerweise übergibt man das Result object via Navigation
            onBack() // Placeholder, siehe unten "Nächste Schritte"
        }
    }

    // Haptic Feedback bei Antwort
    LaunchedEffect(uiState.showFeedback) {
        if (uiState.showFeedback) {
            if (uiState.isAnswerCorrect == false) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Abbrechen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                uiState.currentQuestion?.let { question ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(bottom = 24.dp),
                        )

                        // Frage Karte
                        QuestionCard(
                            questionText = question.questionText,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Antwort Optionen
                        question.options.forEach { option ->
                            AnswerButton(
                                text = option,
                                isSelected = uiState.selectedAnswer == option,
                                isCorrectAnswer = option == question.correctAnswer,
                                isWrongSelection = uiState.selectedAnswer == option && option != question.correctAnswer,
                                showFeedback = uiState.showFeedback,
                                enabled = !uiState.showFeedback, // Buttons sperren während Feedback
                                onClick = { viewModel.submitAnswer(option) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}