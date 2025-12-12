package com.bendfiercely.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bendfiercely.data.QueuedStretch
import com.bendfiercely.data.StretchSide
import com.bendfiercely.ui.components.TimerDisplay
import com.bendfiercely.ui.components.formatDuration
import com.bendfiercely.ui.theme.*
import com.bendfiercely.viewmodel.StretchSessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveStretchScreen(
    sessionState: StretchSessionState,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onAddTime: () -> Unit,
    onRemoveTime: () -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEndConfirmation by remember { mutableStateOf(false) }
    
    val currentStretch = sessionState.currentStretch
    val totalTime = if (sessionState.isResting) 15 else currentStretch?.durationSeconds ?: 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sessionState.sessionType.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${sessionState.completedStretches.size} done",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showEndConfirmation = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Coral
                        )
                    ) {
                        Text("End Session")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current stretch info
            AnimatedContent(
                targetState = currentStretch,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "stretch_transition"
            ) { stretch ->
                if (stretch != null && !sessionState.isResting) {
                    StretchInfoCard(stretch = stretch)
                } else if (sessionState.isResting) {
                    RestingCard(nextStretch = sessionState.nextStretch)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer
            TimerDisplay(
                timeRemaining = sessionState.timeRemaining,
                totalTime = totalTime,
                isResting = sessionState.isResting,
                isPaused = sessionState.isPaused,
                size = 260.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Time adjustment buttons (only show during stretch, not rest)
            if (!sessionState.isResting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remove 15 seconds
                    OutlinedButton(
                        onClick = onRemoveTime,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        enabled = sessionState.timeRemaining > 15
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("15s")
                    }
                    
                    // Add 15 seconds
                    OutlinedButton(
                        onClick = onAddTime,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("15s")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total time indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(sessionState.totalElapsedSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Pause/Play button
                FloatingActionButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(80.dp),
                    containerColor = if (sessionState.isPaused) Success else AmberPrimary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = if (sessionState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (sessionState.isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Skip button
                FilledTonalIconButton(
                    onClick = onSkip,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Up next preview
            if (!sessionState.isResting && sessionState.nextStretch != null) {
                UpNextPreview(nextStretch = sessionState.nextStretch!!)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // End session confirmation dialog
    if (showEndConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndConfirmation = false },
            title = { Text("End Session?") },
            text = { 
                Text("You've completed ${sessionState.completedStretches.size} stretches.\nAre you ready to finish?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndConfirmation = false
                        onEndSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("End Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirmation = false }) {
                    Text("Keep Going")
                }
            }
        )
    }
}

@Composable
fun StretchInfoCard(
    stretch: QueuedStretch,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stretch.exercise.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                if (stretch.side != StretchSide.CENTER) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (stretch.side) {
                            StretchSide.LEFT -> TimerRest.copy(alpha = 0.2f)
                            StretchSide.RIGHT -> Coral.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                    ) {
                        Text(
                            text = stretch.side.name,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = when (stretch.side) {
                                StretchSide.LEFT -> TimerRest
                                StretchSide.RIGHT -> Coral
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stretch.exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
fun RestingCard(
    nextStretch: QueuedStretch?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = TimerRest.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TimerRest
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Take a breath",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TimerRest
            )
            
            if (nextStretch != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Up next: ${nextStretch.exercise.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpNextPreview(
    nextStretch: QueuedStretch,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP NEXT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = nextStretch.exercise.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (nextStretch.side != StretchSide.CENTER) {
                Text(
                    text = nextStretch.side.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

