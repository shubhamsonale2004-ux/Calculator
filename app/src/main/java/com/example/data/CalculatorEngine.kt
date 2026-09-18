package com.example.data

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Pure, dependency-free evaluator for basic calculator expressions. */
object CalculatorEngine {
  private val mathContext = MathContext(16, RoundingMode.HALF_UP)

  fun evaluate(expression: String): BigDecimal {
    val parser = Parser(expression)
    val result = parser.parseExpression()
    parser.skipWhitespace()
    if (!parser.isAtEnd()) throw IllegalArgumentException("Invalid expression")
    return result.stripTrailingZeros()
  }

  fun formatResult(value: BigDecimal): String {
    val stripped = value.stripTrailingZeros()
    val absolute = stripped.abs()
    if (absolute >= BigDecimal("1000000000000") ||
      (absolute > BigDecimal.ZERO && absolute < BigDecimal("0.000001"))) {
      return DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US)).format(stripped)
    }
    val plain = stripped.toPlainString()
    val parts = plain.split('.', limit = 2)
    val integer = parts[0]
    val negative = integer.startsWith('-')
    val digits = if (negative) integer.drop(1) else integer
    val grouped = buildString {
      if (negative) append('-')
      digits.forEachIndexed { index, character ->
        if (index > 0 && (digits.length - index) % 3 == 0) append(',')
        append(character)
      }
    }
    return if (parts.size == 2) "$grouped.${parts[1]}" else grouped
  }

  fun formatInputNumber(input: String): String {
    if (input.isEmpty() || input == "-" || input.endsWith('.')) return input
    val parts = input.split('.', limit = 2)
    val raw = parts[0]
    val negative = raw.startsWith('-')
    val digits = if (negative) raw.drop(1) else raw
    if (digits.isEmpty()) return input
    val grouped = buildString {
      if (negative) append('-')
      digits.forEachIndexed { index, character ->
        if (index > 0 && (digits.length - index) % 3 == 0) append(',')
        append(character)
      }
    }
    return if (parts.size == 2) "$grouped.${parts[1]}" else grouped
  }

  private class Parser(private val source: String) {
    private var index = 0
    fun isAtEnd() = index >= source.length
    fun skipWhitespace() { while (!isAtEnd() && source[index].isWhitespace()) index++ }

    fun parseExpression(): BigDecimal {
      var result = parseTerm()
      while (true) {
        skipWhitespace()
        if (match('+')) result = result.add(parseTerm(), mathContext)
        else if (match('-')) result = result.subtract(parseTerm(), mathContext)
        else return result
      }
    }

    private fun parseTerm(): BigDecimal {
      var result = parseFactor()
      while (true) {
        skipWhitespace()
        if (match('*') || match('×')) result = result.multiply(parseFactor(), mathContext)
        else if (match('/') || match('÷')) {
          val divisor = parseFactor()
          if (divisor.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Cannot divide by 0")
          result = result.divide(divisor, mathContext)
        } else return result
      }
    }

    private fun parseFactor(): BigDecimal {
      skipWhitespace()
      if (match('+')) return parseFactor()
      if (match('-') || match('−')) return parseFactor().negate(mathContext)
      if (match('(')) {
        val value = parseExpression()
        skipWhitespace()
        if (!match(')')) throw IllegalArgumentException("Missing closing parenthesis")
        return value
      }
      val start = index
      var dots = 0
      while (!isAtEnd() && (source[index].isDigit() || source[index] == '.')) {
        if (source[index] == '.') dots++
        if (dots > 1) throw IllegalArgumentException("Invalid number")
        index++
      }
      if (start == index) throw IllegalArgumentException("Number expected")
      return source.substring(start, index).toBigDecimal()
    }

    private fun match(character: Char): Boolean {
      if (!isAtEnd() && source[index] == character) { index++; return true }
      return false
    }
  }
}
