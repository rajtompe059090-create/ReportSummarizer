package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityBmiCalculatorBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiCalculatorBinding
    private var isHeightMetric = true // cm vs ft-in
    private var isWeightMetric = true // kg vs lb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        calculateBmi()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.toggleHeightUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isHeightMetric = (checkedId == R.id.btnUnitCm)
                if (isHeightMetric) {
                    binding.tilHeightCm.visibility = View.VISIBLE
                    binding.layoutHeightFtIn.visibility = View.GONE
                } else {
                    binding.tilHeightCm.visibility = View.GONE
                    binding.layoutHeightFtIn.visibility = View.VISIBLE
                }
                calculateBmi()
            }
        }

        binding.toggleWeightUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isWeightMetric = (checkedId == R.id.btnUnitKg)
                binding.tilWeight.hint = if (isWeightMetric) "Weight in kg" else "Weight in lb"
                calculateBmi()
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateBmi()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etHeightCm.addTextChangedListener(watcher)
        binding.etHeightFt.addTextChangedListener(watcher)
        binding.etHeightIn.addTextChangedListener(watcher)
        binding.etWeight.addTextChangedListener(watcher)

        binding.btnCopyResult.setOnClickListener {
            val score = binding.tvBmiScore.text.toString()
            val cat = binding.tvBmiCategory.text.toString()
            val copyText = "BMI: $score ($cat)\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "BMI Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val score = binding.tvBmiScore.text.toString()
            val cat = binding.tvBmiCategory.text.toString()
            AppFeedback.shareText(this, "Share BMI", "My Body Mass Index (BMI) is $score ($cat). Calculated with DailyKit.")
        }
    }

    private fun calculateBmi() {
        // Calculate Height in Meters
        val heightM: Double = if (isHeightMetric) {
            val cmStr = binding.etHeightCm.text?.toString()?.trim() ?: ""
            val cm = cmStr.toDoubleOrNull() ?: 0.0
            cm / 100.0
        } else {
            val ftStr = binding.etHeightFt.text?.toString()?.trim() ?: ""
            val inStr = binding.etHeightIn.text?.toString()?.trim() ?: ""
            val ft = ftStr.toDoubleOrNull() ?: 0.0
            val inch = inStr.toDoubleOrNull() ?: 0.0
            val totalInches = (ft * 12.0) + inch
            (totalInches * 2.54) / 100.0
        }

        // Calculate Weight in Kilograms
        val weightStr = binding.etWeight.text?.toString()?.trim() ?: ""
        val weightInput = weightStr.toDoubleOrNull() ?: 0.0
        val weightKg: Double = if (isWeightMetric) {
            weightInput
        } else {
            weightInput * 0.45359237
        }

        if (heightM <= 0.3 || weightKg <= 1.0) {
            binding.tvBmiScore.text = "--"
            binding.tvBmiCategory.text = "Enter Measurements"
            binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
            return
        }

        val bmi = weightKg / (heightM * heightM)
        val formattedBmi = FormatUtils.formatNumber(bmi, 1)
        binding.tvBmiScore.text = formattedBmi

        when {
            bmi < 18.5 -> {
                binding.tvBmiCategory.text = "Underweight (< 18.5)"
                binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.accent_cyan))
            }
            bmi in 18.5..24.9 -> {
                binding.tvBmiCategory.text = "Normal Weight (18.5 - 24.9)"
                binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
            }
            bmi in 25.0..29.9 -> {
                binding.tvBmiCategory.text = "Overweight (25.0 - 29.9)"
                binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.accent_amber))
            }
            else -> {
                binding.tvBmiCategory.text = "Obese (≥ 30.0)"
                binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, R.color.accent_red))
            }
        }
    }
}
