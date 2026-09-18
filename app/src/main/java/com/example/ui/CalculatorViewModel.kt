package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.CalculatorEngine
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CalculatorUiState(
  val expression: String = "",
  val currentInput: String = "0",
  val previewResult: String? = null,
  val errorMessage: String? = null,
  val isResultCalculated: Boolean = false
)

/** Small MVVM state holder modelled after standard Compose calculator samples. */
class CalculatorViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  fun onDigit(digit: String) = update { state ->
    val input = if (state.errorMessage != null || state.isResultCalculated) "0" else state.currentInput
    val next = when {
      input == "0" && digit == "00" -> "0"
      input == "0" -> digit
      input.length >= 15 -> input
      else -> input + digit
    }
    state.copy(
      expression = if (state.isResultCalculated) "" else state.expression,
      currentInput = next,
      previewResult = preview(if (state.isResultCalculated) "" else state.expression, next),
      errorMessage = null,
      isResultCalculated = false
    )
  }

  fun onDecimal() = update { state ->
    if (state.errorMessage != null || state.isResultCalculated) {
      CalculatorUiState(currentInput = "0.")
    } else if ('.' !in state.currentInput) {
      val next = state.currentInput + "."
      state.copy(currentInput = next, previewResult = preview(state.expression, next))
    } else state
  }

  fun onOperator(operator: String) = update { state ->
    if (state.errorMessage != null) return@update state
    val value = state.currentInput.replace(",", "")
    val expression = when {
      state.isResultCalculated || state.expression.isEmpty() -> "$value $operator "
      state.currentInput == "0" -> state.expression.trimEnd()
        .dropLastWhile { it in "+−×÷" }.trimEnd() + " $operator "
      else -> state.expression + "$value $operator "
    }
    state.copy(expression = expression, currentInput = "0", previewResult = null, isResultCalculated = false)
  }

  fun onEquals() = update { state ->
    if (state.errorMessage != null || state.expression.isEmpty()) return@update state
    val full = (state.expression + state.currentInput.replace(",", "")).trim()
    try {
      val result = CalculatorEngine.formatResult(CalculatorEngine.evaluate(full))
      state.copy(expression = "$full =", currentInput = result, previewResult = null, isResultCalculated = true)
    } catch (error: ArithmeticException) {
      state.copy(errorMessage = error.message ?: "Cannot divide by 0", previewResult = null)
    } catch (_: IllegalArgumentException) {
      state.copy(errorMessage = "Invalid calculation", previewResult = null)
    }
  }

  fun onClear() = update { state ->
    if (state.currentInput != "0") state.copy(currentInput = "0", previewResult = preview(state.expression, "0"))
    else CalculatorUiState()
  }

  fun onBackspace() = update { state ->
    if (state.isResultCalculated || state.errorMessage != null) CalculatorUiState()
    else {
      val next = state.currentInput.dropLast(1).ifEmpty { "0" }
      state.copy(currentInput = next, previewResult = preview(state.expression, next))
    }
  }

  fun onToggleSign() = update { state ->
    if (state.currentInput == "0") state else {
      val next = if (state.currentInput.startsWith('-')) state.currentInput.drop(1) else "-${state.currentInput}"
      state.copy(currentInput = next, previewResult = preview(state.expression, next))
    }
  }

  fun onPercentage() = update { state ->
    val value = state.currentInput.replace(",", "").toBigDecimalOrNull() ?: return@update state
    val next = value.divide(BigDecimal("100"), 16, RoundingMode.HALF_UP)
      .stripTrailingZeros().toPlainString()
    state.copy(currentInput = next, previewResult = preview(state.expression, next))
  }

  private fun preview(expression: String, input: String): String? {
    if (expression.isBlank()) return null
    return runCatching { CalculatorEngine.formatResult(CalculatorEngine.evaluate(expression + input)) }.getOrNull()
  }

  private fun update(transform: (CalculatorUiState) -> CalculatorUiState) {
    _uiState.value = transform(_uiState.value)
  }
}
