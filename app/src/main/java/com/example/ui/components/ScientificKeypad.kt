package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScientificKeypad(
  visible: Boolean,
  isDegreeMode: Boolean,
  onToggleAngleMode: () -> Unit,
  onScientificUnary: (String) -> Unit,
  onConstant: (String) -> Unit,
  onOperator: (String) -> Unit,
  modifier: Modifier = Modifier,
  isLandscape: Boolean = false
) {
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = if (isLandscape) 0.dp else 6.dp),
      verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 4.dp)
    ) {
      // Row 1: Deg/Rad chip, sin, cos, tan, π
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Degree / Radian toggle button
        CalculatorButton(
          text = if (isDegreeMode) "DEG" else "RAD",
          type = ButtonType.ACTION,
          onClick = onToggleAngleMode,
          modifier = Modifier.weight(1f),
          description = if (isDegreeMode) "Degree mode active" else "Radian mode active",
          testTagKey = "toggle_angle_mode",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "sin",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("sin") },
          modifier = Modifier.weight(1f),
          description = "Sine function",
          testTagKey = "sin",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "cos",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("cos") },
          modifier = Modifier.weight(1f),
          description = "Cosine function",
          testTagKey = "cos",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "tan",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("tan") },
          modifier = Modifier.weight(1f),
          description = "Tangent function",
          testTagKey = "tan",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "π",
          type = ButtonType.ACTION,
          onClick = { onConstant("π") },
          modifier = Modifier.weight(1f),
          description = "Pi constant",
          testTagKey = "pi",
          isLandscape = isLandscape
        )
      }

      // Row 2: √, x², xʸ (^), ln, e
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CalculatorButton(
          text = "√",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("√") },
          modifier = Modifier.weight(1f),
          description = "Square root",
          testTagKey = "sqrt",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "x²",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("x²") },
          modifier = Modifier.weight(1f),
          description = "Square",
          testTagKey = "square",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "xʸ",
          type = ButtonType.OPERATOR,
          onClick = { onOperator("^") },
          modifier = Modifier.weight(1f),
          description = "Exponentiation",
          testTagKey = "power",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "ln",
          type = ButtonType.ACTION,
          onClick = { onScientificUnary("ln") },
          modifier = Modifier.weight(1f),
          description = "Natural logarithm",
          testTagKey = "ln",
          isLandscape = isLandscape
        )
        CalculatorButton(
          text = "e",
          type = ButtonType.ACTION,
          onClick = { onConstant("e") },
          modifier = Modifier.weight(1f),
          description = "Euler number constant",
          testTagKey = "euler",
          isLandscape = isLandscape
        )
      }
    }
  }
}
