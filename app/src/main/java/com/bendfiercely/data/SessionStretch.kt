package com.bendfiercely.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_stretches",
    foreignKeys = [
        ForeignKey(
            entity = StretchSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SessionStretch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val side: String, // "center", "left", or "right"
    val durationSeconds: Int,
    val orderIndex: Int
)

data class SessionStretchWithDetails(
    val id: Long,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val side: String,
    val durationSeconds: Int,
    val orderIndex: Int
)

