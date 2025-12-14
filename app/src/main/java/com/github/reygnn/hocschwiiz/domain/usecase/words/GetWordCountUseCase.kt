package com.github.reygnn.hocschwiiz.domain.usecase.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get word counts - either total or per category.
 */
class GetWordCountUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * Get total word count for a dialect.
     */
    fun total(dialect: Dialect): Flow<Int> {
        return wordRepository.getTotalWordCount(dialect)
    }

    /**
     * Get word count per category.
     */
    fun byCategory(dialect: Dialect): Flow<Map<Category, Int>> {
        return wordRepository.getWordCountByCategory(dialect)
    }
}