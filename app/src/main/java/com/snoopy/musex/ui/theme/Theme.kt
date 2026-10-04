package com.snoopy.musex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy950 = Color(0xFF07111F)
val Navy900 = Color(0xFF0B1827)
val Navy800 = Color(0xFF11283E)
val Blue400 = Color(0xFF4DA3FF)
val Blue300 = Color(0xFF80BBFF)
val Mist = Color(0xFFDCE8F5)
val Muted = Color(0xFF91A8BE)
val ErrorRed = Color(0xFFFF6B78)

private val MusexColors = darkColorScheme(
    primary = Blue400,
    onPrimary = Navy950,
    primaryContainer = Color(0xFF153C61),
    onPrimaryContainer = Color(0xFFD8ECFF),
    secondary = Blue300,
    background = Navy950,
    onBackground = Mist,
    surface = Navy900,
    onSurface = Mist,
    surfaceVariant = Navy800,
    onSurfaceVariant = Muted,
    error = ErrorRed,
)

@Composable
fun MusexTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MusexColors, content = content)
}

