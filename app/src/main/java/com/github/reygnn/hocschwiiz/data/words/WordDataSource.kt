package com.github.reygnn.hocschwiiz.data.words

import android.content.Context
import android.util.Log
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for loading words from JSON asset files.
 *
 * Expected file naming: words_ag.json, words_zh.json
 * Located in: assets/
 */
@Singleton
class WordDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache loaded words per dialect
    private val cache = mutableMapOf<Dialect, List<Word>>()

    /**
     * Load words for the specified dialect.
     * Results are cached after first load.
     *
     * @return List of words, or empty list on error
     */
    fun loadWords(dialect: Dialect): List<Word> {
        // Return cached if available
        cache[dialect]?.let { return it }

        return try {
            val fileName = "words_${dialect.code}.json"
            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val wordFile = json.decodeFromString<WordFileDto>(jsonString)
            val words = wordFile.words.map { it.toDomain(dialect) }

            // Cache for subsequent calls
            cache[dialect] = words

            Log.d(TAG, "Loaded ${words.size} words for ${dialect.displayName}")
            words
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load words for ${dialect.displayName}", e)
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
    }
}