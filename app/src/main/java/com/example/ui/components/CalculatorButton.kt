package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
  NUMBER,
  OPERATOR,
  ACTION,
  EQUALS
}

@Composable
fun CalculatorButton(
  text: String,
  type: ButtonType,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  description: String = text,
  testTagKey: String = text,
  isLandscape: Boolean = false
) {
  val haptic = LocalHapticFeedback.current

  val backgroundColor: Color = when (type) {
    ButtonType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant
    ButtonType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
    ButtonType.ACTION -> MaterialTheme.colorScheme.secondaryContainer
    ButtonType.EQUALS -> MaterialTheme.colorScheme.primary
  }

  val contentColor: Color = when (type) {
    ButtonType.NUMBER -> MaterialTheme.colorScheme.onSurfaceVariant
    ButtonType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
    ButtonType.ACTION -> MaterialTheme.colorScheme.onSecondaryContainer
    ButtonType.EQUALS -> MaterialTheme.colorScheme.onPrimary
  }

  val fontSize = when {
    isLandscape && text.length >= 4 -> 13.sp
    isLandscape && text.length == 3 -> 15.sp
    isLandscape && (type == ButtonType.OPERATOR || type == ButtonType.EQUALS) -> 22.sp
    isLandscape -> 18.sp
    text.length >= 4 -> 15.sp
    text.length == 3 -> 17.sp
    type == ButtonType.OPERATOR -> 28.sp
    type == ButtonType.EQUALS -> 30.sp
    else -> 24.sp
  }

  val fontWeight = when (type) {
    ButtonType.NUMBER -> FontWeight.Normal
    ButtonType.OPERATOR -> FontWeight.SemiBold
    ButtonType.ACTION -> FontWeight.Medium
    ButtonType.EQUALS -> FontWeight.Bold
  }

  val shape = if (isLandscape) RoundedCornerShape(16.dp) else CircleShape

  Surface(
    modifier = modifier
      .padding(if (isLandscape) 2.dp else 4.dp)
      .let { mod ->
        if (isLandscape) mod else mod.aspectRatio(1f)
      }
      .clip(shape)
      .testTag("btn_$testTagKey")
      .semantics {
        contentDescription = description
        role = Role.Button
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = contentColor.copy(alpha = 0.3f))
      ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
      },
    shape = shape,
    color = backgroundColor
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        color = contentColor,
        fontSize = fontSize,
        fontWeight = fontWeight
      )
    }
  }
}
