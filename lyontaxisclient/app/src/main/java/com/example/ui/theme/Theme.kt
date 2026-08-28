package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AberTealPrimary,
    secondary = AberBlue,
    tertiary = AberYellow,
    background = AberDark,
    surface = AberDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AberTealPrimary,
    secondary = AberBlue,
    tertiary = AberYellow,
    background = AberBackground,
    surface = AberWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AberDark,
    onSurface = AberDark,
    surfaceVariant = AberGrayLight,
    outline = AberBorder,
  )

@Composable
fun AberTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  AberTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

