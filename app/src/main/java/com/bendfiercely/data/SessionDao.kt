package com.bendfiercely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    
    @Insert
    suspend fun insertSession(session: StretchSession): Long
    
    @Update
    suspend fun updateSession(session: StretchSession)
    
    @Insert
    suspend fun insertStretch(stretch: SessionStretch): Long
    
    @Insert
    suspend fun insertStretches(stretches: List<SessionStretch>)
    
    @Query("SELECT * FROM stretch_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StretchSession>>
    
    @Query("SELECT * FROM stretch_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): StretchSession?
    
    @Query("SELECT * FROM session_stretches WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getStretchesForSession(sessionId: Long): List<SessionStretch>
    
    @Query("SELECT * FROM session_stretches WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getStretchesForSessionFlow(sessionId: Long): Flow<List<SessionStretch>>
    
    @Query("DELETE FROM stretch_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
    
    @Query("SELECT COUNT(*) FROM stretch_sessions")
    suspend fun getSessionCount(): Int
    
    @Query("SELECT SUM(totalDurationSeconds) FROM stretch_sessions")
    suspend fun getTotalStretchingTime(): Int?
    
    // ========== Statistics Queries ==========
    
    /**
     * Get aggregated stretch time per exercise across all sessions
     */
    @Query("""
        SELECT 
            ss.exerciseId,
            SUM(ss.durationSeconds) as totalSeconds,
            COUNT(*) as occurrences,
            SUM(CASE WHEN ss.side = 'left' THEN ss.durationSeconds ELSE 0 END) as leftSeconds,
            SUM(CASE WHEN ss.side = 'right' THEN ss.durationSeconds ELSE 0 END) as rightSeconds,
            SUM(CASE WHEN ss.side = 'center' THEN ss.durationSeconds ELSE 0 END) as centerSeconds
        FROM session_stretches ss
        INNER JOIN stretch_sessions s ON ss.sessionId = s.id
        GROUP BY ss.exerciseId
        ORDER BY totalSeconds DESC
    """)
    suspend fun getExerciseStatsAllTime(): List<ExerciseStatsRow>
    
    /**
     * Get aggregated stretch time per exercise within a date range
     */
    @Query("""
        SELECT 
            ss.exerciseId,
            SUM(ss.durationSeconds) as totalSeconds,
            COUNT(*) as occurrences,
            SUM(CASE WHEN ss.side = 'left' THEN ss.durationSeconds ELSE 0 END) as leftSeconds,
            SUM(CASE WHEN ss.side = 'right' THEN ss.durationSeconds ELSE 0 END) as rightSeconds,
            SUM(CASE WHEN ss.side = 'center' THEN ss.durationSeconds ELSE 0 END) as centerSeconds
        FROM session_stretches ss
        INNER JOIN stretch_sessions s ON ss.sessionId = s.id
        WHERE s.timestamp >= :startTime AND s.timestamp <= :endTime
        GROUP BY ss.exerciseId
        ORDER BY totalSeconds DESC
    """)
    suspend fun getExerciseStatsByDateRange(startTime: Long, endTime: Long): List<ExerciseStatsRow>
    
    /**
     * Get total stretching time within a date range
     */
    @Query("""
        SELECT COALESCE(SUM(totalDurationSeconds), 0)
        FROM stretch_sessions
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getTotalStretchingTimeByDateRange(startTime: Long, endTime: Long): Int
    
    /**
     * Get session count within a date range
     */
    @Query("""
        SELECT COUNT(*)
        FROM stretch_sessions
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getSessionCountByDateRange(startTime: Long, endTime: Long): Int
}

/**
 * Data class for exercise statistics query result
 */
data class ExerciseStatsRow(
    val exerciseId: String,
    val totalSeconds: Int,
    val occurrences: Int,
    val leftSeconds: Int,
    val rightSeconds: Int,
    val centerSeconds: Int
)

