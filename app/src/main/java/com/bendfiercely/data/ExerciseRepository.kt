package com.bendfiercely.data

import kotlin.math.pow
import kotlin.random.Random

object ExerciseRepository {
    
    // Track recently used exercises to prevent repeats
    private val recentExerciseIds = mutableListOf<String>()
    private const val RECENCY_LIMIT = 5
    
    // Track recently used durations to ensure variety
    private val recentDurations = mutableListOf<Int>()
    private const val DURATION_RECENCY_LIMIT = 3
    
    private val exercises = listOf(
        Exercise(
            id = "seated_straddle_forward_fold",
            name = "Seated Straddle Forward Fold",
            side = ExerciseSide.CENTER,
            pancakeScore = 100,
            description = "Sit with your legs spread comfortably wide. Hinge forward from your hips as you walk your hands out in front of you. Keep your spine long and let your pelvis tip forward first. Only go as far as your torso can stay relaxed. Breathe and allow your inner thighs to soften."
        ),
        Exercise(
            id = "straddle_good_mornings",
            name = "Straddle Good Mornings",
            side = ExerciseSide.CENTER,
            pancakeScore = 92,
            description = "Sit in a straddle with your hands behind your head or across your chest. Hinge at the hips while keeping your spine straight and long. Lean forward with control, then return upright smoothly. This builds active strength for a deeper pancake."
        ),
        Exercise(
            id = "butterfly_pose",
            name = "Butterfly Pose",
            side = ExerciseSide.CENTER,
            pancakeScore = 70,
            description = "Sit with the soles of your feet together and your knees open. Sit tall and bring your heels in to feel a stretch. Hinge from your hips to fold forward while keeping your spine long. Let gravity work without forcing your knees downward."
        ),
        Exercise(
            id = "frog_pose",
            name = "Frog Pose",
            side = ExerciseSide.CENTER,
            pancakeScore = 78,
            description = "Start on hands and knees, then slide your knees outward until you feel an inner thigh stretch. Keep your hips in line with your knees and your spine neutral. Lower your torso gently and breathe deeply. Allow your adductors to release gradually."
        ),
        Exercise(
            id = "standing_wide_leg_forward_fold",
            name = "Standing Wide-Leg Forward Fold",
            side = ExerciseSide.CENTER,
            pancakeScore = 75,
            description = "Stand with your feet wide and hinge forward at the hips. Fold your torso toward the floor with relaxed shoulders and neck. Keep your legs straight but not locked. Breathe into your hamstrings and inner thighs."
        ),
        Exercise(
            id = "jefferson_curl",
            name = "Jefferson Curl",
            side = ExerciseSide.CENTER,
            pancakeScore = 85,
            description = "Stand tall with light weight or none. Slowly roll down through your spine one vertebra at a time while keeping your legs mostly straight. Let your head and arms hang. Roll back up slowly, stacking your spine."
        ),
        Exercise(
            id = "cossack_squat",
            name = "Cossack Squat",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 82,
            description = "Stand in a wide stance and shift your weight to one side, sinking into a deep squat while the other leg stays straight. Keep your chest lifted and your heel grounded. Move slowly side to side or hold each side to deepen the stretch."
        ),
        Exercise(
            id = "side_split_hold",
            name = "Side Split Hold",
            side = ExerciseSide.CENTER,
            pancakeScore = 95,
            description = "Start in a wide stance and gently let your legs slide outward. Keep your toes pointing up and your spine neutral. Go only as far as you can without pain. Stay still and breathe to allow your adductors to lengthen."
        ),
        Exercise(
            id = "90_90_hip_switches",
            name = "90/90 Hip Switches",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 60,
            description = "Sit with both legs bent so the front and back shins form 90-degree angles. Rotate your knees toward the other side without lifting your feet much. Stay tall as you switch. This improves rotational mobility in both hips."
        ),
        Exercise(
            id = "90_90_forward_fold",
            name = "90/90 Forward Fold",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 72,
            description = "From the 90/90 position, hinge forward over your front shin. Keep your spine long and aim your chest at your ankle. Feel the stretch in the outer hip. Breathe steadily and avoid rounding your back."
        ),
        Exercise(
            id = "pigeon_pose",
            name = "Pigeon Pose",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 62,
            description = "Bring one shin forward and extend the opposite leg back. Square your hips as much as possible. Fold forward for a deeper stretch or stay upright for a milder version. Relax into your glutes and hips."
        ),
        Exercise(
            id = "fire_log_pose",
            name = "Fire Log Pose",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 68,
            description = "Sit with one shin stacked on top of the other. Keep your feet flexed and your spine long. If your top knee is elevated, use a cushion for support. Lean forward gently for extra depth."
        ),
        Exercise(
            id = "seated_figure_4",
            name = "Seated Figure-4 Stretch",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 55,
            description = "Sit with legs extended and cross one ankle over the opposite knee. Pull the bottom knee slightly toward your chest. Keep your spine tall as you lean in. Feel the stretch in your outer hip and glutes."
        ),
        Exercise(
            id = "standing_hamstring_fold",
            name = "Standing Hamstring Fold",
            side = ExerciseSide.CENTER,
            pancakeScore = 74,
            description = "Stand tall and hinge forward at your hips with straight legs. Relax your torso and neck as you fold. Only go as far as your hamstrings allow without strain. Breathe into the stretch."
        ),
        Exercise(
            id = "half_split",
            name = "Half Split",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 88,
            description = "From a kneeling lunge, shift your hips back as your front leg straightens. Keep your toes lifted and your spine long. Hinge forward over the front leg to deepen the stretch. Strongly lengthens the hamstrings."
        ),
        Exercise(
            id = "low_lunge",
            name = "Low Lunge",
            side = ExerciseSide.LEFT_RIGHT,
            pancakeScore = 40,
            description = "Step one foot forward and drop your back knee. Sink your hips forward while keeping your chest tall. Feel the stretch in the back-leg hip flexor. Avoid overarching your lower back."
        ),
        Exercise(
            id = "seated_leg_lift_offs",
            name = "Seated Leg Lift-Offs",
            side = ExerciseSide.CENTER,
            pancakeScore = 90,
            description = "Sit with your legs straight in front of you. Place your hands beside your thighs and attempt to lift your legs off the ground. Even tiny lifts count. This builds the compression strength needed for pancake."
        ),
        Exercise(
            id = "puppy_pose",
            name = "Puppy Pose",
            side = ExerciseSide.CENTER,
            pancakeScore = 30,
            description = "Start on hands and knees and walk your hands forward while keeping your hips over your knees. Let your chest sink toward the floor. Relax your shoulders and breathe into your ribcage. Helps your upper spine open for forward folds."
        ),
        Exercise(
            id = "happy_baby_pose",
            name = "Happy Baby Pose",
            side = ExerciseSide.CENTER,
            pancakeScore = 65,
            description = "Lie on your back and draw your knees toward your armpits. Hold the outsides of your feet and gently pull them downward. Keep your tailbone relaxed on the floor. Let your hips open with each exhale."
        )
    )
    
    fun getAllExercises(): List<Exercise> = exercises
    
    /**
     * Select a random exercise using weighted probability based on pancakeScore.
     * 
     * Weighting: weight = pancakeScore^0.7 gives ~2.5x preference for score 100 vs score 30
     * (100^0.7 / 30^0.7 = 25.1 / 10.5 = 2.4x)
     * 
     * Recency filter: Excludes exercises used in the last 5 stretches to ensure variety.
     */
    fun selectWeightedRandomExercise(): Exercise {
        // Filter out recently used exercises (unless we've used almost all of them)
        val availableExercises = exercises.filter { it.id !in recentExerciseIds }
            .ifEmpty { exercises } // Fallback if all have been recently used
        
        // Calculate weights using pancakeScore^0.7 for ~2.5x weighting
        val weights = availableExercises.map { it.pancakeScore.toDouble().pow(0.7) }
        val totalWeight = weights.sum()
        
        var randomValue = Random.nextDouble() * totalWeight
        
        var selectedExercise = availableExercises.last()
        for (i in availableExercises.indices) {
            randomValue -= weights[i]
            if (randomValue <= 0) {
                selectedExercise = availableExercises[i]
                break
            }
        }
        
        // Track this exercise in recency list
        trackRecentExercise(selectedExercise.id)
        
        return selectedExercise
    }
    
    /**
     * Track an exercise as recently used.
     * Keeps only the last RECENCY_LIMIT exercises.
     */
    private fun trackRecentExercise(exerciseId: String) {
        recentExerciseIds.add(exerciseId)
        while (recentExerciseIds.size > RECENCY_LIMIT) {
            recentExerciseIds.removeAt(0)
        }
    }
    
    /**
     * Clear the recency tracking (call when starting a new session).
     */
    fun clearRecentHistory() {
        recentExerciseIds.clear()
        recentDurations.clear()
    }
    
    /**
     * Generate a random duration for a stretch based on session type.
     * Shallow: 30, 45, 60, 75, or 90 seconds
     * Deep: 120, 135, 150, ... 285, 300 seconds (15-second increments)
     */
    fun generateDuration(isDeepSession: Boolean): Int {
        val allOptions = if (isDeepSession) {
            (120..300 step 15).toList()
        } else {
            listOf(30, 45, 60, 75, 90)
        }
        
        // Filter out recently used durations for variety
        val availableOptions = allOptions.filter { it !in recentDurations }
            .ifEmpty { allOptions } // Fallback if all have been recently used
        
        val selectedDuration = availableOptions.random()
        
        // Track this duration
        recentDurations.add(selectedDuration)
        while (recentDurations.size > DURATION_RECENCY_LIMIT) {
            recentDurations.removeAt(0)
        }
        
        return selectedDuration
    }
    
    /**
     * Create queued stretches for the selected exercise.
     * For LEFT_RIGHT exercises, creates two stretches (left then right) with the same duration.
     * For CENTER exercises, creates a single stretch.
     */
    fun createQueuedStretches(exercise: Exercise, isDeepSession: Boolean): List<QueuedStretch> {
        val duration = generateDuration(isDeepSession)
        
        return when (exercise.side) {
            ExerciseSide.CENTER -> listOf(
                QueuedStretch(
                    exercise = exercise,
                    side = StretchSide.CENTER,
                    durationSeconds = duration
                )
            )
            ExerciseSide.LEFT_RIGHT -> listOf(
                QueuedStretch(
                    exercise = exercise,
                    side = StretchSide.LEFT,
                    durationSeconds = duration
                ),
                QueuedStretch(
                    exercise = exercise,
                    side = StretchSide.RIGHT,
                    durationSeconds = duration
                )
            )
        }
    }
    
    fun getExerciseById(id: String): Exercise? {
        return exercises.find { it.id == id }
    }
}

