package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityGstCalculatorBinding
import com.priti.dailykit.storage.DailyKitPreferences
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils

class GstCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGstCalculatorBinding
    private lateinit var prefs: DailyKitPreferences
    private var isAddGstMode = true
    private var isUpdatingRateFromChip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGstCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = DailyKitPreferences.getInstance(this)

        setupListeners()
        calculateGst()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.toggleGstMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isAddGstMode = (checkedId == R.id.btnAddGst)
                calculateGst()
            }
        }

        binding.chipGroupRates.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty() || isUpdatingRateFromChip) return@setOnCheckedStateChangeListener
            val rate = when (checkedIds.first()) {
                R.id.chip0 -> "0"
                R.id.chip5 -> "5"
                R.id.chip12 -> "12"
                R.id.chip18 -> "18"
                R.id.chip28 -> "28"
                else -> null
            }
            if (rate != null) {
                isUpdatingRateFromChip = true
                binding.etCustomRate.setText(rate)
                isUpdatingRateFromChip = false
                calculateGst()
            }
        }

        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateGst()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etCustomRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdatingRateFromChip) {
                    val rate = s?.toString()?.trim()
                    when (rate) {
                        "0" -> binding.chipGroupRates.check(R.id.chip0)
                        "5" -> binding.chipGroupRates.check(R.id.chip5)
                        "12" -> binding.chipGroupRates.check(R.id.chip12)
                        "18" -> binding.chipGroupRates.check(R.id.chip18)
                        "28" -> binding.chipGroupRates.check(R.id.chip28)
                        else -> binding.chipGroupRates.check(R.id.chipCustom)
                    }
                }
                calculateGst()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCopyResult.setOnClickListener {
            val currency = prefs.getCurrency()
            val finalAmt = binding.tvFinalAmount.text.toString()
            val base = binding.tvBaseAmount.text.toString()
            val gst = binding.tvTotalGst.text.toString()
            val copyText = "Final Amount: $finalAmt\nBase Amount: $base\nGST: $gst\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "GST Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val finalAmt = binding.tvFinalAmount.text.toString()
            val base = binding.tvBaseAmount.text.toString()
            val gst = binding.tvTotalGst.text.toString()
            val shareText = "DailyKit GST Breakdown:\nFinal Amount: $finalAmt\nBase Amount: $base\nGST: $gst"
            AppFeedback.shareText(this, "Share GST Calculation", shareText)
        }
    }

    private fun calculateGst() {
        val currency = prefs.getCurrency()

        val amountStr = binding.etAmount.text?.toString()?.trim() ?: ""
        val rateStr = binding.etCustomRate.text?.toString()?.trim() ?: ""

        val inputAmount = amountStr.toDoubleOrNull() ?: 0.0
        val rate = rateStr.toDoubleOrNull() ?: 0.0

        if (inputAmount <= 0.0) {
            binding.tvFinalAmount.text = "$currency 0"
            binding.tvBaseAmount.text = "$currency 0"
            binding.tvTotalGst.text = "$currency 0"
            binding.tvCgst.text = "$currency 0"
            binding.tvSgst.text = "$currency 0"
            return
        }

        val baseAmount: Double
        val totalGst: Double
        val finalAmount: Double

        if (isAddGstMode) {
            baseAmount = inputAmount
            totalGst = (baseAmount * rate) / 100.0
            finalAmount = baseAmount + totalGst
            binding.tvResultHeader.text = getString(R.string.label_final_amount)
        } else {
            finalAmount = inputAmount
            baseAmount = finalAmount / (1.0 + (rate / 100.0))
            totalGst = finalAmount - baseAmount
            binding.tvResultHeader.text = getString(R.string.label_base_amount)
        }

        val halfGst = totalGst / 2.0
        val halfRate = rate / 2.0

        binding.tvFinalAmount.text = FormatUtils.formatCurrency(if (isAddGstMode) finalAmount else baseAmount, currency)
        binding.tvBaseAmount.text = FormatUtils.formatCurrency(baseAmount, currency)
        binding.tvTotalGst.text = FormatUtils.formatCurrency(totalGst, currency)
        binding.tvCgst.text = FormatUtils.formatCurrency(halfGst, currency)
        binding.tvSgst.text = FormatUtils.formatCurrency(halfGst, currency)

        binding.tvGstLabel.text = "Total GST (${FormatUtils.formatNumber(rate, 2)}%)"
        binding.tvCgstLabel.text = "CGST (${FormatUtils.formatNumber(halfRate, 2)}%)"
        binding.tvSgstLabel.text = "SGST (${FormatUtils.formatNumber(halfRate, 2)}%)"
    }
}
