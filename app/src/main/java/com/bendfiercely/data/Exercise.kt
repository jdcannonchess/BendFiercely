package com.bendfiercely.data

data class Exercise(
    val id: String,
    val name: String,
    val side: ExerciseSide,
    val pancakeScore: Int,
    val description: String
)

enum class ExerciseSide {
    CENTER,
    LEFT_RIGHT
}

enum class StretchSide {
    CENTER,
    LEFT,
    RIGHT
}

data class QueuedStretch(
    val exercise: Exercise,
    val side: StretchSide,
    val durationSeconds: Int
)

