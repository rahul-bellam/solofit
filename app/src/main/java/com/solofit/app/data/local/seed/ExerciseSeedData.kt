package com.solofit.app.data.local.seed

/**
 * Static catalog of exercises grouped by primary muscle group.
 * Used by the Routine Builder to let users pick exercises offline.
 * No images are bundled (keeps APK small); each exercise maps to a
 * Material icon by muscle group at the UI layer.
 */
data class ExerciseTemplate(
    val name: String,
    val muscleGroup: String,
    val equipment: String = "Barbell",
    val difficulty: String = "Intermediate",
    val secondaryMuscles: String = "",
    val cue: String = ""
)

object ExerciseSeedData {
    val exercises: List<ExerciseTemplate> = listOf(
        // ── Chest ──
        ExerciseTemplate("Barbell Bench Press", "Chest", "Barbell", "Intermediate", "triceps,front delts", "Lower to mid-chest, press up and slightly back"),
        ExerciseTemplate("Incline Dumbbell Press", "Chest", "Dumbbell", "Intermediate", "front delts,triceps", "Set bench to 30-45 degrees, press dumbbells up and together"),
        ExerciseTemplate("Dumbbell Bench Press", "Chest", "Dumbbell", "Beginner", "triceps,front delts", "Keep slight arch in back, press dumbbells to meet at top"),
        ExerciseTemplate("Cable Fly", "Chest", "Cable", "Intermediate", "front delts", "Slight bend in elbows, squeeze chest at center"),
        ExerciseTemplate("Push-Up", "Chest", "Bodyweight", "Beginner", "triceps,front delts", "Hands shoulder-width, body straight as a plank"),
        ExerciseTemplate("Chest Dip", "Chest", "Bodyweight", "Intermediate", "triceps,front delts", "Lean forward slightly, lower until upper arms parallel"),
        ExerciseTemplate("Decline Press", "Chest", "Barbell", "Intermediate", "lower chest,triceps", "Lower to lower chest, press up with control"),
        ExerciseTemplate("Pec Deck", "Chest", "Machine", "Beginner", "", "Squeeze at center, controlled return"),

        // ── Back ──
        ExerciseTemplate("Deadlift", "Back", "Barbell", "Advanced", "glutes,hamstrings,core", "Drive through heels, keep bar close to body"),
        ExerciseTemplate("Pull-Up", "Back", "Bodyweight", "Intermediate", "biceps,forearms", "Pull from dead hang until chin clears bar"),
        ExerciseTemplate("Bent-Over Barbell Row", "Back", "Barbell", "Intermediate", "biceps,rear delts", "Hinge at hips, pull bar to lower ribcage"),
        ExerciseTemplate("Lat Pulldown", "Back", "Cable", "Beginner", "biceps", "Pull bar to upper chest, squeeze lats at bottom"),
        ExerciseTemplate("Seated Cable Row", "Back", "Cable", "Beginner", "biceps,lower back", "Sit tall, pull handle to lower chest, squeeze shoulder blades"),
        ExerciseTemplate("Single-Arm Dumbbell Row", "Back", "Dumbbell", "Beginner", "biceps,core", "Support on bench, pull dumbbell to hip with control"),
        ExerciseTemplate("Face Pull", "Back", "Cable", "Beginner", "rear delts,rotator cuff", "Pull rope to face, elbows high and externally rotate"),
        ExerciseTemplate("T-Bar Row", "Back", "Barbell", "Intermediate", "lats,biceps", "Keep chest against pad, pull weight toward chest"),
        ExerciseTemplate("Chest-Supported Row", "Back", "Machine", "Beginner", "lats,upper back", "Lean into pad, squeeze shoulder blades together"),
        ExerciseTemplate("Straight-Arm Pulldown", "Back", "Cable", "Intermediate", "lats,triceps", "Keep arms straight, pull cable to hips"),

        // ── Legs ──
        ExerciseTemplate("Barbell Back Squat", "Legs", "Barbell", "Intermediate", "glutes,core", "Break at hips and knees, descend until thighs parallel"),
        ExerciseTemplate("Front Squat", "Legs", "Barbell", "Advanced", "quads,core", "Elbows high, keep torso upright, squat deep"),
        ExerciseTemplate("Romanian Deadlift", "Legs", "Barbell", "Intermediate", "hamstrings,glutes", "Hinge at hips with slight knee bend, feel stretch in hamstrings"),
        ExerciseTemplate("Leg Press", "Legs", "Machine", "Beginner", "quads,glutes", "Place feet shoulder-width, lower until knees at 90 degrees"),
        ExerciseTemplate("Walking Lunge", "Legs", "Dumbbell", "Beginner", "glutes,quads", "Take long step forward, lower back knee toward ground"),
        ExerciseTemplate("Leg Extension", "Legs", "Machine", "Beginner", "", "Squeeze quads at top, control the negative"),
        ExerciseTemplate("Leg Curl", "Legs", "Machine", "Beginner", "", "Squeeze hamstrings at bottom, control the return"),
        ExerciseTemplate("Standing Calf Raise", "Legs", "Machine", "Beginner", "", "Full range of motion, pause at top for a squeeze"),
        ExerciseTemplate("Bulgarian Split Squat", "Legs", "Dumbbell", "Intermediate", "glutes,quads", "Rear foot elevated, lower until front thigh parallel"),
        ExerciseTemplate("Hip Thrust", "Legs", "Barbell", "Intermediate", "glutes,hamstrings", "Drive through heels, squeeze glutes hard at top"),
        ExerciseTemplate("Goblet Squat", "Legs", "Dumbbell", "Beginner", "quads,glutes", "Hold dumbbell at chest, squat deep"),
        ExerciseTemplate("Hack Squat", "Legs", "Machine", "Intermediate", "", "Keep back flat on pad, full depth"),
        ExerciseTemplate("Seated Leg Curl", "Legs", "Machine", "Intermediate", "", "Squeeze hamstrings at bottom, controlled return"),
        ExerciseTemplate("Glute Kickback", "Legs", "Machine", "Beginner", "", "Drive heel toward ceiling, pause at top"),

        // ── Shoulders ──
        ExerciseTemplate("Overhead Press", "Shoulders", "Barbell", "Intermediate", "triceps,upper chest", "Press from front rack, lock out overhead"),
        ExerciseTemplate("Seated Dumbbell Press", "Shoulders", "Dumbbell", "Beginner", "triceps", "Press dumbbells overhead until arms extended"),
        ExerciseTemplate("Lateral Raise", "Shoulders", "Dumbbell", "Beginner", "", "Raise arms to shoulder height, slight bend in elbows"),
        ExerciseTemplate("Front Raise", "Shoulders", "Dumbbell", "Beginner", "front delts", "Raise dumbbell to eye level, control the descent"),
        ExerciseTemplate("Rear Delt Fly", "Shoulders", "Dumbbell", "Beginner", "upper back", "Hinge forward, fly arms out targeting rear delts"),
        ExerciseTemplate("Arnold Press", "Shoulders", "Dumbbell", "Intermediate", "triceps", "Rotate palms from facing you to facing forward as you press"),
        ExerciseTemplate("Upright Row", "Shoulders", "Barbell", "Intermediate", "traps", "Pull bar to chin level, elbows lead the movement"),

        // ── Arms ──
        ExerciseTemplate("Barbell Curl", "Arms", "Barbell", "Beginner", "", "Keep elbows pinned, curl with control"),
        ExerciseTemplate("Dumbbell Curl", "Arms", "Dumbbell", "Beginner", "", "Alternate arms or curl together, squeeze at top"),
        ExerciseTemplate("Hammer Curl", "Arms", "Dumbbell", "Beginner", "brachialis,forearms", "Palms face each other, curl without rotating wrists"),
        ExerciseTemplate("Preacher Curl", "Arms", "Dumbbell", "Intermediate", "", "Rest upper arms on pad, full extension at bottom"),
        ExerciseTemplate("Triceps Pushdown", "Arms", "Cable", "Beginner", "", "Keep elbows at sides, push down and lock out"),
        ExerciseTemplate("Skull Crusher", "Arms", "Barbell", "Intermediate", "", "Lower bar to forehead, extend arms back up"),
        ExerciseTemplate("Overhead Triceps Extension", "Arms", "Dumbbell", "Beginner", "", "Hold dumbbell overhead, lower behind head, extend up"),
        ExerciseTemplate("Close-Grip Bench Press", "Arms", "Barbell", "Intermediate", "chest,triceps", "Hands shoulder-width apart, keep elbows close to body"),
        ExerciseTemplate("Cable Curl", "Arms", "Cable", "Beginner", "", "Keep elbows pinned, curl with control"),
        ExerciseTemplate("Concentration Curl", "Arms", "Dumbbell", "Beginner", "", "Brace elbow on thigh, squeeze at top"),
        ExerciseTemplate("Triceps Dip", "Arms", "Bodyweight", "Intermediate", "triceps,chest", "Lean forward slightly for chest, upright for triceps"),
        ExerciseTemplate("Triceps Kickback", "Arms", "Dumbbell", "Beginner", "", "Upper arm parallel to floor, extend fully"),

        // ── Core ──
        ExerciseTemplate("Plank", "Core", "Bodyweight", "Beginner", "", "Keep body straight from head to heels, breathe steadily"),
        ExerciseTemplate("Hanging Leg Raise", "Core", "Bodyweight", "Intermediate", "hip flexors", "Hang from bar, raise legs to parallel or higher"),
        ExerciseTemplate("Cable Crunch", "Core", "Cable", "Beginner", "", "Kneel, crunch down bringing elbows toward knees"),
        ExerciseTemplate("Russian Twist", "Core", "Bodyweight", "Beginner", "obliques", "Lean back slightly, rotate torso side to side"),
        ExerciseTemplate("Ab Wheel Rollout", "Core", "Bodyweight", "Advanced", "lats,shoulders", "Roll out keeping core tight, pull back with abs"),
        ExerciseTemplate("Bicycle Crunch", "Core", "Bodyweight", "Beginner", "obliques,rectus abdominis", "Alternate elbow to knee, extend other leg"),
        ExerciseTemplate("Dead Bug", "Core", "Bodyweight", "Beginner", "core,hip flexors", "Press lower back into floor, extend opposite arm/leg"),
        ExerciseTemplate("Mountain Climbers", "Core", "Bodyweight", "Beginner", "core,hip flexors", "Drive knees to chest quickly, keep hips level"),
        ExerciseTemplate("Pallof Press", "Core", "Cable", "Intermediate", "core,obliques", "Press cable straight out, resist rotation"),

        // ── Cardio ──
        ExerciseTemplate("Treadmill Run", "Cardio", "Machine", "Beginner", "", "Maintain consistent pace, focus on breathing"),
        ExerciseTemplate("Stationary Bike", "Cardio", "Machine", "Beginner", "", "Adjust resistance as needed, keep steady cadence"),
        ExerciseTemplate("Rowing Machine", "Cardio", "Machine", "Beginner", "legs,back", "Drive with legs first, lean back slightly, pull handle to chest"),
        ExerciseTemplate("Stair Climber", "Cardio", "Machine", "Beginner", "quads,glutes", "Stand tall, avoid leaning on rails"),
        ExerciseTemplate("Jump Rope", "Cardio", "Bodyweight", "Beginner", "", "Stay on balls of feet, consistent rhythm"),
        ExerciseTemplate("Elliptical", "Cardio", "Machine", "Beginner", "", "Maintain upright posture, use arms"),
        ExerciseTemplate("HIIT Intervals", "Cardio", "Bodyweight", "Int", "cardio,full body", "Alternate 30s work/30s rest, give max effort"),
        ExerciseTemplate("Outdoor Run", "Cardio", "Bodyweight", "Beginner", "cardio,legs", "Moderate pace, focus on breathing"),

        // ── Mobility ──
        ExerciseTemplate("Cat-Cow", "Mobility", "Bodyweight", "Beginner", "", "Alternate arching and rounding back"),
        ExerciseTemplate("Hip Circles", "Mobility", "Bodyweight", "Beginner", "hip flexors,glutes", "Large controlled circles, 10 each direction"),
        ExerciseTemplate("Band Pull-Apart", "Mobility", "Bodyweight", "Beginner", "rear delts,upper back", "Squeeze shoulder blades, arms straight"),
        ExerciseTemplate("Arm Circles", "Mobility", "Bodyweight", "Beginner", "", "Small to large circles, forward and back")
    )

    val muscleGroups: List<String> =
        exercises.map { it.muscleGroup }.distinct().sorted()
}
