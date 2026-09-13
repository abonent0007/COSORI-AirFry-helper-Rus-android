package com.cosory.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = Color(0xFFA855F7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAE8FF),
    onTertiaryContainer = Color(0xFF3B0764),
    background = Color(0xFFFAF9FC),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFAF9FC),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF32215C),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF241447),
    secondaryContainer = Color(0xFF2E2450),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF33255A),
    tertiaryContainer = Color(0xFF473A6E),
    onTertiaryContainer = Color(0xFFE9DDFF),
    background = Color(0xFF131316),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF131316),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2A2A30),
    onSurfaceVariant = Color(0xFFC7C5CE),
    outline = Color(0xFF938F99),
    surfaceContainerLowest = Color(0xFF0E0E11),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F24),
    surfaceContainerHigh = Color(0xFF2A2A30),
    surfaceContainerHighest = Color(0xFF35353C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun CosoryTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
