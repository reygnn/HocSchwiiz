package com.github.reygnn.hocschwiiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.reygnn.hocschwiiz.presentation.categories.CategoriesScreen
import com.github.reygnn.hocschwiiz.presentation.home.HomeScreen
import com.github.reygnn.hocschwiiz.presentation.quiz.QuizGraphScreen
import com.github.reygnn.hocschwiiz.presentation.quiz.QuizScreen
import com.github.reygnn.hocschwiiz.presentation.settings.SettingsScreen
import com.github.reygnn.hocschwiiz.presentation.wordlist.WordListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
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

        // 3. Word List Screen
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
            QuizGraphScreen(
                onStartQuiz = { categoryId ->
                    // Startet Quiz mit gewählter Kategorie und Einstellungen aus Preferences
                    navController.navigate(Screen.QuizPlay.createRoute(categoryId, null))
                }
            )
        }

        // 5. Quiz Play (Das eigentliche Spiel)
        composable(
            route = Screen.QuizPlay.route,
            arguments = listOf(
                navArgument("quizType") { type = NavType.StringType },
                navArgument("categoryId") {
                    type = NavType.StringType
                    nullable = true
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