package com.github.reygnn.hocschwiiz.data.words

import android.content.Context
import android.util.Log
import com.github.reygnn.hocschwiiz.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for loading categories from the manifest file.
 *
 * Expected file: assets/words/categories.json
 */
@Singleton
class CategoryDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache loaded categories
    private var cache: List<Category>? = null
    private var cacheById: Map<String, Category>? = null

    /**
     * Load all categories from manifest.
     * Results are cached after first load.
     *
     * @return List of categories sorted by order, or empty list on error
     */
    fun loadCategories(): List<Category> {
        cache?.let { return it }

        return try {
            val jsonString = context.assets.open(MANIFEST_FILE)
                .bufferedReader()
                .use { it.readText() }

            val manifest = json.decodeFromString<CategoriesFileDto>(jsonString)
            val categories = manifest.categories
                .map { it.toDomain() }
                .sortedBy { it.order }

            // Cache for subsequent calls
            cache = categories
            cacheById = categories.associateBy { it.id }

            Log.d(TAG, "Loaded ${categories.size} categories from manifest")
            categories
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load categories manifest", e)
            emptyList()
        }
    }

    /**
     * Get a category by its ID.
     *
     * @return Category if found, or [Category.UNKNOWN] as fallback
     */
    fun getById(id: String): Category {
        // Ensure cache is populated
        if (cacheById == null) {
            loadCategories()
        }

        return cacheById?.get(id) ?: Category.UNKNOWN
    }

    /**
     * Check if a category ID exists.
     */
    fun exists(id: String): Boolean {
        if (cacheById == null) {
            loadCategories()
        }
        return cacheById?.containsKey(id) == true
    }

    /**
     * Clear the cache (e.g., for testing or hot reload).
     */
    fun clearCache() {
        cache = null
        cacheById = null
    }

    companion object {
        private const val TAG = "CategoryDataSource"
        private const val MANIFEST_FILE = "words/categories.json"
    }
}