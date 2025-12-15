package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Gender
import com.github.reygnn.hocschwiiz.domain.model.Word
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root object for word JSON files.
 *
 * Note: Category is no longer in this file - it's derived from the filename.
 */
@Serializable
data class WordFileDto(
    val version: Int,
    val dialect: String,
    val words: List<WordDto>
)

/**
 * DTO for a single word entry in JSON.
 *
 * Note: category field removed - category is determined by which file the word is in.
 */
@Serializable
data class WordDto(
    val id: String,
    val german: String,
    val swiss: String,
    val vietnamese: String,
    val gender: String? = null,
    @SerialName("alt_spellings")
    val altSpellings: List<String> = emptyList(),
    val notes: String? = null,
    val examples: List<String> = emptyList()
) {
    /**
     * Convert DTO to domain model.
     *
     * @param dialect The dialect this word belongs to
     * @param category The category this word belongs to (from filename)
     */
    fun toDomain(dialect: Dialect, category: Category): Word {
        return Word(
            id = id,
            german = german,
            swiss = swiss,
            vietnamese = vietnamese,
            category = category,
            dialect = dialect,
            gender = gender?.let { parseGender(it) },
            altSpellings = altSpellings,
            notes = notes,
            examples = examples
        )
    }

    private fun parseGender(value: String): Gender? {
        return try {
            Gender.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}