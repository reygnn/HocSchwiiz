package com.github.reygnn.hocschwiiz.data.words

import com.github.reygnn.hocschwiiz.domain.model.Category
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.Word
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import com.github.reygnn.hocschwiiz.domain.util.TextNormalization.containsNormalized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDataSource: WordDataSource,
    private val categoryDataSource: CategoryDataSource
) : WordRepository {

    override fun getAll(dialect: Dialect): Flow<List<Word>> = flow {
        emit(wordDataSource.loadWords(dialect))
    }

    override fun getByCategory(categoryId: String, dialect: Dialect): Flow<List<Word>> = flow {
        val words = wordDataSource.loadWords(dialect)
            .filter { it.category.id == categoryId }
        emit(words)
    }

    override suspend fun getById(id: String, dialect: Dialect): Word? {
        return wordDataSource.loadWords(dialect).find { it.id == id }
    }

    override fun getCategories(dialect: Dialect): Flow<List<Category>> = flow {
        val words = wordDataSource.loadWords(dialect)

        // Get all categories that have at least one word
        val categoriesWithWords = words
            .map { it.category }
            .distinctBy { it.id }
            .sortedBy { it.order }

        emit(categoriesWithWords)
    }

    override fun getWordCountByCategory(dialect: Dialect): Flow<Map<Category, Int>> = flow {
        val words = wordDataSource.loadWords(dialect)
        val countMap = words
            .groupBy { it.category }
            .mapValues { it.value.size }
        emit(countMap)
    }

    override fun getTotalWordCount(dialect: Dialect): Flow<Int> = flow {
        emit(wordDataSource.loadWords(dialect).size)
    }

    override fun search(query: String, dialect: Dialect): Flow<List<Word>> = flow {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val words = wordDataSource.loadWords(dialect)
        val results = words.filter { word ->
            word.german.containsNormalized(trimmedQuery) ||
                    word.swiss.containsNormalized(trimmedQuery) ||
                    word.vietnamese.containsNormalized(trimmedQuery) ||
                    word.altSpellings.any { it.containsNormalized(trimmedQuery) }
        }
        emit(results)
    }

    override suspend fun getRandomWords(
        count: Int,
        dialect: Dialect,
        excludeId: String?,
        preferCategoryId: String?
    ): List<Word> {
        val allWords = wordDataSource.loadWords(dialect)
            .filter { it.id != excludeId }

        if (allWords.isEmpty()) return emptyList()

        // Prefer words from same category for better quiz distractors
        return if (preferCategoryId != null) {
            val sameCategoryWords = allWords.filter { it.category.id == preferCategoryId }
            val otherWords = allWords.filter { it.category.id != preferCategoryId }

            // Take from same category first, fill with others
            (sameCategoryWords.shuffled() + otherWords.shuffled())
                .take(count)
        } else {
            allWords.shuffled().take(count)
        }
    }
}