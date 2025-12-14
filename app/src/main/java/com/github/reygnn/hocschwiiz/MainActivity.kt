package com.github.reygnn.hocschwiiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.reygnn.hocschwiiz.presentation.home.HomeScreen
import com.github.reygnn.hocschwiiz.presentation.theme.HocSchwiizTheme
import dagger.hilt.android.AndroidEntryPoint

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

    // Hier später BottomBar und NavHost einfügen.
    // Für den Moment zeigen wir direkt den HomeScreen an, damit die App läuft.
    Scaffold(
        modifier = Modifier.fillMaxSize()
        // bottomBar = { ... }
    ) { innerPadding ->
        // Später: NavHost(navController = navController, ...)
        HomeScreen(
            modifier = Modifier.padding(innerPadding), // Wichtig: Padding anwenden!
            onStartQuiz = { /* TODO: Navigate to Quiz */ },
            onPracticeWeakWords = { /* TODO: Navigate to Weak Words */ }
        )
    }
}