package com.solofit.app.domain.model

enum class OnboardingFocus(
    val displayName: String,
    val description: String
) {
    MOVE_MORE("Move more", "Movement is your focus. You want to increase activity and build momentum."),
    RETURNING("Returning to fitness", "You're coming back after a break. Rebuilding consistency is key."),
    ALREADY_EXERCISING("Already exercise regularly", "You maintain a routine. Focus on quality and progression."),
    BALANCE_RECOVERY("Better balance and recovery", "Wellness and recovery are your priorities. Train with intention.")
}
