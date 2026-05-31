package com.solofit.app.data.local.seed

/**
 * Static catalog of exercises grouped by primary muscle group.
 * Used by the Routine Builder to let users pick exercises offline.
 * No images are bundled (keeps APK small); each exercise maps to a
 * Material icon by muscle group at the UI layer.
 */
data class ExerciseTemplate(val name: String, val muscleGroup: String)

object ExerciseSeedData {
    val exercises: List<ExerciseTemplate> = listOf(
        // Chest
        ExerciseTemplate("Barbell Bench Press", "Chest"),
        ExerciseTemplate("Incline Dumbbell Press", "Chest"),
        ExerciseTemplate("Dumbbell Bench Press", "Chest"),
        ExerciseTemplate("Cable Fly", "Chest"),
        ExerciseTemplate("Push-Up", "Chest"),
        ExerciseTemplate("Chest Dip", "Chest"),
        // Back
        ExerciseTemplate("Deadlift", "Back"),
        ExerciseTemplate("Pull-Up", "Back"),
        ExerciseTemplate("Bent-Over Barbell Row", "Back"),
        ExerciseTemplate("Lat Pulldown", "Back"),
        ExerciseTemplate("Seated Cable Row", "Back"),
        ExerciseTemplate("Single-Arm Dumbbell Row", "Back"),
        ExerciseTemplate("Face Pull", "Back"),
        // Legs
        ExerciseTemplate("Barbell Back Squat", "Legs"),
        ExerciseTemplate("Front Squat", "Legs"),
        ExerciseTemplate("Romanian Deadlift", "Legs"),
        ExerciseTemplate("Leg Press", "Legs"),
        ExerciseTemplate("Walking Lunge", "Legs"),
        ExerciseTemplate("Leg Extension", "Legs"),
        ExerciseTemplate("Leg Curl", "Legs"),
        ExerciseTemplate("Standing Calf Raise", "Legs"),
        ExerciseTemplate("Bulgarian Split Squat", "Legs"),
        ExerciseTemplate("Hip Thrust", "Legs"),
        // Shoulders
        ExerciseTemplate("Overhead Press", "Shoulders"),
        ExerciseTemplate("Seated Dumbbell Press", "Shoulders"),
        ExerciseTemplate("Lateral Raise", "Shoulders"),
        ExerciseTemplate("Front Raise", "Shoulders"),
        ExerciseTemplate("Rear Delt Fly", "Shoulders"),
        ExerciseTemplate("Arnold Press", "Shoulders"),
        ExerciseTemplate("Upright Row", "Shoulders"),
        // Arms
        ExerciseTemplate("Barbell Curl", "Arms"),
        ExerciseTemplate("Dumbbell Curl", "Arms"),
        ExerciseTemplate("Hammer Curl", "Arms"),
        ExerciseTemplate("Preacher Curl", "Arms"),
        ExerciseTemplate("Triceps Pushdown", "Arms"),
        ExerciseTemplate("Skull Crusher", "Arms"),
        ExerciseTemplate("Overhead Triceps Extension", "Arms"),
        ExerciseTemplate("Close-Grip Bench Press", "Arms"),
        // Core
        ExerciseTemplate("Plank", "Core"),
        ExerciseTemplate("Hanging Leg Raise", "Core"),
        ExerciseTemplate("Cable Crunch", "Core"),
        ExerciseTemplate("Russian Twist", "Core"),
        ExerciseTemplate("Ab Wheel Rollout", "Core"),
        // Cardio
        ExerciseTemplate("Treadmill Run", "Cardio"),
        ExerciseTemplate("Stationary Bike", "Cardio"),
        ExerciseTemplate("Rowing Machine", "Cardio"),
        ExerciseTemplate("Stair Climber", "Cardio")
    )

    val muscleGroups: List<String> =
        exercises.map { it.muscleGroup }.distinct()
}
