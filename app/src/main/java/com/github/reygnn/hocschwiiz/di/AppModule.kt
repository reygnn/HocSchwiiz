package com.github.reygnn.hocschwiiz.di

import com.github.reygnn.hocschwiiz.domain.util.SystemTimeProvider
import com.github.reygnn.hocschwiiz.domain.util.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider {
        return SystemTimeProvider()
    }
}