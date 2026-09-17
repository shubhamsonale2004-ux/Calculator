package com.example.data

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CalculatorEngine {
  private val mathContext = MathContext(16, RoundingMode.HALF_UP)

  /**
   * Evaluates a mathematical expression string containing numbers and operators:
   * +, − (-), × (*), ÷ (/)
   * Respects operator precedence: × and ÷ before + and −.
   */
  fun evaluate(expression: String): BigDecimal {
    // Normalize operator characters
    val sanitized = expression
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .trim()

    if (sanitized.isEmpty()) {
      return BigDecimal.ZERO
    }

    val tokens = tokenize(sanitized)
    if (tokens.isEmpty()) return BigDecimal.ZERO

    return evaluateTokens(tokens)
  }

  private fun tokenize(input: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < input.length) {
      val c = input[i]
      when {
        c.isWhitespace() -> {
          i++
        }
        c in "+*/^√" -> {
          tokens.add(c.toString())
          i++
        }
        c == '-' -> {
          // Check if '-' is a unary negative sign or binary subtraction
          // It's unary if it's at the beginning or preceded by an operator
          val prevToken = tokens.lastOrNull()
          val isUnary = prevToken == null || prevToken in listOf("+", "-", "*", "/", "^")
          if (isUnary) {
            // Read the negative number
            var j = i + 1
            while (j < input.length && (input[j].isDigit() || input[j] == '.')) {
              j++
            }
            if (j > i + 1) {
              tokens.add(input.substring(i, j))
              i = j
            } else {
              tokens.add(c.toString())
              i++
            }
          } else {
            tokens.add(c.toString())
            i++
          }
        }
        c.isDigit() || c == '.' -> {
          var j = i
          while (j < input.length && (input[j].isDigit() || input[j] == '.')) {
            j++
          }
          tokens.add(input.substring(i, j))
          i = j
        }
        else -> {
          i++
        }
      }
    }
    return tokens
  }

  private fun evaluateTokens(tokens: List<String>): BigDecimal {
    if (tokens.isEmpty()) return BigDecimal.ZERO

    // Pass -1: Handle unary square root √ (e.g., √144 -> 12)
    val tokensAfterSqrt = mutableListOf<String>()
    var s = 0
    while (s < tokens.size) {
      val token = tokens[s]
      if (token == "√") {
        if (s + 1 >= tokens.size) {
          throw IllegalArgumentException("Invalid syntax")
        }
        val operandStr = tokens[s + 1]
        val operand = operandStr.toDouble()
        if (operand < 0.0) {
          throw ArithmeticException("Invalid input")
        }
        val root = Math.sqrt(operand)
        val bdRoot = BigDecimal(root, mathContext).stripTrailingZeros()
        tokensAfterSqrt.add(bdRoot.toPlainString())
        s += 2
      } else {
        tokensAfterSqrt.add(token)
        s++
      }
    }

    // Pass 0: Handle exponentiation ^ (right-associative or left-to-right evaluation)
    val tokensAfterExp = mutableListOf<String>()
    var e = 0
    while (e < tokensAfterSqrt.size) {
      val token = tokensAfterSqrt[e]
      if (token == "^") {
        if (tokensAfterExp.isEmpty() || e + 1 >= tokensAfterSqrt.size) {
          throw IllegalArgumentException("Invalid syntax")
        }
        val baseStr = tokensAfterExp.removeAt(tokensAfterExp.size - 1)
        val expStr = tokensAfterSqrt[e + 1]
        val base = baseStr.toDouble()
        val exp = expStr.toDouble()
        val result = Math.pow(base, exp)
        if (result.isNaN() || result.isInfinite()) {
          throw ArithmeticException("Result is undefined")
        }
        val bdResult = BigDecimal(result, mathContext).stripTrailingZeros()
        tokensAfterExp.add(bdResult.toPlainString())
        e += 2
      } else {
        tokensAfterExp.add(token)
        e++
      }
    }

    // First pass: Handle * and /
    val intermediateTokens = mutableListOf<String>()
    var i = 0
    while (i < tokensAfterExp.size) {
      val token = tokensAfterExp[i]
      if (token == "*" || token == "/") {
        if (intermediateTokens.isEmpty() || i + 1 >= tokensAfterExp.size) {
          throw IllegalArgumentException("Invalid syntax")
        }
        val leftStr = intermediateTokens.removeAt(intermediateTokens.size - 1)
        val rightStr = tokensAfterExp[i + 1]
        val left = BigDecimal(leftStr)
        val right = BigDecimal(rightStr)

        val result = if (token == "*") {
          left.multiply(right, mathContext)
        } else {
          if (right.compareTo(BigDecimal.ZERO) == 0) {
            throw ArithmeticException("Cannot divide by 0")
          }
          left.divide(right, mathContext)
        }
        intermediateTokens.add(result.stripTrailingZeros().toPlainString())
        i += 2
      } else {
        intermediateTokens.add(token)
        i++
      }
    }

    // Second pass: Handle + and -
    if (intermediateTokens.isEmpty()) return BigDecimal.ZERO

    var accumulator = BigDecimal(intermediateTokens[0])
    var j = 1
    while (j < intermediateTokens.size) {
      val op = intermediateTokens[j]
      if (j + 1 >= intermediateTokens.size) {
        break // Trailing operator, ignore or accept current accumulator
      }
      val nextVal = BigDecimal(intermediateTokens[j + 1])
      accumulator = when (op) {
        "+" -> accumulator.add(nextVal, mathContext)
        "-" -> accumulator.subtract(nextVal, mathContext)
        else -> throw IllegalArgumentException("Unexpected operator: $op")
      }
      j += 2
    }

    return accumulator.stripTrailingZeros()
  }

  /**
   * Formats a BigDecimal into a clean user-facing string.
   * Eliminates unnecessary trailing zeroes and formats large numbers gracefully.
   */
  fun formatResult(value: BigDecimal): String {
    val stripped = value.stripTrailingZeros()
    val plain = stripped.toPlainString()

    // If extremely large or tiny scientific notation is needed
    if (plain.length > 15 || stripped.scale() > 8) {
      // Use scientific if scale is very large or abs value is huge
      if (stripped.abs() >= BigDecimal("1000000000000") || (stripped.abs() > BigDecimal.ZERO && stripped.abs() < BigDecimal("0.000001"))) {
        val scientificFormat = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
        return scientificFormat.format(value)
      }
    }

    // Regular formatting with thousands separator
    val parts = plain.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else null

    // Format integer part with commas
    val isNegative = integerPart.startsWith("-")
    val rawDigits = if (isNegative) integerPart.substring(1) else integerPart
    val formattedInteger = buildString {
      if (isNegative) append("-")
      val len = rawDigits.length
      for (idx in 0 until len) {
        if (idx > 0 && (len - idx) % 3 == 0) {
          append(",")
        }
        append(rawDigits[idx])
      }
    }

    return if (decimalPart != null && decimalPart.isNotEmpty()) {
      "$formattedInteger.$decimalPart"
    } else {
      formattedInteger
    }
  }

  /**
   * Formats an input number string with commas while user is typing.
   */
  fun formatInputNumber(numberStr: String): String {
    if (numberStr.isEmpty() || numberStr == "-" || numberStr == "Error" || numberStr.contains("Cannot")) {
      return numberStr
    }
    val parts = numberStr.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else null
    val hasDot = numberStr.endsWith(".")

    val isNegative = integerPart.startsWith("-")
    val rawDigits = if (isNegative) integerPart.substring(1) else integerPart
    if (rawDigits.isEmpty()) {
      return numberStr
    }

    val formattedInteger = buildString {
      if (isNegative) append("-")
      val len = rawDigits.length
      for (idx in 0 until len) {
        if (idx > 0 && (len - idx) % 3 == 0) {
          append(",")
        }
        append(rawDigits[idx])
      }
    }

    return when {
      decimalPart != null -> "$formattedInteger.$decimalPart"
      hasDot -> "$formattedInteger."
      else -> formattedInteger
    }
  }
}
