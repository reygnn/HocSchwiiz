package com.github.reygnn.hocschwiiz.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")

    // Einfache Route für Quiz Settings/Start
    object QuizGraph : Screen("quiz_graph")
    object QuizPlay : Screen("quiz_play/{categoryId}/{quizType}") {
        fun createRoute(categoryId: String?, quizType: String?) =
            "quiz_play/${categoryId ?: "all"}/${quizType ?: "FROM_SETTINGS"}"
    }

    object WordList : Screen("wordlist/{categoryId}") {
        fun createRoute(categoryId: String) = "wordlist/$categoryId"
    }

    object Settings : Screen("settings")
}