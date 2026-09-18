package com.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalculatorEngine
import com.example.ui.components.ButtonType
import com.example.ui.components.CalculatorButton

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel, modifier: Modifier = Modifier) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
    Column(
      modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(contentPadding).padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text("Calculator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      CalculatorDisplay(state, Modifier.fillMaxWidth().weight(1f), landscape)
      CalculatorKeypad(viewModel, landscape)
    }
  }
}

@Composable
private fun CalculatorDisplay(state: CalculatorUiState, modifier: Modifier, landscape: Boolean) {
  val display = state.errorMessage ?: CalculatorEngine.formatInputNumber(state.currentInput)
  Box(
    modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)).padding(16.dp),
    contentAlignment = Alignment.BottomEnd
  ) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
      Text(state.expression, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, maxLines = 2)
      if (state.previewResult != null && state.errorMessage == null) {
        Text("= ${state.previewResult}", color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
      }
      Text(display, fontSize = if (landscape) 36.sp else 52.sp, fontWeight = FontWeight.Bold,
        color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
        fontFamily = FontFamily.Monospace, maxLines = 1, textAlign = TextAlign.End, modifier = Modifier.testTag("display_result"))
    }
  }
}

@Composable
private fun CalculatorKeypad(viewModel: CalculatorViewModel, landscape: Boolean) {
  val spacing = if (landscape) 2.dp else 4.dp
  val rows = listOf(listOf("AC", "±", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"), listOf("1", "2", "3", "+"), listOf("0", ".", "⌫", "="))
  Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
    rows.forEach { row ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
        row.forEach { key ->
          val type = when (key) {
            "= " -> ButtonType.EQUALS
            "÷", "×", "−", "+", "=" -> if (key == "=") ButtonType.EQUALS else ButtonType.OPERATOR
            "AC", "±", "%", "⌫" -> ButtonType.ACTION
            else -> ButtonType.NUMBER
          }
          CalculatorButton(key, type, onClick = {
            when (key) {
              "AC" -> viewModel.onClear(); "±" -> viewModel.onToggleSign(); "%" -> viewModel.onPercentage(); "⌫" -> viewModel.onBackspace(); "." -> viewModel.onDecimal(); "=" -> viewModel.onEquals()
              "÷", "×", "−", "+" -> viewModel.onOperator(key)
              else -> viewModel.onDigit(key)
            }
          }, modifier = Modifier.weight(1f), isLandscape = landscape)
        }
      }
    }
  }
}
