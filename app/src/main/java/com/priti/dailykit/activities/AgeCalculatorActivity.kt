package com.priti.dailykit.activities

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityAgeCalculatorBinding
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AgeCalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgeCalculatorBinding
    private var birthCalendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.YEAR, -25) // Default to 25 years ago
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgeCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        calculateAge()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        val openPicker = {
            val now = Calendar.getInstance()
            val dialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    birthCalendar.set(Calendar.YEAR, year)
                    birthCalendar.set(Calendar.MONTH, month)
                    birthCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    calculateAge()
                },
                birthCalendar.get(Calendar.YEAR),
                birthCalendar.get(Calendar.MONTH),
                birthCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.datePicker.maxDate = now.timeInMillis
            dialog.show()
        }

        binding.cardSelectDob.setOnClickListener { openPicker() }
        binding.btnChangeDob.setOnClickListener { openPicker() }

        binding.btnCopyResult.setOnClickListener {
            val y = binding.tvAgeYears.text.toString()
            val m = binding.tvAgeMonths.text.toString()
            val d = binding.tvAgeDays.text.toString()
            val nb = binding.tvNextBirthday.text.toString()
            val copyText = "Exact Age: $y Years, $m Months, $d Days\nNext Birthday: $nb\nCalculated with DailyKit"
            AppFeedback.copyToClipboard(this, "Age Result", copyText)
        }

        binding.btnShare.setOnClickListener {
            val y = binding.tvAgeYears.text.toString()
            val m = binding.tvAgeMonths.text.toString()
            val d = binding.tvAgeDays.text.toString()
            val nb = binding.tvNextBirthday.text.toString()
            AppFeedback.shareText(this, "Share Age", "My exact age is $y Years, $m Months, $d Days!\nNext Birthday: $nb")
        }
    }

    private fun calculateAge() {
        val today = Calendar.getInstance()

        // Format selected DOB
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvSelectedDob.text = sdf.format(birthCalendar.time)

        var birthYear = birthCalendar.get(Calendar.YEAR)
        var birthMonth = birthCalendar.get(Calendar.MONTH)
        var birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH)

        var currentYear = today.get(Calendar.YEAR)
        var currentMonth = today.get(Calendar.MONTH)
        var currentDay = today.get(Calendar.DAY_OF_MONTH)

        // Calculate days
        if (currentDay < birthDay) {
            val prevMonth = (today.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
            }
            currentDay += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            currentMonth--
        }

        // Calculate months
        if (currentMonth < birthMonth) {
            currentMonth += 12
            currentYear--
        }

        val years = currentYear - birthYear
        val months = currentMonth - birthMonth
        val days = currentDay - birthDay

        binding.tvAgeYears.text = years.toString()
        binding.tvAgeMonths.text = months.toString()
        binding.tvAgeDays.text = days.toString()

        // Total Days & Weeks Lived
        val diffMillis = today.timeInMillis - birthCalendar.timeInMillis
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0)
        val totalWeeks = totalDays / 7

        binding.tvTotalDaysLived.text = FormatUtils.formatNumber(totalDays.toDouble(), 0)
        binding.tvTotalWeeksLived.text = FormatUtils.formatNumber(totalWeeks.toDouble(), 0)

        // Next Birthday calculation
        val nextBday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, birthCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (nextBday.before(todayMidnight)) {
            nextBday.add(Calendar.YEAR, 1)
        }

        val daysUntilBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - todayMidnight.timeInMillis)
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeek = dayOfWeekFormat.format(nextBday.time)

        if (daysUntilBday == 0L) {
            binding.tvNextBirthday.text = "Today! 🎉 Happy Birthday!"
        } else {
            binding.tvNextBirthday.text = "In $daysUntilBday days ($dayOfWeek)"
        }
    }
}
