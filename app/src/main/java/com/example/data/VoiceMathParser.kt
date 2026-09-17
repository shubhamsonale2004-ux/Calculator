package com.example.data

import java.math.BigDecimal

object VoiceMathParser {

  private val numberWords = mapOf(
    "zero" to 0L,
    "one" to 1L,
    "two" to 2L,
    "three" to 3L,
    "four" to 4L,
    "five" to 5L,
    "six" to 6L,
    "seven" to 7L,
    "eight" to 8L,
    "nine" to 9L,
    "ten" to 10L,
    "eleven" to 11L,
    "twelve" to 12L,
    "thirteen" to 13L,
    "fourteen" to 14L,
    "fifteen" to 15L,
    "sixteen" to 16L,
    "seventeen" to 17L,
    "eighteen" to 18L,
    "nineteen" to 19L,
    "twenty" to 20L,
    "thirty" to 30L,
    "forty" to 40L,
    "fifty" to 50L,
    "sixty" to 60L,
    "seventy" to 70L,
    "eighty" to 80L,
    "ninety" to 90L
  )

  /**
   * Translates spoken English math phrases into a math expression string.
   * E.g.:
   *  "five times twelve" -> "5 × 12"
   *  "square root of 144" -> "√144"
   *  "twenty plus thirty five" -> "20 + 35"
   *  "10 divided by 2" -> "10 ÷ 2"
   *  "7 minus 3 point 5" -> "7 − 3.5"
   *  "2 to the power of 4" -> "2 ^ 4"
   *  "pi plus 5" -> "π + 5"
   */
  fun parseSpokenMath(spoken: String): String {
    if (spoken.isBlank()) return ""

    var text = spoken.lowercase().trim()

    // Normalize common speech quirks & synonyms
    text = text
      // Multi-word phrases
      .replace("square root of", "√")
      .replace("squareroot of", "√")
      .replace("square root", "√")
      .replace("squareroot", "√")
      .replace("cube root of", "cbrt")
      .replace("raised to the power of", "^")
      .replace("to the power of", "^")
      .replace("power of", "^")
      .replace("to the power", "^")
      .replace("divided by", " ÷ ")
      .replace("divide by", " ÷ ")
      .replace("multiplied by", " × ")
      .replace("multiply by", " × ")
      .replace("open parenthesis", "(")
      .replace("close parenthesis", ")")
      .replace("into", " × ")
      .replace("times", " × ")
      .replace("x", " × ")
      .replace("plus", " + ")
      .replace("add", " + ")
      .replace("minus", " − ")
      .replace("subtract", " − ")
      .replace("over", " ÷ ")
      .replace("percent", "%")
      .replace("percentage", "%")
      .replace("equals", "")
      .replace("equal to", "")
      .replace("equal", "")
      .replace("is equal to", "")
      .replace("what is", "")
      .replace("calculate", "")
      .replace("solve", "")
      .replace("compute", "")
      .replace("how much is", "")
      .replace("pie", "π")
      .replace("pi", "π")

    // Handle phrases like "negative five" or "minus five" at start
    val tokens = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
    val resultTokens = mutableListOf<String>()

    var idx = 0
    while (idx < tokens.size) {
      val token = tokens[idx]

      when {
        token in listOf("+", "−", "-", "×", "*", "÷", "/", "^", "%", "√", "(", ")") -> {
          val op = when (token) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> token
          }
          resultTokens.add(op)
          idx++
        }
        token == "negative" -> {
          resultTokens.add("-")
          idx++
        }
        token == "point" || token == "dot" -> {
          // If previous token was a number, merge dot
          if (resultTokens.isNotEmpty() && resultTokens.last().matches("-?\\d+".toRegex())) {
            resultTokens[resultTokens.size - 1] = resultTokens.last() + "."
          } else {
            resultTokens.add("0.")
          }
          idx++
        }
        token.matches("-?\\d+(\\.\\d+)?".toRegex()) -> {
          // It's already a numeric string like "12" or "3.5"
          val num = token
          if (resultTokens.isNotEmpty() && resultTokens.last().endsWith(".")) {
            resultTokens[resultTokens.size - 1] = resultTokens.last() + num
          } else {
            resultTokens.add(num)
          }
          idx++
        }
        token.startsWith("√") && token.length > 1 -> {
          val sub = token.substring(1)
          resultTokens.add("√")
          val parsedSub = parseNumberSequence(listOf(sub))
          if (parsedSub != null) {
            resultTokens.add(parsedSub)
          } else {
            resultTokens.add(sub)
          }
          idx++
        }
        token == "π" || token == "e" -> {
          resultTokens.add(token)
          idx++
        }
        else -> {
          // Check if token or slice of tokens is a word-number sequence like "twenty five" or "one hundred"
          val numberWordsSlice = mutableListOf<String>()
          var tempIdx = idx
          while (tempIdx < tokens.size && isNumberWord(tokens[tempIdx])) {
            numberWordsSlice.add(tokens[tempIdx])
            tempIdx++
          }

          if (numberWordsSlice.isNotEmpty()) {
            val parsedNum = parseWordNumbers(numberWordsSlice)
            if (parsedNum != null) {
              if (resultTokens.isNotEmpty() && resultTokens.last().endsWith(".")) {
                resultTokens[resultTokens.size - 1] = resultTokens.last() + parsedNum
              } else {
                resultTokens.add(parsedNum)
              }
              idx = tempIdx
            } else {
              resultTokens.add(token)
              idx++
            }
          } else {
            // Unrecognized or already formatted word
            resultTokens.add(token)
            idx++
          }
        }
      }
    }

    // Stitch together formatted expression cleanly
    val sb = StringBuilder()
    for (i in resultTokens.indices) {
      val tok = resultTokens[i]
      val prev = resultTokens.getOrNull(i - 1)

      if (tok == "-" && (prev == null || isOperator(prev))) {
        // Unary minus
        sb.append("-")
      } else if (tok == "√") {
        if (sb.isNotEmpty() && !sb.endsWith(" ") && !sb.endsWith("(")) {
          sb.append(" ")
        }
        sb.append("√")
      } else if (isOperator(tok)) {
        if (sb.isNotEmpty() && !sb.endsWith(" ")) {
          sb.append(" ")
        }
        sb.append(tok).append(" ")
      } else {
        sb.append(tok)
      }
    }

    return sb.toString().trim()
  }

  private fun isOperator(t: String): Boolean = t in listOf("+", "−", "×", "÷", "^")

  private fun isNumberWord(word: String): Boolean {
    val clean = word.replace("-", "")
    return clean in numberWords || clean in listOf("hundred", "thousand", "million", "and")
  }

  private fun parseNumberSequence(words: List<String>): String? {
    if (words.isEmpty()) return null
    if (words.size == 1 && words[0].matches("-?\\d+(\\.\\d+)?".toRegex())) {
      return words[0]
    }
    return parseWordNumbers(words)
  }

  /**
   * Converts word list like ["twenty", "five"] -> "25",
   * ["one", "hundred", "twenty"] -> "120"
   */
  fun parseWordNumbers(words: List<String>): String? {
    if (words.isEmpty()) return null

    var currentTotal = 0L
    var currentSegment = 0L
    var hasNumber = false

    for (rawWord in words) {
      val word = rawWord.trim().replace("-", " ")
      val subWords = word.split(" ").filter { it.isNotBlank() }

      for (w in subWords) {
        if (w == "and") continue

        // Check if raw digit in words
        val digitVal = w.toLongOrNull()
        if (digitVal != null) {
          currentSegment += digitVal
          hasNumber = true
          continue
        }

        val baseVal = numberWords[w]
        if (baseVal != null) {
          currentSegment += baseVal
          hasNumber = true
        } else if (w == "hundred") {
          currentSegment = if (currentSegment == 0L) 100L else currentSegment * 100L
          hasNumber = true
        } else if (w == "thousand") {
          currentTotal += if (currentSegment == 0L) 1000L else currentSegment * 1000L
          currentSegment = 0L
          hasNumber = true
        } else if (w == "million") {
          currentTotal += if (currentSegment == 0L) 1_000_000L else currentSegment * 1_000_000L
          currentSegment = 0L
          hasNumber = true
        } else {
          return null
        }
      }
    }

    if (!hasNumber) return null
    val total = currentTotal + currentSegment
    return total.toString()
  }
}
