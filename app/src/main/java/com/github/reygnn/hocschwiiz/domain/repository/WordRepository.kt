package com.github.reygnn.hocschwiiz.domain.repository

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    /**
     * Get all words for a dialect.
     */
    fun getAll(dialect: Dialect): Flow<List<Word>>

    /**
     * Get words for a specific category.
     *
     * @param categoryId The category ID (e.g., "greetings")
     */
    fun getByCategory(categoryId: String, dialect: Dialect): Flow<List<Word>>

    /**
     * Get a single word by ID.
     */
    suspend fun getById(id: String, dialect: Dialect): Word?

    /**
     * Get all available categories (from manifest).
     * Only returns categories that have at least one word for the dialect.
     */
    fun getCategories(dialect: Dialect): Flow<List<Category>>

    /**
     * Get word count per category.
     */
    fun getWordCountByCategory(dialect: Dialect): Flow<Map<Category, Int>>

    /**
     * Get total word count for a dialect.
     */
    fun getTotalWordCount(dialect: Dialect): Flow<Int>

    /**
     * Search words by query string.
     */
    fun search(query: String, dialect: Dialect): Flow<List<Word>>

    /**
     * Get random words for quiz distractors.
     *
     * @param preferCategoryId Prefer words from this category for better distractors
     */
    suspend fun getRandomWords(
        count: Int,
        dialect: Dialect,
        excludeId: String? = null,
        preferCategoryId: String? = null
    ): List<Word>
}