package com.footballclassics.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.footballclassics.app.data.model.Match
import com.footballclassics.app.ui.competitions.CompetitionsScreen
import com.footballclassics.app.ui.details.MatchDetailsScreen
import com.footballclassics.app.ui.favorites.FavoritesScreen
import com.footballclassics.app.ui.home.HomeScreen
import com.footballclassics.app.ui.player.PlayerScreen
import com.footballclassics.app.ui.search.SearchScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Competitions : Screen("competitions")
    object Favorites : Screen("favorites")
    object MatchDetails : Screen("match/{matchId}") {
        fun createRoute(matchId: String) = "match/$matchId"
    }
    object Player : Screen("player/{matchId}") {
        fun createRoute(matchId: String) = "player/$matchId"
    }
    object CompetitionMatches : Screen("competition/{competitionId}") {
        fun createRoute(competitionId: String) = "competition/$competitionId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onMatchClick = { match ->
                    navController.navigate(Screen.MatchDetails.createRoute(match.id))
                },
                onPlayMatch = { match ->
                    navController.navigate(Screen.Player.createRoute(match.id))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onMatchClick = { match ->
                    navController.navigate(Screen.MatchDetails.createRoute(match.id))
                }
            )
        }

        composable(Screen.Competitions.route) {
            CompetitionsScreen(
                onCompetitionClick = { competition ->
                    navController.navigate(Screen.CompetitionMatches.createRoute(competition.id))
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onMatchClick = { match ->
                    navController.navigate(Screen.MatchDetails.createRoute(match.id))
                }
            )
        }

        composable(
            route = Screen.MatchDetails.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
            MatchDetailsScreen(
                matchId = matchId,
                onBackClick = { navController.popBackStack() },
                onPlayClick = { match ->
                    navController.navigate(Screen.Player.createRoute(match.id))
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
            PlayerScreen(
                matchId = matchId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
