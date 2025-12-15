package com.github.reygnn.hocschwiiz.presentation.wordlist.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.reygnn.hocschwiiz.domain.model.Word

@Composable
fun WordCard(
    word: Word,
    showVietnamese: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize() // Animiert das Aufklappen
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Haupt-Zeile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Schweizerdeutsch (Gross & Fett)
                    SwissWordRow(word)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Deutsch
                    Text(
                        text = word.german,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Vietnamesisch (Optional)
                    if (showVietnamese) {
                        Text(
                            text = word.vietnamese,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded Content (Details)
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Beispiele
                if (word.examples.isNotEmpty()) {
                    LabelText("Beispiele:")
                    word.examples.forEach { example ->
                        Text("• $example", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Notizen
                if (!word.notes.isNullOrBlank()) {
                    LabelText("Notiz:")
                    Text(word.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun SwissWordRow(word: Word) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Artikel anzeigen, falls vorhanden (z.B. "de" Tisch)
        if (word.gender != null) {
            Text(
                text = word.gender.swiss,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Das eigentliche Wort
        Text(
            text = word.swiss,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}