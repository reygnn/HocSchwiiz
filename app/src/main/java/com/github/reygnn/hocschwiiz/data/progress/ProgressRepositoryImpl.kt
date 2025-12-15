package com.github.reygnn.hocschwiiz.data.progress

import com.github.reygnn.hocschwiiz.data.local.ProgressDao
import com.github.reygnn.hocschwiiz.data.local.ProgressEntity
import com.github.reygnn.hocschwiiz.domain.model.LearningProgress
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao
) : ProgressRepository {

    override suspend fun getProgress(wordId: String): LearningProgress? {
        return progressDao.getProgress(wordId)?.toDomain()
    }

    override fun getProgressFlow(wordId: String): Flow<LearningProgress?> {
        return progressDao.getProgressFlow(wordId).map { it?.toDomain() }
    }

    override suspend fun getProgressForWords(wordIds: List<String>): List<LearningProgress> {
        return progressDao.getProgressForWords(wordIds).map { it.toDomain() }
    }

    override fun getAllProgress(): Flow<List<LearningProgress>> {
        return progressDao.getAllProgress().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveProgress(progress: LearningProgress) {
        progressDao.insertProgress(ProgressEntity.Companion.fromDomain(progress))
    }

    override fun getWeakWordIds(): Flow<List<String>> {
        return progressDao.getWeakWordIds()
    }

    override fun getPracticedWordCount(): Flow<Int> {
        return progressDao.getPracticedWordCount()
    }

    override fun getTotalCorrectCount(): Flow<Int> {
        return progressDao.getTotalCorrectCount()
    }

    override fun getTotalWrongCount(): Flow<Int> {
        return progressDao.getTotalWrongCount()
    }

    override fun getMaxStreak(): Flow<Int> {
        return progressDao.getMaxStreak()
    }

    override fun getTotalWordsLearned(): Flow<Int> =
        progressDao.getPracticedWordCount()

    override suspend fun resetAllProgress() {
        progressDao.deleteAll()
    }

    override suspend fun resetProgress(wordId: String) {
        progressDao.deleteProgress(wordId)
    }
}