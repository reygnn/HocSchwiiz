package com.github.reygnn.hocschwiiz.di

import com.github.reygnn.hocschwiiz.data.preferences.PreferencesRepositoryImpl
import com.github.reygnn.hocschwiiz.data.progress.ProgressRepositoryImpl
import com.github.reygnn.hocschwiiz.data.words.WordRepositoryImpl
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.domain.repository.ProgressRepository
import com.github.reygnn.hocschwiiz.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        impl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}