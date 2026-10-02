package com.priti.dailykit.utils

import com.priti.dailykit.R
import com.priti.dailykit.activities.*
import com.priti.dailykit.models.ToolCategory
import com.priti.dailykit.models.ToolItem

object ToolsRegistry {
    val allTools: List<ToolItem> = listOf(
        ToolItem(
            id = "calculator",
            titleRes = R.string.tool_calculator_title,
            descRes = R.string.tool_calculator_desc,
            iconRes = R.drawable.ic_tool_calculator,
            category = ToolCategory.CALCULATORS,
            accentColorRes = R.color.accent_blue,
            isPopular = true,
            isQuick = true,
            targetActivityClass = CalculatorActivity::class.java
        ),
        ToolItem(
            id = "emi",
            titleRes = R.string.tool_emi_title,
            descRes = R.string.tool_emi_desc,
            iconRes = R.drawable.ic_tool_emi,
            category = ToolCategory.CALCULATORS,
            accentColorRes = R.color.accent_violet,
            isPopular = true,
            isQuick = true,
            targetActivityClass = EmiCalculatorActivity::class.java
        ),
        ToolItem(
            id = "gst",
            titleRes = R.string.tool_gst_title,
            descRes = R.string.tool_gst_desc,
            iconRes = R.drawable.ic_tool_gst,
            category = ToolCategory.CALCULATORS,
            accentColorRes = R.color.accent_amber,
            isPopular = true,
            isQuick = true,
            targetActivityClass = GstCalculatorActivity::class.java
        ),
        ToolItem(
            id = "discount",
            titleRes = R.string.tool_discount_title,
            descRes = R.string.tool_discount_desc,
            iconRes = R.drawable.ic_tool_discount,
            category = ToolCategory.CALCULATORS,
            accentColorRes = R.color.accent_rose,
            isPopular = true,
            isQuick = true,
            targetActivityClass = DiscountCalculatorActivity::class.java
        ),
        ToolItem(
            id = "percentage",
            titleRes = R.string.tool_percentage_title,
            descRes = R.string.tool_percentage_desc,
            iconRes = R.drawable.ic_tool_percentage,
            category = ToolCategory.CALCULATORS,
            accentColorRes = R.color.accent_blue,
            isPopular = false,
            isQuick = true,
            targetActivityClass = PercentageCalculatorActivity::class.java
        ),
        ToolItem(
            id = "age",
            titleRes = R.string.tool_age_title,
            descRes = R.string.tool_age_desc,
            iconRes = R.drawable.ic_tool_age,
            category = ToolCategory.DATE_TIME,
            accentColorRes = R.color.accent_cyan,
            isPopular = true,
            isQuick = true,
            targetActivityClass = AgeCalculatorActivity::class.java
        ),
        ToolItem(
            id = "date_diff",
            titleRes = R.string.tool_date_diff_title,
            descRes = R.string.tool_date_diff_desc,
            iconRes = R.drawable.ic_tool_date_diff,
            category = ToolCategory.DATE_TIME,
            accentColorRes = R.color.accent_violet,
            isPopular = false,
            isQuick = false,
            targetActivityClass = DateDifferenceActivity::class.java
        ),
        ToolItem(
            id = "countdown",
            titleRes = R.string.tool_countdown_title,
            descRes = R.string.tool_countdown_desc,
            iconRes = R.drawable.ic_tool_countdown,
            category = ToolCategory.DATE_TIME,
            accentColorRes = R.color.accent_cyan,
            isPopular = false,
            isQuick = false,
            targetActivityClass = CountdownActivity::class.java
        ),
        ToolItem(
            id = "bmi",
            titleRes = R.string.tool_bmi_title,
            descRes = R.string.tool_bmi_desc,
            iconRes = R.drawable.ic_tool_bmi,
            category = ToolCategory.HEALTH,
            accentColorRes = R.color.accent_green,
            isPopular = true,
            isQuick = false,
            targetActivityClass = BmiCalculatorActivity::class.java
        ),
        ToolItem(
            id = "unit_converter",
            titleRes = R.string.tool_unit_converter_title,
            descRes = R.string.tool_unit_converter_desc,
            iconRes = R.drawable.ic_tool_converter,
            category = ToolCategory.CONVERTERS,
            accentColorRes = R.color.accent_amber,
            isPopular = true,
            isQuick = false,
            targetActivityClass = UnitConverterActivity::class.java
        ),
        ToolItem(
            id = "notes",
            titleRes = R.string.tool_notes_title,
            descRes = R.string.tool_notes_desc,
            iconRes = R.drawable.ic_tool_notes,
            category = ToolCategory.PRODUCTIVITY,
            accentColorRes = R.color.accent_violet,
            isPopular = true,
            isQuick = true,
            targetActivityClass = NotesActivity::class.java
        )
    )

    fun getToolById(id: String): ToolItem? = allTools.find { it.id == id }

    fun getQuickTools(): List<ToolItem> = allTools.filter { it.isQuick }

    fun getPopularTools(): List<ToolItem> = allTools.filter { it.isPopular }

    fun getToolsByCategory(category: ToolCategory): List<ToolItem> {
        return if (category == ToolCategory.ALL) allTools else allTools.filter { it.category == category }
    }
}
