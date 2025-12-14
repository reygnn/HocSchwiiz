package com.hocschwiiz.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Wir nennen es explizit "HocSchwiizTypography" um Verwechslungen zu vermeiden
val HocSchwiizTypography = Typography(
    // Standard Text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Für Quiz-Fragen (Briefing: "etwas grösser")
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // Für Überschriften in Listen/Karten
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    /* Hinweis zu DE/CH/VN Styles:
     Da wir CH fett und VN kursiv wollen, machen wir das am besten
     direkt in den Text-Composables (via modifier oder style.copy),
     statt hier starre Styles zu definieren. Das ist flexibler.
    */
)