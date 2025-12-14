package com.github.reygnn.hocschwiiz.domain.usecase.words

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Search words across all fields.
 * Search is case-insensitive and tone-insensitive for Vietnamese.
 */
class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(query: String, dialect: Dialect): Flow<List<Word>> {
        return wordRepository.search(query, dialect)
    }
}