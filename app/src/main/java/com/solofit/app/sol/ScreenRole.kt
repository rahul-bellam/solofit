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
