package com.priti.dailykit.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityCalculatorBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.CalcEngine

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private var expression: StringBuilder = StringBuilder()
    private var openParenCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateDisplay()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnCopy.setOnClickListener {
            val text = binding.tvResult.text.toString()
            if (text.isNotBlank()) {
                AppFeedback.copyToClipboard(this, "Calculation Result", text)
            }
        }

        // Digits
        val numButtons = mapOf(
            binding.btnKey0 to "0",
            binding.btnKey1 to "1",
            binding.btnKey2 to "2",
            binding.btnKey3 to "3",
            binding.btnKey4 to "4",
            binding.btnKey5 to "5",
            binding.btnKey6 to "6",
            binding.btnKey7 to "7",
            binding.btnKey8 to "8",
            binding.btnKey9 to "9"
        )
        numButtons.forEach { (btn, value) ->
            btn.setOnClickListener { appendChar(value) }
        }

        // Operators
        binding.btnKeyAdd.setOnClickListener { appendOperator("+") }
        binding.btnKeySub.setOnClickListener { appendOperator("−") }
        binding.btnKeyMul.setOnClickListener { appendOperator("×") }
        binding.btnKeyDiv.setOnClickListener { appendOperator("÷") }
        binding.btnKeyPercent.setOnClickListener { appendOperator("%") }

        // Decimal
        binding.btnKeyDot.setOnClickListener {
            if (canAppendDot()) {
                if (expression.isEmpty() || isLastCharOperator()) {
                    expression.append("0.")
                } else {
                    expression.append(".")
                }
                updateDisplay()
            }
        }

        // Parentheses
        binding.btnKeyParen.setOnClickListener {
            handleParentheses()
        }

        // Clear & Delete
        binding.btnKeyC.setOnClickListener {
            expression.clear()
            openParenCount = 0
            binding.tvExpression.text = ""
            binding.tvResult.text = "0"
        }

        binding.btnKeyDel.setOnClickListener {
            if (expression.isNotEmpty()) {
                val lastChar = expression.last()
                if (lastChar == '(') openParenCount--
                if (lastChar == ')') openParenCount++
                expression.deleteCharAt(expression.length - 1)
                updateDisplay()
            }
        }

        // Equals
        binding.btnKeyEq.setOnClickListener {
            if (expression.isNotEmpty()) {
                val expr = expression.toString()
                when (val result = CalcEngine.evaluate(expr)) {
                    is CalcEngine.CalcResult.Success -> {
                        binding.tvExpression.text = expr
                        binding.tvResult.text = result.formatted
                        expression.clear()
                        expression.append(result.formatted)
                    }
                    is CalcEngine.CalcResult.Error -> {
                        binding.tvResult.text = result.message
                    }
                }
            }
        }
    }

    private fun appendChar(c: String) {
        expression.append(c)
        updateDisplay()
    }

    private fun appendOperator(op: String) {
        if (expression.isEmpty()) {
            if (op == "−") {
                expression.append(op)
                updateDisplay()
            }
            return
        }

        if (isLastCharOperator()) {
            expression.setCharAt(expression.length - 1, op[0])
        } else {
            expression.append(op)
        }
        updateDisplay()
    }

    private fun handleParentheses() {
        if (openParenCount > 0 && !isLastCharOperator() && expression.last() != '(') {
            expression.append(")")
            openParenCount--
        } else {
            if (expression.isNotEmpty() && !isLastCharOperator() && expression.last() != '(') {
                expression.append("×")
            }
            expression.append("(")
            openParenCount++
        }
        updateDisplay()
    }

    private fun canAppendDot(): Boolean {
        if (expression.isEmpty()) return true
        val str = expression.toString()
        val lastOpIndex = maxOf(
            str.lastIndexOf('+'),
            str.lastIndexOf('−'),
            str.lastIndexOf('×'),
            str.lastIndexOf('÷'),
            str.lastIndexOf('%'),
            str.lastIndexOf('(')
        )
        val currentNumber = if (lastOpIndex != -1) str.substring(lastOpIndex + 1) else str
        return !currentNumber.contains('.')
    }

    private fun isLastCharOperator(): Boolean {
        if (expression.isEmpty()) return false
        val last = expression.last()
        return last == '+' || last == '−' || last == '×' || last == '÷' || last == '%'
    }

    private fun updateDisplay() {
        val exprStr = expression.toString()
        binding.tvExpression.text = exprStr

        if (exprStr.isEmpty()) {
            binding.tvResult.text = "0"
            return
        }

        if (!isLastCharOperator()) {
            when (val res = CalcEngine.evaluate(exprStr)) {
                is CalcEngine.CalcResult.Success -> {
                    binding.tvResult.text = res.formatted
                }
                is CalcEngine.CalcResult.Error -> {
                    // Keep existing display until user completes expression
                }
            }
        }
    }
}
