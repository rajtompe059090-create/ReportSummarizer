package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityPercentageCalculatorBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils

class PercentageCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPercentageCalculatorBinding
    private var currentMode = 1 // 1: X% of Y, 2: X is what % of Y, 3: % Increase, 4: % Decrease

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPercentageCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateModeUI()
        calculatePercentage()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.chipGroupModes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            currentMode = when (checkedIds.first()) {
                R.id.chipMode1 -> 1
                R.id.chipMode2 -> 2
                R.id.chipMode3 -> 3
                R.id.chipMode4 -> 4
                else -> 1
            }
            updateModeUI()
            calculatePercentage()
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculatePercentage()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etInputX.addTextChangedListener(watcher)
        binding.etInputY.addTextChangedListener(watcher)

        binding.btnCopyResult.setOnClickListener {
            val result = binding.tvPercentageResult.text.toString()
            val explanation = binding.tvFormulaExplanation.text.toString()
            val copyText = "$explanation\nResult: $result\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "Percentage Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val result = binding.tvPercentageResult.text.toString()
            val explanation = binding.tvFormulaExplanation.text.toString()
            AppFeedback.shareText(this, "Share Percentage", "$explanation\nResult: $result")
        }
    }

    private fun updateModeUI() {
        when (currentMode) {
            1 -> {
                binding.tilInputX.hint = "Percentage (X %)"
                binding.tilInputY.hint = "Total Value (Y)"
            }
            2 -> {
                binding.tilInputX.hint = "Value (X)"
                binding.tilInputY.hint = "Total Base (Y)"
            }
            3 -> {
                binding.tilInputX.hint = "Initial Value (From)"
                binding.tilInputY.hint = "Final Value (To)"
            }
            4 -> {
                binding.tilInputX.hint = "Initial Value (From)"
                binding.tilInputY.hint = "Final Value (To)"
            }
        }
    }

    private fun calculatePercentage() {
        val xStr = binding.etInputX.text?.toString()?.trim() ?: ""
        val yStr = binding.etInputY.text?.toString()?.trim() ?: ""

        val x = xStr.toDoubleOrNull() ?: 0.0
        val y = yStr.toDoubleOrNull() ?: 0.0

        when (currentMode) {
            1 -> {
                // What is X% of Y?
                val res = (x / 100.0) * y
                binding.tvPercentageResult.text = FormatUtils.formatNumber(res, 2)
                binding.tvFormulaExplanation.text = "${FormatUtils.formatNumber(x, 2)}% of ${FormatUtils.formatNumber(y, 2)} = ${FormatUtils.formatNumber(res, 2)}"
            }
            2 -> {
                // X is what % of Y?
                if (y == 0.0) {
                    binding.tvPercentageResult.text = "Error"
                    binding.tvFormulaExplanation.text = "Cannot divide by zero"
                } else {
                    val res = (x / y) * 100.0
                    binding.tvPercentageResult.text = "${FormatUtils.formatNumber(res, 2)}%"
                    binding.tvFormulaExplanation.text = "${FormatUtils.formatNumber(x, 2)} is ${FormatUtils.formatNumber(res, 2)}% of ${FormatUtils.formatNumber(y, 2)}"
                }
            }
            3 -> {
                // % Increase
                if (x == 0.0) {
                    binding.tvPercentageResult.text = "Error"
                    binding.tvFormulaExplanation.text = "Initial value cannot be zero"
                } else {
                    val diff = y - x
                    val res = (diff / x) * 100.0
                    val sign = if (res >= 0) "+" else ""
                    binding.tvPercentageResult.text = "$sign${FormatUtils.formatNumber(res, 2)}%"
                    binding.tvFormulaExplanation.text = "Change from ${FormatUtils.formatNumber(x, 2)} to ${FormatUtils.formatNumber(y, 2)} is $sign${FormatUtils.formatNumber(res, 2)}%"
                }
            }
            4 -> {
                // % Decrease
                if (x == 0.0) {
                    binding.tvPercentageResult.text = "Error"
                    binding.tvFormulaExplanation.text = "Initial value cannot be zero"
                } else {
                    val diff = x - y
                    val res = (diff / x) * 100.0
                    binding.tvPercentageResult.text = "-${FormatUtils.formatNumber(res, 2)}%"
                    binding.tvFormulaExplanation.text = "Decrease from ${FormatUtils.formatNumber(x, 2)} to ${FormatUtils.formatNumber(y, 2)} is ${FormatUtils.formatNumber(res, 2)}%"
                }
            }
        }
    }
}
