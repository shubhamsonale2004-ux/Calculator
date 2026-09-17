package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

fun buildColorScheme(colorTheme: ColorTheme, isDark: Boolean) = if (isDark) {
  darkColorScheme(
    primary = colorTheme.primaryDark,
    onPrimary = colorTheme.onPrimaryDark,
    primaryContainer = colorTheme.primaryContainerDark,
    onPrimaryContainer = colorTheme.onPrimaryContainerDark,
    secondary = colorTheme.secondaryDark,
    secondaryContainer = colorTheme.secondaryContainerDark,
    onSecondaryContainer = colorTheme.onSecondaryContainerDark,
    surface = colorTheme.surfaceDark,
    onSurface = colorTheme.onSurfaceDark,
    surfaceVariant = colorTheme.surfaceVariantDark,
    onSurfaceVariant = colorTheme.onSurfaceVariantDark,
    background = colorTheme.surfaceDark,
    onBackground = colorTheme.onSurfaceDark
  )
} else {
  lightColorScheme(
    primary = colorTheme.primaryLight,
    onPrimary = colorTheme.onPrimaryLight,
    primaryContainer = colorTheme.primaryContainerLight,
    onPrimaryContainer = colorTheme.onPrimaryContainerLight,
    secondary = colorTheme.secondaryLight,
    secondaryContainer = colorTheme.secondaryContainerLight,
    onSecondaryContainer = colorTheme.onSecondaryContainerLight,
    surface = colorTheme.surfaceLight,
    onSurface = colorTheme.onSurfaceLight,
    surfaceVariant = colorTheme.surfaceVariantLight,
    onSurfaceVariant = colorTheme.onSurfaceVariantLight,
    background = colorTheme.surfaceLight,
    onBackground = colorTheme.onSurfaceLight
  )
}

@Composable
fun MyApplicationTheme(
  colorTheme: ColorTheme = ColorTheme.SUNSET_AMBER,
  darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemInDark = isSystemInDarkTheme()
  val darkTheme = when (darkModePreference) {
    DarkModePreference.SYSTEM -> systemInDark
    DarkModePreference.DARK -> true
    DarkModePreference.LIGHT -> false
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    else -> buildColorScheme(colorTheme, darkTheme)
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


