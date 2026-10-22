package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyanPrimary,
    onPrimary = MidnightObsidian,
    primaryContainer = ElectricViolet,
    onPrimaryContainer = Color.White,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = CardSurfaceDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = NeonPinkAccent,
    onTertiary = Color.White,
    background = MidnightObsidian,
    onBackground = TextPrimaryDark,
    surface = DeepNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderStroke,
    error = CrimsonError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
