package com.github.reygnn.hocschwiiz.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")

    // Einfache Route für Quiz Settings/Start
    object QuizGraph : Screen("quiz_graph")
    object QuizPlay : Screen("quiz_play/{category}/{quizType}") {
        fun createRoute(categoryName: String?, quizType: String) =
            "quiz_play/${categoryName ?: "all"}/$quizType"
    }

    object WordList : Screen("wordlist/{categoryName}") {
        fun createRoute(categoryName: String) = "wordlist/$categoryName"
    }

    object Settings : Screen("settings")
}