package com.github.reygnn.hocschwiiz.fakes

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake repository for testing progress functionality.
 */
class FakeProgressRepository : ProgressRepository {

    private val progressMap = MutableStateFlow<Map<String, LearningProgress>>(emptyMap())

    var shouldThrowError = false
    var errorMessage = "Test error"

    // ==================== Test data setup ====================

    fun setProgress(wordId: String, progress: LearningProgress) {
        progressMap.update { it + (wordId to progress) }
    }

    fun setProgressMap(map: Map<String, LearningProgress>) {
        progressMap.value = map
    }

    fun clearProgress() {
        progressMap.value = emptyMap()
    }

    fun getProgressMap(): Map<String, LearningProgress> = progressMap.value

    // ==================== Repository implementation ====================

    override suspend fun getProgress(wordId: String): LearningProgress? {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        return progressMap.value[wordId]
    }

    override fun getProgressFlow(wordId: String): Flow<LearningProgress?> {
        return progressMap.map { it[wordId] }
    }

    override suspend fun getProgressForWords(wordIds: List<String>): List<LearningProgress> {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        return wordIds.mapNotNull { progressMap.value[it] }
    }

    override fun getAllProgress(): Flow<List<LearningProgress>> {
        return progressMap.map { it.values.toList() }
    }

    override suspend fun saveProgress(progress: LearningProgress) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        progressMap.update { it + (progress.wordId to progress) }
    }

    override fun getWeakWordIds(): Flow<List<String>> {
        return progressMap.map { map ->
            map.values
                .filter { it.wrongCount > it.correctCount && it.streak < 2 }
                .sortedByDescending { it.wrongCount - it.correctCount }
                .map { it.wordId }
        }
    }

    override fun getPracticedWordCount(): Flow<Int> {
        return progressMap.map { it.size }
    }

    override fun getTotalCorrectCount(): Flow<Int> {
        return progressMap.map { map ->
            map.values.sumOf { it.correctCount }
        }
    }

    override fun getTotalWrongCount(): Flow<Int> {
        return progressMap.map { map ->
            map.values.sumOf { it.wrongCount }
        }
    }

    override fun getMaxStreak(): Flow<Int> {
        return progressMap.map { map ->
            map.values.maxOfOrNull { it.streak } ?: 0
        }
    }

    override fun getTotalWordsLearned(): Flow<Int> {
        return getPracticedWordCount()
    }

    override suspend fun resetAllProgress() {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        progressMap.value = emptyMap()
    }

    override suspend fun resetProgress(wordId: String) {
        if (shouldThrowError) throw RuntimeException(errorMessage)
        progressMap.update { it - wordId }
    }
}