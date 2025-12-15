package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root object for categories.json manifest file.
 */
@Serializable
data class CategoriesFileDto(
    val version: Int,
    val categories: List<CategoryDto>
)

/**
 * DTO for a single category entry in the manifest.
 */
@Serializable
data class CategoryDto(
    val id: String,
    @SerialName("display_name_de")
    val displayNameDe: String,
    @SerialName("display_name_vi")
    val displayNameVi: String,
    val icon: String,
    val order: Int
) {
    /**
     * Convert DTO to domain model.
     */
    fun toDomain(): Category {
        return Category(
            id = id,
            displayNameDe = displayNameDe,
            displayNameVi = displayNameVi,
            icon = icon,
            order = order
        )
    }
}