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
    private val dataSource: WordDataSource
) : WordRepository {

    override fun getAll(dialect: Dialect): Flow<List<Word>> = flow {
        emit(dataSource.loadWords(dialect))
    }

    override fun getByCategory(category: Category, dialect: Dialect): Flow<List<Word>> = flow {
        val words = dataSource.loadWords(dialect)
            .filter { it.category == category }
        emit(words)
    }

    override suspend fun getById(id: String, dialect: Dialect): Word? {
        return dataSource.loadWords(dialect).find { it.id == id }
    }

    override fun getCategories(dialect: Dialect): Flow<List<Category>> = flow {
        val words = dataSource.loadWords(dialect)
        val categories = words
            .map { it.category }
            .distinct()
            .sortedBy { it.ordinal }
        emit(categories)
    }

    override fun getWordCountByCategory(dialect: Dialect): Flow<Map<Category, Int>> = flow {
        val words = dataSource.loadWords(dialect)
        val countMap = words
            .groupBy { it.category }
            .mapValues { it.value.size }
        emit(countMap)
    }

    override fun getTotalWordCount(dialect: Dialect): Flow<Int> = flow {
        emit(dataSource.loadWords(dialect).size)
    }

    override fun search(query: String, dialect: Dialect): Flow<List<Word>> = flow {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val words = dataSource.loadWords(dialect)
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
        preferCategory: Category?
    ): List<Word> {
        val allWords = dataSource.loadWords(dialect)
            .filter { it.id != excludeId }

        if (allWords.isEmpty()) return emptyList()

        // Prefer words from same category for better quiz distractors
        return if (preferCategory != null) {
            val sameCategoryWords = allWords.filter { it.category == preferCategory }
            val otherWords = allWords.filter { it.category != preferCategory }

            // Take from same category first, fill with others
            (sameCategoryWords.shuffled() + otherWords.shuffled())
                .take(count)
        } else {
            allWords.shuffled().take(count)
        }
    }
}