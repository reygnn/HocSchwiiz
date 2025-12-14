package com.github.reygnn.hocschwiiz.domain.repository

import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing learning progress.
 * Implementation uses Room database for persistence.
 */
interface ProgressRepository {

    /**
     * Get progress for a specific word.
     * Returns null if the word has never been practiced.
     */
    suspend fun getProgress(wordId: String): LearningProgress?

    /**
     * Get progress for a specific word as Flow.
     */
    fun getProgressFlow(wordId: String): Flow<LearningProgress?>

    /**
     * Get progress for multiple words.
     */
    suspend fun getProgressForWords(wordIds: List<String>): List<LearningProgress>

    /**
     * Get all progress entries.
     */
    fun getAllProgress(): Flow<List<LearningProgress>>

    /**
     * Save or update progress for a word.
     */
    suspend fun saveProgress(progress: LearningProgress)

    /**
     * Get words that the user struggles with (isWeak = true).
     * Returns word IDs sorted by success rate (worst first).
     */
    fun getWeakWordIds(): Flow<List<String>>

    /**
     * Get the total number of words practiced.
     */
    fun getPracticedWordCount(): Flow<Int>

    /**
     * Get the total number of correct answers.
     */
    fun getTotalCorrectCount(): Flow<Int>

    /**
     * Get the total number of wrong answers.
     */
    fun getTotalWrongCount(): Flow<Int>

    /**
     * Get the current streak (consecutive correct answers).
     * This is the maximum streak across all words.
     */
    fun getMaxStreak(): Flow<Int>

    /**
     * Reset all progress data.
     */
    suspend fun resetAllProgress()

    /**
     * Reset progress for a specific word.
     */
    suspend fun resetProgress(wordId: String)
}