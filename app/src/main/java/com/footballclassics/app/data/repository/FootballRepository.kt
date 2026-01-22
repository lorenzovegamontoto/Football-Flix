package com.footballclassics.app.data.repository

import com.footballclassics.app.data.api.FootballiaScraperService
import com.footballclassics.app.data.api.HomePageContent
import com.footballclassics.app.data.local.*
import com.footballclassics.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballRepository @Inject constructor(
    private val scraperService: FootballiaScraperService,
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val competitionDao: CompetitionDao,
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao
) {

    // ==================== Home Page ====================

    suspend fun getHomeContent(): Result<HomePageContent> {
        return scraperService.getHomePage()
    }

    fun getHomeCategories(): Flow<List<MatchCategory>> = flow {
        // First emit cached data
        val cachedMatches = matchDao.getRecentMatches(20)
        if (cachedMatches.isNotEmpty()) {
            emit(buildCategoriesFromMatches(cachedMatches))
        }

        // Then fetch fresh data
        scraperService.getHomePage().onSuccess { content ->
            // Cache matches
            matchDao.insertMatches(content.recentMatches)
            
            val categories = mutableListOf<MatchCategory>()
            
            // Featured
            if (content.featuredMatches.isNotEmpty()) {
                categories.add(
                    MatchCategory(
                        id = "featured",
                        title = "Featured Matches",
                        matches = content.featuredMatches,
                        type = CategoryType.FEATURED
                    )
                )
            }

            // Continue Watching
            val continueWatching = getContinueWatchingMatches()
            if (continueWatching.isNotEmpty()) {
                categories.add(
                    MatchCategory(
                        id = "continue_watching",
                        title = "Continue Watching",
                        matches = continueWatching.map { it.match },
                        type = CategoryType.HORIZONTAL
                    )
                )
            }

            // Recent matches
            if (content.recentMatches.isNotEmpty()) {
                categories.add(
                    MatchCategory(
                        id = "recent",
                        title = "Recently Added",
                        matches = content.recentMatches,
                        type = CategoryType.HORIZONTAL
                    )
                )
            }

            emit(categories)
        }
    }

    private fun buildCategoriesFromMatches(matches: List<Match>): List<MatchCategory> {
        val categories = mutableListOf<MatchCategory>()
        
        // Group by competition
        val byCompetition = matches.groupBy { it.competition }
        byCompetition.forEach { (competition, competitionMatches) ->
            if (competition.isNotBlank() && competitionMatches.size >= 3) {
                categories.add(
                    MatchCategory(
                        id = "competition_$competition",
                        title = competition,
                        matches = competitionMatches,
                        type = CategoryType.HORIZONTAL
                    )
                )
            }
        }

        return categories
    }

    // ==================== Search ====================

    suspend fun search(query: String): SearchResult {
        val localMatches = matchDao.searchMatches(query)
        val localTeams = teamDao.searchTeams(query)
        val localCompetitions = competitionDao.searchCompetitions(query)

        // Also search online
        val onlineMatches = scraperService.searchMatches(query).getOrNull() ?: emptyList()
        
        // Cache online results
        if (onlineMatches.isNotEmpty()) {
            matchDao.insertMatches(onlineMatches)
        }

        val allMatches = (localMatches + onlineMatches).distinctBy { it.id }

        return SearchResult(
            matches = allMatches,
            teams = localTeams,
            competitions = localCompetitions,
            totalResults = allMatches.size + localTeams.size + localCompetitions.size
        )
    }

    suspend fun searchOnline(query: String, page: Int = 1): Result<List<Match>> {
        return scraperService.searchMatches(query, page).also { result ->
            result.onSuccess { matches ->
                matchDao.insertMatches(matches)
            }
        }
    }

    // ==================== Matches ====================

    suspend fun getMatchDetails(matchId: String): Result<Match> {
        // Check cache first
        matchDao.getMatchById(matchId)?.let { cached ->
            if (cached.videoUrl != null) {
                return Result.success(cached)
            }
        }

        // Fetch from web
        val match = matchDao.getMatchById(matchId)
        return if (match != null) {
            scraperService.getMatchDetails(match.pageUrl).also { result ->
                result.onSuccess { matchDao.insertMatch(it) }
            }
        } else {
            Result.failure(Exception("Match not found"))
        }
    }

    suspend fun getMatchByUrl(pageUrl: String): Result<Match> {
        return scraperService.getMatchDetails(pageUrl).also { result ->
            result.onSuccess { matchDao.insertMatch(it) }
        }
    }

    suspend fun getVideoSource(matchId: String): Result<VideoSource> {
        val match = matchDao.getMatchById(matchId) 
            ?: return Result.failure(Exception("Match not found"))
        
        // If we already have video URL cached
        match.videoUrl?.let { url ->
            return Result.success(
                VideoSource(
                    url = url,
                    quality = match.quality ?: "HD",
                    type = when {
                        url.contains(".m3u8") -> VideoType.HLS
                        url.contains(".mpd") -> VideoType.DASH
                        else -> VideoType.MP4
                    }
                )
            )
        }

        return scraperService.getVideoSource(match.pageUrl)
    }

    fun getAllMatches(): Flow<List<Match>> = matchDao.getAllMatches()

    fun getMatchesByCompetition(competition: String): Flow<List<Match>> = 
        matchDao.getMatchesByCompetition(competition)

    fun getMatchesByTeam(team: String): Flow<List<Match>> = 
        matchDao.getMatchesByTeam(team)

    suspend fun getCompetitionMatches(competitionSlug: String, page: Int = 1): Result<List<Match>> {
        return scraperService.getCompetitionMatches(competitionSlug, page).also { result ->
            result.onSuccess { matchDao.insertMatches(it) }
        }
    }

    suspend fun getTeamMatches(teamSlug: String, page: Int = 1): Result<List<Match>> {
        return scraperService.getTeamMatches(teamSlug, page).also { result ->
            result.onSuccess { matchDao.insertMatches(it) }
        }
    }

    // ==================== Competitions ====================

    suspend fun getCompetitions(): Result<List<Competition>> {
        return scraperService.getCompetitions().also { result ->
            result.onSuccess { competitionDao.insertCompetitions(it) }
        }
    }

    fun getAllCompetitions(): Flow<List<Competition>> = competitionDao.getAllCompetitions()

    fun getCompetitionsByType(type: CompetitionType): Flow<List<Competition>> = 
        competitionDao.getCompetitionsByType(type)

    // ==================== Teams ====================

    suspend fun getTeams(letter: Char? = null): Result<List<Team>> {
        return scraperService.getTeams(letter).also { result ->
            result.onSuccess { teamDao.insertTeams(it) }
        }
    }

    fun getAllTeams(): Flow<List<Team>> = teamDao.getAllTeams()

    // ==================== Favorites ====================

    fun getFavoriteMatches(): Flow<List<Match>> = favoriteDao.getFavoriteMatches()

    fun isFavorite(matchId: String): Flow<Boolean> = favoriteDao.isFavorite(matchId)

    suspend fun toggleFavorite(matchId: String) {
        if (favoriteDao.isFavoriteSync(matchId)) {
            favoriteDao.removeFavorite(matchId)
        } else {
            favoriteDao.addFavorite(FavoriteMatch(matchId))
        }
    }

    suspend fun addToFavorites(matchId: String) {
        favoriteDao.addFavorite(FavoriteMatch(matchId))
    }

    suspend fun removeFromFavorites(matchId: String) {
        favoriteDao.removeFavorite(matchId)
    }

    // ==================== Watch History ====================

    suspend fun getContinueWatchingMatches(): List<ContinueWatchingItem> {
        return watchHistoryDao.getContinueWatching().map { item ->
            ContinueWatchingItem(
                match = item.match,
                progressMs = item.progressMs,
                durationMs = item.durationMs,
                progressPercent = if (item.durationMs > 0) {
                    (item.progressMs.toFloat() / item.durationMs.toFloat())
                } else 0f
            )
        }
    }

    fun getRecentlyWatched(): Flow<List<Match>> = watchHistoryDao.getRecentlyWatched()

    suspend fun getWatchProgress(matchId: String): WatchHistory? {
        return watchHistoryDao.getWatchProgress(matchId)
    }

    suspend fun updateWatchProgress(matchId: String, progressMs: Long, durationMs: Long) {
        val completed = durationMs > 0 && progressMs >= (durationMs * 0.95)
        watchHistoryDao.updateWatchProgress(
            WatchHistory(
                matchId = matchId,
                progressMs = progressMs,
                durationMs = durationMs,
                completed = completed
            )
        )
    }

    suspend fun markAsWatched(matchId: String, durationMs: Long) {
        watchHistoryDao.updateWatchProgress(
            WatchHistory(
                matchId = matchId,
                progressMs = durationMs,
                durationMs = durationMs,
                completed = true
            )
        )
    }

    suspend fun removeFromHistory(matchId: String) {
        watchHistoryDao.removeFromHistory(matchId)
    }

    suspend fun clearWatchHistory() {
        watchHistoryDao.clearHistory()
    }

    // ==================== Filter Options ====================

    suspend fun getFilterOptions(): FilterOptions {
        return FilterOptions(
            competitions = matchDao.getAllCompetitionNames(),
            seasons = matchDao.getAllSeasons()
        )
    }
}
