package com.daymark.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PinkAccent = Color(0xFFE91E63)
val OrangeAccent = Color(0xFFFF9800)
val BlueAccent = Color(0xFF69DDF9)
val DarkBg = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2A2A2A)
val TextPrimary = Color(0xFFEEEEEE)
val TextMuted = Color(0xFF888888)

private val DayMarkColors = darkColorScheme(
    primary = PinkAccent,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun DayMarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DayMarkColors, content = content)
}
