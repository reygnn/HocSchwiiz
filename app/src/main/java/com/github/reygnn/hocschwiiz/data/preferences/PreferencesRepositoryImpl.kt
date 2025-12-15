package com.github.reygnn.hocschwiiz.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.reygnn.hocschwiiz.domain.model.AppPreferences
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.model.Dialect
import com.github.reygnn.hocschwiiz.domain.model.QuizType
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "hocschwiiz_preferences"
)

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object Keys {
        val DIALECT = stringPreferencesKey("selected_dialect")
        val QUIZ_QUESTION_COUNT = intPreferencesKey("quiz_question_count")
        val QUIZ_TYPE = stringPreferencesKey("preferred_quiz_type")
        val SHOW_VIETNAMESE = booleanPreferencesKey("show_vietnamese")
        val DARK_MODE = stringPreferencesKey("dark_mode")
    }

    override val preferences: Flow<AppPreferences> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            AppPreferences(
                selectedDialect = prefs[Keys.DIALECT]?.toDialect() ?: Dialect.AARGAU,
                quizQuestionCount = prefs[Keys.QUIZ_QUESTION_COUNT] ?: AppPreferences.DEFAULT_QUIZ_QUESTIONS,
                preferredQuizType = prefs[Keys.QUIZ_TYPE]?.toQuizType() ?: QuizType.MIXED,
                showVietnamese = prefs[Keys.SHOW_VIETNAMESE] ?: true,
                darkMode = prefs[Keys.DARK_MODE]?.toDarkMode() ?: DarkMode.SYSTEM
            )
        }

    override val selectedDialect: Flow<Dialect> = preferences.map { it.selectedDialect }

    override suspend fun setDialect(dialect: Dialect) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DIALECT] = dialect.name
        }
    }

    override val quizQuestionCount: Flow<Int> = preferences.map { it.quizQuestionCount }

    override suspend fun setQuizQuestionCount(count: Int) {
        val validCount = count.coerceIn(
            AppPreferences.MIN_QUIZ_QUESTIONS,
            AppPreferences.MAX_QUIZ_QUESTIONS
        )
        context.dataStore.edit { prefs ->
            prefs[Keys.QUIZ_QUESTION_COUNT] = validCount
        }
    }

    override val preferredQuizType: Flow<QuizType> = preferences.map { it.preferredQuizType }

    override suspend fun setPreferredQuizType(quizType: QuizType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.QUIZ_TYPE] = quizType.name
        }
    }

    override val showVietnamese: Flow<Boolean> = preferences.map { it.showVietnamese }

    override suspend fun setShowVietnamese(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_VIETNAMESE] = show
        }
    }

    override val darkMode: Flow<DarkMode> = preferences.map { it.darkMode }

    override suspend fun setDarkMode(mode: DarkMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = mode.name
        }
    }

    override suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }

    // Extension functions for safe parsing
    private fun String.toDialect(): Dialect {
        return try {
            Dialect.valueOf(this)
        } catch (e: IllegalArgumentException) {
            Dialect.AARGAU
        }
    }

    private fun String.toQuizType(): QuizType {
        return try {
            QuizType.valueOf(this)
        } catch (e: IllegalArgumentException) {
            QuizType.MIXED
        }
    }

    private fun String.toDarkMode(): DarkMode {
        return try {
            DarkMode.valueOf(this)
        } catch (e: IllegalArgumentException) {
            DarkMode.SYSTEM
        }
    }
}