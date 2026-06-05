package com.solofit.app.sol

object VoicePersonalityTransformer {

    fun transform(text: String, personality: VoicePersonality): String = when (personality) {
        VoicePersonality.COACH -> coach(text)
        VoicePersonality.COMPANION -> companion(text)
        VoicePersonality.MINIMAL -> minimal(text)
    }

    // Coach: Short, direct — imperative tone, removes softening phrases
    private fun coach(text: String): String = text
        .replace("a little lower than usual", "lower than usual")
        .replace("may help", "consider")
        .replace("may be beneficial", "consider")
        .replace("It appears", "")
        .replace("appears to be", "is")
        .replace("appears", "is")
        .replace("may contribute", "contributes")
        .replace("could be", "is")
        .replace("may support", "supports")
        .replace("may benefit", "benefits")
        .replace("may make", "makes")
        .trim()

    // Companion: Warm, supportive — softer, suggestion-based
    private fun companion(text: String): String = text
        .replace("Recovery is low", "Recovery is a little lower than usual")
        .replace("Recovery is lower than usual", "Recovery is a little lower than usual today")
        .replace("Reduce intensity", "It may help to reduce intensity")
        .replace("Reduce training intensity", "Consider reducing intensity")
        .replace("may be beneficial", "could be a good idea")
        .replace("may be helpful", "could be helpful")
        .replace("Nice work today.", "Well done today.")
        .trim()

    // Minimal: Very concise — single line, drops greetings, shortens
    private fun minimal(text: String): String {
        val lines = text.split(". ").filter { it.isNotBlank() }
        if (lines.isEmpty()) return ""

        val first = lines.first()
            .replace("Good morning.", "")
            .replace("Good morning", "")
            .replace("Your body appears to be asking for extra recovery.", "Prioritize recovery.")
            .replace("Your body appears ready for a normal or challenging training session.", "Ready for training.")
            .replace("Consistency is one of the strongest predictors of progress.", "Consistency matters.")
            .trim()

        if (first.isEmpty() && lines.size > 1) {
            val second = lines[1]
                .replace("Recovery looks strong today.", "Recovery strong.")
                .replace("Recovery is within your normal range.", "Recovery normal.")
                .replace("Recovery is lower than usual today.", "Recovery low.")
                .replace("You've reached your protein goal today.", "Protein goal met.")
                .replace("You've reached your movement goal today.", "Movement goal met.")
                .trim()
            return if (second.isNotEmpty()) "$second." else text.split(". ").getOrElse(1) { "" }
        }

        return when {
            first.contains("Recovery looks strong") -> "Recovery strong."
            first.contains("Recovery is within") -> "Recovery normal."
            first.contains("Recovery is lower than usual") || first.contains("Recovery low") -> "Recovery low."
            first.contains("No workout") -> "No workout logged."
            first.contains("Workout complete") -> "Workout done."
            first.contains("Protein goal") || first.contains("reached your protein") -> "Protein goal met."
            first.contains("Movement goal") || first.contains("reached your movement") -> "Movement goal met."
            first.contains("Sleep") && first.contains("improved") -> "Sleep improved."
            first.contains("Sleep") && first.contains("shorter") -> "Sleep short."
            first.contains("below normal") -> "Sleep low."
            first.contains("Stress appears") || first.contains("Stress") -> "Stress elevated."
            first.contains("Meditation") -> "Meditation streak."
            first.contains("Journaling") || first.contains("journaling") -> "Journal streak."
            first.contains("reached") -> "Goal met."
            else -> first.removeSuffix(".").let { if (it.length > 40) it.take(37) + "..." else it } + "."
        }
    }
}
