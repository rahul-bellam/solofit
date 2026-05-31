package com.solofit.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solofit.app.domain.model.ActivityLevel
import com.solofit.app.domain.model.FitnessGoal
import com.solofit.app.domain.model.Gender

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: Gender,
    val weightKg: Double,
    val heightCm: Double,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    // Cached computed targets (recomputed whenever inputs change)
    val targetCalories: Int,
    val targetProtein: Int,
    val targetCarbs: Int,
    val targetFats: Int,
    val bmr: Int,
    val tdee: Int,
    val createdAt: Long = System.currentTimeMillis()
)
