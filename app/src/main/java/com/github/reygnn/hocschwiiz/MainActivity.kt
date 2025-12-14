package com.github.reygnn.hocschwiiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aktiviert den Edge-to-Edge Modus (transparente Statusbar)
        enableEdgeToEdge()

        setContent {
            HocSchwiizTheme {
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
                        // Quick Start: Navigiere zum Quiz (Dummy für jetzt)
                        navController.navigate(Screen.QuizGraph.route)
                    },
                    onPracticeWeakWords = { /* TODO later */ }
                )
            }

            // 2. Categories Screen (Wörterbuch)
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onCategoryClick = { category ->
                        // Hier geht's dann später zur WordList
                        // navController.navigate(Screen.WordList.createRoute(category.name))
                    }
                )
            }

            // 3. Quiz Placeholder (damit die App nicht abstürzt beim Klick)
            composable(Screen.QuizGraph.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Quiz-Bereich kommt bald!")
                }
            }

            // 4. Settings Placeholder
            composable(Screen.Settings.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Einstellungen kommen bald!")
                }
            }

            // Hier später: WordList Route, QuizPlay Route, Result Route...
        }
    }
}