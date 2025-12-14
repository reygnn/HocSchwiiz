package com.github.reygnn.hocschwiiz.domain.usecase.progress

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Get words that the user struggles with.
 */
class GetWeakWordsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository
) {
    /**
     * Get weak words for a dialect.
     *
     * @param dialect The dialect
     * @param limit Maximum number of words to return
     * @return Flow of weak words
     */
    operator fun invoke(dialect: Dialect, limit: Int = 20): Flow<List<Word>> = flow {
        val weakWordIds = progressRepository.getWeakWordIds().first()
        val allWords = wordRepository.getAll(dialect).first()

        val weakWords = weakWordIds
            .mapNotNull { id -> allWords.find { it.id == id } }
            .take(limit)

        emit(weakWords)
    }

    /**
     * Get count of weak words.
     */
    fun count(): Flow<Int> {
        return progressRepository.getWeakWordIds().map { it.size }
    }
}