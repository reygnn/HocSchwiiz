package com.github.reygnn.hocschwiiz.presentation.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.presentation.categories.components.CategoryCard

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier, // Padding vom Scaffold der MainActivity
    viewModel: CategoriesViewModel = hiltViewModel(),
    onCategoryClick: (Category) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Search Bar Area
        SearchBarArea(
            query = state.searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            onClear = viewModel::clearSearch
        )

        // 2. Content
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (state.isSearching) {
            // Such-Ergebnisse Liste
            SearchResultsList(
                results = state.searchResults,
                onWordClick = { /* Optional: Navigieren zum Wort-Detail oder Sound abspielen */ },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Kategorien Grid
            CategoriesGrid(
                categories = state.categories,
                wordCounts = state.wordCounts,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SearchBarArea(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Wörter suchen...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Löschen")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun CategoriesGrid(
    categories: List<Category>,
    wordCounts: Map<Category, Int>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                wordCount = wordCounts[category] ?: 0,
                onClick = { onCategoryClick(category) }
            )
        }
        // Extra Space unten für Scrollbarkeit
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SearchResultsList(
    results: List<Word>,
    onWordClick: (Word) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier, // WICHTIG: Hier wird das weight(1f) angewendet
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (results.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine Wörter gefunden.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(results) { word ->
                SearchResultItem(
                    word = word,
                    onClick = { onWordClick(word) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultItem(
    word: Word,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Zeile 1: Schweizerdeutsch (Hervorgehoben)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (word.gender != null) {
                    Text(
                        text = word.gender.swiss,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = word.swiss,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Zeile 2: Deutsch
            Text(
                text = word.german,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Zeile 3: Vietnamesisch (kleiner)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = word.vietnamese,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}