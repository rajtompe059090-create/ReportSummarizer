package com.priti.dailykit.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    fun formatCurrency(amount: Double, symbol: String = "₹"): String {
        if (amount.isNaN() || amount.isInfinite()) return "$symbol 0"

        val isWhole = (amount % 1.0) == 0.0
        val pattern = if (isWhole) "#,##0" else "#,##0.00"
        val df = DecimalFormat(pattern, java.text.DecimalFormatSymbols(Locale.US))
        return "$symbol ${df.format(amount)}"
    }

    fun formatNumber(value: Double, maxDecimals: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val isWhole = (value % 1.0) == 0.0
        val pattern = if (isWhole) {
            "#,###"
        } else {
            val decimals = "#".repeat(maxDecimals)
            "#,###.$decimals"
        }
        val df = DecimalFormat(pattern, java.text.DecimalFormatSymbols(Locale.US))
        return df.format(value)
    }

    fun formatPercentage(value: Double): String {
        val df = DecimalFormat("#,##0.##", java.text.DecimalFormatSymbols(Locale.US))
        return "${df.format(value)}%"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}
