package com.solofit.app.domain.model

/** A planned exercise inside a routine being built (UI/domain transfer object). */
data class ExercisePlan(
    val name: String,
    val muscleGroup: String,
    val targetSets: Int = 3
)
