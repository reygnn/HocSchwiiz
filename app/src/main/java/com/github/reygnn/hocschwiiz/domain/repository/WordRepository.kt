package com.github.reygnn.hocschwiiz.domain.repository

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing words from the vocabulary database.
 * Implementation loads from JSON assets.
 */
interface WordRepository {

    /**
     * Get all words for the specified dialect.
     */
    fun getAll(dialect: Dialect): Flow<List<Word>>

    /**
     * Get words filtered by category and dialect.
     */
    fun getByCategory(category: Category, dialect: Dialect): Flow<List<Word>>

    /**
     * Get a single word by its ID.
     * Returns null if not found.
     */
    suspend fun getById(id: String, dialect: Dialect): Word?

    /**
     * Get all available categories that have at least one word.
     */
    fun getCategories(dialect: Dialect): Flow<List<Category>>

    /**
     * Get the word count per category for the specified dialect.
     */
    fun getWordCountByCategory(dialect: Dialect): Flow<Map<Category, Int>>

    /**
     * Get total word count for a dialect.
     */
    fun getTotalWordCount(dialect: Dialect): Flow<Int>

    /**
     * Search words across all fields (german, swiss, vietnamese, altSpellings).
     * Search is case-insensitive and tone-insensitive for Vietnamese.
     */
    fun search(query: String, dialect: Dialect): Flow<List<Word>>

    /**
     * Get random words for quiz distractor generation.
     * Excludes the word with [excludeId].
     */
    suspend fun getRandomWords(
        count: Int,
        dialect: Dialect,
        excludeId: String? = null,
        preferCategory: Category? = null
    ): List<Word>
}