package com.github.reygnn.hocschwiiz.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Reset Progress Dialog
    if (state.showResetProgressDialog) {
        ResetConfirmationDialog(
            title = "Lernfortschritt zurücksetzen?",
            message = "Alle ${state.totalWordsLearned} gelernten Wörter und Quiz-Statistiken werden gelöscht. Diese Aktion kann nicht rückgängig gemacht werden.",
            onConfirm = viewModel::confirmResetProgress,
            onDismiss = viewModel::dismissResetProgressDialog
        )
    }

    // Reset Preferences Dialog
    if (state.showResetPreferencesDialog) {
        ResetConfirmationDialog(
            title = "Einstellungen zurücksetzen?",
            message = "Alle Einstellungen werden auf die Standardwerte zurückgesetzt.",
            onConfirm = viewModel::confirmResetPreferences,
            onDismiss = viewModel::dismissResetPreferencesDialog
        )
    }

    // Success Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.showResetSuccessMessage) {
        if (state.showResetSuccessMessage) {
            snackbarHostState.showSnackbar("Lernfortschritt wurde zurückgesetzt")
            viewModel.dismissSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ===== Dialekt =====
                SettingsSection(title = "Dialekt") {
                    DialectSelector(
                        selectedDialect = state.preferences.selectedDialect,
                        onDialectSelected = viewModel::setDialect
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ===== Quiz =====
                SettingsSection(title = "Quiz") {
                    QuizQuestionCountSlider(
                        count = state.preferences.quizQuestionCount,
                        onCountChange = viewModel::setQuizQuestionCount
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    QuizTypeSelector(
                        selectedType = state.preferences.preferredQuizType,
                        onTypeSelected = viewModel::setPreferredQuizType
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ===== Anzeige =====
                SettingsSection(title = "Anzeige") {
                    SwitchSettingItem(
                        title = "Vietnamesisch anzeigen",
                        subtitle = "Zeigt vietnamesische Übersetzungen in Wortlisten",
                        checked = state.preferences.showVietnamese,
                        onCheckedChange = viewModel::setShowVietnamese,
                        icon = Icons.Default.Translate
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DarkModeSelector(
                        selectedMode = state.preferences.darkMode,
                        onModeSelected = viewModel::setDarkMode
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ===== Daten =====
                SettingsSection(title = "Daten") {
                    if (state.totalWordsLearned > 0) {
                        Text(
                            text = "${state.totalWordsLearned} Wörter gelernt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::showResetProgressDialog,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lernfortschritt zurücksetzen")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = viewModel::showResetPreferencesDialog,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.SettingsBackupRestore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Einstellungen zurücksetzen")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // ===== Info =====
                SettingsSection(title = "Info") {
                    AppInfoCard()
                }

                // Bottom padding for navigation bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ========== Section Wrapper ==========

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

// ========== Dialect Selector ==========

@Composable
private fun DialectSelector(
    selectedDialect: Dialect,
    onDialectSelected: (Dialect) -> Unit
) {
    Column {
        Text(
            text = "Wähle deinen Dialekt",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Dialect.entries.forEachIndexed { index, dialect ->
                SegmentedButton(
                    selected = dialect == selectedDialect,
                    onClick = { onDialectSelected(dialect) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = Dialect.entries.size
                    )
                ) {
                    Text(dialect.displayName)
                }
            }
        }
    }
}

// ========== Quiz Question Count ==========

@Composable
private fun QuizQuestionCountSlider(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fragen pro Quiz",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = count.toFloat(),
            onValueChange = { onCountChange(it.roundToInt()) },
            valueRange = AppPreferences.MIN_QUIZ_QUESTIONS.toFloat()..AppPreferences.MAX_QUIZ_QUESTIONS.toFloat(),
            steps = AppPreferences.MAX_QUIZ_QUESTIONS - AppPreferences.MIN_QUIZ_QUESTIONS - 1,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${AppPreferences.MIN_QUIZ_QUESTIONS}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${AppPreferences.MAX_QUIZ_QUESTIONS}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ========== Quiz Type Selector ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTypeSelector(
    selectedType: QuizType,
    onTypeSelected: (QuizType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Quiz-Richtung",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                QuizType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        },
                        leadingIcon = if (type == selectedType) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}

// ========== Switch Setting Item ==========

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ========== Dark Mode Selector ==========

@Composable
private fun DarkModeSelector(
    selectedMode: DarkMode,
    onModeSelected: (DarkMode) -> Unit
) {
    Column {
        Text(
            text = "Erscheinungsbild",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            DarkMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = DarkMode.entries.size
                    ),
                    icon = {
                        val icon = when (mode) {
                            DarkMode.LIGHT -> Icons.Default.LightMode
                            DarkMode.DARK -> Icons.Default.DarkMode
                            DarkMode.SYSTEM -> Icons.Default.SettingsBrightness
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                ) {
                    Text(
                        text = when (mode) {
                            DarkMode.LIGHT -> "Hell"
                            DarkMode.DARK -> "Dunkel"
                            DarkMode.SYSTEM -> "System"
                        }
                    )
                }
            }
        }
    }
}

// ========== Reset Confirmation Dialog ==========

@Composable
private fun ResetConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Zurücksetzen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

// ========== App Info Card ==========

@Composable
private fun AppInfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HocSchwiiz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Schweizerdeutsch lernen für alle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"Học\" (Vietnamesisch) = lernen\n\"Schwiiz\" (Schweizerdeutsch) = Schweiz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Von Uli mit Liebe für seine Phương ❤️\nĐể em học tiếng Thụy Sĩ!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Version 0.9",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}