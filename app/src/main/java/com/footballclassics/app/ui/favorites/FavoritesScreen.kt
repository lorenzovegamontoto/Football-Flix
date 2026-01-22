package com.footballclassics.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footballclassics.app.data.model.Match
import com.footballclassics.app.data.repository.FootballRepository
import com.footballclassics.app.ui.components.EmptyState
import com.footballclassics.app.ui.components.MatchListItem
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: FootballRepository) : ViewModel() {
    val favorites: StateFlow<List<Match>> = repository.getFavoriteMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFromFavorites(matchId: String) {
        viewModelScope.launch { repository.removeFromFavorites(matchId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onMatchClick: (Match) -> Unit, viewModel: FavoritesViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(FootballColors.background)) {
        TopAppBar(
            title = { Text("My List", style = MaterialTheme.typography.headlineSmall, color = FootballColors.textPrimary, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FootballColors.background)
        )

        if (favorites.isEmpty()) {
            EmptyState(
                title = "Your list is empty",
                message = "Add matches to your list to watch them later"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites, key = { it.id }) { match ->
                    MatchListItem(
                        match = match,
                        onClick = { onMatchClick(match) },
                        onFavoriteClick = { viewModel.removeFromFavorites(match.id) },
                        isFavorite = true
                    )
                }
            }
        }
    }
}
