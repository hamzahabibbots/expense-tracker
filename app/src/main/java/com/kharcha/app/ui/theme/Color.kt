package com.kharcha.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
val Teal = Color(0xFF4ECDC4)
val TealDark = Color(0xFF3BA99F)
val Coral = Color(0xFFFF6B6B)
val CoralDark = Color(0xFFE85555)

// Light theme
val LightBackground = Color(0xFFF5F5F5)
val LightCard = Color(0xFFFFFFFF)
val LightText = Color(0xFF000000)
val LightTextSecondary = Color(0xFF666666)
val LightBorder = Color(0xFFE0E0E0)

// Dark theme
val DarkBackground = Color(0xFF121212)
val DarkCard = Color(0xFF1E1E1E)
val DarkText = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFAAAAAA)
val DarkBorder = Color(0xFF333333)

// Category colors
val CategoryFood = Color(0xFFFF6B6B)
val CategoryTransport = Color(0xFF4ECDC4)
val CategoryRentBills = Color(0xFF45B7D1)
val CategoryHealth = Color(0xFF96CEB4)
val CategoryEntertainment = Color(0xFFFFEAA7)
val CategoryShopping = Color(0xFFDDA0DD)
val CategoryOther = Color(0xFFB8B8B8)

// Tip severity colors
val TipAlert = Color(0xFFFF6B6B)
val TipWarning = Color(0xFFFFEAA7)
val TipInfo = Color(0xFF4ECDC4)

// Chart colors
val ChartColors = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
    Color(0xFF96CEB4), Color(0xFFFFEAA7), Color(0xFFDDA0DD),
    Color(0xFFF8C471), Color(0xFF82E0AA), Color(0xFFF1948A),
    Color(0xFFC39BD3), Color(0xFF73C6B6), Color(0xFFB8B8B8),
)

// Predefined color picker options
val CategoryPickerColors = listOf(
    "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
    "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
    "#F8C471", "#82E0AA", "#F1948A", "#C39BD3", "#73C6B6",
    "#F39C12", "#E74C3C", "#3498DB", "#2ECC71", "#9B59B6",
)

fun parseColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    val colorLong = cleaned.toLongOrNull(16) ?: return CategoryOther
    return if (cleaned.length == 6) {
        Color(0xFF000000 or colorLong)
    } else {
        Color(colorLong)
    }
}
