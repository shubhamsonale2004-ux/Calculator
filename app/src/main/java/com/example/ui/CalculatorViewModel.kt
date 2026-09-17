package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistory
import com.example.data.CalculatorDatabase
import com.example.data.CalculatorEngine
import com.example.data.CalculatorRepository
import com.example.data.VoiceMathParser
import android.content.Context
import com.example.ui.theme.ColorTheme
import com.example.ui.theme.DarkModePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CalculatorUiState(
  val expression: String = "",
  val currentInput: String = "0",
  val previewResult: String? = null,
  val isResultCalculated: Boolean = false,
  val errorMessage: String? = null,
  val isHistoryOpen: Boolean = false,
  val showClearHistoryDialog: Boolean = false,
  val isScientificMode: Boolean = false,
  val isDegreeMode: Boolean = true,
  val isConverterOpen: Boolean = false,
  val isSettingsOpen: Boolean = false,
  val selectedTheme: ColorTheme = ColorTheme.SUNSET_AMBER,
  val darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
  val isVoiceInputOpen: Boolean = false,
  val voiceHeardText: String = "",
  val voiceParsedExpression: String = "",
  val isVoiceListening: Boolean = false,
  val voiceErrorMessage: String? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: CalculatorRepository

  private val prefs = application.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

  init {
    val database = CalculatorDatabase.getDatabase(application)
    repository = CalculatorRepository(database.historyDao())
    loadSavedSettings()
  }

  private fun loadSavedSettings() {
    val savedThemeName = prefs.getString("selected_color_theme", ColorTheme.SUNSET_AMBER.name)
    val theme = try {
      ColorTheme.valueOf(savedThemeName ?: ColorTheme.SUNSET_AMBER.name)
    } catch (e: Exception) {
      ColorTheme.SUNSET_AMBER
    }

    val savedModeName = prefs.getString("dark_mode_pref", DarkModePreference.SYSTEM.name)
    val mode = try {
      DarkModePreference.valueOf(savedModeName ?: DarkModePreference.SYSTEM.name)
    } catch (e: Exception) {
      DarkModePreference.SYSTEM
    }

    _uiState.value = _uiState.value.copy(
      selectedTheme = theme,
      darkModePreference = mode
    )
  }

  val historyList: StateFlow<List<CalculationHistory>> = repository.allHistory
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  fun onDigit(digit: String) {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null || state.isResultCalculated) {
        val newInput = if (digit == "00") "0" else digit
        state.copy(
          errorMessage = null,
          isResultCalculated = false,
          expression = "",
          currentInput = newInput,
          previewResult = null
        )
      } else {
        val newInput = when {
          state.currentInput == "0" && digit == "00" -> "0"
          state.currentInput == "0" -> digit
          state.currentInput == "-0" -> "-$digit"
          state.currentInput.length >= 15 -> state.currentInput // Limit input length
          else -> state.currentInput + digit
        }
        val preview = calculatePreview(state.expression, newInput)
        state.copy(
          currentInput = newInput,
          previewResult = preview
        )
      }
    }
  }

  fun onDecimal() {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null || state.isResultCalculated) {
        state.copy(
          errorMessage = null,
          isResultCalculated = false,
          expression = "",
          currentInput = "0.",
          previewResult = null
        )
      } else if (!state.currentInput.contains(".")) {
        val newInput = state.currentInput + "."
        state.copy(
          currentInput = newInput,
          previewResult = calculatePreview(state.expression, newInput)
        )
      } else {
        state
      }
    }
  }

  fun onOperator(op: String) {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null) return@let state

      if (state.isResultCalculated) {
        // Continue from calculated result
        val cleanValue = state.currentInput.replace(",", "")
        state.copy(
          expression = "$cleanValue $op ",
          currentInput = "0",
          isResultCalculated = false,
          previewResult = null
        )
      } else if (state.currentInput == "0" && state.expression.isNotEmpty()) {
        // If user changed their mind about operator (e.g. pressed + then pressed ×)
        val trimmed = state.expression.trimEnd()
        val lastOpIndex = trimmed.lastIndexOfAny(charArrayOf('+', '−', '×', '÷'))
        if (lastOpIndex != -1) {
          val newExpr = trimmed.substring(0, lastOpIndex) + "$op "
          state.copy(expression = newExpr)
        } else {
          state
        }
      } else {
        val cleanInput = state.currentInput.replace(",", "")
        val newExpr = state.expression + "$cleanInput $op "
        state.copy(
          expression = newExpr,
          currentInput = "0",
          previewResult = calculatePreview(newExpr, "0")
        )
      }
    }
  }

  fun onPercentage() {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null) return@let state
      val cleanInput = state.currentInput.replace(",", "")
      try {
        val bd = BigDecimal(cleanInput)
        val percent = bd.divide(BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
        val formatted = percent.toPlainString()
        state.copy(
          currentInput = formatted,
          previewResult = calculatePreview(state.expression, formatted)
        )
      } catch (e: Exception) {
        state
      }
    }
  }

  fun onToggleSign() {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null || state.currentInput == "0") return@let state
      val newInput = if (state.currentInput.startsWith("-")) {
        state.currentInput.substring(1)
      } else {
        "-${state.currentInput}"
      }
      state.copy(
        currentInput = newInput,
        previewResult = calculatePreview(state.expression, newInput)
      )
    }
  }

  fun onBackspace() {
    _uiState.value = _uiState.value.let { state ->
      if (state.errorMessage != null || state.isResultCalculated) {
        state.copy(
          errorMessage = null,
          isResultCalculated = false,
          expression = "",
          currentInput = "0",
          previewResult = null
        )
      } else {
        val newInput = when {
          state.currentInput.length <= 1 -> "0"
          state.currentInput.length == 2 && state.currentInput.startsWith("-") -> "0"
          else -> state.currentInput.dropLast(1)
        }
        state.copy(
          currentInput = newInput,
          previewResult = calculatePreview(state.expression, newInput)
        )
      }
    }
  }

  fun onClear() {
    _uiState.value = _uiState.value.let { state ->
      if (state.currentInput != "0") {
        state.copy(
          currentInput = "0",
          previewResult = calculatePreview(state.expression, "0")
        )
      } else {
        // If currentInput is already 0, clear full expression (All Clear)
        state.copy(
          expression = "",
          currentInput = "0",
          previewResult = null,
          errorMessage = null,
          isResultCalculated = false
        )
      }
    }
  }

  fun onAllClear() {
    _uiState.value = CalculatorUiState(
      isHistoryOpen = _uiState.value.isHistoryOpen
    )
  }

  fun onEquals() {
    val state = _uiState.value
    if (state.errorMessage != null) return

    val cleanInput = state.currentInput.replace(",", "")
    val fullExpression = (state.expression + cleanInput).trim()
    if (fullExpression.isEmpty() || fullExpression == cleanInput && state.expression.isEmpty()) {
      return
    }

    try {
      val result = CalculatorEngine.evaluate(fullExpression)
      val formattedResult = CalculatorEngine.formatResult(result)

      viewModelScope.launch {
        repository.addCalculation(
          expression = fullExpression,
          result = formattedResult
        )
      }

      _uiState.value = state.copy(
        expression = "$fullExpression =",
        currentInput = formattedResult,
        previewResult = null,
        isResultCalculated = true,
        errorMessage = null
      )
    } catch (e: ArithmeticException) {
      _uiState.value = state.copy(
        errorMessage = "Cannot divide by 0",
        previewResult = null
      )
    } catch (e: Exception) {
      _uiState.value = state.copy(
        errorMessage = "Error",
        previewResult = null
      )
    }
  }

  private fun calculatePreview(expression: String, currentInput: String): String? {
    if (expression.isEmpty()) return null
    val cleanInput = currentInput.replace(",", "")
    val full = (expression + cleanInput).trim()
    if (full.endsWith("+") || full.endsWith("−") || full.endsWith("×") || full.endsWith("÷") || full.endsWith("^")) {
      return null
    }
    return try {
      val result = CalculatorEngine.evaluate(full)
      CalculatorEngine.formatResult(result)
    } catch (e: Exception) {
      null
    }
  }

  fun toggleScientificMode() {
    _uiState.value = _uiState.value.copy(
      isScientificMode = !_uiState.value.isScientificMode
    )
  }

  fun toggleAngleMode() {
    _uiState.value = _uiState.value.copy(
      isDegreeMode = !_uiState.value.isDegreeMode
    )
  }

  fun onScientificUnary(op: String) {
    val state = _uiState.value
    if (state.errorMessage != null) return

    val cleanInput = state.currentInput.replace(",", "")
    try {
      val inputVal = cleanInput.toDouble()
      val (resultVal, displayExpr) = when (op) {
        "√" -> {
          if (inputVal < 0) throw ArithmeticException("Invalid input")
          Math.sqrt(inputVal) to "√($cleanInput)"
        }
        "x²" -> {
          (inputVal * inputVal) to "sqr($cleanInput)"
        }
        "sin" -> {
          val rad = if (state.isDegreeMode) Math.toRadians(inputVal) else inputVal
          Math.sin(rad) to "sin($cleanInput)"
        }
        "cos" -> {
          val rad = if (state.isDegreeMode) Math.toRadians(inputVal) else inputVal
          Math.cos(rad) to "cos($cleanInput)"
        }
        "tan" -> {
          if (state.isDegreeMode && Math.abs((inputVal - 90.0) % 180.0) < 1e-9) {
            throw ArithmeticException("Invalid input")
          }
          val rad = if (state.isDegreeMode) Math.toRadians(inputVal) else inputVal
          Math.tan(rad) to "tan($cleanInput)"
        }
        "ln" -> {
          if (inputVal <= 0) throw ArithmeticException("Invalid input")
          Math.log(inputVal) to "ln($cleanInput)"
        }
        "log" -> {
          if (inputVal <= 0) throw ArithmeticException("Invalid input")
          Math.log10(inputVal) to "log($cleanInput)"
        }
        "1/x" -> {
          if (inputVal == 0.0) throw ArithmeticException("Cannot divide by 0")
          (1.0 / inputVal) to "1/($cleanInput)"
        }
        else -> inputVal to cleanInput
      }

      if (resultVal.isNaN() || resultVal.isInfinite()) {
        throw ArithmeticException("Invalid input")
      }

      // Round clean trigonometric zero or near-zero edge cases (e.g. sin(180) or cos(90))
      val cleanedVal = if (Math.abs(resultVal) < 1e-15) 0.0 else resultVal
      val formattedResult = CalculatorEngine.formatResult(BigDecimal(cleanedVal, java.math.MathContext(16, java.math.RoundingMode.HALF_UP)))

      viewModelScope.launch {
        repository.addCalculation(
          expression = displayExpr,
          result = formattedResult
        )
      }

      _uiState.value = state.copy(
        expression = "$displayExpr =",
        currentInput = formattedResult,
        previewResult = null,
        isResultCalculated = true,
        errorMessage = null
      )
    } catch (e: ArithmeticException) {
      _uiState.value = state.copy(
        errorMessage = e.message ?: "Invalid input",
        previewResult = null
      )
    } catch (e: Exception) {
      _uiState.value = state.copy(
        errorMessage = "Invalid input",
        previewResult = null
      )
    }
  }

  fun onConstant(constant: String) {
    val state = _uiState.value
    val valueStr = when (constant) {
      "π" -> "3.141592653589793"
      "e" -> "2.718281828459045"
      else -> return
    }
    val bd = BigDecimal(valueStr).stripTrailingZeros()
    val formatted = CalculatorEngine.formatResult(bd)

    _uiState.value = state.copy(
      currentInput = formatted,
      isResultCalculated = false,
      errorMessage = null,
      previewResult = calculatePreview(state.expression, formatted)
    )
  }

  fun onHistoryItemClick(item: CalculationHistory) {
    val clean = item.result.replace(",", "")
    _uiState.value = _uiState.value.copy(
      expression = "",
      currentInput = clean,
      previewResult = null,
      isResultCalculated = true,
      errorMessage = null,
      isHistoryOpen = false
    )
  }

  fun onHistoryExpressionClick(item: CalculationHistory) {
    _uiState.value = _uiState.value.copy(
      expression = item.expression + " =",
      currentInput = item.result.replace(",", ""),
      previewResult = null,
      isResultCalculated = true,
      errorMessage = null,
      isHistoryOpen = false
    )
  }

  fun onDeleteHistoryItem(id: Long) {
    viewModelScope.launch {
      repository.deleteCalculation(id)
    }
  }

  fun onClearAllHistory() {
    viewModelScope.launch {
      repository.clearHistory()
      _uiState.value = _uiState.value.copy(showClearHistoryDialog = false)
    }
  }

  fun setHistoryOpen(open: Boolean) {
    _uiState.value = _uiState.value.copy(isHistoryOpen = open)
  }

  fun setShowClearHistoryDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showClearHistoryDialog = show)
  }

  fun setConverterOpen(open: Boolean) {
    _uiState.value = _uiState.value.copy(isConverterOpen = open)
  }

  fun setSettingsOpen(open: Boolean) {
    _uiState.value = _uiState.value.copy(isSettingsOpen = open)
  }

  fun onSelectColorTheme(theme: ColorTheme) {
    _uiState.value = _uiState.value.copy(selectedTheme = theme)
    prefs.edit().putString("selected_color_theme", theme.name).apply()
  }

  fun onSelectDarkModePreference(preference: DarkModePreference) {
    _uiState.value = _uiState.value.copy(darkModePreference = preference)
    prefs.edit().putString("dark_mode_pref", preference.name).apply()
  }

  fun onInsertConvertedValue(value: String) {
    val clean = value.replace(",", "")
    _uiState.value = _uiState.value.copy(
      currentInput = clean,
      isResultCalculated = true,
      errorMessage = null,
      previewResult = calculatePreview(_uiState.value.expression, clean)
    )
  }

  fun setVoiceInputOpen(open: Boolean) {
    _uiState.value = _uiState.value.copy(
      isVoiceInputOpen = open,
      voiceHeardText = if (open) "" else _uiState.value.voiceHeardText,
      voiceParsedExpression = if (open) "" else _uiState.value.voiceParsedExpression,
      voiceErrorMessage = null,
      isVoiceListening = false
    )
  }

  fun setVoiceListening(isListening: Boolean) {
    _uiState.value = _uiState.value.copy(isVoiceListening = isListening)
  }

  fun setVoiceError(error: String?) {
    _uiState.value = _uiState.value.copy(
      voiceErrorMessage = error,
      isVoiceListening = false
    )
  }

  fun onVoiceSpeechRecognized(spokenText: String) {
    val parsed = VoiceMathParser.parseSpokenMath(spokenText)
    _uiState.value = _uiState.value.copy(
      voiceHeardText = spokenText,
      voiceParsedExpression = parsed,
      isVoiceListening = false,
      voiceErrorMessage = null
    )
  }

  fun onApplyVoiceCalculation(expressionToCompute: String? = null) {
    val expr = expressionToCompute ?: _uiState.value.voiceParsedExpression
    if (expr.isBlank()) return

    // Close voice sheet
    _uiState.value = _uiState.value.copy(isVoiceInputOpen = false)

    try {
      val result = CalculatorEngine.evaluate(expr)
      val formattedResult = CalculatorEngine.formatResult(result)

      viewModelScope.launch {
        repository.addCalculation(
          expression = expr,
          result = formattedResult
        )
      }

      _uiState.value = _uiState.value.copy(
        expression = "$expr =",
        currentInput = formattedResult,
        previewResult = null,
        isResultCalculated = true,
        errorMessage = null
      )
    } catch (e: ArithmeticException) {
      _uiState.value = _uiState.value.copy(
        expression = "$expr =",
        errorMessage = "Cannot divide by 0",
        previewResult = null
      )
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        expression = "$expr =",
        errorMessage = "Invalid format",
        previewResult = null
      )
    }
  }
}
