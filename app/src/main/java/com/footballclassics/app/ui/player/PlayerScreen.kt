package com.footballclassics.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.footballclassics.app.data.model.Match
import com.footballclassics.app.data.model.VideoSource
import com.footballclassics.app.data.repository.FootballRepository
import com.footballclassics.app.ui.components.ErrorScreen
import com.footballclassics.app.ui.components.LoadingScreen
import com.footballclassics.app.ui.theme.FootballColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(private val repository: FootballRepository) : ViewModel() {
    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match.asStateFlow()

    private val _videoSource = MutableStateFlow<VideoSource?>(null)
    val videoSource: StateFlow<VideoSource?> = _videoSource.asStateFlow()

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _resumePosition = MutableStateFlow(0L)
    val resumePosition: StateFlow<Long> = _resumePosition.asStateFlow()

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            
            // Get match details
            repository.getMatchDetails(matchId)
                .onSuccess { match ->
                    _match.value = match
                    
                    // Get video source
                    repository.getVideoSource(matchId)
                        .onSuccess { source ->
                            _videoSource.value = source
                            _uiState.value = PlayerUiState.Ready
                        }
                        .onFailure {
                            _uiState.value = PlayerUiState.Error("Could not load video: ${it.message}")
                        }
                    
                    // Get resume position
                    repository.getWatchProgress(matchId)?.let {
                        if (!it.completed) {
                            _resumePosition.value = it.progressMs
                        }
                    }
                }
                .onFailure {
                    _uiState.value = PlayerUiState.Error(it.message ?: "Failed to load match")
                }
        }
    }

    fun saveProgress(positionMs: Long, durationMs: Long) {
        _match.value?.let { match ->
            viewModelScope.launch {
                repository.updateWatchProgress(match.id, positionMs, durationMs)
            }
        }
    }
}

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    object Ready : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    matchId: String,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val uiState by viewModel.uiState.collectAsState()
    val match by viewModel.match.collectAsState()
    val videoSource by viewModel.videoSource.collectAsState()
    val resumePosition by viewModel.resumePosition.collectAsState()

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var showControls by remember { mutableStateOf(true) }

    // Force landscape for video
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    // Save progress periodically
    LaunchedEffect(player) {
        while (true) {
            delay(10000)
            player?.let { p ->
                if (p.isPlaying) {
                    viewModel.saveProgress(p.currentPosition, p.duration)
                }
            }
        }
    }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player?.let { p ->
                        viewModel.saveProgress(p.currentPosition, p.duration)
                        p.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> player?.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player?.let { p ->
                viewModel.saveProgress(p.currentPosition, p.duration)
                p.release()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (uiState) {
            is PlayerUiState.Loading -> {
                LoadingScreen(modifier = Modifier.fillMaxSize())
            }

            is PlayerUiState.Error -> {
                ErrorScreen(
                    message = (uiState as PlayerUiState.Error).message,
                    onRetry = { viewModel.loadMatch(matchId) }
                )
            }

            is PlayerUiState.Ready -> {
                videoSource?.let { source ->
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                useController = true
                                setShowNextButton(false)
                                setShowPreviousButton(false)
                                
                                player = ExoPlayer.Builder(ctx).build().also { exo ->
                                    this.player = exo
                                    val mediaItem = MediaItem.fromUri(source.url)
                                    exo.setMediaItem(mediaItem)
                                    exo.prepare()
                                    
                                    if (resumePosition > 0) {
                                        exo.seekTo(resumePosition)
                                    }
                                    
                                    exo.playWhenReady = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Custom overlay with back button and title
                    if (showControls) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.TopStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        player?.let { p ->
                                            viewModel.saveProgress(p.currentPosition, p.duration)
                                        }
                                        onBackClick()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                match?.let { m ->
                                    Text(
                                        text = if (m.awayTeam.isNotBlank()) 
                                            "${m.homeTeam} vs ${m.awayTeam}" 
                                        else m.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
