package com.github.reygnn.hocschwiiz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM learning_progress WHERE wordId = :wordId")
    suspend fun getProgress(wordId: String): ProgressEntity?

    @Query("SELECT * FROM learning_progress WHERE wordId = :wordId")
    fun getProgressFlow(wordId: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM learning_progress WHERE wordId IN (:wordIds)")
    suspend fun getProgressForWords(wordIds: List<String>): List<ProgressEntity>

    @Query("SELECT * FROM learning_progress")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("""
        SELECT wordId FROM learning_progress 
        WHERE (correctCount + wrongCount) >= 3 
        AND (CAST(correctCount AS REAL) / (correctCount + wrongCount)) < 0.5
        ORDER BY (CAST(correctCount AS REAL) / (correctCount + wrongCount)) ASC
    """)
    fun getWeakWordIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM learning_progress")
    fun getPracticedWordCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(correctCount), 0) FROM learning_progress")
    fun getTotalCorrectCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(wrongCount), 0) FROM learning_progress")
    fun getTotalWrongCount(): Flow<Int>

    @Query("SELECT COALESCE(MAX(streak), 0) FROM learning_progress")
    fun getMaxStreak(): Flow<Int>

    @Query("DELETE FROM learning_progress")
    suspend fun deleteAll()

    @Query("DELETE FROM learning_progress WHERE wordId = :wordId")
    suspend fun deleteProgress(wordId: String)
}