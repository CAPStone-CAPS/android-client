package com.example.capstone_2.util

import androidx.compose.ui.graphics.Color

fun generateAppColorMap(appNames: List<String>): Map<String, Color> {
    val baseColors = listOf(
        Color(0xFF64B5F6),
        Color(0xFF81C784),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8),
        Color(0xFFFF8A65),
        Color(0xFFA1887F),
        Color(0xFFFFD54F),
        Color(0xFF4DD0E1),
        Color(0xFF7986CB),
        Color(0xFF90A4AE)
    )
    return appNames
        .distinct()
        .sorted()
        .mapIndexed { index, name ->
            name to baseColors[index % baseColors.size]
        }.toMap()
}
