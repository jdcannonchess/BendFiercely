package com.bendfiercely.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bendfiercely.data.*
import com.bendfiercely.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StretchSessionState(
    val isActive: Boolean = false,
    val sessionId: Long = 0,
    val sessionType: SessionType = SessionType.SHALLOW,
    val currentStretch: QueuedStretch? = null,
    val nextStretch: QueuedStretch? = null,
    val timeRemaining: Int = 0,
    val isResting: Boolean = false,
    val isPaused: Boolean = false,
    val completedStretches: List<CompletedStretch> = emptyList(),
    val totalElapsedSeconds: Int = 0,
    val stretchQueue: List<QueuedStretch> = emptyList()
)

data class CompletedStretch(
    val exercise: Exercise,
    val side: StretchSide,
    val durationSeconds: Int
)

class StretchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionRepository: SessionRepository
    private val soundManager: SoundManager
    private var timerJob: Job? = null
    
    private val _sessionState = MutableStateFlow(StretchSessionState())
    val sessionState: StateFlow<StretchSessionState> = _sessionState.asStateFlow()
    
    init {
        val database = SessionDatabase.getDatabase(application)
        sessionRepository = SessionRepository(database.sessionDao())
        soundManager = SoundManager(application)
    }
    
    fun startSession(sessionType: SessionType) {
        viewModelScope.launch {
            // Clear exercise recency tracking for fresh session variety
            ExerciseRepository.clearRecentHistory()
            
            val sessionId = sessionRepository.createSession(sessionType)
            
            _sessionState.value = StretchSessionState(
                isActive = true,
                sessionId = sessionId,
                sessionType = sessionType
            )
            
            startNextStretch()
        }
    }
    
    private fun startNextStretch() {
        var currentQueue = _sessionState.value.stretchQueue
        val isDeep = _sessionState.value.sessionType == SessionType.DEEP
        
        // Ensure we have stretches in the queue
        if (currentQueue.isEmpty()) {
            val exercise = ExerciseRepository.selectWeightedRandomExercise()
            val newStretches = ExerciseRepository.createQueuedStretches(exercise, isDeep)
            currentQueue = currentQueue + newStretches
        }
        
        if (currentQueue.isEmpty()) return
        
        // Take the first stretch as current
        val currentStretch = currentQueue.first()
        var remainingQueue = currentQueue.drop(1)
        
        // Pre-fill queue if running low (need at least 2 for "up next" preview)
        while (remainingQueue.size < 2) {
            val exercise = ExerciseRepository.selectWeightedRandomExercise()
            val newStretches = ExerciseRepository.createQueuedStretches(exercise, isDeep)
            remainingQueue = remainingQueue + newStretches
        }
        
        // Now peek is accurate since queue is fully built
        val peekNext = remainingQueue.firstOrNull()
        
        _sessionState.value = _sessionState.value.copy(
            currentStretch = currentStretch,
            nextStretch = peekNext,
            timeRemaining = currentStretch.durationSeconds,
            isResting = false,
            stretchQueue = remainingQueue
        )
        
        startTimer()
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_sessionState.value.timeRemaining > 0) {
                if (!_sessionState.value.isPaused) {
                    delay(1000)
                    if (!_sessionState.value.isPaused) {
                        _sessionState.value = _sessionState.value.copy(
                            timeRemaining = _sessionState.value.timeRemaining - 1,
                            totalElapsedSeconds = _sessionState.value.totalElapsedSeconds + 1
                        )
                    }
                } else {
                    delay(100)
                }
            }
            
            onTimerComplete()
        }
    }
    
    private fun onTimerComplete() {
        val state = _sessionState.value
        
        if (state.isResting) {
            // Rest period ended, start next stretch
            startNextStretch()
        } else {
            // Stretch ended
            playChime()
            
            // Save completed stretch
            state.currentStretch?.let { stretch ->
                viewModelScope.launch {
                    sessionRepository.addStretchToSession(
                        sessionId = state.sessionId,
                        exerciseId = stretch.exercise.id,
                        side = stretch.side,
                        durationSeconds = stretch.durationSeconds,
                        orderIndex = state.completedStretches.size
                    )
                }
                
                _sessionState.value = state.copy(
                    completedStretches = state.completedStretches + CompletedStretch(
                        exercise = stretch.exercise,
                        side = stretch.side,
                        durationSeconds = stretch.durationSeconds
                    )
                )
            }
            
            // Start rest period
            _sessionState.value = _sessionState.value.copy(
                isResting = true,
                timeRemaining = 15
            )
            startTimer()
        }
    }
    
    fun togglePause() {
        _sessionState.value = _sessionState.value.copy(
            isPaused = !_sessionState.value.isPaused
        )
    }
    
    fun addTime(seconds: Int = 15) {
        val state = _sessionState.value
        if (!state.isResting) {
            _sessionState.value = state.copy(
                timeRemaining = state.timeRemaining + seconds
            )
        }
    }
    
    fun removeTime(seconds: Int = 15) {
        val state = _sessionState.value
        if (!state.isResting && state.timeRemaining > seconds) {
            _sessionState.value = state.copy(
                timeRemaining = state.timeRemaining - seconds
            )
        } else if (!state.isResting && state.timeRemaining > 0) {
            // If less than 15 seconds remaining, just set to 1 second
            _sessionState.value = state.copy(
                timeRemaining = 1
            )
        }
    }
    
    fun skipStretch() {
        timerJob?.cancel()
        val state = _sessionState.value
        
        if (state.isResting) {
            // If already resting, skip to next stretch immediately
            startNextStretch()
            return
        }
        
        // Save the stretch even if skipped
        state.currentStretch?.let { stretch ->
            val actualDuration = stretch.durationSeconds - state.timeRemaining
            if (actualDuration > 0) {
                viewModelScope.launch {
                    sessionRepository.addStretchToSession(
                        sessionId = state.sessionId,
                        exerciseId = stretch.exercise.id,
                        side = stretch.side,
                        durationSeconds = actualDuration,
                        orderIndex = state.completedStretches.size
                    )
                }
                
                _sessionState.value = state.copy(
                    completedStretches = state.completedStretches + CompletedStretch(
                        exercise = stretch.exercise,
                        side = stretch.side,
                        durationSeconds = actualDuration
                    ),
                    totalElapsedSeconds = state.totalElapsedSeconds + actualDuration
                )
            }
        }
        
        // Play chime and start rest period (same as normal completion)
        playChime()
        _sessionState.value = _sessionState.value.copy(
            isResting = true,
            timeRemaining = 15
        )
        startTimer()
    }
    
    fun endSession(): Long {
        timerJob?.cancel()
        val state = _sessionState.value
        
        viewModelScope.launch {
            sessionRepository.finalizeSession(
                sessionId = state.sessionId,
                totalDurationSeconds = state.totalElapsedSeconds,
                stretchCount = state.completedStretches.size
            )
        }
        
        val sessionId = state.sessionId
        
        _sessionState.value = StretchSessionState()
        
        return sessionId
    }
    
    private fun playChime() {
        soundManager.playChime()
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }
    
    fun getCompletedStretches(): List<CompletedStretch> {
        return _sessionState.value.completedStretches
    }
    
    fun getTotalDuration(): Int {
        return _sessionState.value.totalElapsedSeconds
    }
    
    fun getSessionType(): SessionType {
        return _sessionState.value.sessionType
    }
}

