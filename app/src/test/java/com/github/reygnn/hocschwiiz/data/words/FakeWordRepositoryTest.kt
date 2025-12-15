package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository.Companion.CATEGORY_DAILY_LIFE
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository.Companion.CATEGORY_FOOD_DRINK
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository.Companion.CATEGORY_GREETINGS
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository.Companion.CATEGORY_NATURE
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeWordRepositoryTest {

    private lateinit var repository: FakeWordRepository

    @Before
    fun setup() {
        repository = FakeWordRepository()
        repository.addSampleWords()
    }

    // ==================== getAll ====================

    @Test
    fun `getAll returns all words for dialect`() = runTest {
        val words = repository.getAll(Dialect.AARGAU).first()

        assertEquals(10, words.size)
        assertTrue(words.all { it.dialect == Dialect.AARGAU })
    }

    @Test
    fun `getAll returns empty list for dialect with no words`() = runTest {
        repository.clearWords()

        val words = repository.getAll(Dialect.AARGAU).first()

        assertTrue(words.isEmpty())
    }

    @Test
    fun `getAll separates dialects correctly`() = runTest {
        val aargauWords = repository.getAll(Dialect.AARGAU).first()
        val zurichWords = repository.getAll(Dialect.ZUERICH).first()

        assertEquals(10, aargauWords.size)
        assertEquals(2, zurichWords.size)
    }

    // ==================== getByCategory ====================

    @Test
    fun `getByCategory returns filtered words`() = runTest {
        val greetings = repository.getByCategory(CATEGORY_GREETINGS.id, Dialect.AARGAU).first()

        assertEquals(4, greetings.size)
        assertTrue(greetings.all { it.category == CATEGORY_GREETINGS })
    }

    @Test
    fun `getByCategory returns empty for category with no words`() = runTest {
        val nature = repository.getByCategory(CATEGORY_NATURE.id, Dialect.AARGAU).first()

        assertTrue(nature.isEmpty())
    }

    // ==================== getById ====================

    @Test
    fun `getById returns correct word`() = runTest {
        val word = repository.getById("greetings_001", Dialect.AARGAU)

        assertEquals("Guete Tag", word?.swiss)
    }

    @Test
    fun `getById returns null for unknown id`() = runTest {
        val word = repository.getById("unknown_id", Dialect.AARGAU)

        assertNull(word)
    }

    @Test
    fun `getById returns null for wrong dialect`() = runTest {
        val word = repository.getById("greetings_001", Dialect.ZUERICH)

        assertNull(word)
    }

    // ==================== getCategories ====================

    @Test
    fun `getCategories returns distinct categories`() = runTest {
        val categories = repository.getCategories(Dialect.AARGAU).first()

        assertEquals(3, categories.size)
        assertTrue(categories.contains(CATEGORY_GREETINGS))
        assertTrue(categories.contains(CATEGORY_FOOD_DRINK))
        assertTrue(categories.contains(CATEGORY_DAILY_LIFE))
    }

    @Test
    fun `getCategories returns sorted by order`() = runTest {
        val categories = repository.getCategories(Dialect.AARGAU).first()

        assertEquals(CATEGORY_GREETINGS, categories[0])
        assertEquals(CATEGORY_FOOD_DRINK, categories[1])
        assertEquals(CATEGORY_DAILY_LIFE, categories[2])
    }

    // ==================== getWordCountByCategory ====================

    @Test
    fun `getWordCountByCategory returns correct counts`() = runTest {
        val counts = repository.getWordCountByCategory(Dialect.AARGAU).first()

        assertEquals(4, counts[CATEGORY_GREETINGS])
        assertEquals(4, counts[CATEGORY_FOOD_DRINK])
        assertEquals(2, counts[CATEGORY_DAILY_LIFE])
    }

    // ==================== search ====================

    @Test
    fun `search finds german words`() = runTest {
        val results = repository.search("Frühstück", Dialect.AARGAU).first()

        assertEquals(1, results.size)
        assertEquals("food_001", results[0].id)
    }

    @Test
    fun `search finds swiss words`() = runTest {
        val results = repository.search("Zmorge", Dialect.AARGAU).first()

        assertEquals(1, results.size)
        assertEquals("food_001", results[0].id)
    }

    @Test
    fun `search finds vietnamese words`() = runTest {
        val results = repository.search("Bữa sáng", Dialect.AARGAU).first()

        assertEquals(1, results.size)
        assertEquals("food_001", results[0].id)
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val results = repository.search("ZMORGE", Dialect.AARGAU).first()

        assertEquals(1, results.size)
    }

    @Test
    fun `search is tone insensitive for vietnamese`() = runTest {
        val results = repository.search("bua sang", Dialect.AARGAU).first()

        assertEquals(1, results.size)
        assertEquals("food_001", results[0].id)
    }

    @Test
    fun `search finds partial matches`() = runTest {
        val results = repository.search("Sali", Dialect.AARGAU).first()
        assertEquals(1, results.size)
        assertEquals("greetings_002", results[0].id)
    }

    @Test
    fun `search finds words by alt spelling`() = runTest {
        val results = repository.search("Salü", Dialect.AARGAU).first()

        assertEquals(1, results.size)
        assertEquals("greetings_002", results[0].id)
    }

    @Test
    fun `search returns empty for blank query`() = runTest {
        val results = repository.search("   ", Dialect.AARGAU).first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `search returns empty for no matches`() = runTest {
        val results = repository.search("xyz123", Dialect.AARGAU).first()

        assertTrue(results.isEmpty())
    }

    // ==================== getRandomWords ====================

    @Test
    fun `getRandomWords returns requested count`() = runTest {
        val words = repository.getRandomWords(3, Dialect.AARGAU)

        assertEquals(3, words.size)
    }

    @Test
    fun `getRandomWords excludes specified id`() = runTest {
        val words = repository.getRandomWords(10, Dialect.AARGAU, excludeId = "greetings_001")

        assertTrue(words.none { it.id == "greetings_001" })
    }

    @Test
    fun `getRandomWords prefers same category`() = runTest {
        // Run multiple times to account for randomness
        repeat(10) {
            val words = repository.getRandomWords(
                count = 4,
                dialect = Dialect.AARGAU,
                excludeId = "food_001",
                preferCategoryId = CATEGORY_FOOD_DRINK.id
            )

            // Should have at least some from FOOD_DRINK (3 remaining after exclude)
            val foodCount = words.count { it.category == CATEGORY_FOOD_DRINK }
            assertTrue("Expected some FOOD_DRINK words", foodCount >= 2)
        }
    }

    @Test
    fun `getRandomWords returns empty for empty repository`() = runTest {
        repository.clearWords()

        val words = repository.getRandomWords(5, Dialect.AARGAU)

        assertTrue(words.isEmpty())
    }

    // ==================== Error handling ====================

    @Test(expected = RuntimeException::class)
    fun `getAll throws when shouldThrowError is true`() = runTest {
        repository.shouldThrowError = true
        repository.errorMessage = "Simulated error"

        repository.getAll(Dialect.AARGAU).first()
    }

    @Test(expected = RuntimeException::class)
    fun `getById throws when shouldThrowError is true`() = runTest {
        repository.shouldThrowError = true

        repository.getById("any", Dialect.AARGAU)
    }
}