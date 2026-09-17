package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ConversionUnit
import com.example.data.UnitCategory
import com.example.data.UnitConverterEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterBottomSheet(
  sheetState: SheetState,
  initialValue: String,
  onDismiss: () -> Unit,
  onInsertIntoCalculator: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }

  // Units per category
  val availableUnits = remember(selectedCategory) {
    UnitConverterEngine.getUnitsForCategory(selectedCategory)
  }

  var fromUnit by remember(selectedCategory) {
    mutableStateOf(availableUnits[0])
  }
  var toUnit by remember(selectedCategory) {
    mutableStateOf(availableUnits.getOrElse(1) { availableUnits[0] })
  }

  // Input text
  val cleanInitial = remember(initialValue) {
    val trimmed = initialValue.replace(",", "").trim()
    if (trimmed.isEmpty() || trimmed == "0" || trimmed.toDoubleOrNull() == null) "1" else trimmed
  }
  var inputValueStr by remember { mutableStateOf(cleanInitial) }

  // Converted value
  val convertedResult = remember(inputValueStr, fromUnit, toUnit) {
    val doubleVal = inputValueStr.toDoubleOrNull()
    if (doubleVal != null) {
      val res = UnitConverterEngine.convert(doubleVal, fromUnit, toUnit)
      UnitConverterEngine.formatValue(res)
    } else {
      "0"
    }
  }

  val focusManager = LocalFocusManager.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("unit_converter_bottom_sheet")
  ) {
    val scrollState = rememberScrollState()

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
        Column {
          Text(
            text = stringResource(R.string.unit_converter_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = stringResource(R.string.unit_converter_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("btn_close_converter")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category selector chips (Length, Weight, Temperature)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        UnitCategory.entries.forEach { category ->
          val isSelected = category == selectedCategory
          val icon: ImageVector = when (category) {
            UnitCategory.LENGTH -> Icons.Default.Height
            UnitCategory.WEIGHT -> Icons.Default.Scale
            UnitCategory.TEMPERATURE -> Icons.Default.Thermostat
          }
          FilterChip(
            selected = isSelected,
            onClick = {
              if (selectedCategory != category) {
                selectedCategory = category
                val newUnits = UnitConverterEngine.getUnitsForCategory(category)
                fromUnit = newUnits[0]
                toUnit = newUnits.getOrElse(1) { newUnits[0] }
              }
            },
            label = {
              Text(
                text = category.displayName,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            leadingIcon = {
              Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.weight(1f).testTag("chip_category_${category.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Conversion Cards
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // From Row
          Text(
            text = stringResource(R.string.convert_from),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = inputValueStr,
              onValueChange = { newVal ->
                if (newVal.isEmpty() || newVal == "-" || newVal.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                  inputValueStr = newVal
                }
              },
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
              ),
              singleLine = true,
              textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("input_converter_value")
            )

            UnitDropdownSelector(
              currentUnit = fromUnit,
              availableUnits = availableUnits,
              onSelectUnit = { fromUnit = it },
              modifier = Modifier.width(140.dp),
              testTag = "dropdown_from_unit"
            )
          }

          // Swap Button in between
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = {
                val temp = fromUnit
                fromUnit = toUnit
                toUnit = temp
              },
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .testTag("btn_swap_units")
            ) {
              Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = stringResource(R.string.swap_units),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          // To Row
          Text(
            text = stringResource(R.string.convert_to),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
              ),
              modifier = Modifier
                .weight(1f)
                .height(56.dp)
            ) {
              Box(
                modifier = Modifier
                  .padding(horizontal = 14.dp)
                  .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
              ) {
                Text(
                  text = convertedResult,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag("text_converted_result")
                )
              }
            }

            UnitDropdownSelector(
              currentUnit = toUnit,
              availableUnits = availableUnits,
              onSelectUnit = { toUnit = it },
              modifier = Modifier.width(140.dp),
              testTag = "dropdown_to_unit"
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Insert result into Calculator button
      Button(
        onClick = {
          onInsertIntoCalculator(convertedResult)
          onDismiss()
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_insert_into_calculator")
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.use_in_calculator),
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
fun UnitDropdownSelector(
  currentUnit: ConversionUnit,
  availableUnits: List<ConversionUnit>,
  onSelectUnit: (ConversionUnit) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHighest,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .clickable { expanded = true }
        .testTag(testTag)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = currentUnit.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = currentUnit.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }

        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.testTag("${testTag}_menu")
    ) {
      availableUnits.forEach { unit ->
        DropdownMenuItem(
          text = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = unit.name,
                fontWeight = if (unit == currentUnit) FontWeight.Bold else FontWeight.Normal
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "(${unit.symbol})",
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          },
          onClick = {
            onSelectUnit(unit)
            expanded = false
          },
          modifier = Modifier.testTag("item_${unit.symbol}")
        )
      }
    }
  }
}
