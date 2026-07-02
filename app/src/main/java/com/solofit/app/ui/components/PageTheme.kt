package com.solofit.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.solofit.app.ui.theme.BodyRecompAccent
import com.solofit.app.ui.theme.JournalAccent
import com.solofit.app.ui.theme.MeditationAccent
import com.solofit.app.ui.theme.NutritionAccent
import com.solofit.app.ui.theme.RecoveryAccent
import com.solofit.app.ui.theme.SolGold
import com.solofit.app.ui.theme.SoloFitTheme
import com.solofit.app.ui.theme.WorkoutAccent

data class PageAccent(
    val primary: Color = SolGold,
    val name: String = "default"
)

val LocalPageAccent = staticCompositionLocalOf { PageAccent() }
val DefaultAccent = PageAccent(primary = SolGold)

@Composable
private fun SectionAccent(accent: Color, name: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPageAccent provides PageAccent(primary = accent, name = name)) {
        content()
    }
}

@Composable
fun NutritionTheme(content: @Composable () -> Unit) =
    SectionAccent(NutritionAccent, "nutrition", content)

@Composable
fun JournalTheme(content: @Composable () -> Unit) =
    SectionAccent(JournalAccent, "journal", content)

@Composable
fun ProgressTheme(content: @Composable () -> Unit) =
    SectionAccent(BodyRecompAccent, "progress", content)

@Composable
fun RecoveryTheme(content: @Composable () -> Unit) =
    SectionAccent(RecoveryAccent, "recovery", content)

@Composable
fun MeditationTheme(content: @Composable () -> Unit) =
    SectionAccent(MeditationAccent, "meditation", content)

@Composable
fun BodyTheme(content: @Composable () -> Unit) =
    SectionAccent(BodyRecompAccent, "body", content)

// ── Workout & Strength force the dark "intense" skin, regardless of app theme. ──
@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
    SoloFitTheme(darkTheme = true) {
        SectionAccent(WorkoutAccent, "workout", content)
    }
}

@Composable
fun StrengthTheme(content: @Composable () -> Unit) {
    SoloFitTheme(darkTheme = true) {
        SectionAccent(WorkoutAccent, "strength", content)
    }
}
