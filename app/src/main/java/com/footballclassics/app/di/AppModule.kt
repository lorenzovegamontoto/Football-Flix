package com.footballclassics.app.di

import android.content.Context
import androidx.room.Room
import com.footballclassics.app.data.api.FootballiaScraperService
import com.footballclassics.app.data.local.*
import com.footballclassics.app.data.repository.FootballRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideFootballDatabase(
        @ApplicationContext context: Context
    ): FootballDatabase {
        return Room.databaseBuilder(
            context,
            FootballDatabase::class.java,
            "football_classics_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMatchDao(database: FootballDatabase): MatchDao = database.matchDao()

    @Provides
    @Singleton
    fun provideTeamDao(database: FootballDatabase): TeamDao = database.teamDao()

    @Provides
    @Singleton
    fun provideCompetitionDao(database: FootballDatabase): CompetitionDao = database.competitionDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: FootballDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: FootballDatabase): WatchHistoryDao = database.watchHistoryDao()

    @Provides
    @Singleton
    fun provideFootballiaScraperService(): FootballiaScraperService {
        return FootballiaScraperService()
    }

    @Provides
    @Singleton
    fun provideFootballRepository(
        scraperService: FootballiaScraperService,
        matchDao: MatchDao,
        teamDao: TeamDao,
        competitionDao: CompetitionDao,
        favoriteDao: FavoriteDao,
        watchHistoryDao: WatchHistoryDao
    ): FootballRepository {
        return FootballRepository(
            scraperService = scraperService,
            matchDao = matchDao,
            teamDao = teamDao,
            competitionDao = competitionDao,
            favoriteDao = favoriteDao,
            watchHistoryDao = watchHistoryDao
        )
    }
}
