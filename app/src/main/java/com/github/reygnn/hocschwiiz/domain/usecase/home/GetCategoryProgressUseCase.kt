package com.github.reygnn.hocschwiiz.domain.usecase.home

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Returns learning progress for each category.
 *
 * A word is considered "learned" if it has at least one correct answer.
 */
class GetCategoryProgressUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) {

    operator fun invoke(dialect: Dialect): Flow<List<CategoryProgress>> {
        return combine(
            wordRepository.getAll(dialect),
            wordRepository.getCategories(dialect),
            progressRepository.getAllProgress()
        ) { allWords, categories, allProgress ->
            // Create a set of learned word IDs (at least one correct answer)
            val learnedWordIds = allProgress
                .filter { it.correctCount > 0 }
                .map { it.wordId }
                .toSet()

            // Group words by category
            val wordsByCategory = allWords.groupBy { it.category }

            categories.map { category ->
                val wordsInCategory = wordsByCategory[category] ?: emptyList()
                val totalWords = wordsInCategory.size
                val learnedWords = wordsInCategory.count { it.id in learnedWordIds }

                CategoryProgress(
                    category = category,
                    totalWords = totalWords,
                    learnedWords = learnedWords,
                    progress = if (totalWords > 0) {
                        learnedWords.toFloat() / totalWords
                    } else 0f
                )
            }.sortedBy { it.category.order }
        }
    }
}

data class CategoryProgress(
    val category: Category,
    val totalWords: Int,
    val learnedWords: Int,
    val progress: Float
)