package com.github.reygnn.hocschwiiz.domain.usecase.progress

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.fakes.FakeProgressRepository
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetWeakWordsUseCaseTest {

    private lateinit var wordRepository: FakeWordRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var useCase: GetWeakWordsUseCase

    @Before
    fun setup() {
        wordRepository = FakeWordRepository()
        wordRepository.addSampleWords()
        progressRepository = FakeProgressRepository()
        useCase = GetWeakWordsUseCase(progressRepository, wordRepository)
    }

    // ==================== invoke() ====================

    @Test
    fun `returns empty list when no progress exists`() = runTest {
        val weakWords = useCase(Dialect.AARGAU).first()

        assertTrue(weakWords.isEmpty())
    }

    @Test
    fun `returns empty list when no weak words`() = runTest {
        // All words have good success rate
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 8,
                wrongCount = 2,
                streak = 3
            )
        )

        val weakWords = useCase(Dialect.AARGAU).first()

        assertTrue(weakWords.isEmpty())
    }

    @Test
    fun `returns weak words sorted by success rate`() = runTest {
        // Word with 20% success rate (weakest)
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 1,
                wrongCount = 4,
                streak = 0
            )
        )
        // Word with 40% success rate
        progressRepository.setProgress(
            "greetings_002",
            LearningProgress(
                wordId = "greetings_002",
                correctCount = 2,
                wrongCount = 3,
                streak = 0
            )
        )

        val weakWords = useCase(Dialect.AARGAU).first()

        assertEquals(2, weakWords.size)
        assertEquals("greetings_001", weakWords[0].id) // Weakest first
        assertEquals("greetings_002", weakWords[1].id)
    }

    @Test
    fun `respects limit parameter`() = runTest {
        // Create 5 weak words
        listOf("greetings_001", "greetings_002", "greetings_003", "food_001", "food_002").forEach { id ->
            progressRepository.setProgress(
                id,
                LearningProgress(
                    wordId = id,
                    correctCount = 1,
                    wrongCount = 5,
                    streak = 0
                )
            )
        }

        val weakWords = useCase(Dialect.AARGAU, limit = 3).first()

        assertEquals(3, weakWords.size)
    }

    @Test
    fun `filters by dialect`() = runTest {
        // Weak word in Aargau
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 1,
                wrongCount = 5,
                streak = 0
            )
        )
        // Weak word in Zurich
        progressRepository.setProgress(
            "greetings_001_zh",
            LearningProgress(
                wordId = "greetings_001_zh",
                correctCount = 1,
                wrongCount = 5,
                streak = 0
            )
        )

        val aargauWeak = useCase(Dialect.AARGAU).first()
        val zurichWeak = useCase(Dialect.ZUERICH).first()

        assertEquals(1, aargauWeak.size)
        assertEquals("greetings_001", aargauWeak[0].id)

        assertEquals(1, zurichWeak.size)
        assertEquals("greetings_001_zh", zurichWeak[0].id)
    }

    @Test
    fun `ignores progress for words not in repository`() = runTest {
        // Progress for non-existent word
        progressRepository.setProgress(
            "nonexistent_word",
            LearningProgress(
                wordId = "nonexistent_word",
                correctCount = 1,
                wrongCount = 10,
                streak = 0
            )
        )

        val weakWords = useCase(Dialect.AARGAU).first()

        assertTrue(weakWords.isEmpty())
    }

    @Test
    fun `word is not weak with less than 3 attempts`() = runTest {
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 0,
                wrongCount = 2, // Only 2 attempts
                streak = 0
            )
        )

        val weakWords = useCase(Dialect.AARGAU).first()

        assertTrue(weakWords.isEmpty())
    }

    @Test
    fun `word is weak with 3 plus attempts and below 50 percent`() = runTest {
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 1,
                wrongCount = 2, // 3 attempts, 33% success
                streak = 0
            )
        )

        val weakWords = useCase(Dialect.AARGAU).first()

        assertEquals(1, weakWords.size)
        assertEquals("greetings_001", weakWords[0].id)
    }

    // ==================== count() ====================

    @Test
    fun `count returns 0 when no weak words`() = runTest {
        val count = useCase.count().first()

        assertEquals(0, count)
    }

    @Test
    fun `count returns correct number of weak words`() = runTest {
        listOf("greetings_001", "greetings_002", "food_001").forEach { id ->
            progressRepository.setProgress(
                id,
                LearningProgress(
                    wordId = id,
                    correctCount = 1,
                    wrongCount = 5,
                    streak = 0
                )
            )
        }

        val count = useCase.count().first()

        assertEquals(3, count)
    }

    @Test
    fun `count ignores non-weak words`() = runTest {
        // Weak word
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 1,
                wrongCount = 5,
                streak = 0
            )
        )
        // Strong word
        progressRepository.setProgress(
            "greetings_002",
            LearningProgress(
                wordId = "greetings_002",
                correctCount = 8,
                wrongCount = 2,
                streak = 5
            )
        )

        val count = useCase.count().first()

        assertEquals(1, count)
    }
}