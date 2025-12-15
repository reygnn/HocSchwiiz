package com.github.reygnn.hocschwiiz.data.words

import android.content.Context
import android.util.Log
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for loading words from JSON asset files.
 *
 * Expected file structure:
 * assets/words/
 * ├── categories.json           # Manifest
 * ├── greetings_aargau.json
 * ├── greetings_zuerich.json
 * ├── numbers_aargau.json
 * └── ...
 *
 * File naming convention: {categoryId}_{dialectCode}.json
 */
@Singleton
class WordDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDataSource: CategoryDataSource
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache: dialect -> list of all words
    private val cache = mutableMapOf<Dialect, List<Word>>()

    /**
     * Load all words for the specified dialect.
     * Automatically discovers and loads all category files.
     * Results are cached after first load.
     *
     * @return List of words, or empty list on error
     */
    fun loadWords(dialect: Dialect): List<Word> {
        // Return cached if available
        cache[dialect]?.let { return it }

        return try {
            val categories = categoryDataSource.loadCategories()
            if (categories.isEmpty()) {
                Log.w(TAG, "No categories found in manifest")
                return emptyList()
            }

            val allWords = mutableListOf<Word>()

            for (category in categories) {
                val words = loadWordsForCategory(category, dialect)
                allWords.addAll(words)
            }

            // Cache for subsequent calls
            cache[dialect] = allWords

            Log.d(TAG, "Loaded ${allWords.size} words for ${dialect.displayName}")
            allWords
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load words for ${dialect.displayName}", e)
            emptyList()
        }
    }

    /**
     * Load words for a specific category and dialect.
     *
     * @return List of words, or empty list if file doesn't exist or on error
     */
    private fun loadWordsForCategory(category: Category, dialect: Dialect): List<Word> {
        val fileName = "$WORDS_DIR/${category.id}_${dialect.code}.json"

        return try {
            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val wordFile = json.decodeFromString<WordFileDto>(jsonString)
            val words = wordFile.words.map { dto ->
                dto.toDomain(dialect, category)
            }

            Log.d(TAG, "Loaded ${words.size} words from $fileName")
            words
        } catch (e: java.io.FileNotFoundException) {
            // File doesn't exist - this is okay, category might not have words for this dialect yet
            Log.d(TAG, "No word file found: $fileName")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load words from $fileName", e)
            emptyList()
        }
    }

    /**
     * Clear the cache (e.g., for testing or hot reload).
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Check if words are loaded for a dialect.
     */
    fun isLoaded(dialect: Dialect): Boolean {
        return cache.containsKey(dialect)
    }

    companion object {
        private const val TAG = "WordDataSource"
        private const val WORDS_DIR = "words"
    }
}