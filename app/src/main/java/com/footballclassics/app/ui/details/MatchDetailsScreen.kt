package com.footballclassics.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.footballclassics.app.data.model.Match
import com.footballclassics.app.data.repository.FootballRepository
import com.footballclassics.app.ui.components.ErrorScreen
import com.footballclassics.app.ui.components.LoadingScreen
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(private val repository: FootballRepository) : ViewModel() {
    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            repository.getMatchDetails(matchId)
                .onSuccess { _match.value = it; _uiState.value = DetailsUiState.Success }
                .onFailure { _uiState.value = DetailsUiState.Error(it.message ?: "Failed") }
            repository.isFavorite(matchId).collect { _isFavorite.value = it }
        }
    }

    fun toggleFavorite() { _match.value?.let { viewModelScope.launch { repository.toggleFavorite(it.id) } } }
}

sealed class DetailsUiState { object Loading : DetailsUiState(); object Success : DetailsUiState(); data class Error(val message: String) : DetailsUiState() }

@Composable
fun MatchDetailsScreen(matchId: String, onBackClick: () -> Unit, onPlayClick: (Match) -> Unit, viewModel: MatchDetailsViewModel = hiltViewModel()) {
    val match by viewModel.match.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(matchId) { viewModel.loadMatch(matchId) }

    when (uiState) {
        is DetailsUiState.Loading -> LoadingScreen()
        is DetailsUiState.Error -> ErrorScreen(message = (uiState as DetailsUiState.Error).message, onRetry = { viewModel.loadMatch(matchId) })
        is DetailsUiState.Success -> match?.let { m ->
            Box(Modifier.fillMaxSize().background(FootballColors.background)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
                        AsyncImage(m.thumbnailUrl, m.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(FootballColors.heroGradient)))
                        IconButton(onBackClick, Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(0.5f), CircleShape)) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Column(Modifier.padding(16.dp)) {
                        if (m.competition.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(4.dp), color = FootballColors.accent) {
                                Text(m.competition.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 4.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        Text(if (m.awayTeam.isNotBlank()) "${m.homeTeam} vs ${m.awayTeam}" else m.title, style = MaterialTheme.typography.headlineMedium, color = FootballColors.textPrimary, fontWeight = FontWeight.Bold)
                        if (m.homeScore != null && m.awayScore != null) { Spacer(Modifier.height(8.dp)); Text("Final: ${m.homeScore} - ${m.awayScore}", style = MaterialTheme.typography.titleLarge, color = FootballColors.accent, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (m.matchDate.isNotBlank()) { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FootballColors.textSecondary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(m.matchDate, style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary); Spacer(Modifier.width(16.dp)) }
                            if (m.season.isNotBlank()) Text("Season: ${m.season}", style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary)
                        }
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button({ onPlayClick(m) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(FootballColors.textPrimary, Color.Black), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("Play", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton({ viewModel.toggleFavorite() }, shape = RoundedCornerShape(8.dp)) {
                                Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (isFavorite) FootballColors.red else FootballColors.textPrimary)
                                Spacer(Modifier.width(8.dp)); Text(if (isFavorite) "In My List" else "Add to List", color = FootballColors.textPrimary)
                            }
                        }
                        m.description?.let { if (it.isNotBlank()) { Spacer(Modifier.height(24.dp)); Text("About", style = MaterialTheme.typography.titleMedium, color = FootballColors.textPrimary, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(it, style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary) } }
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
