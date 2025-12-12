package com.bendfiercely.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stretch_sessions")
data class StretchSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionType: String, // "shallow" or "deep"
    val totalDurationSeconds: Int = 0,
    val stretchCount: Int = 0
)

enum class SessionType(val displayName: String) {
    SHALLOW("Shallow"),
    DEEP("Deep");
    
    companion object {
        fun fromString(value: String): SessionType {
            return when (value.lowercase()) {
                "shallow" -> SHALLOW
                "deep" -> DEEP
                else -> SHALLOW
            }
        }
    }
}

