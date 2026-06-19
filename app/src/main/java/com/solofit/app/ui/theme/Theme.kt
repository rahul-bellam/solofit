package com.solofit.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Primary drives default buttons & emphasis. Per the design system, Honey Gold is
// reserved for Sol, so primary is the warm ink — premium, restrained, never gold.
private val DarkColorScheme = darkColorScheme(
    primary = DarkSolo.ink,
    onPrimary = DarkSolo.canvas,
    primaryContainer = DarkSolo.surface2,
    onPrimaryContainer = DarkSolo.ink,
    secondary = DarkSolo.inkSoft,
    onSecondary = DarkSolo.canvas,
    secondaryContainer = DarkSolo.surface2,
    onSecondaryContainer = DarkSolo.ink,
    tertiary = SolGold,
    onTertiary = DarkSolo.canvas,
    background = DarkSolo.canvas,
    onBackground = DarkSolo.ink,
    surface = DarkSolo.surface,
    onSurface = DarkSolo.ink,
    surfaceVariant = DarkSolo.surface2,
    onSurfaceVariant = DarkSolo.inkSoft,
    error = ErrorClay,
    onError = DarkSolo.ink,
    outline = DarkSolo.line,
    outlineVariant = DarkSolo.line,
    inverseSurface = DarkSolo.ink,
    inverseOnSurface = DarkSolo.canvas,
    inversePrimary = LightSolo.ink,
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = LightSolo.ink,
    onPrimary = LightSolo.surface,
    primaryContainer = LightSolo.surface2,
    onPrimaryContainer = LightSolo.ink,
    secondary = LightSolo.inkSoft,
    onSecondary = LightSolo.surface,
    secondaryContainer = LightSolo.surface2,
    onSecondaryContainer = LightSolo.ink,
    tertiary = SolGold,
    onTertiary = LightSolo.surface,
    background = LightSolo.canvas,
    onBackground = LightSolo.ink,
    surface = LightSolo.surface,
    onSurface = LightSolo.ink,
    surfaceVariant = LightSolo.surface2,
    onSurfaceVariant = LightSolo.inkSoft,
    error = ErrorClay,
    onError = LightSolo.surface,
    outline = LightSolo.line,
    outlineVariant = LightSolo.line,
    inverseSurface = LightSolo.ink,
    inverseOnSurface = LightSolo.surface,
    inversePrimary = DarkSolo.ink,
    surfaceTint = Color.Transparent
)

@Composable
fun SoloFitTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val solo = if (darkTheme) DarkSolo else LightSolo
    CompositionLocalProvider(LocalSoloColors provides solo) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
