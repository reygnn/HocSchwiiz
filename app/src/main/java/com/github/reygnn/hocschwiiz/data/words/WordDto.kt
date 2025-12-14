package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Gender
import com.github.reygnn.hocschwiiz.domain.model.Word
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root object for word JSON files.
 */
@Serializable
data class WordFileDto(
    val version: Int,
    val dialect: String,
    val words: List<WordDto>
)

/**
 * DTO for a single word entry in JSON.
 */
@Serializable
data class WordDto(
    val id: String,
    val german: String,
    val swiss: String,
    val vietnamese: String,
    val category: String,
    val gender: String? = null,
    @SerialName("alt_spellings")
    val altSpellings: List<String> = emptyList(),
    val notes: String? = null,
    val examples: List<String> = emptyList()
) {
    /**
     * Convert DTO to domain model.
     */
    fun toDomain(dialect: Dialect): Word {
        return Word(
            id = id,
            german = german,
            swiss = swiss,
            vietnamese = vietnamese,
            category = parseCategory(category),
            dialect = dialect,
            gender = gender?.let { parseGender(it) },
            altSpellings = altSpellings,
            notes = notes,
            examples = examples
        )
    }

    private fun parseCategory(value: String): Category {
        return try {
            Category.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            Category.DAILY_LIFE // Fallback
        }
    }

    private fun parseGender(value: String): Gender? {
        return try {
            Gender.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}