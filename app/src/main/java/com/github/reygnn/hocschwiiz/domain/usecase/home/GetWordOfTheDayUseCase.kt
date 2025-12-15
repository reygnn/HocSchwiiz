package com.github.reygnn.hocschwiiz.domain.usecase.home

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns a deterministic "Word of the Day" based on the current date.
 *
 * The same word is shown to all users on the same day.
 * Uses epoch day modulo word count to select the word index.
 */
class GetWordOfTheDayUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {

    operator fun invoke(
        dialect: Dialect,
        date: LocalDate = LocalDate.now()
    ): Flow<WordOfDayResult> {
        return wordRepository.getAll(dialect).map { words ->
            if (words.isEmpty()) {
                WordOfDayResult.NoWords
            } else {
                val daysSinceEpoch = date.toEpochDay()
                val index = (daysSinceEpoch % words.size).toInt().let { i ->
                    if (i < 0) i + words.size else i
                }
                WordOfDayResult.Success(words[index])
            }
        }
    }
}

sealed interface WordOfDayResult {
    data class Success(val word: Word) : WordOfDayResult
    data object NoWords : WordOfDayResult
}