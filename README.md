# Football Classics - Historic Football Streaming App

A Netflix-style Android application for streaming classic football matches from Footballia.eu. Built with modern Android development practices using Jetpack Compose, Kotlin, and Material 3.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

## 📱 Features

### Core Features
- **Netflix-style UI** - Cinematic dark theme with golden accents
- **Browse Matches** - Featured heroes, horizontal carousels, category rows
- **Search** - Find matches by team, competition, or year
- **Watch History** - Resume where you left off with "Continue Watching"
- **Favorites** - Save matches to "My List" for later
- **Video Player** - Full-screen ExoPlayer with progress tracking

### Technical Features
- **Web Scraping** - JSoup-based parser for Footballia.eu content
- **Offline Caching** - Room database for matches, teams, competitions
- **Dependency Injection** - Hilt for clean architecture
- **Reactive UI** - Kotlin Flow + Compose state management
- **Pull-to-Refresh** - Material 3 refresh indicators

## 🏗️ Architecture

```
app/
├── data/
│   ├── api/          # FootballiaScraperService (web scraping)
│   ├── local/        # Room database, DAOs
│   ├── model/        # Data classes (Match, Team, Competition)
│   └── repository/   # FootballRepository (single source of truth)
├── di/               # Hilt modules
├── navigation/       # Navigation graph and routes
├── ui/
│   ├── components/   # Reusable composables (MatchCard, etc.)
│   ├── theme/        # Colors, typography, theme
│   ├── home/         # Home screen
│   ├── search/       # Search screen
│   ├── details/      # Match details screen
│   ├── player/       # Video player screen
│   ├── favorites/    # My List screen
│   └── competitions/ # Leagues & Cups screen
└── utils/            # Helper functions
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Min SDK 26 (Android 8.0)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd footballia-app
   ```

2. **Open in Android Studio**
   - File → Open → Select the project folder
   - Let Gradle sync complete

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use the Run button in Android Studio

### Configuration

The app scrapes content from `https://footballia.eu`. The base URL can be modified in:
```kotlin
// FootballiaScraperService.kt
companion object {
    const val BASE_URL = "https://footballia.eu"
}
```

## 📦 Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose | Modern declarative UI |
| Material 3 | Design system |
| Hilt | Dependency injection |
| Room | Local database |
| Retrofit + OkHttp | Network requests |
| JSoup | HTML parsing |
| Coil | Image loading |
| Media3 ExoPlayer | Video playback |
| Navigation Compose | Screen navigation |
| Kotlinx Serialization | JSON serialization |

## 🎨 Design System

### Colors
- **Primary**: Golden Accent (#D4AF37)
- **Background**: Cinema Black (#0A0A0A)
- **Cards**: Deep Charcoal (#1A1A1A)
- **Accent**: Netflix Red (#E50914)

### Typography
- Display: Bold headlines for heroes
- Title: Semi-bold for section headers
- Body: Regular for descriptions

## 📂 Key Files

| File | Description |
|------|-------------|
| `FootballiaScraperService.kt` | Web scraping logic for match data |
| `FootballRepository.kt` | Data layer orchestration |
| `HomeScreen.kt` | Main browsing interface |
| `PlayerScreen.kt` | Video playback with ExoPlayer |
| `Components.kt` | Reusable UI components |
| `Theme.kt` | App-wide styling |

## 🔧 Customization

### Adding New Content Sources
1. Create a new scraper service in `data/api/`
2. Implement parsing methods for the source
3. Add to repository with caching logic

### Modifying UI Theme
Edit `ui/theme/Theme.kt` to change:
- Color palette
- Typography scale
- Component shapes

## ⚠️ Legal Notice

This app is for **personal/educational use only**. It scrapes publicly available content from Footballia.eu. Users are responsible for ensuring compliance with:
- Footballia.eu terms of service
- Local copyright laws
- Content licensing restrictions

## 🐛 Known Issues

1. **Video playback** - Some embedded videos may require additional handling
2. **Rate limiting** - Excessive requests may be blocked by the source
3. **Content changes** - Web scraping may break if Footballia changes their HTML structure

## 📄 License

This project is provided for educational purposes. The codebase is MIT licensed, but the content accessed through it belongs to their respective copyright holders.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

Built with ❤️ for football fans who love the classics
