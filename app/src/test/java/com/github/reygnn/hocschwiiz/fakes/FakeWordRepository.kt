package com.github.reygnn.hocschwiiz.fakes

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Gender
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import com.github.reygnn.hocschwiiz.domain.util.TextNormalization.containsNormalized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake repository for testing.
 * Allows injecting test data and simulating errors.
 */
class FakeWordRepository : WordRepository {

    private val words = mutableMapOf<Dialect, MutableList<Word>>()

    var shouldThrowError = false
    var errorMessage = "Test error"

    // ==================== Test data setup ====================

    fun addWord(word: Word) {
        words.getOrPut(word.dialect) { mutableListOf() }.add(word)
    }

    fun addWords(wordList: List<Word>) {
        wordList.forEach { addWord(it) }
    }

    fun clearWords() {
        words.clear()
    }

    fun setWords(dialect: Dialect, wordList: List<Word>) {
        words[dialect] = wordList.toMutableList()
    }

    /**
     * Add sample words for testing.
     */
    fun addSampleWords() {
        addWords(createSampleWords())
    }

    // ==================== Repository implementation ====================

    override fun getAll(dialect: Dialect): Flow<List<Word>> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        emit(words[dialect]?.toList() ?: emptyList())
    }

    override fun getByCategory(categoryId: String, dialect: Dialect): Flow<List<Word>> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        val result = words[dialect]?.filter { it.category.id == categoryId } ?: emptyList()
        emit(result)
    }

    override suspend fun getById(id: String, dialect: Dialect): Word? {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        return words[dialect]?.find { it.id == id }
    }

    override fun getCategories(dialect: Dialect): Flow<List<Category>> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        val categories = words[dialect]
            ?.map { it.category }
            ?.distinct()
            ?.sortedBy { it.order }
            ?: emptyList()
        emit(categories)
    }

    override fun getWordCountByCategory(dialect: Dialect): Flow<Map<Category, Int>> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        val countMap = words[dialect]
            ?.groupBy { it.category }
            ?.mapValues { it.value.size }
            ?: emptyMap()
        emit(countMap)
    }

    override fun getTotalWordCount(dialect: Dialect): Flow<Int> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        emit(words[dialect]?.size ?: 0)
    }

    override fun search(query: String, dialect: Dialect): Flow<List<Word>> = flow {
        if (shouldThrowError) throw RuntimeException(errorMessage)

        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val results = words[dialect]?.filter { word ->
            word.german.containsNormalized(trimmedQuery) ||
                    word.swiss.containsNormalized(trimmedQuery) ||
                    word.vietnamese.containsNormalized(trimmedQuery) ||
                    word.altSpellings.any { it.containsNormalized(trimmedQuery) }
        } ?: emptyList()

        emit(results)
    }

    override suspend fun getRandomWords(
        count: Int,
        dialect: Dialect,
        excludeId: String?,
        preferCategoryId: String?
    ): List<Word> {
        if (shouldThrowError) throw RuntimeException(errorMessage)

        val allWords = words[dialect]?.filter { it.id != excludeId } ?: emptyList()

        return if (preferCategoryId != null) {
            val sameCategoryWords = allWords.filter { it.category.id == preferCategoryId }
            val otherWords = allWords.filter { it.category.id != preferCategoryId }
            (sameCategoryWords.shuffled() + otherWords.shuffled()).take(count)
        } else {
            allWords.shuffled().take(count)
        }
    }

    companion object {
        // Test categories
        val CATEGORY_GREETINGS = Category(
            id = "greetings",
            displayNameDe = "Begrüssungen",
            displayNameVi = "Lời chào",
            icon = "waving_hand",
            order = 0
        )

        val CATEGORY_FOOD_DRINK = Category(
            id = "food_drink",
            displayNameDe = "Essen & Trinken",
            displayNameVi = "Đồ ăn & Thức uống",
            icon = "restaurant",
            order = 1
        )

        val CATEGORY_DAILY_LIFE = Category(
            id = "daily_life",
            displayNameDe = "Alltag",
            displayNameVi = "Cuộc sống hàng ngày",
            icon = "home",
            order = 2
        )

        val CATEGORY_NATURE = Category(
            id = "nature",
            displayNameDe = "Natur",
            displayNameVi = "Thiên nhiên",
            icon = "park",
            order = 3
        )

        /**
         * Create sample words for testing.
         */
        fun createSampleWords(): List<Word> = listOf(
            // Greetings
            Word(
                id = "greetings_001",
                german = "Guten Tag",
                swiss = "Guete Tag",
                vietnamese = "Xin chào",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.AARGAU,
                notes = "Formelle Begrüssung"
            ),
            Word(
                id = "greetings_002",
                german = "Hallo",
                swiss = "Sali",
                vietnamese = "Chào",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.AARGAU,
                altSpellings = listOf("Salü")
            ),
            Word(
                id = "greetings_003",
                german = "Auf Wiedersehen",
                swiss = "Uf Widerluege",
                vietnamese = "Tạm biệt",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.AARGAU,
                altSpellings = listOf("Uf Widerseh")
            ),
            Word(
                id = "greetings_004",
                german = "Tschüss",
                swiss = "Tschau",
                vietnamese = "Tạm biệt",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.AARGAU
            ),
            // Food & Drink
            Word(
                id = "food_001",
                german = "Frühstück",
                swiss = "Zmorge",
                vietnamese = "Bữa sáng",
                category = CATEGORY_FOOD_DRINK,
                dialect = Dialect.AARGAU,
                gender = Gender.NEUTER
            ),
            Word(
                id = "food_002",
                german = "Mittagessen",
                swiss = "Zmittag",
                vietnamese = "Bữa trưa",
                category = CATEGORY_FOOD_DRINK,
                dialect = Dialect.AARGAU,
                gender = Gender.NEUTER
            ),
            Word(
                id = "food_003",
                german = "Abendessen",
                swiss = "Znacht",
                vietnamese = "Bữa tối",
                category = CATEGORY_FOOD_DRINK,
                dialect = Dialect.AARGAU,
                gender = Gender.NEUTER
            ),
            Word(
                id = "food_004",
                german = "Brot",
                swiss = "Brot",
                vietnamese = "Bánh mì",
                category = CATEGORY_FOOD_DRINK,
                dialect = Dialect.AARGAU,
                gender = Gender.NEUTER
            ),
            // Daily Life
            Word(
                id = "daily_001",
                german = "Tisch",
                swiss = "Tisch",
                vietnamese = "Cái bàn",
                category = CATEGORY_DAILY_LIFE,
                dialect = Dialect.AARGAU,
                gender = Gender.MASCULINE
            ),
            Word(
                id = "daily_002",
                german = "Stuhl",
                swiss = "Stuehl",
                vietnamese = "Cái ghế",
                category = CATEGORY_DAILY_LIFE,
                dialect = Dialect.AARGAU,
                gender = Gender.MASCULINE
            ),
            // Zürich dialect versions
            Word(
                id = "greetings_001_zh",
                german = "Guten Tag",
                swiss = "Grüezi",
                vietnamese = "Xin chào",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.ZUERICH,
                notes = "Typisch Züridütsch"
            ),
            Word(
                id = "greetings_002_zh",
                german = "Hallo",
                swiss = "Hoi",
                vietnamese = "Chào",
                category = CATEGORY_GREETINGS,
                dialect = Dialect.ZUERICH
            )
        )
    }
}