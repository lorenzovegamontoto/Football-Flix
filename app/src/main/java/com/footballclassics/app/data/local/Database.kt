package com.footballclassics.app.data.local

import androidx.room.*
import com.footballclassics.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [Match::class, Team::class, Competition::class, FavoriteMatch::class, WatchHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FootballDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun competitionDao(): CompetitionDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}

class Converters {
    @TypeConverter
    fun fromCompetitionType(type: CompetitionType): String = type.name

    @TypeConverter
    fun toCompetitionType(value: String): CompetitionType = 
        CompetitionType.valueOf(value)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY addedDate DESC")
    fun getAllMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: String): Match?

    @Query("SELECT * FROM matches WHERE competition = :competition ORDER BY matchDate DESC")
    fun getMatchesByCompetition(competition: String): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE homeTeam = :team OR awayTeam = :team ORDER BY matchDate DESC")
    fun getMatchesByTeam(team: String): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE season = :season ORDER BY matchDate DESC")
    fun getMatchesBySeason(season: String): Flow<List<Match>>

    @Query("""
        SELECT * FROM matches 
        WHERE title LIKE '%' || :query || '%' 
        OR homeTeam LIKE '%' || :query || '%' 
        OR awayTeam LIKE '%' || :query || '%'
        OR competition LIKE '%' || :query || '%'
        ORDER BY matchDate DESC
        LIMIT :limit
    """)
    suspend fun searchMatches(query: String, limit: Int = 50): List<Match>

    @Query("SELECT * FROM matches ORDER BY addedDate DESC LIMIT :limit")
    suspend fun getRecentMatches(limit: Int = 20): List<Match>

    @Query("SELECT DISTINCT competition FROM matches ORDER BY competition")
    suspend fun getAllCompetitionNames(): List<String>

    @Query("SELECT DISTINCT season FROM matches ORDER BY season DESC")
    suspend fun getAllSeasons(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<Match>)

    @Delete
    suspend fun deleteMatch(match: Match)

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches()
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name")
    fun getAllTeams(): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: String): Team?

    @Query("SELECT * FROM teams WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    suspend fun searchTeams(query: String, limit: Int = 20): List<Team>

    @Query("SELECT * FROM teams WHERE country = :country ORDER BY name")
    fun getTeamsByCountry(country: String): Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<Team>)

    @Query("DELETE FROM teams")
    suspend fun clearAllTeams()
}

@Dao
interface CompetitionDao {
    @Query("SELECT * FROM competitions ORDER BY name")
    fun getAllCompetitions(): Flow<List<Competition>>

    @Query("SELECT * FROM competitions WHERE id = :id")
    suspend fun getCompetitionById(id: String): Competition?

    @Query("SELECT * FROM competitions WHERE type = :type ORDER BY name")
    fun getCompetitionsByType(type: CompetitionType): Flow<List<Competition>>

    @Query("SELECT * FROM competitions WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    suspend fun searchCompetitions(query: String, limit: Int = 20): List<Competition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetition(competition: Competition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetitions(competitions: List<Competition>)

    @Query("DELETE FROM competitions")
    suspend fun clearAllCompetitions()
}

@Dao
interface FavoriteDao {
    @Query("""
        SELECT m.* FROM matches m 
        INNER JOIN favorites f ON m.id = f.matchId 
        ORDER BY f.addedAt DESC
    """)
    fun getFavoriteMatches(): Flow<List<Match>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE matchId = :matchId)")
    fun isFavorite(matchId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE matchId = :matchId)")
    suspend fun isFavoriteSync(matchId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteMatch)

    @Query("DELETE FROM favorites WHERE matchId = :matchId")
    suspend fun removeFavorite(matchId: String)

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoriteCount(): Flow<Int>
}

@Dao
interface WatchHistoryDao {
    @Query("""
        SELECT m.*, w.progressMs, w.durationMs 
        FROM matches m 
        INNER JOIN watch_history w ON m.id = w.matchId 
        WHERE w.completed = 0 AND w.progressMs > 0
        ORDER BY w.watchedAt DESC
        LIMIT :limit
    """)
    suspend fun getContinueWatching(limit: Int = 10): List<MatchWithProgress>

    @Query("SELECT * FROM watch_history WHERE matchId = :matchId")
    suspend fun getWatchProgress(matchId: String): WatchHistory?

    @Query("""
        SELECT m.* FROM matches m 
        INNER JOIN watch_history w ON m.id = w.matchId 
        ORDER BY w.watchedAt DESC
        LIMIT :limit
    """)
    fun getRecentlyWatched(limit: Int = 20): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWatchProgress(history: WatchHistory)

    @Query("DELETE FROM watch_history WHERE matchId = :matchId")
    suspend fun removeFromHistory(matchId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

data class MatchWithProgress(
    @Embedded val match: Match,
    val progressMs: Long,
    val durationMs: Long
)
