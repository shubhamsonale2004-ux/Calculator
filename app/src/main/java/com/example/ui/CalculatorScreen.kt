package com.example.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CalculatorEngine
import com.example.ui.components.ButtonType
import com.example.ui.components.CalculatorButton
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ScientificKeypad
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.UnitConverterBottomSheet
import com.example.ui.components.VoiceInputBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val historyList by viewModel.historyList.collectAsStateWithLifecycle()

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  val converterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val copiedMessage = stringResource(R.string.copied_to_calculator)

  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      if (isLandscape) {
        // Landscape Mode: Left Pane (Top bar + Display), Right Pane (Scientific Keypad + Keypad Grid)
        Row(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Left Column: Header controls and full-height Display
          Column(
            modifier = Modifier
              .weight(0.95f)
              .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            // Header Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Scientific Mode Toggle Switch
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      if (uiState.isScientificMode)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                      else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = stringResource(R.string.scientific_mode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (uiState.isScientificMode) FontWeight.Bold else FontWeight.Normal,
                    color = if (uiState.isScientificMode)
                      MaterialTheme.colorScheme.primary
                    else
                      MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Switch(
                    checked = uiState.isScientificMode,
                    onCheckedChange = { viewModel.toggleScientificMode() },
                    modifier = Modifier.testTag("switch_scientific_mode"),
                    thumbContent = {
                      Icon(
                        imageVector = if (uiState.isScientificMode) Icons.Default.Science else Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                      )
                    }
                  )
                }

                // Voice Input button
                IconButton(
                  onClick = { viewModel.setVoiceInputOpen(true) },
                  modifier = Modifier.testTag("btn_open_voice")
                ) {
                  Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.voice_input_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                  )
                }

                // Settings button
                IconButton(
                  onClick = { viewModel.setSettingsOpen(true) },
                  modifier = Modifier.testTag("btn_open_settings")
                ) {
                  Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                  )
                }

                // Unit Converter button
                IconButton(
                  onClick = { viewModel.setConverterOpen(true) },
                  modifier = Modifier.testTag("btn_open_converter")
                ) {
                  Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = stringResource(R.string.unit_converter_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                  )
                }

                // History button
                IconButton(
                  onClick = { viewModel.setHistoryOpen(true) },
                  modifier = Modifier.testTag("btn_open_history")
                ) {
                  BadgedBox(
                    badge = {
                      if (historyList.isNotEmpty()) {
                        Badge {
                          Text(
                            text = "${historyList.size}",
                            fontSize = 10.sp
                          )
                        }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Default.History,
                      contentDescription = stringResource(R.string.history_title),
                      tint = MaterialTheme.colorScheme.onBackground,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display taking remaining vertical height on left side
            CalculatorDisplay(
              expression = uiState.expression,
              currentInput = uiState.currentInput,
              previewResult = uiState.previewResult,
              errorMessage = uiState.errorMessage,
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              isLandscape = true
            )
          }

          // Right Column: Keypads
          Column(
            modifier = Modifier
              .weight(1.05f)
              .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
          ) {
            // Scientific Keypad (when enabled)
            ScientificKeypad(
              visible = uiState.isScientificMode,
              isDegreeMode = uiState.isDegreeMode,
              onToggleAngleMode = viewModel::toggleAngleMode,
              onScientificUnary = viewModel::onScientificUnary,
              onConstant = viewModel::onConstant,
              onOperator = viewModel::onOperator,
              modifier = Modifier.fillMaxWidth(),
              isLandscape = true
            )

            // Keypad Grid
            KeypadGrid(
              currentInput = uiState.currentInput,
              onDigit = viewModel::onDigit,
              onDecimal = viewModel::onDecimal,
              onOperator = viewModel::onOperator,
              onPercentage = viewModel::onPercentage,
              onToggleSign = viewModel::onToggleSign,
              onBackspace = viewModel::onBackspace,
              onClear = viewModel::onClear,
              onAllClear = viewModel::onAllClear,
              onEquals = viewModel::onEquals,
              modifier = Modifier.fillMaxWidth(),
              isLandscape = true
            )
          }
        }
      } else {
        // Portrait Mode: Vertical Column
        Column(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 500.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // Top Bar with App Title, Scientific Mode Toggle, and History Button
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Scientific Mode Toggle Switch with icon
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(
                    if (uiState.isScientificMode)
                      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                  )
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = stringResource(R.string.scientific_mode),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (uiState.isScientificMode) FontWeight.Bold else FontWeight.Normal,
                  color = if (uiState.isScientificMode)
                    MaterialTheme.colorScheme.primary
                  else
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                  checked = uiState.isScientificMode,
                  onCheckedChange = { viewModel.toggleScientificMode() },
                  modifier = Modifier
                    .testTag("switch_scientific_mode"),
                  thumbContent = {
                    Icon(
                      imageVector = if (uiState.isScientificMode) Icons.Default.Science else Icons.Default.Calculate,
                      contentDescription = null,
                      modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                  }
                )
              }

              // Voice Input button
              IconButton(
                onClick = { viewModel.setVoiceInputOpen(true) },
                modifier = Modifier.testTag("btn_open_voice")
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = stringResource(R.string.voice_input_title),
                  tint = MaterialTheme.colorScheme.onBackground,
                  modifier = Modifier.size(26.dp)
                )
              }

              // Settings button
              IconButton(
                onClick = { viewModel.setSettingsOpen(true) },
                modifier = Modifier.testTag("btn_open_settings")
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = stringResource(R.string.settings_title),
                  tint = MaterialTheme.colorScheme.onBackground,
                  modifier = Modifier.size(26.dp)
                )
              }

              // Unit Converter button
              IconButton(
                onClick = { viewModel.setConverterOpen(true) },
                modifier = Modifier.testTag("btn_open_converter")
              ) {
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = stringResource(R.string.unit_converter_title),
                  tint = MaterialTheme.colorScheme.onBackground,
                  modifier = Modifier.size(26.dp)
                )
              }

              IconButton(
                onClick = { viewModel.setHistoryOpen(true) },
                modifier = Modifier.testTag("btn_open_history")
              ) {
                BadgedBox(
                  badge = {
                    if (historyList.isNotEmpty()) {
                      Badge {
                        Text(
                          text = "${historyList.size}",
                          fontSize = 10.sp
                        )
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(R.string.history_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                  )
                }
              }
            }
          }

          // Display Screen Area
          CalculatorDisplay(
            expression = uiState.expression,
            currentInput = uiState.currentInput,
            previewResult = uiState.previewResult,
            errorMessage = uiState.errorMessage,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            isLandscape = false
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Scientific Keypad (when toggled on)
          ScientificKeypad(
            visible = uiState.isScientificMode,
            isDegreeMode = uiState.isDegreeMode,
            onToggleAngleMode = viewModel::toggleAngleMode,
            onScientificUnary = viewModel::onScientificUnary,
            onConstant = viewModel::onConstant,
            onOperator = viewModel::onOperator,
            modifier = Modifier.fillMaxWidth(),
            isLandscape = false
          )

          // Keypad Grid
          KeypadGrid(
            currentInput = uiState.currentInput,
            onDigit = viewModel::onDigit,
            onDecimal = viewModel::onDecimal,
            onOperator = viewModel::onOperator,
            onPercentage = viewModel::onPercentage,
            onToggleSign = viewModel::onToggleSign,
            onBackspace = viewModel::onBackspace,
            onClear = viewModel::onClear,
            onAllClear = viewModel::onAllClear,
            onEquals = viewModel::onEquals,
            modifier = Modifier.fillMaxWidth(),
            isLandscape = false
          )
        }
      }
    }
  }

  // History Bottom Sheet
  if (uiState.isHistoryOpen) {
    HistoryBottomSheet(
      sheetState = sheetState,
      historyList = historyList,
      showClearConfirmDialog = uiState.showClearHistoryDialog,
      onDismiss = { viewModel.setHistoryOpen(false) },
      onItemClick = { item ->
        viewModel.onHistoryItemClick(item)
        scope.launch {
          snackbarHostState.showSnackbar(copiedMessage)
        }
      },
      onDeleteItem = viewModel::onDeleteHistoryItem,
      onClearAll = viewModel::onClearAllHistory,
      onShowClearConfirm = viewModel::setShowClearHistoryDialog
    )
  }

  // Unit Converter Bottom Sheet
  if (uiState.isConverterOpen) {
    UnitConverterBottomSheet(
      sheetState = converterSheetState,
      initialValue = uiState.currentInput,
      onDismiss = { viewModel.setConverterOpen(false) },
      onInsertIntoCalculator = { convertedVal ->
        viewModel.onInsertConvertedValue(convertedVal)
        scope.launch {
          snackbarHostState.showSnackbar(copiedMessage)
        }
      }
    )
  }

  // Settings / Theme Selection Bottom Sheet
  if (uiState.isSettingsOpen) {
    SettingsBottomSheet(
      sheetState = settingsSheetState,
      currentTheme = uiState.selectedTheme,
      currentDarkMode = uiState.darkModePreference,
      onSelectTheme = viewModel::onSelectColorTheme,
      onSelectDarkMode = viewModel::onSelectDarkModePreference,
      onDismiss = { viewModel.setSettingsOpen(false) }
    )
  }

  // Voice Input Bottom Sheet
  if (uiState.isVoiceInputOpen) {
    VoiceInputBottomSheet(
      sheetState = voiceSheetState,
      isListening = uiState.isVoiceListening,
      heardText = uiState.voiceHeardText,
      parsedExpression = uiState.voiceParsedExpression,
      errorMessage = uiState.voiceErrorMessage,
      onStartListening = { viewModel.setVoiceListening(true) },
      onStopListening = { viewModel.setVoiceListening(false) },
      onSpeechResult = viewModel::onVoiceSpeechRecognized,
      onError = viewModel::setVoiceError,
      onCalculate = { viewModel.onApplyVoiceCalculation() },
      onDismiss = { viewModel.setVoiceInputOpen(false) }
    )
  }
}

@Composable
fun CalculatorDisplay(
  expression: String,
  currentInput: String,
  previewResult: String?,
  errorMessage: String?,
  modifier: Modifier = Modifier,
  isLandscape: Boolean = false
) {
  val scrollState = rememberScrollState()
  val formattedInput = remember(currentInput) {
    CalculatorEngine.formatInputNumber(currentInput)
  }

  // Dynamic font sizing for long numbers
  val displayText = errorMessage ?: formattedInput
  val fontSize = when {
    isLandscape && displayText.length > 13 -> 24.sp
    isLandscape && displayText.length > 9 -> 30.sp
    isLandscape -> 38.sp
    displayText.length > 13 -> 34.sp
    displayText.length > 9 -> 44.sp
    else -> 56.sp
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(if (isLandscape) 18.dp else 24.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
      .padding(
        horizontal = if (isLandscape) 16.dp else 20.dp,
        vertical = if (isLandscape) 10.dp else 16.dp
      ),
    contentAlignment = Alignment.BottomEnd
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.Bottom
    ) {
      // Expression running string (e.g. "124 + 58 × ")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState, reverseScrolling = true),
        horizontalArrangement = Arrangement.End
      ) {
        Text(
          text = expression,
          style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          fontFamily = FontFamily.Monospace,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("display_expression")
        )
      }

      Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

      // Main Input / Result Display
      AnimatedContent(
        targetState = displayText,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "DisplayResultAnimation"
      ) { targetText ->
        Text(
          text = targetText,
          fontSize = fontSize,
          fontWeight = FontWeight.Bold,
          color = if (errorMessage != null) {
            MaterialTheme.colorScheme.error
          } else {
            MaterialTheme.colorScheme.onSurface
          },
          fontFamily = FontFamily.Monospace,
          maxLines = 1,
          textAlign = TextAlign.End,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("display_result")
        )
      }

      // Live Evaluation Preview (before user taps '=')
      if (previewResult != null && errorMessage == null) {
        Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 4.dp))
        Text(
          text = "= $previewResult",
          style = if (isLandscape) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
          fontFamily = FontFamily.Monospace,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("display_preview")
        )
      }
    }
  }
}

@Composable
fun KeypadGrid(
  currentInput: String,
  onDigit: (String) -> Unit,
  onDecimal: () -> Unit,
  onOperator: (String) -> Unit,
  onPercentage: () -> Unit,
  onToggleSign: () -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onAllClear: () -> Unit,
  onEquals: () -> Unit,
  modifier: Modifier = Modifier,
  isLandscape: Boolean = false
) {
  val clearButtonText = if (currentInput == "0") "AC" else "C"
  val spacing = if (isLandscape) 2.dp else 4.dp

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(spacing)
  ) {
    // Row 1: AC/C, ±, %, ÷
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      CalculatorButton(
        text = clearButtonText,
        type = ButtonType.ACTION,
        onClick = { if (clearButtonText == "AC") onAllClear() else onClear() },
        modifier = Modifier.weight(1f),
        description = if (clearButtonText == "AC") "All Clear" else "Clear",
        testTagKey = "clear",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "±",
        type = ButtonType.ACTION,
        onClick = onToggleSign,
        modifier = Modifier.weight(1f),
        description = "Plus minus",
        testTagKey = "plus_minus",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "%",
        type = ButtonType.ACTION,
        onClick = onPercentage,
        modifier = Modifier.weight(1f),
        description = "Percent",
        testTagKey = "percent",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "÷",
        type = ButtonType.OPERATOR,
        onClick = { onOperator("÷") },
        modifier = Modifier.weight(1f),
        description = "Divide",
        testTagKey = "divide",
        isLandscape = isLandscape
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      CalculatorButton(
        text = "7",
        type = ButtonType.NUMBER,
        onClick = { onDigit("7") },
        modifier = Modifier.weight(1f),
        testTagKey = "7",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "8",
        type = ButtonType.NUMBER,
        onClick = { onDigit("8") },
        modifier = Modifier.weight(1f),
        testTagKey = "8",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "9",
        type = ButtonType.NUMBER,
        onClick = { onDigit("9") },
        modifier = Modifier.weight(1f),
        testTagKey = "9",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "×",
        type = ButtonType.OPERATOR,
        onClick = { onOperator("×") },
        modifier = Modifier.weight(1f),
        description = "Multiply",
        testTagKey = "multiply",
        isLandscape = isLandscape
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      CalculatorButton(
        text = "4",
        type = ButtonType.NUMBER,
        onClick = { onDigit("4") },
        modifier = Modifier.weight(1f),
        testTagKey = "4",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "5",
        type = ButtonType.NUMBER,
        onClick = { onDigit("5") },
        modifier = Modifier.weight(1f),
        testTagKey = "5",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "6",
        type = ButtonType.NUMBER,
        onClick = { onDigit("6") },
        modifier = Modifier.weight(1f),
        testTagKey = "6",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "−",
        type = ButtonType.OPERATOR,
        onClick = { onOperator("−") },
        modifier = Modifier.weight(1f),
        description = "Subtract",
        testTagKey = "subtract",
        isLandscape = isLandscape
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      CalculatorButton(
        text = "1",
        type = ButtonType.NUMBER,
        onClick = { onDigit("1") },
        modifier = Modifier.weight(1f),
        testTagKey = "1",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "2",
        type = ButtonType.NUMBER,
        onClick = { onDigit("2") },
        modifier = Modifier.weight(1f),
        testTagKey = "2",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "3",
        type = ButtonType.NUMBER,
        onClick = { onDigit("3") },
        modifier = Modifier.weight(1f),
        testTagKey = "3",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "+",
        type = ButtonType.OPERATOR,
        onClick = { onOperator("+") },
        modifier = Modifier.weight(1f),
        description = "Add",
        testTagKey = "add",
        isLandscape = isLandscape
      )
    }

    // Row 5: 0, ., ⌫, =
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
      CalculatorButton(
        text = "0",
        type = ButtonType.NUMBER,
        onClick = { onDigit("0") },
        modifier = Modifier.weight(1f),
        testTagKey = "0",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = ".",
        type = ButtonType.NUMBER,
        onClick = onDecimal,
        modifier = Modifier.weight(1f),
        description = "Decimal point",
        testTagKey = "decimal",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "⌫",
        type = ButtonType.ACTION,
        onClick = onBackspace,
        modifier = Modifier.weight(1f),
        description = "Backspace",
        testTagKey = "backspace",
        isLandscape = isLandscape
      )
      CalculatorButton(
        text = "=",
        type = ButtonType.EQUALS,
        onClick = onEquals,
        modifier = Modifier.weight(1f),
        description = "Equals",
        testTagKey = "equals",
        isLandscape = isLandscape
      )
    }
  }
}
