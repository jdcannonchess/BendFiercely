package com.bendfiercely.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.bendfiercely.ui.components.formatDuration
import com.bendfiercely.ui.theme.*
import com.bendfiercely.viewmodel.DateRangeFilter
import com.bendfiercely.viewmodel.ExerciseStats
import com.bendfiercely.viewmodel.StatisticsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statisticsState: StatisticsState,
    onFilterSelected: (DateRangeFilter) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val filterDisplayText = when (statisticsState.selectedFilter) {
        DateRangeFilter.CUSTOM -> {
            if (statisticsState.customStartDate != null && statisticsState.customEndDate != null) {
                "${dateFormat.format(Date(statisticsState.customStartDate))} - ${dateFormat.format(Date(statisticsState.customEndDate))}"
            } else "Custom"
        }
        else -> statisticsState.selectedFilter.displayName
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Compact date filter in top bar
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showFilterMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AmberPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AmberPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = filterDisplayText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AmberPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AmberPrimary
                            )
                        }
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
        ) {
            if (statisticsState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmberPrimary)
                }
            } else {
                // Overall stats header
                OverallStatsCard(
                    totalSeconds = statisticsState.totalStretchingSeconds,
                    totalSessions = statisticsState.totalSessions,
                    exerciseCount = statisticsState.exerciseStats.size
                )
                
                // Exercise stats header
                Text(
                    text = "Time Per Exercise",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                if (statisticsState.exerciseStats.isEmpty()) {
                    EmptyStatsContent()
                } else {
                    // Exercise list with progress bars
                    val maxSeconds = statisticsState.exerciseStats.maxOfOrNull { it.totalSeconds } ?: 1
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(statisticsState.exerciseStats) { stats ->
                            ExerciseStatsCard(
                                stats = stats,
                                maxSeconds = maxSeconds
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Filter dropdown menu
    if (showFilterMenu) {
        FilterDropdownMenu(
            selectedFilter = statisticsState.selectedFilter,
            onFilterSelected = { filter ->
                showFilterMenu = false
                if (filter == DateRangeFilter.CUSTOM) {
                    showDatePicker = true
                } else {
                    onFilterSelected(filter)
                }
            },
            onDismiss = { showFilterMenu = false }
        )
    }
    
    // Custom date range picker dialog
    if (showDatePicker) {
        CustomDateRangeDialog(
            onDismiss = { showDatePicker = false },
            onDateRangeSelected = { start, end ->
                showDatePicker = false
                onCustomRangeSelected(start, end)
            }
        )
    }
}

@Composable
fun FilterDropdownMenu(
    selectedFilter: DateRangeFilter,
    onFilterSelected: (DateRangeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time Period") },
        text = {
            Column {
                DateRangeFilter.values().forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(filter) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filter == selectedFilter,
                            onClick = { onFilterSelected(filter) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AmberPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Convert UTC timestamp from DatePicker to local timezone timestamp
 */
private fun utcToLocal(utcMillis: Long): Long {
    val offset = TimeZone.getDefault().getOffset(utcMillis)
    return utcMillis + offset
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    // Store dates already converted to local time
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showingPicker by remember { mutableStateOf<String?>(null) } // "start" or "end" or null
    
    // Local timezone formatter
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState()
    
    // Show date picker for start or end
    if (showingPicker != null) {
        DatePickerDialog(
            onDismissRequest = { showingPicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcDate ->
                            // Convert UTC to local immediately when selecting
                            val localDate = utcToLocal(utcDate)
                            if (showingPicker == "start") {
                                startDate = localDate
                            } else {
                                endDate = localDate
                            }
                            showingPicker = null
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showingPicker = null }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Main dialog with start/end date buttons
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Date Range") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start date button
                OutlinedButton(
                    onClick = { showingPicker = "start" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (startDate != null) {
                            "From: ${dateFormat.format(Date(startDate!!))}"
                        } else {
                            "Select Start Date"
                        }
                    )
                }
                
                // End date button
                OutlinedButton(
                    onClick = { showingPicker = "end" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (endDate != null) {
                            "To: ${dateFormat.format(Date(endDate!!))}"
                        } else {
                            "Select End Date"
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startDate != null && endDate != null) {
                        val actualStart = minOf(startDate!!, endDate!!)
                        // End of day for the end date
                        val actualEnd = maxOf(startDate!!, endDate!!) + (24 * 60 * 60 * 1000 - 1)
                        onDateRangeSelected(actualStart, actualEnd)
                    }
                },
                enabled = startDate != null && endDate != null
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OverallStatsCard(
    totalSeconds: Int,
    totalSessions: Int,
    exerciseCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmberPrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            OverallStatItem(
                value = formatTotalTime(totalSeconds),
                label = "Total Time"
            )
            
            Divider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.3f)
            )
            
            OverallStatItem(
                value = totalSessions.toString(),
                label = "Sessions"
            )
            
            Divider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.3f)
            )
            
            OverallStatItem(
                value = exerciseCount.toString(),
                label = "Exercises"
            )
        }
    }
}

@Composable
fun OverallStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ExerciseStatsCard(
    stats: ExerciseStats,
    maxSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = stats.totalSeconds.toFloat() / maxSeconds.toFloat()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Exercise name (single line, truncated)
            Text(
                text = stats.exercise.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Progress bar with stats inside
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmberPrimary)
                )
                
                // Stats text overlay
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.occurrences}x",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (progress > 0.15f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatDuration(stats.totalSeconds),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (progress > 0.85f) Color.White else AmberPrimaryDark
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStatsContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No data for this period",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Complete some stretching sessions\nto see your statistics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Format total time with hours if applicable
 */
fun formatTotalTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

