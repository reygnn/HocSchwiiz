package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Gender
import com.github.reygnn.hocschwiiz.domain.model.Word
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * DTO for a single word entry in JSON.
 *
 * Note: category field removed - category is determined by which file the word is in.
 *
 * Supports both snake_case and camelCase field names for backwards compatibility.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class WordDto(
    val id: String,
    val german: String,
    val swiss: String,
    // Accepts: "phonetic", "swissPhonetic", "swiss_phonetic"
    @JsonNames("swissPhonetic", "swiss_phonetic")
    val phonetic: String? = null,
    val freiamt: String? = null,
    // Accepts: "freiamtPhonetic", "freiamt_phonetic"
    @JsonNames("freiamt_phonetic")
    val freiamtPhonetic: String? = null,
    // Accepts: "ruleHint", "rule_hint"
    @JsonNames("rule_hint")
    val ruleHint: String? = null,
    val vietnamese: String,
    val gender: String? = null,
    // Accepts: "altSpellings", "alt_spellings"
    @JsonNames("alt_spellings")
    val altSpellings: List<String> = emptyList(),
    val notes: String? = null,
    val examples: List<String> = emptyList()
) {
    fun toDomain(dialect: Dialect, category: Category): Word {
        return Word(
            id = id,
            german = german,
            swiss = swiss,
            phonetic = phonetic,
            freiamt = freiamt,
            freiamtPhonetic = freiamtPhonetic,
            ruleHint = ruleHint,
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