package com.example.data

import java.math.BigDecimal
import java.math.RoundingMode

enum class UnitCategory(val displayName: String) {
  LENGTH("Length"),
  WEIGHT("Weight"),
  TEMPERATURE("Temperature")
}

data class ConversionUnit(
  val symbol: String,
  val name: String,
  val category: UnitCategory,
  // Factor relative to base unit (Meters for Length, Kilograms for Weight)
  val toBaseFactor: Double = 1.0
)

object UnitConverterEngine {

  val lengthUnits = listOf(
    ConversionUnit("m", "Meters", UnitCategory.LENGTH, 1.0),
    ConversionUnit("km", "Kilometers", UnitCategory.LENGTH, 1000.0),
    ConversionUnit("cm", "Centimeters", UnitCategory.LENGTH, 0.01),
    ConversionUnit("mm", "Millimeters", UnitCategory.LENGTH, 0.001),
    ConversionUnit("mi", "Miles", UnitCategory.LENGTH, 1609.344),
    ConversionUnit("yd", "Yards", UnitCategory.LENGTH, 0.9144),
    ConversionUnit("ft", "Feet", UnitCategory.LENGTH, 0.3048),
    ConversionUnit("in", "Inches", UnitCategory.LENGTH, 0.0254)
  )

  val weightUnits = listOf(
    ConversionUnit("kg", "Kilograms", UnitCategory.WEIGHT, 1.0),
    ConversionUnit("g", "Grams", UnitCategory.WEIGHT, 0.001),
    ConversionUnit("mg", "Milligrams", UnitCategory.WEIGHT, 0.000001),
    ConversionUnit("t", "Metric Tons", UnitCategory.WEIGHT, 1000.0),
    ConversionUnit("lb", "Pounds", UnitCategory.WEIGHT, 0.45359237),
    ConversionUnit("oz", "Ounces", UnitCategory.WEIGHT, 0.028349523125)
  )

  val tempUnits = listOf(
    ConversionUnit("°C", "Celsius", UnitCategory.TEMPERATURE),
    ConversionUnit("°F", "Fahrenheit", UnitCategory.TEMPERATURE),
    ConversionUnit("K", "Kelvin", UnitCategory.TEMPERATURE)
  )

  fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
    return when (category) {
      UnitCategory.LENGTH -> lengthUnits
      UnitCategory.WEIGHT -> weightUnits
      UnitCategory.TEMPERATURE -> tempUnits
    }
  }

  fun convert(
    value: Double,
    fromUnit: ConversionUnit,
    toUnit: ConversionUnit
  ): Double {
    if (fromUnit == toUnit) return value

    return when (fromUnit.category) {
      UnitCategory.LENGTH, UnitCategory.WEIGHT -> {
        val baseVal = value * fromUnit.toBaseFactor
        baseVal / toUnit.toBaseFactor
      }
      UnitCategory.TEMPERATURE -> {
        // First convert to Celsius
        val celsius = when (fromUnit.symbol) {
          "°C" -> value
          "°F" -> (value - 32.0) * 5.0 / 9.0
          "K" -> value - 273.15
          else -> value
        }
        // Then convert Celsius to target
        when (toUnit.symbol) {
          "°C" -> celsius
          "°F" -> (celsius * 9.0 / 5.0) + 32.0
          "K" -> celsius + 273.15
          else -> celsius
        }
      }
    }
  }

  fun formatValue(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return try {
      val bd = BigDecimal(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
      bd.toPlainString()
    } catch (e: Exception) {
      String.format("%.4f", value).trimEnd('0').trimEnd('.')
    }
  }
}
