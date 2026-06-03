package com.solofit.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.solofit.app.ui.theme.Amber
import com.solofit.app.ui.theme.Clay
import com.solofit.app.ui.theme.ClayDark
import com.solofit.app.ui.theme.Cyan
import com.solofit.app.ui.theme.Emerald
import com.solofit.app.ui.theme.Sage
import com.solofit.app.ui.theme.Taupe

data class PageAccent(
    val primary: Color,
    val secondary: Color = primary,
    val container: Color = primary.copy(alpha = 0.15f),
    val onContainer: Color = primary,
    val name: String = ""
)

val LocalPageAccent = staticCompositionLocalOf {
    PageAccent(primary = Clay, secondary = ClayDark, name = "default")
}

val DefaultAccent = PageAccent(primary = Clay, secondary = ClayDark, name = "default")
val WorkoutAccent = PageAccent(primary = Amber, secondary = Cyan, name = "workout")
val NutritionAccent = PageAccent(primary = Sage, secondary = Sage, name = "nutrition")
val StrengthAccent = PageAccent(primary = Taupe, secondary = Taupe, name = "strength")

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides WorkoutAccent) { content() }
}

@Composable
fun NutritionTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides NutritionAccent) { content() }
}

@Composable
fun StrengthTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides StrengthAccent) { content() }
}

@Composable
fun DefaultTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides DefaultAccent) { content() }
}
