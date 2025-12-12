package com.bendfiercely.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.bendfiercely.data.Exercise
import com.bendfiercely.data.SessionType
import com.bendfiercely.data.StretchSide
import com.bendfiercely.ui.components.formatDuration
import com.bendfiercely.ui.theme.*
import com.bendfiercely.viewmodel.CompletedStretch

/**
 * Aggregated exercise data for session summary
 */
data class AggregatedExercise(
    val exercise: Exercise,
    val totalDurationSeconds: Int,
    val occurrences: Int,
    val leftDurationSeconds: Int = 0,
    val rightDurationSeconds: Int = 0,
    val centerDurationSeconds: Int = 0
)

/**
 * Aggregate completed stretches by exercise, combining durations
 */
fun aggregateStretches(stretches: List<CompletedStretch>): List<AggregatedExercise> {
    return stretches
        .groupBy { it.exercise.id }
        .map { (_, exerciseStretches) ->
            val exercise = exerciseStretches.first().exercise
            val leftTime = exerciseStretches
                .filter { it.side == StretchSide.LEFT }
                .sumOf { it.durationSeconds }
            val rightTime = exerciseStretches
                .filter { it.side == StretchSide.RIGHT }
                .sumOf { it.durationSeconds }
            val centerTime = exerciseStretches
                .filter { it.side == StretchSide.CENTER }
                .sumOf { it.durationSeconds }
            
            AggregatedExercise(
                exercise = exercise,
                totalDurationSeconds = leftTime + rightTime + centerTime,
                occurrences = exerciseStretches.size,
                leftDurationSeconds = leftTime,
                rightDurationSeconds = rightTime,
                centerDurationSeconds = centerTime
            )
        }
        .sortedByDescending { it.totalDurationSeconds }
}

@Composable
fun SummaryScreen(
    completedStretches: List<CompletedStretch>,
    totalDurationSeconds: Int,
    sessionType: SessionType,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aggregatedExercises = remember(completedStretches) {
        aggregateStretches(completedStretches)
    }
    val uniqueExercises = aggregatedExercises.size
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with celebration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Success, SuccessLight)
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Great Session!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                
                Text(
                    text = sessionType.displayName + " stretch complete",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                value = completedStretches.size.toString(),
                label = "Stretches",
                icon = Icons.Default.FitnessCenter
            )
            
            StatCard(
                value = formatDuration(totalDurationSeconds),
                label = "Total Time",
                icon = Icons.Default.Timer
            )
            
            StatCard(
                value = uniqueExercises.toString(),
                label = "Exercises",
                icon = Icons.Default.Category
            )
        }
        
        // Stretch list header
        Text(
            text = "Exercise Breakdown",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Aggregated exercise list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(aggregatedExercises) { aggregated ->
                AggregatedExerciseCard(aggregated = aggregated)
            }
        }
        
        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberPrimary
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AmberPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AggregatedExerciseCard(
    aggregated: AggregatedExercise,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with name, count, and total time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = aggregated.exercise.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AmberPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${aggregated.occurrences}x",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AmberPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Score: ${aggregated.exercise.pancakeScore}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = formatDuration(aggregated.totalDurationSeconds),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AmberPrimary
                )
            }
            
            // Side breakdown (if applicable)
            if (aggregated.leftDurationSeconds > 0 || aggregated.rightDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (aggregated.leftDurationSeconds > 0) {
                        SideTimeChip(
                            label = "Left",
                            duration = aggregated.leftDurationSeconds,
                            color = TimerRest
                        )
                    }
                    
                    if (aggregated.rightDurationSeconds > 0) {
                        SideTimeChip(
                            label = "Right",
                            duration = aggregated.rightDurationSeconds,
                            color = Coral
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SideTimeChip(
    label: String,
    duration: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "$label: ${formatDuration(duration)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = color
            )
        }
    }
}
