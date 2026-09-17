package com.example

import com.example.data.CalculatorEngine
import com.example.data.VoiceMathParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class VoiceMathParserTest {

  @Test
  fun testSimpleWordMultiplication() {
    val result = VoiceMathParser.parseSpokenMath("five times twelve")
    assertEquals("5 × 12", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("60").compareTo(eval))
  }

  @Test
  fun testWordAddition() {
    val result = VoiceMathParser.parseSpokenMath("twenty plus thirty five")
    assertEquals("20 + 35", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("55").compareTo(eval))
  }

  @Test
  fun testDivisionAndSubtraction() {
    val result = VoiceMathParser.parseSpokenMath("one hundred divided by four")
    assertEquals("100 ÷ 4", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("25").compareTo(eval))

    val resultSub = VoiceMathParser.parseSpokenMath("fifty minus fifteen")
    assertEquals("50 − 15", resultSub)
    val evalSub = CalculatorEngine.evaluate(resultSub)
    assertEquals(0, BigDecimal("35").compareTo(evalSub))
  }

  @Test
  fun testDigitsWithWords() {
    val result = VoiceMathParser.parseSpokenMath("50 times 3 plus 10")
    assertEquals("50 × 3 + 10", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("160").compareTo(eval))
  }

  @Test
  fun testDecimalsAndRoots() {
    val result = VoiceMathParser.parseSpokenMath("square root of 144")
    assertEquals("√144", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("12").compareTo(eval))

    val resultDot = VoiceMathParser.parseSpokenMath("two point five plus three point five")
    assertEquals("2.5 + 3.5", resultDot)
    val evalDot = CalculatorEngine.evaluate(resultDot)
    assertEquals(0, BigDecimal("6").compareTo(evalDot))
  }

  @Test
  fun testPowerAndPi() {
    val result = VoiceMathParser.parseSpokenMath("two raised to the power of three")
    assertEquals("2 ^ 3", result)
    val eval = CalculatorEngine.evaluate(result)
    assertEquals(0, BigDecimal("8").compareTo(eval))
  }
}
