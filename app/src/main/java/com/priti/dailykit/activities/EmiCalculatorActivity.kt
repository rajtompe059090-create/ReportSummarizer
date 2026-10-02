package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityEmiCalculatorBinding
import com.priti.dailykit.storage.DailyKitPreferences
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils
import kotlin.math.pow

class EmiCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmiCalculatorBinding
    private lateinit var prefs: DailyKitPreferences
    private var isTenureInYears = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmiCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = DailyKitPreferences.getInstance(this)

        setupListeners()
        calculateEmi()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnReset.setOnClickListener {
            binding.etLoanAmount.setText("1000000")
            binding.etInterestRate.setText("8.5")
            binding.etTenure.setText("5")
            binding.toggleTenureUnit.check(R.id.btnUnitYears)
            isTenureInYears = true
            calculateEmi()
        }

        binding.toggleTenureUnit.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isTenureInYears = (checkedId == R.id.btnUnitYears)
                calculateEmi()
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateEmi()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etLoanAmount.addTextChangedListener(watcher)
        binding.etInterestRate.addTextChangedListener(watcher)
        binding.etTenure.addTextChangedListener(watcher)

        binding.btnCopyResult.setOnClickListener {
            val emi = binding.tvMonthlyEmi.text.toString()
            val totalPayment = binding.tvTotalPayment.text.toString()
            val totalInterest = binding.tvTotalInterest.text.toString()
            val copyText = "Monthly EMI: $emi\nTotal Payment: $totalPayment\nTotal Interest: $totalInterest\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "EMI Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val emi = binding.tvMonthlyEmi.text.toString()
            val totalPayment = binding.tvTotalPayment.text.toString()
            val totalInterest = binding.tvTotalInterest.text.toString()
            val shareText = "DailyKit EMI Breakdown:\nMonthly EMI: $emi\nTotal Payment: $totalPayment\nTotal Interest: $totalInterest"
            AppFeedback.shareText(this, "Share EMI Calculation", shareText)
        }
    }

    private fun calculateEmi() {
        val currency = prefs.getCurrency()

        val amountStr = binding.etLoanAmount.text?.toString()?.trim() ?: ""
        val rateStr = binding.etInterestRate.text?.toString()?.trim() ?: ""
        val tenureStr = binding.etTenure.text?.toString()?.trim() ?: ""

        val principal = amountStr.toDoubleOrNull() ?: 0.0
        val annualRate = rateStr.toDoubleOrNull() ?: 0.0
        val tenureValue = tenureStr.toDoubleOrNull() ?: 0.0

        if (principal <= 0 || tenureValue <= 0) {
            binding.tvMonthlyEmi.text = "$currency 0"
            binding.tvTotalPrincipal.text = "$currency 0"
            binding.tvTotalInterest.text = "$currency 0"
            binding.tvTotalPayment.text = "$currency 0"
            updateBreakdownBars(50f, 50f)
            return
        }

        val months = if (isTenureInYears) tenureValue * 12.0 else tenureValue
        val monthlyRate = (annualRate / 100.0) / 12.0

        val monthlyEmi: Double
        val totalPayment: Double
        val totalInterest: Double

        if (annualRate == 0.0) {
            monthlyEmi = principal / months
            totalPayment = principal
            totalInterest = 0.0
        } else {
            val compound = (1.0 + monthlyRate).pow(months)
            monthlyEmi = principal * monthlyRate * (compound / (compound - 1.0))
            totalPayment = monthlyEmi * months
            totalInterest = totalPayment - principal
        }

        binding.tvMonthlyEmi.text = FormatUtils.formatCurrency(monthlyEmi, currency)
        binding.tvTotalPrincipal.text = FormatUtils.formatCurrency(principal, currency)
        binding.tvTotalInterest.text = FormatUtils.formatCurrency(totalInterest, currency)
        binding.tvTotalPayment.text = FormatUtils.formatCurrency(totalPayment, currency)

        val principalPercent = ((principal / totalPayment) * 100.0).coerceIn(0.0, 100.0)
        val interestPercent = ((totalInterest / totalPayment) * 100.0).coerceIn(0.0, 100.0)

        binding.tvPrincipalPercent.text = "Principal (${FormatUtils.formatNumber(principalPercent, 1)}%)"
        binding.tvInterestPercent.text = "Interest (${FormatUtils.formatNumber(interestPercent, 1)}%)"

        updateBreakdownBars(principalPercent.toFloat(), interestPercent.toFloat())
    }

    private fun updateBreakdownBars(principalWeight: Float, interestWeight: Float) {
        val safePrincipalWeight = principalWeight.coerceAtLeast(1f)
        val safeInterestWeight = interestWeight.coerceAtLeast(1f)

        val lpPrincipal = binding.barPrincipal.layoutParams as LinearLayout.LayoutParams
        lpPrincipal.weight = safePrincipalWeight
        binding.barPrincipal.layoutParams = lpPrincipal

        val lpInterest = binding.barInterest.layoutParams as LinearLayout.LayoutParams
        lpInterest.weight = safeInterestWeight
        binding.barInterest.layoutParams = lpInterest
    }
}
