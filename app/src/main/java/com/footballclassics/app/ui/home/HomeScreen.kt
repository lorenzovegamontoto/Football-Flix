package com.footballclassics.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.footballclassics.app.data.model.*
import com.footballclassics.app.ui.components.*
import com.footballclassics.app.ui.theme.FootballColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMatchClick: (Match) -> Unit,
    onPlayMatch: (Match) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val featuredMatch by viewModel.featuredMatch.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is HomeUiState.Loading) {
            isRefreshing = false
        }
    }

    when (uiState) {
        is HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Error -> ErrorScreen(
            message = (uiState as HomeUiState.Error).message,
            onRetry = { viewModel.refresh() }
        )
        is HomeUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true; viewModel.refresh() },
                modifier = Modifier.fillMaxSize().background(FootballColors.background)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    featuredMatch?.let { match ->
                        item(key = "featured_hero") {
                            FeaturedMatchCard(
                                match = match,
                                onClick = { onMatchClick(match) },
                                onPlayClick = { onPlayMatch(match) },
                                onInfoClick = { onMatchClick(match) }
                            )
                        }
                    }

                    if (continueWatching.isNotEmpty()) {
                        item(key = "continue_watching_row") {
                            Spacer(modifier = Modifier.height(24.dp))
                            ContinueWatchingSection(items = continueWatching, onMatchClick = onPlayMatch)
                        }
                    }

                    items(items = categories.filter { it.id != "continue_watching" }, key = { it.id }) { category ->
                        Spacer(modifier = Modifier.height(24.dp))
                        MatchCategoryRow(category = category, onMatchClick = onMatchClick, onSeeAllClick = null)
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(items: List<ContinueWatchingItem>, onMatchClick: (Match) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.titleLarge,
            color = FootballColors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = items, key = { it.match.id }) { item ->
                MatchCard(match = item.match, onClick = { onMatchClick(item.match) }, showProgress = true, progressPercent = item.progressPercent)
            }
        }
    }
}
