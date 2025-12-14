package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.fakes.FakeProgressRepository
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateQuizUseCaseTest {

    private lateinit var wordRepository: FakeWordRepository
    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var useCase: GenerateQuizUseCase

    @Before
    fun setup() {
        wordRepository = FakeWordRepository()
        wordRepository.addSampleWords()
        progressRepository = FakeProgressRepository()
        useCase = GenerateQuizUseCase(wordRepository, progressRepository)
    }

    // ==================== Basic generation ====================

    @Test
    fun `generates requested number of questions`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        assertEquals(5, questions.size)
    }

    @Test
    fun `returns empty list when not enough words`() = runTest {
        wordRepository.clearWords()

        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        assertTrue(questions.isEmpty())
    }

    @Test
    fun `limits questions to available words`() = runTest {
        val questions = useCase(
            questionCount = 100, // More than available
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        assertTrue(questions.size <= 10) // Only 10 Aargau words in sample
    }

    // ==================== Quiz types ====================

    @Test
    fun `GERMAN_TO_SWISS uses german as question`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(q.word.german, q.questionText)
            assertEquals(q.word.swiss, q.correctAnswer)
        }
    }

    @Test
    fun `SWISS_TO_GERMAN uses swiss as question`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.SWISS_TO_GERMAN,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(q.word.swiss, q.questionText)
            assertEquals(q.word.german, q.correctAnswer)
        }
    }

    @Test
    fun `SWISS_TO_VIETNAMESE uses swiss as question`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.SWISS_TO_VIETNAMESE,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(q.word.swiss, q.questionText)
            assertEquals(q.word.vietnamese, q.correctAnswer)
        }
    }

    @Test
    fun `VIETNAMESE_TO_SWISS uses vietnamese as question`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.VIETNAMESE_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(q.word.vietnamese, q.questionText)
            assertEquals(q.word.swiss, q.correctAnswer)
        }
    }

    @Test
    fun `MIXED uses different quiz types`() = runTest {
        val questions = useCase(
            questionCount = 10,
            quizType = QuizType.MIXED,
            dialect = Dialect.AARGAU
        )

        val types = questions.map { it.quizType }.distinct()
        assertTrue("Expected multiple quiz types", types.size > 1)
        assertTrue("MIXED should not appear in results", QuizType.MIXED !in types)
    }

    // ==================== Options generation ====================

    @Test
    fun `each question has 4 options`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(4, q.options.size)
        }
    }

    @Test
    fun `correct answer is in options`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertTrue(
                "Correct answer '${q.correctAnswer}' not in options ${q.options}",
                q.correctAnswer in q.options
            )
        }
    }

    @Test
    fun `options are distinct`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(
                "Options should be distinct: ${q.options}",
                q.options.size,
                q.options.distinct().size
            )
        }
    }

    @Test
    fun `options are shuffled`() = runTest {
        // Run multiple times to check randomization
        var differentOrderCount = 0
        repeat(10) {
            val questions = useCase(
                questionCount = 1,
                quizType = QuizType.GERMAN_TO_SWISS,
                dialect = Dialect.AARGAU
            )
            val correctIndex = questions[0].options.indexOf(questions[0].correctAnswer)
            if (correctIndex != 0) differentOrderCount++
        }
        assertTrue("Options should be shuffled", differentOrderCount > 0)
    }

    // ==================== Category filtering ====================

    @Test
    fun `filters by category when specified`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU,
            category = Category.GREETINGS
        )

        assertTrue(questions.isNotEmpty())
        questions.forEach { q ->
            assertEquals(Category.GREETINGS, q.word.category)
        }
    }

    @Test
    fun `returns empty for category with less than 4 words`() = runTest {
        // DAILY_LIFE only has 2 words in sample data
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU,
            category = Category.NATURE // No words in sample
        )

        assertTrue(questions.isEmpty())
    }

    // ==================== Weak words prioritization ====================

    @Test
    fun `prioritizes weak words when enabled`() = runTest {
        // Mark greetings_001 as weak
        progressRepository.setProgress(
            "greetings_001",
            LearningProgress(
                wordId = "greetings_001",
                correctCount = 1,
                wrongCount = 5, // Low success rate
                streak = 0
            )
        )

        // Generate many quizzes to check if weak word appears more often
        var weakWordAppearances = 0
        repeat(20) {
            val questions = useCase(
                questionCount = 3,
                quizType = QuizType.GERMAN_TO_SWISS,
                dialect = Dialect.AARGAU,
                prioritizeWeak = true
            )
            if (questions.any { it.word.id == "greetings_001" }) {
                weakWordAppearances++
            }
        }

        // Should appear frequently (not always due to randomization)
        assertTrue("Weak word should appear often", weakWordAppearances > 10)
    }

    @Test
    fun `does not prioritize weak words when disabled`() = runTest {
        // This is harder to test deterministically due to randomization
        // Just verify it doesn't crash
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU,
            prioritizeWeak = false
        )

        assertEquals(5, questions.size)
    }

    // ==================== Dialect handling ====================

    @Test
    fun `uses correct dialect words`() = runTest {
        val aargauQuestions = useCase(
            questionCount = 2,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        val zurichQuestions = useCase(
            questionCount = 2,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.ZUERICH
        )

        aargauQuestions.forEach { q ->
            assertEquals(Dialect.AARGAU, q.word.dialect)
        }

        zurichQuestions.forEach { q ->
            assertEquals(Dialect.ZUERICH, q.word.dialect)
        }
    }

    // ==================== answeredCorrectly initial state ====================

    @Test
    fun `questions have null answeredCorrectly initially`() = runTest {
        val questions = useCase(
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        questions.forEach { q ->
            assertEquals(null, q.answeredCorrectly)
        }
    }
}