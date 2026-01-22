package com.footballclassics.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.footballclassics.app.navigation.AppNavGraph
import com.footballclassics.app.navigation.Screen
import com.footballclassics.app.ui.components.BottomNavBar
import com.footballclassics.app.ui.theme.FootballClassicsTheme
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FootballClassicsTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom nav should be hidden
    val hideBottomNavRoutes = listOf(
        Screen.MatchDetails.route,
        Screen.Player.route
    )
    val showBottomNav = currentRoute !in hideBottomNavRoutes && 
        !currentRoute.orEmpty().startsWith("match/") && 
        !currentRoute.orEmpty().startsWith("player/")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballColors.background),
        containerColor = FootballColors.background,
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = when {
                        currentRoute == Screen.Home.route -> "home"
                        currentRoute == Screen.Search.route -> "search"
                        currentRoute == Screen.Competitions.route -> "competitions"
                        currentRoute == Screen.Favorites.route -> "favorites"
                        else -> "home"
                    },
                    onNavigate = { route ->
                        val screen = when (route) {
                            "home" -> Screen.Home.route
                            "search" -> Screen.Search.route
                            "competitions" -> Screen.Competitions.route
                            "favorites" -> Screen.Favorites.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(screen) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (showBottomNav) paddingValues.calculateBottomPadding() else 0.dp
                )
        ) {
            AppNavGraph(navController = navController)
        }
    }
}
