package com.priti.dailykit.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityUnitConverterBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils

class UnitConverterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitConverterBinding
    private var currentCategory = "Length" // "Length", "Weight", "Temperature"

    private val lengthUnits = listOf(
        "Millimeter (mm)",
        "Centimeter (cm)",
        "Meter (m)",
        "Kilometer (km)",
        "Inch (in)",
        "Foot (ft)",
        "Yard (yd)",
        "Mile (mi)"
    )

    // Base unit: Meter
    private val lengthFactorsToMeters = mapOf(
        "Millimeter (mm)" to 0.001,
        "Centimeter (cm)" to 0.01,
        "Meter (m)" to 1.0,
        "Kilometer (km)" to 1000.0,
        "Inch (in)" to 0.0254,
        "Foot (ft)" to 0.3048,
        "Yard (yd)" to 0.9144,
        "Mile (mi)" to 1609.344
    )

    private val weightUnits = listOf(
        "Milligram (mg)",
        "Gram (g)",
        "Kilogram (kg)",
        "Ounce (oz)",
        "Pound (lb)",
        "Ton (t)"
    )

    // Base unit: Gram
    private val weightFactorsToGrams = mapOf(
        "Milligram (mg)" to 0.001,
        "Gram (g)" to 1.0,
        "Kilogram (kg)" to 1000.0,
        "Ounce (oz)" to 28.349523125,
        "Pound (lb)" to 453.59237,
        "Ton (t)" to 1_000_000.0
    )

    private val temperatureUnits = listOf(
        "Celsius (°C)",
        "Fahrenheit (°F)",
        "Kelvin (K)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateCategory("Length")

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.chipGroupUnitTypes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                R.id.chipLength -> updateCategory("Length")
                R.id.chipWeight -> updateCategory("Weight")
                R.id.chipTemperature -> updateCategory("Temperature")
            }
        }

        binding.etInputValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                convert()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSwapUnits.setOnClickListener {
            val fromPos = binding.spinnerFrom.selectedItemPosition
            val toPos = binding.spinnerTo.selectedItemPosition
            binding.spinnerFrom.setSelection(toPos)
            binding.spinnerTo.setSelection(fromPos)
            convert()
        }

        val itemListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                convert()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerFrom.onItemSelectedListener = itemListener
        binding.spinnerTo.onItemSelectedListener = itemListener

        binding.btnCopyResult.setOnClickListener {
            val res = binding.tvConvertedResult.text.toString()
            val expl = binding.tvConversionExplanation.text.toString()
            val copyText = "$res\n$expl\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "Conversion Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val res = binding.tvConvertedResult.text.toString()
            val expl = binding.tvConversionExplanation.text.toString()
            AppFeedback.shareText(this, "Share Conversion", "$expl: $res (DailyKit)")
        }
    }

    private fun updateCategory(category: String) {
        currentCategory = category
        val list = when (category) {
            "Length" -> lengthUnits
            "Weight" -> weightUnits
            "Temperature" -> temperatureUnits
            else -> lengthUnits
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        binding.spinnerFrom.adapter = adapter
        binding.spinnerTo.adapter = adapter

        // Set default selections
        binding.spinnerFrom.setSelection(0)
        binding.spinnerTo.setSelection(if (list.size > 1) 1 else 0)

        convert()
    }

    private fun convert() {
        val inputStr = binding.etInputValue.text?.toString()?.trim() ?: ""
        val value = inputStr.toDoubleOrNull() ?: 0.0

        val fromUnit = binding.spinnerFrom.selectedItem?.toString() ?: return
        val toUnit = binding.spinnerTo.selectedItem?.toString() ?: return

        val result: Double
        when (currentCategory) {
            "Length" -> {
                val fromFactor = lengthFactorsToMeters[fromUnit] ?: 1.0
                val toFactor = lengthFactorsToMeters[toUnit] ?: 1.0
                val meters = value * fromFactor
                result = meters / toFactor
            }
            "Weight" -> {
                val fromFactor = weightFactorsToGrams[fromUnit] ?: 1.0
                val toFactor = weightFactorsToGrams[toUnit] ?: 1.0
                val grams = value * fromFactor
                result = grams / toFactor
            }
            "Temperature" -> {
                result = convertTemperature(value, fromUnit, toUnit)
            }
            else -> result = value
        }

        binding.tvConvertedResult.text = "${FormatUtils.formatNumber(result, 5)} $toUnit"
        binding.tvConversionExplanation.text = "${FormatUtils.formatNumber(value, 2)} $fromUnit = ${FormatUtils.formatNumber(result, 5)} $toUnit"
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        if (from == to) return value

        // Convert From -> Celsius
        val celsius = when (from) {
            "Celsius (°C)" -> value
            "Fahrenheit (°F)" -> (value - 32.0) * (5.0 / 9.0)
            "Kelvin (K)" -> value - 273.15
            else -> value
        }

        // Convert Celsius -> To
        return when (to) {
            "Celsius (°C)" -> celsius
            "Fahrenheit (°F)" -> (celsius * (9.0 / 5.0)) + 32.0
            "Kelvin (K)" -> celsius + 273.15
            else -> celsius
        }
    }
}
