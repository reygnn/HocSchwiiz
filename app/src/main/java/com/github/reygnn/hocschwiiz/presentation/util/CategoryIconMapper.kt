package com.github.reygnn.hocschwiiz.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.reygnn.hocschwiiz.domain.model.Category

/**
 * Maps a Category's icon string to a Material ImageVector.
 */
fun Category.getIconVector(): ImageVector {
    return mapIconName(this.icon)
}

/**
 * Maps an icon name string to a Material ImageVector.
 * Useful when you have the icon name but not the full Category object.
 */
fun mapIconName(iconName: String): ImageVector {
    return when (iconName) {
        "waving_hand" -> Icons.Default.WavingHand
        "restaurant" -> Icons.Default.Restaurant
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "schedule" -> Icons.Default.Schedule
        "park" -> Icons.Default.Park
        "family_restroom" -> Icons.Default.FamilyRestroom
        "directions_car" -> Icons.Default.DirectionsCar
        "chat_bubble" -> Icons.Default.ChatBubble
        "sentiment_very_dissatisfied" -> Icons.Default.SentimentVeryDissatisfied
        else -> Icons.Default.Help // Fallback
    }
}