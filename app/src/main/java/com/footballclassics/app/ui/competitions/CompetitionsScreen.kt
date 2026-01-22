package com.footballclassics.app.ui.competitions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.footballclassics.app.data.model.Competition
import com.footballclassics.app.data.repository.FootballRepository
import com.footballclassics.app.ui.components.CompetitionCard
import com.footballclassics.app.ui.components.ErrorScreen
import com.footballclassics.app.ui.components.LoadingScreen
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetitionsViewModel @Inject constructor(private val repository: FootballRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CompetitionsUiState>(CompetitionsUiState.Loading)
    val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()

    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions.asStateFlow()

    init { loadCompetitions() }

    fun loadCompetitions() {
        viewModelScope.launch {
            _uiState.value = CompetitionsUiState.Loading
            repository.getCompetitions()
                .onSuccess { _competitions.value = it; _uiState.value = CompetitionsUiState.Success }
                .onFailure { _uiState.value = CompetitionsUiState.Error(it.message ?: "Failed to load") }
        }
    }
}

sealed class CompetitionsUiState { object Loading : CompetitionsUiState(); object Success : CompetitionsUiState(); data class Error(val message: String) : CompetitionsUiState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(onCompetitionClick: (Competition) -> Unit, viewModel: CompetitionsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val competitions by viewModel.competitions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(FootballColors.background)) {
        TopAppBar(
            title = { Text("Leagues & Cups", style = MaterialTheme.typography.headlineSmall, color = FootballColors.textPrimary, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FootballColors.background)
        )

        when (uiState) {
            is CompetitionsUiState.Loading -> LoadingScreen()
            is CompetitionsUiState.Error -> ErrorScreen((uiState as CompetitionsUiState.Error).message) { viewModel.loadCompetitions() }
            is CompetitionsUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(competitions, key = { it.id }) { competition ->
                        CompetitionCard(competition = competition, onClick = { onCompetitionClick(competition) })
                    }
                }
            }
        }
    }
}
