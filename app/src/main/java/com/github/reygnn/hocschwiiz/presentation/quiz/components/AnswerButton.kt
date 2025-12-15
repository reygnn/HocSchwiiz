package com.github.reygnn.hocschwiiz.presentation.quiz.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hocschwiiz.presentation.theme.CorrectGreen
import com.hocschwiiz.presentation.theme.WrongRed

@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrectAnswer: Boolean, // Ist dies die korrekte Antwort?
    isWrongSelection: Boolean, // Wurde dies fälschlicherweise gewählt?
    showFeedback: Boolean,     // Sind wir im Feedback-Modus?
    enabled: Boolean,
    onClick: () -> Unit
) {
    // Farben animieren
    val containerColor by animateColorAsState(
        targetValue = when {
            showFeedback && isCorrectAnswer -> CorrectGreen // Immer grün zeigen wenn Auflösung
            showFeedback && isWrongSelection -> WrongRed    // Rot wenn falsch geklickt
            isSelected && !showFeedback -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "ButtonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            showFeedback && (isCorrectAnswer || isWrongSelection) -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "TextColor"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor, // Farben behalten auch wenn disabled
            disabledContentColor = contentColor
        ),
        border = if (isSelected && !showFeedback)
            ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
        else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}