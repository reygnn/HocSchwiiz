package com.github.reygnn.hocschwiiz.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hocschwiiz.presentation.theme.HocSchwiizTypography
import com.hocschwiiz.presentation.theme.NeutralSurface
import com.hocschwiiz.presentation.theme.NeutralSurfaceDark
import com.hocschwiiz.presentation.theme.SwissRed
import com.hocschwiiz.presentation.theme.SwissRedDark
import com.hocschwiiz.presentation.theme.SwissRedLight
import com.hocschwiiz.presentation.theme.VietnamYellow

// Farb-Definitionen (könnten auch aus Color.kt importiert werden, hier zur Vollständigkeit)
private val DarkColorScheme = darkColorScheme(
    primary = SwissRed,
    secondary = SwissRedLight,
    tertiary = VietnamYellow,
    background = NeutralSurfaceDark,
    surface = NeutralSurfaceDark,
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SwissRed,
    secondary = SwissRedDark,
    tertiary = VietnamYellow,
    background = NeutralSurface,
    surface = NeutralSurface,
    onPrimary = Color.White
)

@Composable
fun HocSchwiizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic Color deaktiviert für konsistentes Branding (kann bei Bedarf aktiviert werden)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 1. Statusbar transparent machen für Edge-to-Edge
            // Suppress Deprecation, da dies der offizielle Weg für Transparenz ist,
            // bis window.isStatusBarContrastEnforced o.ä. in allen APIs greift.
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()

            // 2. Icon-Farbe steuern (Uhrzeit, Batterie etc.)
            // false = Helle Icons (gut auf roter/dunkler AppBar)
            // true = Dunkle Icons (gut auf hellem Hintergrund)
            // Da unsere TopAppBar immer SwissRed (dunkel) ist, wollen wir helle Icons (false).
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HocSchwiizTypography, // Verweist auf deine Typography.kt
        content = content
    )
}