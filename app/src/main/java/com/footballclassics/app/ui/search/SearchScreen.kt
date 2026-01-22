package com.footballclassics.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footballclassics.app.data.model.*
import com.footballclassics.app.data.repository.FootballRepository
import com.footballclassics.app.ui.components.*
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: FootballRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Match>>(emptyList())
    val searchResults: StateFlow<List<Match>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) { _uiState.value = SearchUiState.Initial; _searchResults.value = emptyList(); return }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { delay(300); performSearch(newQuery) }
    }

    fun search() {
        val currentQuery = _query.value
        if (currentQuery.isNotBlank()) { searchJob?.cancel(); viewModelScope.launch { performSearch(currentQuery) } }
    }

    private suspend fun performSearch(searchQuery: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val result = repository.search(searchQuery)
            if (result.matches.isEmpty()) _uiState.value = SearchUiState.Empty
            else { _searchResults.value = result.matches; _uiState.value = SearchUiState.Success }
        } catch (e: Exception) { _uiState.value = SearchUiState.Error(e.message ?: "Search failed") }
    }

    fun clearQuery() { _query.value = ""; _searchResults.value = emptyList(); _uiState.value = SearchUiState.Initial }
}

sealed class SearchUiState {
    object Initial : SearchUiState()
    object Loading : SearchUiState()
    object Success : SearchUiState()
    object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@Composable
fun SearchScreen(onMatchClick: (Match) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize().background(FootballColors.background)) {
        TextField(
            value = query,
            onValueChange = { viewModel.updateQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search matches, teams, competitions...", color = FootballColors.textTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FootballColors.textSecondary) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { viewModel.clearQuery() }) { Icon(Icons.Default.Clear, "Clear", tint = FootballColors.textSecondary) } },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FootballColors.cardBackground, unfocusedContainerColor = FootballColors.cardBackground,
                focusedTextColor = FootballColors.textPrimary, unfocusedTextColor = FootballColors.textPrimary,
                cursorColor = FootballColors.accent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search(); focusManager.clearFocus() })
        )

        when (uiState) {
            is SearchUiState.Initial -> EmptyState(title = "Search for Matches", message = "Find classic football matches by team, competition, or year")
            is SearchUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = FootballColors.accent) }
            is SearchUiState.Success -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("${searchResults.size} results", style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary) }
                items(searchResults, key = { it.id }) { match -> MatchListItem(match = match, onClick = { onMatchClick(match) }) }
            }
            is SearchUiState.Empty -> EmptyState(title = "No Results Found", message = "Try searching with different keywords")
            is SearchUiState.Error -> ErrorScreen(message = (uiState as SearchUiState.Error).message, onRetry = { viewModel.search() })
        }
    }
}
