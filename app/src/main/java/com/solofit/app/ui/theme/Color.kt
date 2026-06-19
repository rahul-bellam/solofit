package com.solofit.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════════════════
//  SoloFit palette — paper, leather & aged metal.
//  No blue. No purple. No neon. No gradients. (See MASTER DESIGN SYSTEM.)
//  Neutral surfaces/text resolve through the theme via LocalSoloColors,
//  so every screen flips between light & dark with no per-call edits.
// ════════════════════════════════════════════════════════════════════

// ─── RAW NEUTRALS: light (the default — premium notebook) ───
val LightBg = Color(0xFFF4EFE8)            // Canvas
val LightCardPrimary = Color(0xFFFCF8F3)   // Primary surface
val LightCardSecondary = Color(0xFFEFE7DD) // Secondary surface
val LightHairline = Color(0xFFDDD4C8)
val LightTextPrimary = Color(0xFF2D2A26)
val LightTextSecondary = Color(0xFF756F69)
private val LightTextTertiary = Color(0xFF9A938B)

// ─── RAW NEUTRALS: dark (warm charcoal — also the forced workout skin) ───
private val DarkCanvas = Color(0xFF191613)
private val DarkSurface1 = Color(0xFF221E1A)
private val DarkSurface2 = Color(0xFF2E2823)
private val DarkLine = Color(0xFF38322C)
private val DarkInk = Color(0xFFF4EEE6)
private val DarkInkSoft = Color(0xFFA39A8F)
private val DarkInkFaint = Color(0xFF6E655B)

// ─── Theme-resolving neutral holder ───
@Immutable
data class SoloColors(
    val canvas: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val isDark: Boolean
)

val LightSolo = SoloColors(
    canvas = LightBg, surface = LightCardPrimary, surface2 = LightCardSecondary,
    line = LightHairline, ink = LightTextPrimary, inkSoft = LightTextSecondary,
    inkFaint = LightTextTertiary, isDark = false
)
val DarkSolo = SoloColors(
    canvas = DarkCanvas, surface = DarkSurface1, surface2 = DarkSurface2,
    line = DarkLine, ink = DarkInk, inkSoft = DarkInkSoft,
    inkFaint = DarkInkFaint, isDark = true
)

val LocalSoloColors = staticCompositionLocalOf { LightSolo }

// ─── PUBLIC NEUTRAL TOKENS (theme-aware via the CompositionLocal) ───
val SurfaceBg: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.canvas
val CardPrimary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.surface
val CardSecondary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.surface2
val Hairline: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.line
val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.ink
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.inkSoft
val TextTertiary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.inkFaint

// Legacy neutral aliases — kept so existing imports keep working.
val PrimaryText: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.ink
val SecondaryText: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.inkSoft
val CardCream: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.surface
val DarkBg: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.canvas
val DarkCard: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.surface
val DarkSurface: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.surface2
val DarkHairline: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.line
val DarkText: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.ink
val DarkTextSecondary: Color @Composable @ReadOnlyComposable get() = LocalSoloColors.current.inkSoft

// ─── SOL — Honey Gold. Reserved for Sol only (never generic buttons). ───
val SolGold = Color(0xFFC98A3D)
val HoneyGold = SolGold
val SolAccent = SolGold

// ─── SECTION ACCENTS (earthy leather & aged metal) ───
val WorkoutAccent = Color(0xFFB45F38)    // Burnished Copper
val NutritionAccent = Color(0xFF7A6B4A)  // Olive Leather
val RecoveryAccent = Color(0xFF6E655B)   // Slate Bronze
val MeditationAccent = Color(0xFF8A7C72) // Smoked Taupe
val WalkingAccent = Color(0xFF827A6E)    // Weathered Stone
val JournalAccent = Color(0xFF9A6E58)    // Clay
val BodyRecompAccent = Color(0xFFA17A50) // Aged Bronze
val HabitsAccent = Color(0xFF8B7355)     // Weathered Bronze

// Raw accent names (kept for compatibility).
val Copper = WorkoutAccent
val OliveLeather = NutritionAccent
val SlateBronze = RecoveryAccent
val SmokedTaupe = MeditationAccent
val WeatheredStone = WalkingAccent
val Clay = JournalAccent
val AgedBronze = BodyRecompAccent

// Legacy accent aliases — repointed onto the earthy palette (no more blue/lavender).
val Terracotta = JournalAccent
val MossGreen = NutritionAccent
val RustIron = WorkoutAccent
val TwilightBlue = RecoveryAccent
val LavenderGrey = MeditationAccent
val SlateBlue = BodyRecompAccent
val OliveClay = NutritionAccent
val WorkoutActiveTimer = WorkoutAccent
val Ochre = WorkoutAccent

// ─── SEMANTIC SIGNALS ───
val SuccessGreen = Color(0xFF70805E)
val WarningAmber = Color(0xFFA67B4A)
val ErrorClay = Color(0xFFA06052)

val HighGreen = SuccessGreen
val MidAmber = WarningAmber
val LowRed = ErrorClay
val DarkSuccess = SuccessGreen
val DarkWarning = WarningAmber
val DarkError = ErrorClay

// ─── MACRO NUTRIENTS (warm & distinct — no teal/blue) ───
val ProteinColor = Color(0xFFA66A4D) // Sienna
val CarbsColor = Color(0xFF9E8C5A)   // Wheat
val FatsColor = Color(0xFFB28A52)    // Caramel
val NutritionProtein = ProteinColor
