package com.priti.dailykit.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CalcEngine {

    sealed class CalcResult {
        data class Success(val value: Double, val formatted: String) : CalcResult()
        data class Error(val message: String) : CalcResult()
    }

    fun evaluate(expression: String): CalcResult {
        if (expression.isBlank()) return CalcResult.Success(0.0, "0")

        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")

        return try {
            val parser = ExprParser(sanitized)
            val result = parser.parse()

            if (result.isNaN() || result.isInfinite()) {
                CalcResult.Error("Cannot divide by zero")
            } else {
                val formatted = formatResult(result)
                CalcResult.Success(result, formatted)
            }
        } catch (e: ArithmeticException) {
            CalcResult.Error("Cannot divide by zero")
        } catch (e: Exception) {
            CalcResult.Error("Invalid format")
        }
    }

    private fun formatResult(value: Double): String {
        return try {
            val bd = BigDecimal(value.toString(), MathContext(12, RoundingMode.HALF_UP))
            val stripped = bd.stripTrailingZeros()
            stripped.toPlainString()
        } catch (e: Exception) {
            val isWhole = (value % 1.0) == 0.0
            if (isWhole) value.toLong().toString() else value.toString()
        }
    }

    private class ExprParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> x = (x / 100.0)
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                val numStr = str.substring(startPos, pos)
                x = numStr.toDouble()
            } else {
                throw RuntimeException("Unexpected token: " + ch.toChar())
            }

            return x
        }
    }
}
