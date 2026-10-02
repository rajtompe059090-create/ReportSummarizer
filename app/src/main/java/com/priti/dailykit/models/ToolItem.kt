package com.priti.dailykit.models

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class ToolCategory(val id: String, @StringRes val titleRes: Int) {
    ALL("all", com.priti.dailykit.R.string.category_all),
    CALCULATORS("calculators", com.priti.dailykit.R.string.category_calculators),
    CONVERTERS("converters", com.priti.dailykit.R.string.category_converters),
    DATE_TIME("date_time", com.priti.dailykit.R.string.category_date_time),
    HEALTH("health", com.priti.dailykit.R.string.category_health),
    PRODUCTIVITY("productivity", com.priti.dailykit.R.string.category_productivity)
}

data class ToolItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val category: ToolCategory,
    @ColorRes val accentColorRes: Int,
    val isPopular: Boolean = false,
    val isQuick: Boolean = false,
    val targetActivityClass: Class<out Activity>
)
