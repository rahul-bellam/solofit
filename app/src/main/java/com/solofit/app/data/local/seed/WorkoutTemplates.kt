package com.solofit.app.data.local.seed

/**
 * Pre-built workout templates that map to [TrainingGoal] options.
 * Each template defines a split, exercise selection, sets, reps, and optional notes.
 */
data class WorkoutTemplate(
    val name: String,
    val description: String,
    val daysPerWeek: Int,
    val goal: String,
    val exercises: List<TemplateExercise>
)

data class TemplateExercise(
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: String,
    val notes: String = ""
)

object WorkoutTemplates {

    val templates: List<WorkoutTemplate> = listOf(
        // ── 1. Full Body 3x/week (Beginner) ──
        WorkoutTemplate(
            name = "Full Body 3x/week",
            description = "Hit every muscle group 3 times per week. Ideal for beginners building a base.",
            daysPerWeek = 3,
            goal = "GENERAL_FITNESS",
            exercises = listOf(
                TemplateExercise("Barbell Back Squat", "Legs", 3, "8-10", "Warm up with 2 progressive sets"),
                TemplateExercise("Barbell Bench Press", "Chest", 3, "8-10", "Use spotter or safety pins"),
                TemplateExercise("Bent-Over Barbell Row", "Back", 3, "8-10", "Squeeze shoulder blades at top"),
                TemplateExercise("Overhead Press", "Shoulders", 3, "8-10", "Brace core throughout"),
                TemplateExercise("Romanian Deadlift", "Legs", 3, "10-12", "Feel the hamstring stretch"),
                TemplateExercise("Cable Fly", "Chest", 2, "12-15", "Control the negative"),
                TemplateExercise("Lat Pulldown", "Back", 2, "12-15", "Full stretch at top"),
                TemplateExercise("Plank", "Core", 3, "45s", "Keep breathing steadily")
            )
        ),

        // ── 2. Upper/Lower 4 days ──
        WorkoutTemplate(
            name = "Upper/Lower Split",
            description = "Alternate upper and lower body days. Great balance of volume and recovery.",
            daysPerWeek = 4,
            goal = "GENERAL_FITNESS",
            exercises = listOf(
                // Upper A
                TemplateExercise("Barbell Bench Press", "Chest", 4, "6-8", "Heavy compound day"),
                TemplateExercise("Bent-Over Barbell Row", "Back", 4, "6-8", "Overhand grip"),
                TemplateExercise("Seated Dumbbell Press", "Shoulders", 3, "8-10", "Control the descent"),
                TemplateExercise("Cable Fly", "Chest", 3, "10-12", "Squeeze at center"),
                TemplateExercise("Lat Pulldown", "Back", 3, "10-12", "Wide grip"),
                TemplateExercise("Barbell Curl", "Arms", 2, "10-12", "No swinging"),
                TemplateExercise("Triceps Pushdown", "Arms", 2, "10-12", "Lock out at bottom"),
                // Lower A
                TemplateExercise("Barbell Back Squat", "Legs", 4, "6-8", "Heavy compound day"),
                TemplateExercise("Romanian Deadlift", "Legs", 3, "8-10", "Slight knee bend"),
                TemplateExercise("Leg Press", "Legs", 3, "10-12", "Feet shoulder-width"),
                TemplateExercise("Leg Curl", "Legs", 3, "10-12", "Squeeze hamstrings"),
                TemplateExercise("Standing Calf Raise", "Legs", 3, "12-15", "Full range of motion"),
                TemplateExercise("Plank", "Core", 3, "60s", "Maintain tension")
            )
        ),

        // ── 3. Push/Pull/Legs 6 days ──
        WorkoutTemplate(
            name = "Push/Pull/Legs",
            description = "Classic 6-day split. High volume for intermediate to advanced lifters.",
            daysPerWeek = 6,
            goal = "BODYBUILDING",
            exercises = listOf(
                // Push
                TemplateExercise("Barbell Bench Press", "Chest", 4, "6-8", "Progressive overload each week"),
                TemplateExercise("Incline Dumbbell Press", "Chest", 3, "8-10", "30-degree incline"),
                TemplateExercise("Cable Fly", "Chest", 3, "12-15", "Constant tension"),
                TemplateExercise("Overhead Press", "Shoulders", 4, "6-8", "Strict form, no leg drive"),
                TemplateExercise("Lateral Raise", "Shoulders", 4, "12-15", "Slow negative"),
                TemplateExercise("Triceps Pushdown", "Arms", 3, "10-12", "V-bar attachment"),
                TemplateExercise("Skull Crusher", "Arms", 3, "10-12", "EZ bar preferred"),
                // Pull
                TemplateExercise("Deadlift", "Back", 4, "5-6", "Heavy, rest 3-4 min between sets"),
                TemplateExercise("Pull-Up", "Back", 4, "6-10", "Add weight when 10 reps easy"),
                TemplateExercise("Bent-Over Barbell Row", "Back", 3, "8-10", "Overhand grip"),
                TemplateExercise("Face Pull", "Back", 3, "12-15", "External rotation at top"),
                TemplateExercise("Barbell Curl", "Arms", 3, "8-10", "Strict form"),
                TemplateExercise("Hammer Curl", "Arms", 3, "10-12", "Target brachialis"),
                // Legs
                TemplateExercise("Barbell Back Squat", "Legs", 4, "6-8", "ATG or parallel"),
                TemplateExercise("Romanian Deadlift", "Legs", 4, "8-10", "Deep stretch"),
                TemplateExercise("Leg Press", "Legs", 3, "10-12", "Close stance for quads"),
                TemplateExercise("Walking Lunge", "Legs", 3, "12-14", "Long stride for glutes"),
                TemplateExercise("Leg Extension", "Legs", 3, "12-15", "Squeeze at top"),
                TemplateExercise("Seated Leg Curl", "Legs", 3, "12-15", "Controlled tempo"),
                TemplateExercise("Standing Calf Raise", "Legs", 4, "10-12", "Pause at top"),
                TemplateExercise("Hanging Leg Raise", "Core", 3, "12-15", "Full ROM")
            )
        ),

        // ── 4. Bro Split 5 days ──
        WorkoutTemplate(
            name = "Bro Split",
            description = "One muscle group per day, 5 days. Classic bodybuilding approach.",
            daysPerWeek = 5,
            goal = "BODYBUILDING",
            exercises = listOf(
                // Chest
                TemplateExercise("Barbell Bench Press", "Chest", 4, "8-10", "Flat bench"),
                TemplateExercise("Incline Dumbbell Press", "Chest", 3, "10-12", "Upper chest focus"),
                TemplateExercise("Cable Fly", "Chest", 3, "12-15", "Low to high"),
                TemplateExercise("Chest Dip", "Chest", 3, "10-12", "Lean forward"),
                // Back
                TemplateExercise("Deadlift", "Back", 4, "5-6", "Warm up thoroughly"),
                TemplateExercise("Pull-Up", "Back", 4, "8-10", "Vary grip width"),
                TemplateExercise("Seated Cable Row", "Back", 3, "10-12", "Close grip"),
                TemplateExercise("Single-Arm Dumbbell Row", "Back", 3, "10-12", "Full stretch"),
                TemplateExercise("Lat Pulldown", "Back", 3, "12-15", "Behind neck optional"),
                // Shoulders
                TemplateExercise("Overhead Press", "Shoulders", 4, "8-10", "Standing preferred"),
                TemplateExercise("Arnold Press", "Shoulders", 3, "10-12", "Full rotation"),
                TemplateExercise("Lateral Raise", "Shoulders", 4, "12-15", "Slow controlled reps"),
                TemplateExercise("Rear Delt Fly", "Shoulders", 3, "15-20", "Light weight, high reps"),
                TemplateExercise("Upright Row", "Shoulders", 3, "10-12", "Wide grip for delts"),
                // Arms
                TemplateExercise("Barbell Curl", "Arms", 4, "8-10", "Strict form"),
                TemplateExercise("Preacher Curl", "Arms", 3, "10-12", "Full extension"),
                TemplateExercise("Hammer Curl", "Arms", 3, "10-12", "Alternate arms"),
                TemplateExercise("Close-Grip Bench Press", "Arms", 4, "8-10", "Hands 14 inches apart"),
                TemplateExercise("Triceps Pushdown", "Arms", 3, "10-12", "Rope attachment"),
                TemplateExercise("Overhead Triceps Extension", "Arms", 3, "12-15", "Dumbbell or cable"),
                // Legs
                TemplateExercise("Barbell Back Squat", "Legs", 4, "8-10", "Full depth"),
                TemplateExercise("Leg Press", "Legs", 4, "10-12", "Feet high and wide"),
                TemplateExercise("Romanian Deadlift", "Legs", 3, "10-12", "Feel the stretch"),
                TemplateExercise("Leg Extension", "Legs", 3, "12-15", "Squeeze peak"),
                TemplateExercise("Leg Curl", "Legs", 3, "12-15", "Slow negative"),
                TemplateExercise("Standing Calf Raise", "Legs", 4, "12-15", "Pause at top"),
                TemplateExercise("Hip Thrust", "Legs", 3, "10-12", "Squeeze glutes hard")
            )
        ),

        // ── 5. Dumbbell-Only Home ──
        WorkoutTemplate(
            name = "Dumbbell-Only Home",
            description = "Full workout with just a pair of dumbbells. Perfect for home gyms.",
            daysPerWeek = 3,
            goal = "GENERAL_FITNESS",
            exercises = listOf(
                TemplateExercise("Dumbbell Bench Press", "Chest", 3, "8-12", "Floor press if no bench"),
                TemplateExercise("Incline Dumbbell Press", "Chest", 3, "8-12", "Use pillow for incline"),
                TemplateExercise("Single-Arm Dumbbell Row", "Back", 3, "10-12", "Use chair for support"),
                TemplateExercise("Dumbbell Curl", "Arms", 3, "10-12", "Alternate arms"),
                TemplateExercise("Seated Dumbbell Press", "Shoulders", 3, "8-12", "Sit on edge of bed/chair"),
                TemplateExercise("Lateral Raise", "Shoulders", 3, "12-15", "Light weight, strict form"),
                TemplateExercise("Romanian Deadlift", "Legs", 3, "10-12", "Hold dumbbells at sides"),
                TemplateExercise("Walking Lunge", "Legs", 3, "12-14", "Dumbbells at sides"),
                TemplateExercise("Goblet Squat", "Legs", 3, "10-12", "Hold one dumbbell at chest"),
                TemplateExercise("Triceps Kickback", "Arms", 3, "12-15", "Upper arm parallel to floor"),
                TemplateExercise("Concentration Curl", "Arms", 2, "12-15", "Brace elbow on thigh"),
                TemplateExercise("Plank", "Core", 3, "45-60s", "Keep breathing"),
                TemplateExercise("Russian Twist", "Core", 3, "20 total", "Hold light dumbbell"),
                TemplateExercise("Bicycle Crunch", "Core", 3, "20 total", "Slow and controlled")
            )
        ),

        // ── 6. Quick 20-min ──
        WorkoutTemplate(
            name = "Quick 20-Min",
            description = "Time-efficient full-body session. Superset format for maximum work in minimum time.",
            daysPerWeek = 3,
            goal = "GENERAL_FITNESS",
            exercises = listOf(
                TemplateExercise("Goblet Squat", "Legs", 3, "10", "Superset with Push-Up"),
                TemplateExercise("Push-Up", "Chest", 3, "15", "Superset with Goblet Squat"),
                TemplateExercise("Dumbbell Row", "Back", 3, "10 each", "Superset with Overhead Press"),
                TemplateExercise("Overhead Press", "Shoulders", 3, "10", "Superset with Dumbbell Row"),
                TemplateExercise("Romanian Deadlift", "Legs", 3, "12", "Superset with Plank"),
                TemplateExercise("Plank", "Core", 3, "30s", "Superset with Romanian Deadlift"),
                TemplateExercise("Dumbbell Curl", "Arms", 2, "12", "Finisher"),
                TemplateExercise("Triceps Kickback", "Arms", 2, "12", "Finisher"),
                TemplateExercise("Mountain Climbers", "Core", 2, "30s", "Burnout finish")
            )
        )
    )
}
