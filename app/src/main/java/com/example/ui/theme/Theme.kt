package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BinaDarkColorScheme = darkColorScheme(
    primary = BinaPrimary,
    onPrimary = BinaOnPrimary,
    primaryContainer = BinaPrimaryContainer,
    onPrimaryContainer = BinaOnPrimaryContainer,
    secondary = BinaSecondary,
    onSecondary = BinaOnSecondary,
    secondaryContainer = BinaSecondaryContainer,
    tertiary = BinaTertiary,
    onTertiary = BinaOnTertiary,
    background = BinaBackground,
    onBackground = BinaTextPrimary,
    surface = BinaSurface,
    onSurface = BinaTextPrimary,
    surfaceVariant = BinaSurfaceVariant,
    onSurfaceVariant = BinaTextSecondary,
    outline = BinaBorder,
    outlineVariant = BinaSurfaceHighlight,
    error = BinaLossRed,
    onError = Color.White
)

@Composable
fun BinaTheme(
    darkTheme: Boolean = true, // Default to sleek crypto dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BinaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BinaTheme(darkTheme = true, content = content)
}
