package com.bendfiercely.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bendfiercely.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class HistoryState(
    val sessions: List<StretchSession> = emptyList(),
    val totalSessions: Int = 0,
    val totalStretchingMinutes: Int = 0,
    val isLoading: Boolean = true
)

data class SessionDetailState(
    val session: StretchSession? = null,
    val stretches: List<SessionStretchWithDetails> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * Date range filter options for statistics
 */
enum class DateRangeFilter(val displayName: String) {
    ALL_TIME("All Time"),
    LAST_WEEK("Last Week"),
    LAST_MONTH("Last Month"),
    LAST_YEAR("Last Year"),
    CUSTOM("Custom Range")
}

/**
 * Exercise statistics with exercise details
 */
data class ExerciseStats(
    val exercise: Exercise,
    val totalSeconds: Int,
    val occurrences: Int,
    val leftSeconds: Int,
    val rightSeconds: Int,
    val centerSeconds: Int
)

/**
 * State for the statistics screen
 */
data class StatisticsState(
    val exerciseStats: List<ExerciseStats> = emptyList(),
    val totalStretchingSeconds: Int = 0,
    val totalSessions: Int = 0,
    val selectedFilter: DateRangeFilter = DateRangeFilter.ALL_TIME,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val isLoading: Boolean = true
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionRepository: SessionRepository
    
    private val _historyState = MutableStateFlow(HistoryState())
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()
    
    private val _sessionDetailState = MutableStateFlow(SessionDetailState())
    val sessionDetailState: StateFlow<SessionDetailState> = _sessionDetailState.asStateFlow()
    
    private val _statisticsState = MutableStateFlow(StatisticsState())
    val statisticsState: StateFlow<StatisticsState> = _statisticsState.asStateFlow()
    
    init {
        val database = SessionDatabase.getDatabase(application)
        sessionRepository = SessionRepository(database.sessionDao())
        
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            sessionRepository.allSessions.collect { sessions ->
                val totalMinutes = sessionRepository.getTotalStretchingTime() / 60
                
                _historyState.value = HistoryState(
                    sessions = sessions,
                    totalSessions = sessions.size,
                    totalStretchingMinutes = totalMinutes,
                    isLoading = false
                )
            }
        }
    }
    
    fun loadSessionDetail(sessionId: Long) {
        viewModelScope.launch {
            _sessionDetailState.value = SessionDetailState(isLoading = true)
            
            val session = sessionRepository.getSession(sessionId)
            val stretches = sessionRepository.getStretchesForSession(sessionId)
            
            val stretchesWithDetails = stretches.map { stretch ->
                val exercise = ExerciseRepository.getExerciseById(stretch.exerciseId)
                SessionStretchWithDetails(
                    id = stretch.id,
                    sessionId = stretch.sessionId,
                    exerciseId = stretch.exerciseId,
                    exerciseName = exercise?.name ?: "Unknown Exercise",
                    side = stretch.side,
                    durationSeconds = stretch.durationSeconds,
                    orderIndex = stretch.orderIndex
                )
            }
            
            _sessionDetailState.value = SessionDetailState(
                session = session,
                stretches = stretchesWithDetails,
                isLoading = false
            )
        }
    }
    
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
    
    // ========== Statistics Methods ==========
    
    fun loadStatistics(filter: DateRangeFilter = DateRangeFilter.ALL_TIME) {
        viewModelScope.launch {
            _statisticsState.value = _statisticsState.value.copy(
                isLoading = true,
                selectedFilter = filter
            )
            
            val (startTime, endTime) = getDateRangeForFilter(filter)
            
            val statsRows = if (filter == DateRangeFilter.ALL_TIME) {
                sessionRepository.getExerciseStatsAllTime()
            } else {
                sessionRepository.getExerciseStatsByDateRange(startTime, endTime)
            }
            
            val totalSeconds = if (filter == DateRangeFilter.ALL_TIME) {
                sessionRepository.getTotalStretchingTime()
            } else {
                sessionRepository.getTotalStretchingTimeByDateRange(startTime, endTime)
            }
            
            val sessionCount = if (filter == DateRangeFilter.ALL_TIME) {
                sessionRepository.getSessionCount()
            } else {
                sessionRepository.getSessionCountByDateRange(startTime, endTime)
            }
            
            // Map stats rows to ExerciseStats with exercise details
            val exerciseStats = statsRows.mapNotNull { row ->
                val exercise = ExerciseRepository.getExerciseById(row.exerciseId)
                exercise?.let {
                    ExerciseStats(
                        exercise = it,
                        totalSeconds = row.totalSeconds,
                        occurrences = row.occurrences,
                        leftSeconds = row.leftSeconds,
                        rightSeconds = row.rightSeconds,
                        centerSeconds = row.centerSeconds
                    )
                }
            }
            
            _statisticsState.value = _statisticsState.value.copy(
                exerciseStats = exerciseStats,
                totalStretchingSeconds = totalSeconds,
                totalSessions = sessionCount,
                selectedFilter = filter,
                isLoading = false
            )
        }
    }
    
    fun loadStatisticsWithCustomRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _statisticsState.value = _statisticsState.value.copy(
                isLoading = true,
                selectedFilter = DateRangeFilter.CUSTOM,
                customStartDate = startDate,
                customEndDate = endDate
            )
            
            val statsRows = sessionRepository.getExerciseStatsByDateRange(startDate, endDate)
            val totalSeconds = sessionRepository.getTotalStretchingTimeByDateRange(startDate, endDate)
            val sessionCount = sessionRepository.getSessionCountByDateRange(startDate, endDate)
            
            val exerciseStats = statsRows.mapNotNull { row ->
                val exercise = ExerciseRepository.getExerciseById(row.exerciseId)
                exercise?.let {
                    ExerciseStats(
                        exercise = it,
                        totalSeconds = row.totalSeconds,
                        occurrences = row.occurrences,
                        leftSeconds = row.leftSeconds,
                        rightSeconds = row.rightSeconds,
                        centerSeconds = row.centerSeconds
                    )
                }
            }
            
            _statisticsState.value = _statisticsState.value.copy(
                exerciseStats = exerciseStats,
                totalStretchingSeconds = totalSeconds,
                totalSessions = sessionCount,
                isLoading = false
            )
        }
    }
    
    /**
     * Calculate start and end timestamps for a given filter
     */
    private fun getDateRangeForFilter(filter: DateRangeFilter): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        val startTime = when (filter) {
            DateRangeFilter.LAST_WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateRangeFilter.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateRangeFilter.LAST_YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateRangeFilter.CUSTOM -> {
                _statisticsState.value.customStartDate ?: 0L
            }
            DateRangeFilter.ALL_TIME -> 0L
        }
        
        val actualEndTime = if (filter == DateRangeFilter.CUSTOM) {
            _statisticsState.value.customEndDate ?: System.currentTimeMillis()
        } else {
            endTime
        }
        
        return Pair(startTime, actualEndTime)
    }
}
