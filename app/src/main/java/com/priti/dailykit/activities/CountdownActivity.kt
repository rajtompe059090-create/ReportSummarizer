package com.priti.dailykit.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityCountdownBinding
import com.priti.dailykit.utils.AppFeedback
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CountdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountdownBinding
    private val handler = Handler(Looper.getMainLooper())

    private var targetCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2027)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    private val tickerRunnable = object : Runnable {
        override fun run() {
            updateTicker()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateDateTimeDisplays()
        updateTicker()

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickerRunnable)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.etEventTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvDisplayTitle.text = if (s.isNullOrBlank()) "Event Countdown" else s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val openDatePicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    targetCalendar.set(Calendar.YEAR, year)
                    targetCalendar.set(Calendar.MONTH, month)
                    targetCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateTimeDisplays()
                    updateTicker()
                },
                targetCalendar.get(Calendar.YEAR),
                targetCalendar.get(Calendar.MONTH),
                targetCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val openTimePicker = {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    targetCalendar.set(Calendar.MINUTE, minute)
                    targetCalendar.set(Calendar.SECOND, 0)
                    updateDateTimeDisplays()
                    updateTicker()
                },
                targetCalendar.get(Calendar.HOUR_OF_DAY),
                targetCalendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.layoutPickDate.setOnClickListener { openDatePicker() }
        binding.btnDateAction.setOnClickListener { openDatePicker() }

        binding.layoutPickTime.setOnClickListener { openTimePicker() }
        binding.btnTimeAction.setOnClickListener { openTimePicker() }

        binding.btnShare.setOnClickListener {
            val title = binding.tvDisplayTitle.text.toString()
            val d = binding.tvDays.text.toString()
            val h = binding.tvHours.text.toString()
            val m = binding.tvMinutes.text.toString()
            val s = binding.tvSeconds.text.toString()
            val shareText = "Countdown to $title:\n$d Days, $h Hours, $m Mins, $s Secs remaining!\nDailyKit"
            AppFeedback.shareText(this, "Share Countdown", shareText)
        }
    }

    private fun updateDateTimeDisplays() {
        val dateSdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val fullSdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        binding.tvTargetDateInput.text = dateSdf.format(targetCalendar.time)
        binding.tvTargetTimeInput.text = timeSdf.format(targetCalendar.time)
        binding.tvTargetDatetime.text = "Target: ${fullSdf.format(targetCalendar.time)}"
    }

    private fun updateTicker() {
        val now = System.currentTimeMillis()
        val diff = targetCalendar.timeInMillis - now

        if (diff <= 0) {
            binding.tvDays.text = "0"
            binding.tvHours.text = "0"
            binding.tvMinutes.text = "0"
            binding.tvSeconds.text = "0"
            binding.tvCompletedBanner.visibility = View.VISIBLE
        } else {
            binding.tvCompletedBanner.visibility = View.GONE
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

            binding.tvDays.text = days.toString()
            binding.tvHours.text = String.format("%02d", hours)
            binding.tvMinutes.text = String.format("%02d", minutes)
            binding.tvSeconds.text = String.format("%02d", seconds)
        }
    }
}
