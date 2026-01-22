package com.footballclassics.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footballclassics.app.data.model.*
import com.footballclassics.app.data.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _featuredMatch = MutableStateFlow<Match?>(null)
    val featuredMatch: StateFlow<Match?> = _featuredMatch.asStateFlow()

    private val _categories = MutableStateFlow<List<MatchCategory>>(emptyList())
    val categories: StateFlow<List<MatchCategory>> = _categories.asStateFlow()

    private val _continueWatching = MutableStateFlow<List<ContinueWatchingItem>>(emptyList())
    val continueWatching: StateFlow<List<ContinueWatchingItem>> = _continueWatching.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                // Load continue watching
                val continueWatchingItems = repository.getContinueWatchingMatches()
                _continueWatching.value = continueWatchingItems

                // Load home page content
                repository.getHomeContent()
                    .onSuccess { content ->
                        // Set featured match
                        _featuredMatch.value = content.featuredMatches.firstOrNull()
                            ?: content.recentMatches.firstOrNull()

                        // Build categories
                        val categoryList = mutableListOf<MatchCategory>()

                        // Continue watching
                        if (continueWatchingItems.isNotEmpty()) {
                            categoryList.add(
                                MatchCategory(
                                    id = "continue_watching",
                                    title = "Continue Watching",
                                    matches = continueWatchingItems.map { it.match },
                                    type = CategoryType.HORIZONTAL
                                )
                            )
                        }

                        // Featured matches
                        if (content.featuredMatches.size > 1) {
                            categoryList.add(
                                MatchCategory(
                                    id = "featured",
                                    title = "Featured Classics",
                                    matches = content.featuredMatches.drop(1),
                                    type = CategoryType.HORIZONTAL
                                )
                            )
                        }

                        // Recent matches
                        if (content.recentMatches.isNotEmpty()) {
                            categoryList.add(
                                MatchCategory(
                                    id = "recent",
                                    title = "Recently Added",
                                    matches = content.recentMatches,
                                    type = CategoryType.HORIZONTAL
                                )
                            )
                        }

                        // Group by competition for additional rows
                        val byCompetition = content.recentMatches.groupBy { it.competition }
                        byCompetition.forEach { (competition, matches) ->
                            if (competition.isNotBlank() && matches.size >= 3) {
                                categoryList.add(
                                    MatchCategory(
                                        id = "competition_${competition.hashCode()}",
                                        title = competition,
                                        matches = matches,
                                        type = CategoryType.HORIZONTAL
                                    )
                                )
                            }
                        }

                        _categories.value = categoryList
                        _uiState.value = HomeUiState.Success
                    }
                    .onFailure { error ->
                        _uiState.value = HomeUiState.Error(error.message ?: "Failed to load content")
                    }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun refresh() {
        loadHomeContent()
    }

    fun toggleFavorite(matchId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(matchId)
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
