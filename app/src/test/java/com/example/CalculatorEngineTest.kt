package com.example

import com.example.data.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {

  @Test
  fun testBasicAddition() {
    val result = CalculatorEngine.evaluate("2 + 3")
    assertEquals(BigDecimal("5"), result)
  }

  @Test
  fun testBasicSubtraction() {
    val result = CalculatorEngine.evaluate("10 − 4")
    assertEquals(BigDecimal("6"), result)
  }

  @Test
  fun testBasicMultiplication() {
    val result = CalculatorEngine.evaluate("7 × 8")
    assertEquals(BigDecimal("56"), result)
  }

  @Test
  fun testBasicDivision() {
    val result = CalculatorEngine.evaluate("15 ÷ 3")
    assertEquals(BigDecimal("5"), result)
  }

  @Test
  fun testOperatorPrecedence() {
    val result = CalculatorEngine.evaluate("2 + 3 × 4")
    assertEquals(BigDecimal("14"), result)
  }

  @Test
  fun testDecimalPrecision() {
    val result = CalculatorEngine.evaluate("0.1 + 0.2")
    assertEquals(BigDecimal("0.3"), result)
  }

  @Test
  fun testDivisionByZero() {
    assertThrows(ArithmeticException::class.java) {
      CalculatorEngine.evaluate("5 ÷ 0")
    }
  }

  @Test
  fun testNegativeNumber() {
    val result = CalculatorEngine.evaluate("-5 + 8")
    assertEquals(BigDecimal("3"), result)
  }

  @Test
  fun testFormatNumber() {
    val formatted = CalculatorEngine.formatInputNumber("1234567.89")
    assertEquals("1,234,567.89", formatted)
  }

  @Test
  fun testFormatResultTrailingZeroes() {
    val formatted = CalculatorEngine.formatResult(BigDecimal("12.5000"))
    assertEquals("12.5", formatted)
  }

  @Test
  fun testExponentiation() {
    val result = CalculatorEngine.evaluate("2 ^ 3")
    assertEquals(BigDecimal("8"), result)
  }

  @Test
  fun testExponentPrecedence() {
    val result = CalculatorEngine.evaluate("3 + 2 ^ 3 × 2")
    // 2 ^ 3 = 8, 8 × 2 = 16, 3 + 16 = 19
    assertEquals(BigDecimal("19"), result)
  }
}
