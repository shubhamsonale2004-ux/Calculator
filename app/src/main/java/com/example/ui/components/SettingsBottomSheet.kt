package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.DarkModePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
  sheetState: SheetState,
  currentTheme: ColorTheme,
  currentDarkMode: DarkModePreference,
  onSelectTheme: (ColorTheme) -> Unit,
  onSelectDarkMode: (DarkModePreference) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("settings_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp)
            )
          }

          Column {
            Text(
              text = stringResource(R.string.settings_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = stringResource(R.string.settings_theme_desc),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_settings")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section 1: Dark Mode Preference Selector
      Text(
        text = stringResource(R.string.settings_dark_mode),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DarkModePreference.entries.forEach { mode ->
          val isSelected = mode == currentDarkMode
          val (labelRes, icon) = when (mode) {
            DarkModePreference.SYSTEM -> Pair(R.string.mode_system, Icons.Default.SettingsBrightness)
            DarkModePreference.DARK -> Pair(R.string.mode_dark, Icons.Default.DarkMode)
            DarkModePreference.LIGHT -> Pair(R.string.mode_light, Icons.Default.LightMode)
          }

          FilterChip(
            selected = isSelected,
            onClick = { onSelectDarkMode(mode) },
            label = {
              Text(
                text = stringResource(labelRes),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            },
            leadingIcon = {
              Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("chip_dark_mode_${mode.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section 2: Color Palette Grid/List
      Text(
        text = stringResource(R.string.settings_color_theme),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(12.dp))

      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        ColorTheme.entries.forEach { theme ->
          val isSelected = theme == currentTheme
          ThemeOptionCard(
            theme = theme,
            isSelected = isSelected,
            onSelect = { onSelectTheme(theme) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun ThemeOptionCard(
  theme: ColorTheme,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  }

  val containerColor = if (isSelected) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onSelect)
      .testTag("theme_card_${theme.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Theme Color Palette Swatches Preview (Primary, Container, Dark Background)
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(theme.previewPrimary)
              .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
          )
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(theme.primaryContainerLight)
              .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
          )
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(theme.previewBackground)
              .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
          )
        }

        Column {
          Text(
            text = stringResource(theme.titleRes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
          )
        }
      }

      if (isSelected) {
        Box(
          modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
