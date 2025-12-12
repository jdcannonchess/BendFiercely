package com.bendfiercely.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bendfiercely.data.SessionType
import com.bendfiercely.ui.screens.*
import com.bendfiercely.viewmodel.DateRangeFilter
import com.bendfiercely.viewmodel.HistoryViewModel
import com.bendfiercely.viewmodel.StretchViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SelectSessionType : Screen("session_type")
    object ActiveStretch : Screen("active_stretch/{sessionType}") {
        fun createRoute(sessionType: SessionType) = "active_stretch/${sessionType.name}"
    }
    object Summary : Screen("summary")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }
}

@Composable
fun BendFiercelyNavigation(
    stretchViewModel: StretchViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    val navController = rememberNavController()
    val sessionState by stretchViewModel.sessionState.collectAsState()
    val historyState by historyViewModel.historyState.collectAsState()
    val sessionDetailState by historyViewModel.sessionDetailState.collectAsState()
    val statisticsState by historyViewModel.statisticsState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartSession = {
                    navController.navigate(Screen.SelectSessionType.route)
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        
        composable(Screen.SelectSessionType.route) {
            SessionTypeScreen(
                onSelectSessionType = { sessionType ->
                    stretchViewModel.startSession(sessionType)
                    navController.navigate(Screen.ActiveStretch.createRoute(sessionType)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.ActiveStretch.route,
            arguments = listOf(
                navArgument("sessionType") { type = NavType.StringType }
            )
        ) {
            ActiveStretchScreen(
                sessionState = sessionState,
                onTogglePause = { stretchViewModel.togglePause() },
                onSkip = { stretchViewModel.skipStretch() },
                onAddTime = { stretchViewModel.addTime() },
                onRemoveTime = { stretchViewModel.removeTime() },
                onEndSession = {
                    val sessionId = stretchViewModel.endSession()
                    historyViewModel.loadSessionDetail(sessionId)
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                historyState = historyState,
                onBack = {
                    navController.popBackStack()
                },
                onSessionClick = { sessionId ->
                    historyViewModel.loadSessionDetail(sessionId)
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                },
                onViewStatistics = {
                    historyViewModel.loadStatistics()
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                statisticsState = statisticsState,
                onFilterSelected = { filter ->
                    historyViewModel.loadStatistics(filter)
                },
                onCustomRangeSelected = { start, end ->
                    historyViewModel.loadStatisticsWithCustomRange(start, end)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            SessionDetailScreen(
                sessionDetailState = sessionDetailState,
                onBack = {
                    navController.popBackStack()
                },
                onDelete = {
                    historyViewModel.deleteSession(sessionId)
                    navController.popBackStack()
                }
            )
        }
    }
}
