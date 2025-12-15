package com.github.reygnn.hocschwiiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.reygnn.hocschwiiz.presentation.home.HomeScreen
import com.github.reygnn.hocschwiiz.presentation.theme.HocSchwiizTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.reygnn.hocschwiiz.presentation.navigation.BottomNavBar
import com.github.reygnn.hocschwiiz.presentation.navigation.Screen
import com.github.reygnn.hocschwiiz.presentation.categories.CategoriesScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.presentation.quiz.QuizScreen
import com.github.reygnn.hocschwiiz.presentation.settings.SettingsScreen
import com.github.reygnn.hocschwiiz.presentation.wordlist.WordListScreen
import javax.inject.Inject
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModePref by preferencesRepository.darkMode
                .collectAsState(initial = DarkMode.SYSTEM)

            val darkTheme = when (darkModePref) {
                DarkMode.LIGHT -> false
                DarkMode.DARK -> true
                DarkMode.SYSTEM -> isSystemInDarkTheme()
            }

            HocSchwiizTheme(darkTheme = darkTheme) {
                HocSchwiizApp()
            }
        }
    }
}

@Composable
fun HocSchwiizApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartQuiz = {
                        // Quick Start: Startet sofort ein gemischtes Quiz
                        navController.navigate(Screen.QuizPlay.createRoute(null, null))
                    },
                    onPracticeWeakWords = {
                        navController.navigate(Screen.QuizPlay.createRoute("WEAK_WORDS", null))
                    },
                    onWordOfDayClick = { categoryId ->
                        navController.navigate(Screen.WordList.createRoute(categoryId))
                    }
                )
            }

            // 2. Categories Screen
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onCategoryClick = { category ->
                        navController.navigate(Screen.WordList.createRoute(category.id))
                    }
                )
            }

            // 3. Word List Screen Route
            composable(
                route = Screen.WordList.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType }
                )
            ) {
                WordListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 4. Quiz Tab (Übersicht/Einstellungen vor dem Start)
            composable(Screen.QuizGraph.route) {
                // Hier kommt später der Screen hin, wo man "Anzahl Fragen" etc. einstellt.
                // Für jetzt ein Placeholder:
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Button(onClick = {
                        navController.navigate(Screen.QuizPlay.createRoute(null, null))
                    }) {
                        Text("Quiz starten")
                    }
                }
            }

            // 5. Quiz Play (Das eigentliche Spiel)
            composable(
                route = Screen.QuizPlay.route,
                arguments = listOf(
                    navArgument("quizType") { type = NavType.StringType },
                    navArgument("categoryId") {
                        type = NavType.StringType
                        nullable = true // Category ist optional
                        defaultValue = null
                    }
                )
            ) {
                QuizScreen(
                    onBack = { navController.popBackStack() },
                    onQuizFinished = { result ->
                        // TODO: Später hier zum Result Screen navigieren
                        navController.popBackStack()
                    }
                )
            }

            // 6. Settings
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}