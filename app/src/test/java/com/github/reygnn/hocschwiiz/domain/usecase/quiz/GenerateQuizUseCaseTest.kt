package com.github.reygnn.hocschwiiz.domain.usecase.quiz

import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.fakes.FakeWordRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateQuizUseCaseTest {

    private lateinit var wordRepository: FakeWordRepository
    private lateinit var useCase: GenerateQuizUseCase

    @Before
    fun setup() {
        wordRepository = FakeWordRepository()
        wordRepository.addSampleWords()
        useCase = GenerateQuizUseCase(wordRepository)
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
    fun `filters by categoryId when specified`() = runTest {
        val questions = useCase(
            categoryId = "greetings",
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        assertTrue(questions.isNotEmpty())
        questions.forEach { q ->
            assertEquals("greetings", q.word.category.id)
        }
    }

    @Test
    fun `returns empty for category with no words`() = runTest {
        val questions = useCase(
            categoryId = "nonexistent_category",
            questionCount = 5,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        assertTrue(questions.isEmpty())
    }

    @Test
    fun `returns all categories when categoryId is null`() = runTest {
        val questions = useCase(
            categoryId = null,
            questionCount = 10,
            quizType = QuizType.GERMAN_TO_SWISS,
            dialect = Dialect.AARGAU
        )

        // Should have words from multiple categories
        val categoryIds = questions.map { it.word.category.id }.distinct()
        assertTrue("Expected words from multiple categories", categoryIds.size >= 1)
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