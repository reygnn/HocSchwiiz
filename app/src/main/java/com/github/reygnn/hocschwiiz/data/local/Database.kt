package com.github.reygnn.hocschwiiz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProgressEntity::class],
    version = 1,
    exportSchema = true
)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        const val DATABASE_NAME = "hocschwiiz_progress.db"
    }
}