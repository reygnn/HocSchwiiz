package com.github.reygnn.hocschwiiz.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.reygnn.hocschwiiz.domain.model.Category

fun Category.getIconVector(): ImageVector {
    // Mapping basierend auf dem 'icon' String im Enum
    return when (this.icon) {
        "waving_hand" -> Icons.Default.WavingHand
        "restaurant" -> Icons.Default.Restaurant
        "home" -> Icons.Default.Home
        "work" -> Icons.Default.Work
        "schedule" -> Icons.Default.Schedule
        "park" -> Icons.Default.Park // Ersatz für Nature
        "family_restroom" -> Icons.Default.FamilyRestroom
        "directions_car" -> Icons.Default.DirectionsCar
        "chat_bubble" -> Icons.Default.ChatBubble
        "sentiment_very_dissatisfied" -> Icons.Default.SentimentVeryDissatisfied
        else -> Icons.Default.Help // Fallback
    }
}