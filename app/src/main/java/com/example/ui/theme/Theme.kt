package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DkkEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = DkkEmerald,
    onPrimaryContainer = Color.White,
    secondary = DkkGold,
    onSecondary = Color.Black,
    secondaryContainer = DkkGoldLight.copy(alpha = 0.2f),
    tertiary = EasypaisaGreen,
    background = DkkBackgroundDark,
    surface = DkkNavy,
    surfaceVariant = DkkSlate,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DkkEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6F4EA),
    onPrimaryContainer = DkkEmerald,
    secondary = DkkGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = EasypaisaGreen,
    background = LightSurface,
    surface = LightSurfaceCard,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disabled by default to show authentic DKK green & gold theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
