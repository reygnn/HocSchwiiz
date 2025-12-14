package com.github.reygnn.hocschwiiz.domain.usecase.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get all categories that have at least one word.
 */
class GetCategoriesUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(dialect: Dialect): Flow<List<Category>> {
        return wordRepository.getCategories(dialect)
    }
}