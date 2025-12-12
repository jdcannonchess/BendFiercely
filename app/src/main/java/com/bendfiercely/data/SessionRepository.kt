package com.bendfiercely.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    
    val allSessions: Flow<List<StretchSession>> = sessionDao.getAllSessions()
    
    suspend fun createSession(sessionType: SessionType): Long {
        val session = StretchSession(
            sessionType = sessionType.name.lowercase(),
            totalDurationSeconds = 0,
            stretchCount = 0
        )
        return sessionDao.insertSession(session)
    }
    
    suspend fun updateSession(session: StretchSession) {
        sessionDao.updateSession(session)
    }
    
    suspend fun getSession(sessionId: Long): StretchSession? {
        return sessionDao.getSessionById(sessionId)
    }
    
    suspend fun addStretchToSession(
        sessionId: Long,
        exerciseId: String,
        side: StretchSide,
        durationSeconds: Int,
        orderIndex: Int
    ): Long {
        val stretch = SessionStretch(
            sessionId = sessionId,
            exerciseId = exerciseId,
            side = side.name.lowercase(),
            durationSeconds = durationSeconds,
            orderIndex = orderIndex
        )
        return sessionDao.insertStretch(stretch)
    }
    
    suspend fun getStretchesForSession(sessionId: Long): List<SessionStretch> {
        return sessionDao.getStretchesForSession(sessionId)
    }
    
    fun getStretchesForSessionFlow(sessionId: Long): Flow<List<SessionStretch>> {
        return sessionDao.getStretchesForSessionFlow(sessionId)
    }
    
    suspend fun finalizeSession(sessionId: Long, totalDurationSeconds: Int, stretchCount: Int) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            sessionDao.updateSession(
                it.copy(
                    totalDurationSeconds = totalDurationSeconds,
                    stretchCount = stretchCount
                )
            )
        }
    }
    
    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSession(sessionId)
    }
    
    suspend fun getSessionCount(): Int {
        return sessionDao.getSessionCount()
    }
    
    suspend fun getTotalStretchingTime(): Int {
        return sessionDao.getTotalStretchingTime() ?: 0
    }
    
    // ========== Statistics Methods ==========
    
    suspend fun getExerciseStatsAllTime(): List<ExerciseStatsRow> {
        return sessionDao.getExerciseStatsAllTime()
    }
    
    suspend fun getExerciseStatsByDateRange(startTime: Long, endTime: Long): List<ExerciseStatsRow> {
        return sessionDao.getExerciseStatsByDateRange(startTime, endTime)
    }
    
    suspend fun getTotalStretchingTimeByDateRange(startTime: Long, endTime: Long): Int {
        return sessionDao.getTotalStretchingTimeByDateRange(startTime, endTime)
    }
    
    suspend fun getSessionCountByDateRange(startTime: Long, endTime: Long): Int {
        return sessionDao.getSessionCountByDateRange(startTime, endTime)
    }
}

