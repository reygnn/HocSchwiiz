package com.github.reygnn.hocschwiiz.domain.util

/**
 * Utility for text normalization, especially for Vietnamese tone marks.
 * Used for search functionality to match words regardless of diacritics.
 */
object TextNormalization {

    private val vietnameseToneReplacements = mapOf(
        // Lowercase vowels with tones
        Regex("[àáảãạ]") to "a",
        Regex("[ằắẳẵặ]") to "ă",
        Regex("[ầấẩẫậ]") to "â",
        Regex("[èéẻẽẹ]") to "e",
        Regex("[ềếểễệ]") to "ê",
        Regex("[ìíỉĩị]") to "i",
        Regex("[òóỏõọ]") to "o",
        Regex("[ồốổỗộ]") to "ô",
        Regex("[ờớởỡợ]") to "ơ",
        Regex("[ùúủũụ]") to "u",
        Regex("[ừứửữự]") to "ư",
        Regex("[ỳýỷỹỵ]") to "y",
        // Uppercase vowels with tones
        Regex("[ÀÁẢÃẠ]") to "A",
        Regex("[ẰẮẲẴẶ]") to "Ă",
        Regex("[ẦẤẨẪẬ]") to "Â",
        Regex("[ÈÉẺẼẸ]") to "E",
        Regex("[ỀẾỂỄỆ]") to "Ê",
        Regex("[ÌÍỈĨỊ]") to "I",
        Regex("[ÒÓỎÕỌ]") to "O",
        Regex("[ỒỐỔỖỘ]") to "Ô",
        Regex("[ỜỚỞỠỢ]") to "Ơ",
        Regex("[ÙÚỦŨỤ]") to "U",
        Regex("[ỪỨỬỮỰ]") to "Ư",
        Regex("[ỲÝỶỸỴ]") to "Y"
    )

    /**
     * Removes Vietnamese tone marks from a string.
     * "Chào" → "Chao", "Bữa sáng" → "Bưa sang"
     *
     * Note: Base letters (ă, â, ê, ô, ơ, ư) are preserved as they distinguish
     * different vowels, not just tones.
     */
    fun String.removeVietnameseTones(): String {
        var result = this
        vietnameseToneReplacements.forEach { (regex, replacement) ->
            result = result.replace(regex, replacement)
        }
        return result
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    /**
     * Normalizes a string for case-insensitive, tone-insensitive search.
     * - Converts to lowercase
     * - Removes Vietnamese tone marks
     * - Trims whitespace
     */
    fun String.normalizeForSearch(): String {
        return this.lowercase()
            .removeVietnameseTones()
            .simplifyVietnameseVowels()
            .trim()
    }

    /**
     * Checks if the query matches the text (normalized comparison).
     */
    fun String.containsNormalized(query: String): Boolean {
        return this.normalizeForSearch().contains(query.normalizeForSearch())
    }

    private fun String.simplifyVietnameseVowels(): String {
        return this
            .replace('ă', 'a')
            .replace('â', 'a')
            .replace('ê', 'e')
            .replace('ô', 'o')
            .replace('ơ', 'o')
            .replace('ư', 'u')
    }
}