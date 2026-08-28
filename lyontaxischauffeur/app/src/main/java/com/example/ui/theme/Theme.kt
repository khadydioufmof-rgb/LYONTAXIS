package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AberMint,
    onPrimary = Color(0xFF003829),
    primaryContainer = AberMintDark,
    onPrimaryContainer = Color(0xFFE6FFF7),
    secondary = AberTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF94A3B8),
    tertiary = AberGold,
    onTertiary = Color.Black,
    background = AberDarkBg,
    onBackground = TextPrimaryDark,
    surface = AberDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AberDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = AberDarkBorder,
    error = AberRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AberMintDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF0F172A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF334155),
    tertiary = AberGold,
    onTertiary = Color.Black,
    background = AberLightBg,
    onBackground = TextPrimaryLight,
    surface = AberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = TextSecondaryLight,
    outline = AberLightBorder,
    error = AberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light theme as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
