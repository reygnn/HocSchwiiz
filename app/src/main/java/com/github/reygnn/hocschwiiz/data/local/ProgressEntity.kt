package com.github.reygnn.hocschwiiz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.reygnn.hocschwiiz.domain.model.LearningProgress

@Entity(tableName = "learning_progress")
data class ProgressEntity(
    @PrimaryKey
    val wordId: String,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastPracticed: Long? = null,
    val streak: Int = 0
) {
    fun toDomain(): LearningProgress {
        return LearningProgress(
            wordId = wordId,
            correctCount = correctCount,
            wrongCount = wrongCount,
            lastPracticed = lastPracticed,
            streak = streak
        )
    }

    companion object {
        fun fromDomain(progress: LearningProgress): ProgressEntity {
            return ProgressEntity(
                wordId = progress.wordId,
                correctCount = progress.correctCount,
                wrongCount = progress.wrongCount,
                lastPracticed = progress.lastPracticed,
                streak = progress.streak
            )
        }
    }
}