package com.github.reygnn.hocschwiiz.domain.usecase.home

import app.cash.turbine.test
import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetWordOfTheDayUseCaseTest {

    private lateinit var wordRepository: WordRepository
    private lateinit var useCase: GetWordOfTheDayUseCase

    private val testCategory = Category(
        id = "greetings",
        displayNameDe = "Begrüssungen",
        displayNameVi = "Lời chào",
        icon = "waving_hand",
        order = 0
    )

    private val testWords = listOf(
        createWord("word_001", "Hallo"),
        createWord("word_002", "Tschüss"),
        createWord("word_003", "Danke"),
        createWord("word_004", "Bitte"),
        createWord("word_005", "Guten Tag")
    )

    @Before
    fun setup() {
        wordRepository = mockk()
        useCase = GetWordOfTheDayUseCase(wordRepository)
    }

    @Test
    fun `returns NoWords when word list is empty`() = runTest {
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(emptyList())

        useCase(Dialect.AARGAU).test {
            val result = awaitItem()
            assertTrue(result is WordOfDayResult.NoWords)
            awaitComplete()
        }
    }

    @Test
    fun `returns same word for same date`() = runTest {
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(testWords)
        val fixedDate = LocalDate.of(2025, 1, 15)

        useCase(Dialect.AARGAU, fixedDate).test {
            val result1 = awaitItem()
            awaitComplete()

            useCase(Dialect.AARGAU, fixedDate).test {
                val result2 = awaitItem()
                assertEquals(result1, result2)
                awaitComplete()
            }
        }
    }

    @Test
    fun `returns different word for different date`() = runTest {
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(testWords)
        val date1 = LocalDate.of(2025, 1, 15)
        val date2 = LocalDate.of(2025, 1, 16)

        var word1: Word? = null
        var word2: Word? = null

        useCase(Dialect.AARGAU, date1).test {
            val result = awaitItem() as WordOfDayResult.Success
            word1 = result.word
            awaitComplete()
        }

        useCase(Dialect.AARGAU, date2).test {
            val result = awaitItem() as WordOfDayResult.Success
            word2 = result.word
            awaitComplete()
        }

        // With 5 words, consecutive days should give different words
        assertTrue(word1 != word2)
    }

    @Test
    fun `word index wraps around correctly`() = runTest {
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(testWords)

        // Test that after 5 days, we get the same word again (5 words in list)
        val baseDate = LocalDate.of(2025, 1, 1)
        val words = mutableListOf<Word>()

        for (i in 0 until 10) {
            useCase(Dialect.AARGAU, baseDate.plusDays(i.toLong())).test {
                val result = awaitItem() as WordOfDayResult.Success
                words.add(result.word)
                awaitComplete()
            }
        }

        // Day 0 and Day 5 should have the same word
        assertEquals(words[0], words[5])
        assertEquals(words[1], words[6])
    }

    @Test
    fun `returns Success with correct word`() = runTest {
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(testWords)
        val fixedDate = LocalDate.of(2025, 1, 15)

        useCase(Dialect.AARGAU, fixedDate).test {
            val result = awaitItem()
            assertTrue(result is WordOfDayResult.Success)
            val word = (result as WordOfDayResult.Success).word
            assertTrue(word in testWords)
            awaitComplete()
        }
    }

    @Test
    fun `handles single word list`() = runTest {
        val singleWord = listOf(createWord("only_word", "Einzig"))
        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(singleWord)

        // Any date should return the same word
        useCase(Dialect.AARGAU, LocalDate.of(2025, 1, 1)).test {
            val result = awaitItem() as WordOfDayResult.Success
            assertEquals("only_word", result.word.id)
            awaitComplete()
        }

        useCase(Dialect.AARGAU, LocalDate.of(2025, 12, 31)).test {
            val result = awaitItem() as WordOfDayResult.Success
            assertEquals("only_word", result.word.id)
            awaitComplete()
        }
    }

    @Test
    fun `respects dialect parameter`() = runTest {
        val aargauWords = listOf(createWord("ag_word", "Aargau", Dialect.AARGAU))
        val zurichWords = listOf(createWord("zh_word", "Zürich", Dialect.ZUERICH))

        every { wordRepository.getAll(Dialect.AARGAU) } returns flowOf(aargauWords)
        every { wordRepository.getAll(Dialect.ZUERICH) } returns flowOf(zurichWords)

        val fixedDate = LocalDate.of(2025, 1, 15)

        useCase(Dialect.AARGAU, fixedDate).test {
            val result = awaitItem() as WordOfDayResult.Success
            assertEquals("ag_word", result.word.id)
            awaitComplete()
        }

        useCase(Dialect.ZUERICH, fixedDate).test {
            val result = awaitItem() as WordOfDayResult.Success
            assertEquals("zh_word", result.word.id)
            awaitComplete()
        }
    }

    private fun createWord(
        id: String,
        german: String,
        dialect: Dialect = Dialect.AARGAU
    ) = Word(
        id = id,
        german = german,
        swiss = "Swiss $german",
        vietnamese = "Viet $german",
        category = testCategory,
        dialect = dialect
    )
}