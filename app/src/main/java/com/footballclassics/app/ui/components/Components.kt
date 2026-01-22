package com.footballclassics.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.footballclassics.app.data.model.*
import com.footballclassics.app.ui.theme.FootballColors

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    showProgress: Boolean = false,
    progressPercent: Float = 0f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .aspectRatio(16f / 9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FootballColors.cardBackground)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = match.thumbnailUrl ?: "https://via.placeholder.com/400x225/1a1a1a/666666?text=${match.homeTeam}",
                contentDescription = match.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = FootballColors.cardGradient,
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = if (match.awayTeam.isNotBlank()) 
                        "${match.homeTeam} vs ${match.awayTeam}" 
                    else match.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FootballColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (match.competition.isNotBlank()) {
                    Text(
                        text = buildString {
                            append(match.competition)
                            if (match.season.isNotBlank()) {
                                append(" • ")
                                append(match.season)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = FootballColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(color = FootballColors.accent.copy(alpha = 0.9f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (onFavoriteClick != null) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) FootballColors.red else FootballColors.textSecondary
                    )
                }
            }

            if (showProgress && progressPercent > 0) {
                LinearProgressIndicator(
                    progress = progressPercent,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                    color = FootballColors.red,
                    trackColor = FootballColors.textTertiary.copy(alpha = 0.3f)
                )
            }

            if (match.homeScore != null && match.awayScore != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = FootballColors.cardElevated.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        style = MaterialTheme.typography.labelMedium,
                        color = FootballColors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedMatchCard(
    match: Match,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(screenHeight * 0.55f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = match.thumbnailUrl,
            contentDescription = match.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = FootballColors.heroGradient))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (match.competition.isNotBlank()) {
                Surface(shape = RoundedCornerShape(4.dp), color = FootballColors.accent) {
                    Text(
                        text = match.competition.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = if (match.awayTeam.isNotBlank())
                    "${match.homeTeam}\nvs ${match.awayTeam}"
                else match.title,
                style = MaterialTheme.typography.headlineLarge,
                color = FootballColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildString {
                    if (match.matchDate.isNotBlank()) append(match.matchDate)
                    if (match.season.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(match.season)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FootballColors.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FootballColors.textPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Play", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onInfoClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FootballColors.textPrimary),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "More Info")
                }
            }
        }
    }
}

@Composable
fun MatchCategoryRow(
    category: MatchCategory,
    onMatchClick: (Match) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleLarge,
                color = FootballColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            if (onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) {
                    Text(text = "See All", color = FootballColors.accent)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = FootballColors.accent, modifier = Modifier.size(18.dp))
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = category.matches, key = { it.id }) { match ->
                MatchCard(match = match, onClick = { onMatchClick(match) })
            }
        }
    }
}

@Composable
fun CompetitionCard(
    competition: Competition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FootballColors.cardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(FootballColors.cardElevated),
                contentAlignment = Alignment.Center
            ) {
                if (competition.logoUrl != null) {
                    AsyncImage(model = competition.logoUrl, contentDescription = competition.name, modifier = Modifier.size(60.dp), contentScale = ContentScale.Fit)
                } else {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = FootballColors.accent, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = competition.name,
                style = MaterialTheme.typography.titleSmall,
                color = FootballColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (competition.country != null) {
                Text(text = competition.country, style = MaterialTheme.typography.bodySmall, color = FootballColors.textSecondary)
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(FootballColors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = FootballColors.accent, modifier = Modifier.size(48.dp))
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(FootballColors.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = FootballColors.error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Something went wrong", style = MaterialTheme.typography.headlineSmall, color = FootballColors.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = FootballColors.accent, contentColor = Color.Black)) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = FootballColors.textTertiary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = FootballColors.textPrimary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = FootballColors.textSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = FootballColors.cardBackground, contentColor = FootballColors.textPrimary) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FootballColors.accent,
                selectedTextColor = FootballColors.accent,
                unselectedIconColor = FootballColors.textSecondary,
                unselectedTextColor = FootballColors.textSecondary,
                indicatorColor = FootballColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "search",
            onClick = { onNavigate("search") },
            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FootballColors.accent,
                selectedTextColor = FootballColors.accent,
                unselectedIconColor = FootballColors.textSecondary,
                unselectedTextColor = FootballColors.textSecondary,
                indicatorColor = FootballColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "competitions",
            onClick = { onNavigate("competitions") },
            icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Competitions") },
            label = { Text("Leagues") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FootballColors.accent,
                selectedTextColor = FootballColors.accent,
                unselectedIconColor = FootballColors.textSecondary,
                unselectedTextColor = FootballColors.textSecondary,
                indicatorColor = FootballColors.accent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") },
            icon = { Icon(imageVector = if (currentRoute == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorites") },
            label = { Text("My List") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FootballColors.accent,
                selectedTextColor = FootballColors.accent,
                unselectedIconColor = FootballColors.textSecondary,
                unselectedTextColor = FootballColors.textSecondary,
                indicatorColor = FootballColors.accent.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun MatchListItem(match: Match, onClick: () -> Unit, onFavoriteClick: (() -> Unit)? = null, isFavorite: Boolean = false, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FootballColors.cardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(100.dp, 56.dp).clip(RoundedCornerShape(4.dp)).background(FootballColors.cardElevated)) {
                AsyncImage(model = match.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    modifier = Modifier.align(Alignment.Center).size(28.dp).background(color = FootballColors.accent.copy(alpha = 0.9f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (match.awayTeam.isNotBlank()) "${match.homeTeam} vs ${match.awayTeam}" else match.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = FootballColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildString {
                        if (match.competition.isNotBlank()) append(match.competition)
                        if (match.season.isNotBlank()) { if (isNotEmpty()) append(" • "); append(match.season) }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FootballColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) FootballColors.red else FootballColors.textSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, placeholder: String = "Search matches, teams...", modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().height(56.dp),
        placeholder = { Text(text = placeholder, color = FootballColors.textTertiary) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = FootballColors.textSecondary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = FootballColors.textSecondary)
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FootballColors.cardBackground,
            unfocusedContainerColor = FootballColors.cardBackground,
            focusedTextColor = FootballColors.textPrimary,
            unfocusedTextColor = FootballColors.textPrimary,
            cursorColor = FootballColors.accent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}
