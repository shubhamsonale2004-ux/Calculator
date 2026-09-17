package com.example

import com.example.data.ConversionUnit
import com.example.data.UnitCategory
import com.example.data.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterEngineTest {

  @Test
  fun testLengthConversion() {
    val meters = ConversionUnit("m", "Meters", UnitCategory.LENGTH, 1.0)
    val kilometers = ConversionUnit("km", "Kilometers", UnitCategory.LENGTH, 1000.0)
    val feet = ConversionUnit("ft", "Feet", UnitCategory.LENGTH, 0.3048)

    // 1 km = 1000 m
    val result1 = UnitConverterEngine.convert(1.0, kilometers, meters)
    assertEquals(1000.0, result1, 0.0001)

    // 1 m = ~3.28084 ft
    val result2 = UnitConverterEngine.convert(1.0, meters, feet)
    assertEquals(3.28084, result2, 0.001)
  }

  @Test
  fun testWeightConversion() {
    val kg = ConversionUnit("kg", "Kilograms", UnitCategory.WEIGHT, 1.0)
    val g = ConversionUnit("g", "Grams", UnitCategory.WEIGHT, 0.001)
    val lb = ConversionUnit("lb", "Pounds", UnitCategory.WEIGHT, 0.45359237)

    // 1 kg = 1000 g
    val result1 = UnitConverterEngine.convert(1.0, kg, g)
    assertEquals(1000.0, result1, 0.0001)

    // 1 kg = ~2.20462 lb
    val result2 = UnitConverterEngine.convert(1.0, kg, lb)
    assertEquals(2.20462, result2, 0.001)
  }

  @Test
  fun testTemperatureConversion() {
    val c = ConversionUnit("°C", "Celsius", UnitCategory.TEMPERATURE)
    val f = ConversionUnit("°F", "Fahrenheit", UnitCategory.TEMPERATURE)
    val k = ConversionUnit("K", "Kelvin", UnitCategory.TEMPERATURE)

    // 0 °C = 32 °F
    val fFrom0C = UnitConverterEngine.convert(0.0, c, f)
    assertEquals(32.0, fFrom0C, 0.0001)

    // 100 °C = 212 °F
    val fFrom100C = UnitConverterEngine.convert(100.0, c, f)
    assertEquals(212.0, fFrom100C, 0.0001)

    // 0 °C = 273.15 K
    val kFrom0C = UnitConverterEngine.convert(0.0, c, k)
    assertEquals(273.15, kFrom0C, 0.0001)

    // 32 °F = 0 °C
    val cFrom32F = UnitConverterEngine.convert(32.0, f, c)
    assertEquals(0.0, cFrom32F, 0.0001)
  }
}
