package com.priti.dailykit.activities

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityDateDifferenceBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DateDifferenceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDateDifferenceBinding
    private var startCalendar: Calendar = Calendar.getInstance()
    private var endCalendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, 3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateDifferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        calculateDifference()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        val openStartPicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startCalendar.set(Calendar.YEAR, year)
                    startCalendar.set(Calendar.MONTH, month)
                    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    calculateDifference()
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val openEndPicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    endCalendar.set(Calendar.YEAR, year)
                    endCalendar.set(Calendar.MONTH, month)
                    endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    calculateDifference()
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.layoutStartDate.setOnClickListener { openStartPicker() }
        binding.btnPickStart.setOnClickListener { openStartPicker() }

        binding.layoutEndDate.setOnClickListener { openEndPicker() }
        binding.btnPickEnd.setOnClickListener { openEndPicker() }

        binding.btnSwapDates.setOnClickListener {
            val temp = startCalendar.clone() as Calendar
            startCalendar = endCalendar.clone() as Calendar
            endCalendar = temp
            calculateDifference()
        }

        binding.btnCopyResult.setOnClickListener {
            val days = binding.tvTotalDaysDifference.text.toString()
            val y = binding.tvDiffYears.text.toString()
            val m = binding.tvDiffMonths.text.toString()
            val w = binding.tvDiffWeeks.text.toString()
            val copyText = "Difference: $days\nBreakdown: $y Years, $m Months, $w Weeks\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "Date Difference", copyText)
        }

        binding.btnShare.setOnClickListener {
            val days = binding.tvTotalDaysDifference.text.toString()
            AppFeedback.shareText(this, "Share Date Difference", "Date duration is $days! Calculated with DailyKit.")
        }
    }

    private fun calculateDifference() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvStartDate.text = sdf.format(startCalendar.time)
        binding.tvEndDate.text = sdf.format(endCalendar.time)

        // Absolute difference calculation
        val earlier = if (startCalendar.before(endCalendar)) startCalendar else endCalendar
        val later = if (startCalendar.before(endCalendar)) endCalendar else startCalendar

        val diffMillis = abs(later.timeInMillis - earlier.timeInMillis)
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val totalWeeks = totalDays / 7

        binding.tvTotalDaysDifference.text = "$totalDays Days"
        binding.tvDiffWeeks.text = totalWeeks.toString()
        binding.tvDiffDays.text = totalDays.toString()

        // Calendar breakdown
        val calEarlier = earlier.clone() as Calendar
        var years = later.get(Calendar.YEAR) - calEarlier.get(Calendar.YEAR)
        var months = later.get(Calendar.MONTH) - calEarlier.get(Calendar.MONTH)
        var days = later.get(Calendar.DAY_OF_MONTH) - calEarlier.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            val prevMonth = (later.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            months--
        }

        if (months < 0) {
            months += 12
            years--
        }

        binding.tvDiffYears.text = maxOf(0, years).toString()
        binding.tvDiffMonths.text = maxOf(0, months).toString()
    }
}
