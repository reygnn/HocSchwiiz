package com.github.reygnn.hocschwiiz.domain.usecase.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Get all words for a specific category.
 */
class GetWordsByCategoryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(category: Category, dialect: Dialect): Flow<List<Word>> {
        return wordRepository.getByCategory(category, dialect)
    }
}