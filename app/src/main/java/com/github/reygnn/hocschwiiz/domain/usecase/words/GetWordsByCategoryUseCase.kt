package com.github.reygnn.hocschwiiz.domain.usecase.words

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWordsByCategoryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * Get all words for a specific category.
     *
     * @param categoryId The category ID (e.g., "greetings")
     * @param dialect The dialect to get words for
     */
    operator fun invoke(categoryId: String, dialect: Dialect): Flow<List<Word>> {
        return wordRepository.getByCategory(categoryId, dialect)
    }
}