package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityDiscountCalculatorBinding
import com.priti.dailykit.storage.DailyKitPreferences
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils

class DiscountCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiscountCalculatorBinding
    private lateinit var prefs: DailyKitPreferences
    private var isUpdatingChip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscountCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = DailyKitPreferences.getInstance(this)

        setupListeners()
        calculateDiscount()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateDiscount()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etOriginalPrice.addTextChangedListener(watcher)
        binding.etDiscountPercent.addTextChangedListener(watcher)
        binding.etAdditionalDiscount.addTextChangedListener(watcher)

        binding.chipGroupDiscounts.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty() || isUpdatingChip) return@setOnCheckedStateChangeListener
            val discount = when (checkedIds.first()) {
                R.id.chipDisc10 -> "10"
                R.id.chipDisc20 -> "20"
                R.id.chipDisc30 -> "30"
                R.id.chipDisc50 -> "50"
                else -> null
            }
            if (discount != null) {
                isUpdatingChip = true
                binding.etDiscountPercent.setText(discount)
                isUpdatingChip = false
                calculateDiscount()
            }
        }

        binding.btnCopyResult.setOnClickListener {
            val finalPrice = binding.tvFinalPrice.text.toString()
            val saved = binding.tvSavedAmount.text.toString()
            val orig = binding.tvOriginalPrice.text.toString()
            val copyText = "Final Price: $finalPrice\nYou Saved: $saved\nOriginal Price: $orig\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "Discount Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val finalPrice = binding.tvFinalPrice.text.toString()
            val saved = binding.tvSavedAmount.text.toString()
            val shareText = "DailyKit Discount Breakdown:\nFinal Price: $finalPrice\nYou Save: $saved"
            AppFeedback.shareText(this, "Share Discount Calculation", shareText)
        }
    }

    private fun calculateDiscount() {
        val currency = prefs.getCurrency()

        val priceStr = binding.etOriginalPrice.text?.toString()?.trim() ?: ""
        val discountStr = binding.etDiscountPercent.text?.toString()?.trim() ?: ""
        val addDiscStr = binding.etAdditionalDiscount.text?.toString()?.trim() ?: ""

        val originalPrice = priceStr.toDoubleOrNull() ?: 0.0
        val discount = (discountStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
        val additionalDiscount = (addDiscStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)

        if (originalPrice <= 0.0) {
            binding.tvFinalPrice.text = "$currency 0"
            binding.tvSavedAmount.text = "$currency 0"
            binding.tvOriginalPrice.text = "$currency 0"
            binding.tvEffectiveDiscountBadge.text = "0% OFF"
            return
        }

        val primarySaving = (originalPrice * discount) / 100.0
        val priceAfterPrimary = originalPrice - primarySaving
        val secondarySaving = (priceAfterPrimary * additionalDiscount) / 100.0
        val finalPrice = priceAfterPrimary - secondarySaving
        val totalSaved = primarySaving + secondarySaving

        val effectivePercent = if (originalPrice > 0) (totalSaved / originalPrice) * 100.0 else 0.0

        binding.tvFinalPrice.text = FormatUtils.formatCurrency(finalPrice, currency)
        binding.tvSavedAmount.text = FormatUtils.formatCurrency(totalSaved, currency)
        binding.tvOriginalPrice.text = FormatUtils.formatCurrency(originalPrice, currency)
        binding.tvEffectiveDiscountBadge.text = "${FormatUtils.formatNumber(effectivePercent, 1)}% OFF"
    }
}
