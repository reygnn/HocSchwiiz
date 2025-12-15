package com.github.reygnn.hocschwiiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.reygnn.hocschwiiz.domain.model.DarkMode
import com.github.reygnn.hocschwiiz.domain.repository.PreferencesRepository
import com.github.reygnn.hocschwiiz.presentation.navigation.BottomNavBar
import com.github.reygnn.hocschwiiz.presentation.navigation.NavGraph
import com.github.reygnn.hocschwiiz.presentation.theme.HocSchwiizTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
                MainContent()
            }
        }
    }
}

/**
 * Root-Composable der App. Baut das Grundgerüst mit Scaffold,
 * BottomNavBar und NavGraph auf.
 *
 * Nicht zu verwechseln mit [HocSchwiizApplication] (Hilt Application-Klasse).
 */
@Composable
fun MainContent() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}