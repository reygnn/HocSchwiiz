package com.github.reygnn.hocschwiiz.data.words

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