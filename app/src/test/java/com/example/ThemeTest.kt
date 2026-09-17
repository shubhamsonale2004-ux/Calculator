package com.example

import com.example.ui.theme.ColorTheme
import com.example.ui.theme.DarkModePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeTest {
  @Test
  fun colorThemes_allHaveTitlesAndColors() {
    ColorTheme.entries.forEach { theme ->
      assertNotNull(theme.titleRes)
      assertNotNull(theme.previewPrimary)
      assertNotNull(theme.previewBackground)
      assertNotNull(theme.primaryLight)
      assertNotNull(theme.primaryDark)
    }
    assertEquals(6, ColorTheme.entries.size)
  }

  @Test
  fun darkModePreferences_containsExpectedOptions() {
    assertEquals(3, DarkModePreference.entries.size)
  }
}
