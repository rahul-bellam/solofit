package com.solofit.app.sol

import com.solofit.app.ui.theme.MossGreen
import com.solofit.app.ui.theme.RustIron
import com.solofit.app.ui.theme.SlateBlue
import com.solofit.app.ui.theme.TwilightBlue
import com.solofit.app.ui.theme.LavenderGrey
import com.solofit.app.ui.theme.JournalAccent
import com.solofit.app.ui.theme.WalkingAccent
import com.solofit.app.ui.theme.Terracotta
import androidx.compose.ui.graphics.Color

enum class ScreenRole(
    val displayName: String,
    val description: String,
    val accentColor: Color,
    val voicePersonality: VoicePersonality,
    val tone: String
) {
    DRILL_SERGEANT("Drill Sergeant", "Direct, imperative, strict during workout", RustIron, VoicePersonality.COACH, "commanding"),
    WISE_MENTOR("Wise Mentor", "Normalizes recovery, reduces guilt", TwilightBlue, VoicePersonality.COMPANION, "warm"),
    PRACTICAL_GUIDE("Practical Guide", "Reduces perfectionism, practical advice", MossGreen, VoicePersonality.COMPANION, "supportive"),
    MOVEMENT_ADVOCATE("Movement Advocate", "Celebrates all movement", WalkingAccent, VoicePersonality.COMPANION, "encouraging"),
    COMPASSIONATE_COMPANION("Compassionate Companion", "Removes pressure, gentle", LavenderGrey, VoicePersonality.COMPANION, "gentle"),
    REFLECTIVE_PARTNER("Reflective Partner", "Rewards honesty, reflective", JournalAccent, VoicePersonality.MINIMAL, "contemplative"),
    PATIENT_STRATEGIST("Patient Strategist", "Emphasizes trends over daily changes", SlateBlue, VoicePersonality.COMPANION, "measured"),
    GUIDE("Guide", "Balanced, helpful", Terracotta, VoicePersonality.COMPANION, "balanced")
}

object ScreenRoleEngine {

    fun encouragement(role: ScreenRole, context: String): String = when (role) {
        ScreenRole.DRILL_SERGEANT -> when {
            "before" in context -> "Today's session moves you forward."
            "during" in context -> "Stay focused. Finish the set."
            "after" in context -> "You showed up. That matters."
            else -> "Execute."
        }
        ScreenRole.WISE_MENTOR -> "Recovery supports progress. Rest is productive."
        ScreenRole.PRACTICAL_GUIDE -> "One balanced meal still matters. Small nutritional improvements compound."
        ScreenRole.MOVEMENT_ADVOCATE -> "You moved more today. Short walks count. Consistency matters more than intensity."
        ScreenRole.COMPASSIONATE_COMPANION -> "Two minutes still counts. Stillness is a skill. Returning is progress."
        ScreenRole.REFLECTIVE_PARTNER -> "Reflection builds awareness. Writing things down helps patterns emerge."
        ScreenRole.PATIENT_STRATEGIST -> "Progress is occurring gradually. Body recomposition takes time."
        ScreenRole.GUIDE -> "Small consistent actions compound over time."
    }

    fun greeting(role: ScreenRole): String = when (role) {
        ScreenRole.DRILL_SERGEANT -> "Ready to work."
        ScreenRole.WISE_MENTOR -> "Let's check in with how you're feeling."
        ScreenRole.PRACTICAL_GUIDE -> "Let's look at your nutrition today."
        ScreenRole.MOVEMENT_ADVOCATE -> "Every step counts."
        ScreenRole.COMPASSIONATE_COMPANION -> "Take a breath. You're here."
        ScreenRole.REFLECTIVE_PARTNER -> "Let's reflect on today."
        ScreenRole.PATIENT_STRATEGIST -> "The long view matters most."
        ScreenRole.GUIDE -> "Here's your overview."
    }

    fun emptyState(role: ScreenRole): String = when (role) {
        ScreenRole.DRILL_SERGEANT -> "No workout logged yet. Start when ready."
        ScreenRole.WISE_MENTOR -> "No recovery data yet. Logging takes a moment."
        ScreenRole.PRACTICAL_GUIDE -> "No meals logged yet. Start with one entry."
        ScreenRole.MOVEMENT_ADVOCATE -> "No movement data yet. Even a short walk counts."
        ScreenRole.COMPASSIONATE_COMPANION -> "No meditation yet. Even one minute counts."
        ScreenRole.REFLECTIVE_PARTNER -> "No journal entries yet. One sentence is enough."
        ScreenRole.PATIENT_STRATEGIST -> "Not enough data for trends yet. Log consistently."
        ScreenRole.GUIDE -> "Start tracking to see insights."
    }
}
