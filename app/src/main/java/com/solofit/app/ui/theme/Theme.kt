package com.solofit.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val WarmDarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkText,
    primaryContainer = DarkCard,
    onPrimaryContainer = DarkText,
    secondary = DarkTextSecondary,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkText,
    tertiary = DarkAccent,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    error = DarkError,
    onError = Color.White,
    outline = DarkHairline,
    outlineVariant = DarkHairline,
    inverseSurface = DarkText,
    inverseOnSurface = DarkBg,
    inversePrimary = DarkAccent,
    surfaceTint = DarkAccent
)

private val LightColors = lightColorScheme(
    primary = Amber,
    onPrimary = Color.White,
    primaryContainer = AmberSoft,
    onPrimaryContainer = PrimaryText,
    secondary = SecondaryText,
    onSecondary = Color.White,
    secondaryContainer = CardCream,
    onSecondaryContainer = PrimaryText,
    background = PageBg,
    onBackground = PrimaryText,
    surface = CardCream,
    onSurface = PrimaryText,
    surfaceVariant = Hairline,
    onSurfaceVariant = SecondaryText,
    error = LowRed,
    onError = Color.White,
    outline = Hairline,
    inversePrimary = Amber,
    surfaceTint = Amber
)

@Composable
fun SoloFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WarmDarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
