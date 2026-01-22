package com.footballclassics.app.data.api

import com.footballclassics.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Web scraper service for Footballia.eu
 * Parses HTML pages to extract match information
 */
@Singleton
class FootballiaScraperService @Inject constructor() {

    companion object {
        const val BASE_URL = "https://footballia.eu"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        private const val TIMEOUT_MS = 15000
    }

    /**
     * Fetch and parse the home page for featured content
     */
    suspend fun getHomePage(): Result<HomePageContent> = withContext(Dispatchers.IO) {
        try {
            val doc = fetchDocument(BASE_URL)
            val content = parseHomePage(doc)
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for matches
     */
    suspend fun searchMatches(query: String, page: Int = 1): Result<List<Match>> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "$BASE_URL/search?q=${query.encodeUrl()}&page=$page"
            val doc = fetchDocument(searchUrl)
            val matches = parseMatchList(doc)
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get matches by competition
     */
    suspend fun getCompetitionMatches(competitionSlug: String, page: Int = 1): Result<List<Match>> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/competitions/$competitionSlug?page=$page"
            val doc = fetchDocument(url)
            val matches = parseMatchList(doc)
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get matches by team
     */
    suspend fun getTeamMatches(teamSlug: String, page: Int = 1): Result<List<Match>> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/teams/$teamSlug?page=$page"
            val doc = fetchDocument(url)
            val matches = parseMatchList(doc)
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all competitions
     */
    suspend fun getCompetitions(): Result<List<Competition>> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/competitions"
            val doc = fetchDocument(url)
            val competitions = parseCompetitionList(doc)
            Result.success(competitions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all teams
     */
    suspend fun getTeams(letter: Char? = null): Result<List<Team>> = withContext(Dispatchers.IO) {
        try {
            val url = if (letter != null) "$BASE_URL/teams?letter=$letter" else "$BASE_URL/teams"
            val doc = fetchDocument(url)
            val teams = parseTeamList(doc)
            Result.success(teams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get match details including video URL
     */
    suspend fun getMatchDetails(matchUrl: String): Result<Match> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = if (matchUrl.startsWith("http")) matchUrl else "$BASE_URL$matchUrl"
            val doc = fetchDocument(fullUrl)
            val match = parseMatchDetails(doc, fullUrl)
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get video source URL from match page
     */
    suspend fun getVideoSource(matchUrl: String): Result<VideoSource> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = if (matchUrl.startsWith("http")) matchUrl else "$BASE_URL$matchUrl"
            val doc = fetchDocument(fullUrl)
            val videoSource = extractVideoSource(doc)
            if (videoSource != null) {
                Result.success(videoSource)
            } else {
                Result.failure(Exception("Video source not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Private Parsing Methods ====================

    private fun fetchDocument(url: String): Document {
        return Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .followRedirects(true)
            .get()
    }

    private fun parseHomePage(doc: Document): HomePageContent {
        val featuredMatches = mutableListOf<Match>()
        val recentMatches = mutableListOf<Match>()
        val popularCompetitions = mutableListOf<Competition>()

        // Parse featured/hero section
        doc.select(".featured-match, .hero-match, .spotlight").forEach { element ->
            parseMatchElement(element)?.let { featuredMatches.add(it) }
        }

        // Parse recent matches grid
        doc.select(".match-card, .match-item, .video-item").forEach { element ->
            parseMatchElement(element)?.let { recentMatches.add(it) }
        }

        // Parse popular competitions
        doc.select(".competition-item, .league-item").forEach { element ->
            parseCompetitionElement(element)?.let { popularCompetitions.add(it) }
        }

        // If standard selectors don't work, try generic content parsing
        if (recentMatches.isEmpty()) {
            doc.select("a[href*='/matches/'], a[href*='/match/']").forEach { element ->
                parseMatchFromLink(element)?.let { recentMatches.add(it) }
            }
        }

        return HomePageContent(
            featuredMatches = featuredMatches.take(5),
            recentMatches = recentMatches.distinctBy { it.id }.take(20),
            popularCompetitions = popularCompetitions.take(10)
        )
    }

    private fun parseMatchList(doc: Document): List<Match> {
        val matches = mutableListOf<Match>()

        // Try multiple selector patterns
        val selectors = listOf(
            ".match-card",
            ".match-item", 
            ".video-item",
            ".game-item",
            "article.match",
            ".content-item"
        )

        for (selector in selectors) {
            doc.select(selector).forEach { element ->
                parseMatchElement(element)?.let { matches.add(it) }
            }
            if (matches.isNotEmpty()) break
        }

        // Fallback: parse links
        if (matches.isEmpty()) {
            doc.select("a[href*='/matches/'], a[href*='/match/'], a[href*='/game/']").forEach { element ->
                parseMatchFromLink(element)?.let { matches.add(it) }
            }
        }

        return matches.distinctBy { it.id }
    }

    private fun parseMatchElement(element: Element): Match? {
        return try {
            val linkElement = element.selectFirst("a[href]") ?: element
            val href = linkElement.attr("href")
            if (href.isBlank()) return null

            val id = extractIdFromUrl(href)
            val title = element.selectFirst(".match-title, .title, h3, h4")?.text()
                ?: linkElement.text()
                ?: return null

            val (homeTeam, awayTeam) = parseTeamsFromTitle(title)
            
            val thumbnailUrl = element.selectFirst("img")?.let { img ->
                img.attr("data-src").ifBlank { img.attr("src") }
            }?.let { makeAbsoluteUrl(it) }

            val competition = element.selectFirst(".competition, .league, .tournament")?.text() ?: ""
            val season = element.selectFirst(".season, .year")?.text() ?: ""
            val matchDate = element.selectFirst(".date, .match-date, time")?.text() ?: ""

            Match(
                id = id,
                title = title.trim(),
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                competition = competition,
                season = season,
                matchDate = matchDate,
                thumbnailUrl = thumbnailUrl,
                pageUrl = makeAbsoluteUrl(href)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMatchFromLink(element: Element): Match? {
        return try {
            val href = element.attr("href")
            if (href.isBlank() || !href.contains("match", ignoreCase = true)) return null

            val id = extractIdFromUrl(href)
            val title = element.text().ifBlank { 
                element.attr("title").ifBlank { return null }
            }

            val (homeTeam, awayTeam) = parseTeamsFromTitle(title)

            val thumbnailUrl = element.selectFirst("img")?.let { img ->
                img.attr("data-src").ifBlank { img.attr("src") }
            }?.let { makeAbsoluteUrl(it) }

            Match(
                id = id,
                title = title.trim(),
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                competition = "",
                season = "",
                matchDate = "",
                thumbnailUrl = thumbnailUrl,
                pageUrl = makeAbsoluteUrl(href)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMatchDetails(doc: Document, pageUrl: String): Match {
        val id = extractIdFromUrl(pageUrl)
        
        val title = doc.selectFirst("h1, .match-title, .page-title")?.text() ?: "Unknown Match"
        val (homeTeam, awayTeam) = parseTeamsFromTitle(title)

        // Try to extract scores
        val scoreText = doc.selectFirst(".score, .result, .match-score")?.text()
        val (homeScore, awayScore) = parseScores(scoreText)

        val thumbnailUrl = doc.selectFirst("meta[property=og:image]")?.attr("content")
            ?: doc.selectFirst(".match-poster img, .video-thumbnail img")?.attr("src")

        val competition = doc.selectFirst(".competition-name, .league-name, .tournament")?.text() ?: ""
        val season = doc.selectFirst(".season")?.text() ?: ""
        val matchDate = doc.selectFirst(".match-date, time, .date")?.text() ?: ""
        val round = doc.selectFirst(".round, .matchday")?.text()
        val description = doc.selectFirst("meta[name=description]")?.attr("content")
            ?: doc.selectFirst(".match-description, .description")?.text()

        val videoSource = extractVideoSource(doc)

        return Match(
            id = id,
            title = title,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeScore = homeScore,
            awayScore = awayScore,
            competition = competition,
            season = season,
            matchDate = matchDate,
            round = round,
            thumbnailUrl = thumbnailUrl?.let { makeAbsoluteUrl(it) },
            videoUrl = videoSource?.url,
            pageUrl = pageUrl,
            description = description,
            quality = videoSource?.quality
        )
    }

    private fun extractVideoSource(doc: Document): VideoSource? {
        // Try video tag
        doc.selectFirst("video source")?.let { source ->
            val url = source.attr("src")
            if (url.isNotBlank()) {
                return VideoSource(
                    url = makeAbsoluteUrl(url),
                    quality = source.attr("label").ifBlank { "HD" },
                    type = when {
                        url.contains(".m3u8") -> VideoType.HLS
                        url.contains(".mpd") -> VideoType.DASH
                        else -> VideoType.MP4
                    }
                )
            }
        }

        // Try video tag directly
        doc.selectFirst("video")?.let { video ->
            val url = video.attr("src")
            if (url.isNotBlank()) {
                return VideoSource(
                    url = makeAbsoluteUrl(url),
                    quality = "HD",
                    type = VideoType.MP4
                )
            }
        }

        // Try iframe embed
        doc.selectFirst("iframe[src*=video], iframe[src*=player], iframe[src*=embed]")?.let { iframe ->
            val src = iframe.attr("src")
            if (src.isNotBlank()) {
                return VideoSource(
                    url = makeAbsoluteUrl(src),
                    quality = "HD",
                    type = VideoType.EMBED
                )
            }
        }

        // Try JavaScript video source patterns
        val scripts = doc.select("script").map { it.html() }
        for (script in scripts) {
            // Look for common video URL patterns
            val patterns = listOf(
                Regex("""source:\s*["']([^"']+\.mp4[^"']*)["']"""),
                Regex("""file:\s*["']([^"']+\.mp4[^"']*)["']"""),
                Regex("""src:\s*["']([^"']+\.mp4[^"']*)["']"""),
                Regex("""video[Uu]rl['":\s]+["']([^"']+)["']"""),
                Regex("""["']([^"']+\.m3u8[^"']*)["']""")
            )
            
            for (pattern in patterns) {
                pattern.find(script)?.let { match ->
                    val url = match.groupValues[1]
                    if (url.isNotBlank()) {
                        return VideoSource(
                            url = makeAbsoluteUrl(url),
                            quality = "HD",
                            type = when {
                                url.contains(".m3u8") -> VideoType.HLS
                                url.contains(".mpd") -> VideoType.DASH
                                else -> VideoType.MP4
                            }
                        )
                    }
                }
            }
        }

        return null
    }

    private fun parseCompetitionList(doc: Document): List<Competition> {
        val competitions = mutableListOf<Competition>()

        doc.select(".competition-item, .league-item, .tournament-item, a[href*='/competitions/']").forEach { element ->
            parseCompetitionElement(element)?.let { competitions.add(it) }
        }

        return competitions.distinctBy { it.id }
    }

    private fun parseCompetitionElement(element: Element): Competition? {
        return try {
            val linkElement = element.selectFirst("a[href]") ?: element
            val href = linkElement.attr("href")
            if (href.isBlank()) return null

            val id = extractIdFromUrl(href)
            val name = element.selectFirst(".name, .title, h3, h4")?.text()
                ?: linkElement.text()
                ?: return null

            val logoUrl = element.selectFirst("img")?.attr("src")?.let { makeAbsoluteUrl(it) }
            val country = element.selectFirst(".country")?.text()

            val type = when {
                name.contains("World Cup", ignoreCase = true) -> CompetitionType.INTERNATIONAL
                name.contains("Euro", ignoreCase = true) -> CompetitionType.INTERNATIONAL
                name.contains("Champions League", ignoreCase = true) -> CompetitionType.CONTINENTAL
                name.contains("Copa", ignoreCase = true) -> CompetitionType.CONTINENTAL
                name.contains("Cup", ignoreCase = true) -> CompetitionType.CUP
                name.contains("FA Cup", ignoreCase = true) -> CompetitionType.CUP
                else -> CompetitionType.LEAGUE
            }

            Competition(
                id = id,
                name = name.trim(),
                country = country,
                logoUrl = logoUrl,
                pageUrl = makeAbsoluteUrl(href),
                type = type
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTeamList(doc: Document): List<Team> {
        val teams = mutableListOf<Team>()

        doc.select(".team-item, a[href*='/teams/']").forEach { element ->
            parseTeamElement(element)?.let { teams.add(it) }
        }

        return teams.distinctBy { it.id }
    }

    private fun parseTeamElement(element: Element): Team? {
        return try {
            val linkElement = element.selectFirst("a[href]") ?: element
            val href = linkElement.attr("href")
            if (href.isBlank()) return null

            val id = extractIdFromUrl(href)
            val name = element.selectFirst(".name, .title")?.text()
                ?: linkElement.text()
                ?: return null

            val logoUrl = element.selectFirst("img")?.attr("src")?.let { makeAbsoluteUrl(it) }
            val country = element.selectFirst(".country")?.text()

            Team(
                id = id,
                name = name.trim(),
                country = country,
                logoUrl = logoUrl,
                pageUrl = makeAbsoluteUrl(href)
            )
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Utility Methods ====================

    private fun parseTeamsFromTitle(title: String): Pair<String, String> {
        // Common separators: vs, v, -, –, —, x
        val separators = listOf(" vs ", " v ", " - ", " – ", " — ", " x ", " VS ", " V ")
        
        for (separator in separators) {
            if (title.contains(separator)) {
                val parts = title.split(separator, limit = 2)
                if (parts.size == 2) {
                    return Pair(parts[0].trim(), parts[1].trim())
                }
            }
        }
        
        return Pair(title, "")
    }

    private fun parseScores(scoreText: String?): Pair<Int?, Int?> {
        if (scoreText.isNullOrBlank()) return Pair(null, null)
        
        val pattern = Regex("""(\d+)\s*[-–—:]\s*(\d+)""")
        val match = pattern.find(scoreText)
        
        return if (match != null) {
            Pair(match.groupValues[1].toIntOrNull(), match.groupValues[2].toIntOrNull())
        } else {
            Pair(null, null)
        }
    }

    private fun extractIdFromUrl(url: String): String {
        return url.trim('/')
            .split("/")
            .lastOrNull()
            ?.split("?")
            ?.firstOrNull()
            ?: url.hashCode().toString()
    }

    private fun makeAbsoluteUrl(url: String): String {
        return when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$BASE_URL$url"
            else -> "$BASE_URL/$url"
        }
    }

    private fun String.encodeUrl(): String {
        return java.net.URLEncoder.encode(this, "UTF-8")
    }
}

/**
 * Data class for home page content
 */
data class HomePageContent(
    val featuredMatches: List<Match>,
    val recentMatches: List<Match>,
    val popularCompetitions: List<Competition>
)
