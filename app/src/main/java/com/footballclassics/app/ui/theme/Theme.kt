package com.footballclassics.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.footballclassics.app.R

// ==================== Colors ====================

// Primary brand colors - Deep pitch green with golden accents
val PitchGreen = Color(0xFF0D4D1C)
val DarkPitchGreen = Color(0xFF082E11)
val GoldenAccent = Color(0xFFD4AF37)
val BrightGold = Color(0xFFFFD700)

// Background hierarchy - Cinema-inspired dark palette
val CinemaBlack = Color(0xFF0A0A0A)
val DeepCharcoal = Color(0xFF121212)
val CardDark = Color(0xFF1A1A1A)
val CardElevated = Color(0xFF222222)
val SurfaceVariantDark = Color(0xFF2A2A2A)

// Text colors
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB0B0B0)
val TextTertiary = Color(0xFF707070)

// Semantic colors
val ErrorRed = Color(0xFFCF6679)
val SuccessGreen = Color(0xFF4CAF50)
val WarningAmber = Color(0xFFFFB74D)

// Team colors for dynamic theming
val HomeTeamDefault = Color(0xFF1565C0)
val AwayTeamDefault = Color(0xFFC62828)

// Netflix-inspired accent
val NetflixRed = Color(0xFFE50914)

private val DarkColorScheme = darkColorScheme(
    primary = GoldenAccent,
    onPrimary = CinemaBlack,
    primaryContainer = PitchGreen,
    onPrimaryContainer = TextPrimary,
    secondary = Color(0xFF4A90A4),
    onSecondary = CinemaBlack,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = TextPrimary,
    tertiary = NetflixRed,
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFF5C1010),
    onTertiaryContainer = TextPrimary,
    background = CinemaBlack,
    onBackground = TextPrimary,
    surface = DeepCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    surfaceTint = GoldenAccent,
    error = ErrorRed,
    onError = CinemaBlack,
    errorContainer = Color(0xFF5C1010),
    onErrorContainer = ErrorRed,
    outline = TextTertiary,
    outlineVariant = SurfaceVariantDark
)

// ==================== Typography ====================

// Using system fonts with fallback - in production, add custom fonts
val displayFontFamily = FontFamily.Default
val bodyFontFamily = FontFamily.Default

val AppTypography = Typography(
    // Display styles - for hero sections
    displayLarge = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headlines - for section titles
    headlineLarge = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles - for cards and items
    titleLarge = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Labels
    labelLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ==================== Theme ====================

@Composable
fun FootballClassicsTheme(
    darkTheme: Boolean = true, // Always dark for cinema feel
    dynamicColor: Boolean = false, // Disabled to maintain brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = CinemaBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// ==================== Custom Color Extensions ====================

object FootballColors {
    val background = CinemaBlack
    val cardBackground = CardDark
    val cardElevated = CardElevated
    val accent = GoldenAccent
    val accentBright = BrightGold
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val textTertiary = TextTertiary
    val pitchGreen = PitchGreen
    val red = NetflixRed
    val success = SuccessGreen
    val warning = WarningAmber
    val error = ErrorRed
    
    // Gradient stops
    val heroGradient = listOf(
        Color.Transparent,
        CinemaBlack.copy(alpha = 0.3f),
        CinemaBlack.copy(alpha = 0.7f),
        CinemaBlack
    )
    
    val cardGradient = listOf(
        Color.Transparent,
        CinemaBlack.copy(alpha = 0.8f)
    )
}
