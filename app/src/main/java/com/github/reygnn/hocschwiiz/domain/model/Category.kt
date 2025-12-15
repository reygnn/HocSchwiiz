package com.github.reygnn.hocschwiiz.domain.model

/**
 * Represents a word category, loaded dynamically from categories.json.
 *
 * @param id Unique identifier, matches filename prefix (e.g., "greetings")
 * @param displayNameDe German display name
 * @param displayNameVi Vietnamese display name
 * @param icon Material icon name for UI display
 * @param order Sort order for consistent display
 */
data class Category(
    val id: String,
    val displayNameDe: String,
    val displayNameVi: String,
    val icon: String,
    val order: Int
) {
    companion object {
        /**
         * Fallback category for words with unknown category ID.
         */
        val UNKNOWN = Category(
            id = "unknown",
            displayNameDe = "Sonstiges",
            displayNameVi = "Khác",
            icon = "help",
            order = Int.MAX_VALUE
        )
    }
}