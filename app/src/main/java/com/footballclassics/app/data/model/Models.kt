package com.footballclassics.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a football match from Footballia
 */
@Serializable
@Entity(tableName = "matches")
data class Match(
    @PrimaryKey
    val id: String,
    val title: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val competition: String,
    val season: String,
    val matchDate: String,
    val round: String? = null,
    val thumbnailUrl: String? = null,
    val videoUrl: String? = null,
    val pageUrl: String,
    val duration: String? = null,
    val quality: String? = null,
    val language: String? = null,
    val description: String? = null,
    val addedDate: Long = System.currentTimeMillis()
)

/**
 * Represents a football team
 */
@Serializable
@Entity(tableName = "teams")
data class Team(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String? = null,
    val logoUrl: String? = null,
    val pageUrl: String,
    val matchCount: Int = 0
)

/**
 * Represents a competition (league, cup, etc.)
 */
@Serializable
@Entity(tableName = "competitions")
data class Competition(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String? = null,
    val logoUrl: String? = null,
    val pageUrl: String,
    val type: CompetitionType = CompetitionType.LEAGUE,
    val matchCount: Int = 0
)

enum class CompetitionType {
    LEAGUE,
    CUP,
    INTERNATIONAL,
    CONTINENTAL,
    FRIENDLY
}

/**
 * Represents a season
 */
@Serializable
data class Season(
    val id: String,
    val name: String,
    val year: String,
    val pageUrl: String
)

/**
 * Favorite match entity
 */
@Entity(tableName = "favorites")
data class FavoriteMatch(
    @PrimaryKey
    val matchId: String,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Watch history entity
 */
@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey
    val matchId: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressMs: Long = 0,
    val durationMs: Long = 0,
    val completed: Boolean = false
)

/**
 * Continue watching item combining match with watch progress
 */
data class ContinueWatchingItem(
    val match: Match,
    val progressMs: Long,
    val durationMs: Long,
    val progressPercent: Float
)

/**
 * Category for home screen sections
 */
data class MatchCategory(
    val id: String,
    val title: String,
    val matches: List<Match>,
    val type: CategoryType = CategoryType.HORIZONTAL
)

enum class CategoryType {
    FEATURED,
    HORIZONTAL,
    GRID
}

/**
 * Search result wrapper
 */
data class SearchResult(
    val matches: List<Match> = emptyList(),
    val teams: List<Team> = emptyList(),
    val competitions: List<Competition> = emptyList(),
    val totalResults: Int = 0
)

/**
 * Filter options for browsing
 */
data class FilterOptions(
    val competitions: List<String> = emptyList(),
    val teams: List<String> = emptyList(),
    val seasons: List<String> = emptyList(),
    val decades: List<String> = listOf("1950s", "1960s", "1970s", "1980s", "1990s", "2000s", "2010s", "2020s")
)

/**
 * App state for UI
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * Video source info
 */
data class VideoSource(
    val url: String,
    val quality: String,
    val type: VideoType = VideoType.MP4
)

enum class VideoType {
    MP4,
    HLS,
    DASH,
    EMBED
}
