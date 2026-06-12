package com.solofit.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── GLOBAL FOUNDATIONS: DARK-FIRST (warm charcoal, no pure white/black) ───
val SurfaceBg = Color(0xFF161514)        // Deep warm charcoal
val CardPrimary = Color(0xFF1E1D1B)      // Carbon
val CardSecondary = Color(0xFF282624)    // Darker carbon
val Hairline = Color(0xFF353330)         // Subtle warm border
val TextPrimary = Color(0xFFEAE6DF)      // Warm off-white
val TextSecondary = Color(0xFF9C9690)    // Muted warm gray
val TextTertiary = Color(0xFF6B6560)     // Even more muted

// Light variants (for light mode)
val LightBg = Color(0xFFF0EBE4)          // Warm cream
val LightCardPrimary = Color(0xFFFAF6F1) // Warm white
val LightCardSecondary = Color(0xFFF0EBE4)
val LightHairline = Color(0xFFE0DBD4)
val LightTextPrimary = Color(0xFF2A2824) // Warm near-black
val LightTextSecondary = Color(0xFF8A8480)

// ─── BACKWARD-COMPAT DARK ALIASES ───
val DarkBg = SurfaceBg
val DarkCard = CardPrimary
val DarkSurface = CardSecondary
val DarkHairline = Hairline
val DarkText = TextPrimary
val DarkTextSecondary = TextSecondary

// ─── SECTION ACCENTS (dark-optimised — brighter & more saturated) ───
val Terracotta = Color(0xFFD4895F)       // Onboarding interactive accent
val OliveClay = Color(0xFF7A9448)        // Dashboard healthy completion
val MossGreen = Color(0xFF5C9A65)        // Nutrition progress
val WarmSand = Color(0xFFC8B8A4)         // Nutrition search field
val RustIron = Color(0xFFBF6242)         // Workout routines
val Ochre = Color(0xFFE8A842)            // Workout active timer
val TwilightBlue = Color(0xFF5A8FA8)     // Recovery metrics
val LavenderGrey = Color(0xFF6B6380)     // Meditation background (dark muted purple)
val SlateBlue = Color(0xFF7A9BB5)        // Body charts
val JournalAccent = Color(0xFFC8A88A)    // Journal vintage tan
val WalkingAccent = Color(0xFF7A8E6A)    // Walking muted olive

// ─── SOL INTELLIGENCE ENGINE ACCENT ───
val SolAmber = Color(0xFFDBA060)         // Distinct honey-amber for SOL engine

// ─── SEMANTIC HEALTH SIGNALS (dark-optimised) ───
val HighGreen = Color(0xFF6EA86E)
val MidAmber = Color(0xFFD4A84A)
val LowRed = Color(0xFFC47A5A)

val DarkSuccess = Color(0xFF6B9A5E)
val DarkWarning = Color(0xFFC49A4A)
val DarkError = Color(0xFFA06A5A)

// ─── MACRO COLORS (dark-optimised) ───
val ProteinColor = Color(0xFFD49575)
val CarbsColor = Color(0xFF6BAEC0)
val FatsColor = Color(0xFFD4B06B)

// ─── BACKWARD-COMPAT ALIASES ───
val NutritionAccent = MossGreen
val NutritionProtein = ProteinColor
val RecoveryAccent = TwilightBlue
val WorkoutAccent = RustIron
val MeditationAccent = LavenderGrey
val BodyRecompAccent = SlateBlue
val WorkoutActiveTimer = Ochre

val SolAccent = SolAmber

// ─── TEXT ALIASES ───
val PrimaryText = TextPrimary
val SecondaryText = TextSecondary
val CardCream = CardPrimary
