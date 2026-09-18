package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.CalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode

data class CalculatorUiState(
  val expression: String = "",
  val currentInput: String = "0",
  val errorMessage: String? = null,
  val isResultCalculated: Boolean = false
)

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
      errorMessage = null,
      isResultCalculated = false
    )
  }

  fun onDecimal() = update { state ->
    if (state.errorMessage != null || state.isResultCalculated) CalculatorUiState(currentInput = "0.")
    else if (!state.currentInput.contains('.')) state.copy(currentInput = state.currentInput + ".")
    else state
  }

  fun onOperator(operator: String) = update { state ->
    if (state.errorMessage != null) return@update state
    val value = state.currentInput.replace(",", "")
    when {
      state.isResultCalculated -> state.copy(expression = "$value $operator ", currentInput = "0", isResultCalculated = false)
      state.expression.isEmpty() -> state.copy(expression = "$value $operator ", currentInput = "0")
      else -> state.copy(expression = state.expression + "$value $operator ", currentInput = "0")
    }
  }

  fun onEquals() = update { state ->
    if (state.errorMessage != null || state.expression.isEmpty()) return@update state
    val fullExpression = (state.expression + state.currentInput.replace(",", "")).trim()
    try {
      state.copy(
        expression = "$fullExpression =",
        currentInput = CalculatorEngine.formatResult(CalculatorEngine.evaluate(fullExpression)),
        isResultCalculated = true,
        errorMessage = null
      )
    } catch (error: ArithmeticException) {
      state.copy(errorMessage = error.message ?: "Invalid calculation")
    } catch (_: Exception) {
      state.copy(errorMessage = "Invalid calculation")
    }
  }

  fun onClear() = update { state ->
    if (state.currentInput != "0") state.copy(currentInput = "0") else CalculatorUiState()
  }

  fun onBackspace() = update { state ->
    if (state.isResultCalculated || state.errorMessage != null) CalculatorUiState()
    else state.copy(currentInput = state.currentInput.dropLast(1).ifEmpty { "0" })
  }

  fun onToggleSign() = update { state ->
    if (state.currentInput == "0") state else state.copy(
      currentInput = if (state.currentInput.startsWith("-")) state.currentInput.drop(1) else "-${state.currentInput}"
    )
  }

  fun onPercentage() = update { state ->
    val value = state.currentInput.replace(",", "").toBigDecimalOrNull()
      ?: return@update state
    state.copy(currentInput = value.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
      .stripTrailingZeros().toPlainString())
  }

  private fun update(transform: (CalculatorUiState) -> CalculatorUiState) {
    _uiState.value = transform(_uiState.value)
  }
}
