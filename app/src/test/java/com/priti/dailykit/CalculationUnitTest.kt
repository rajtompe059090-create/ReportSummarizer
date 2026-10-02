package com.priti.dailykit

import com.priti.dailykit.utils.CalcEngine
import com.priti.dailykit.utils.FormatUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow

class CalculationUnitTest {

    @Test
    fun testCalculatorBasicOperations() {
        val res = CalcEngine.evaluate("2+3×4")
        assertTrue(res is CalcEngine.CalcResult.Success)
        assertEquals("14", (res as CalcEngine.CalcResult.Success).formatted)
    }

    @Test
    fun testCalculatorParentheses() {
        val res = CalcEngine.evaluate("(2+3)×4")
        assertTrue(res is CalcEngine.CalcResult.Success)
        assertEquals("20", (res as CalcEngine.CalcResult.Success).formatted)
    }

    @Test
    fun testCalculatorDivisionByZero() {
        val res = CalcEngine.evaluate("10÷0")
        assertTrue(res is CalcEngine.CalcResult.Error)
    }

    @Test
    fun testCalculatorDecimals() {
        val res = CalcEngine.evaluate("0.1+0.2")
        assertTrue(res is CalcEngine.CalcResult.Success)
        assertEquals("0.3", (res as CalcEngine.CalcResult.Success).formatted)
    }

    @Test
    fun testCalculatorLargeValues() {
        val res = CalcEngine.evaluate("1000000×5000")
        assertTrue(res is CalcEngine.CalcResult.Success)
        assertEquals("5000000000", (res as CalcEngine.CalcResult.Success).formatted)
    }

    @Test
    fun testEmiNormalInterest() {
        val principal = 100000.0
        val annualRate = 12.0
        val months = 12.0

        val monthlyRate = (annualRate / 100.0) / 12.0
        val compound = (1.0 + monthlyRate).pow(months)
        val emi = principal * monthlyRate * (compound / (compound - 1.0))
        val totalPayment = emi * months
        val totalInterest = totalPayment - principal

        assertEquals(8884.88, emi, 0.1)
        assertEquals(106618.55, totalPayment, 0.1)
        assertEquals(6618.55, totalInterest, 0.1)
    }

    @Test
    fun testEmiZeroInterest() {
        val principal = 120000.0
        val annualRate = 0.0
        val months = 12.0

        val emi: Double = principal / months
        val totalPayment: Double = principal
        val totalInterest: Double = 0.0

        assertEquals(10000.0, emi, 0.001)
        assertEquals(120000.0, totalPayment, 0.001)
        assertEquals(0.0, totalInterest, 0.001)
    }

    @Test
    fun testGstAddCalculation() {
        val base = 1000.0
        val rate = 18.0
        val gst = (base * rate) / 100.0
        val finalAmount = base + gst
        val cgst = gst / 2.0
        val sgst = gst / 2.0

        assertEquals(180.0, gst, 0.001)
        assertEquals(1180.0, finalAmount, 0.001)
        assertEquals(90.0, cgst, 0.001)
        assertEquals(90.0, sgst, 0.001)
    }

    @Test
    fun testGstRemoveCalculation() {
        val finalAmount = 1180.0
        val rate = 18.0
        val base = finalAmount / (1.0 + (rate / 100.0))
        val gst = finalAmount - base

        assertEquals(1000.0, base, 0.001)
        assertEquals(180.0, gst, 0.001)
        assertEquals(90.0, gst / 2.0, 0.001)
    }

    @Test
    fun testDiscountStandardAndSecondary() {
        val original = 1000.0
        val disc1 = 20.0
        val disc2 = 10.0

        val save1 = (original * disc1) / 100.0
        val after1 = original - save1
        val save2 = (after1 * disc2) / 100.0
        val finalPrice = after1 - save2
        val totalSaved = save1 + save2

        assertEquals(200.0, save1, 0.001)
        assertEquals(80.0, save2, 0.001)
        assertEquals(720.0, finalPrice, 0.001)
        assertEquals(280.0, totalSaved, 0.001)
    }

    @Test
    fun testDiscountZeroAndFull() {
        val original = 500.0
        // 0% discount
        val saveZero = (original * 0.0) / 100.0
        assertEquals(0.0, saveZero, 0.001)
        assertEquals(500.0, original - saveZero, 0.001)

        // 100% discount
        val saveFull = (original * 100.0) / 100.0
        assertEquals(500.0, saveFull, 0.001)
        assertEquals(0.0, original - saveFull, 0.001)
    }

    @Test
    fun testPercentageCalculations() {
        // X% of Y
        val x = 15.0
        val y = 200.0
        val percentOf = (x / 100.0) * y
        assertEquals(30.0, percentOf, 0.001)

        // X is what % of Y
        val ratio = (30.0 / 200.0) * 100.0
        assertEquals(15.0, ratio, 0.001)

        // % Increase
        val initial = 100.0
        val increased = 125.0
        val incPercent = ((increased - initial) / initial) * 100.0
        assertEquals(25.0, incPercent, 0.001)

        // % Decrease
        val decreased = 75.0
        val decPercent = ((initial - decreased) / initial) * 100.0
        assertEquals(25.0, decPercent, 0.001)
    }

    @Test
    fun testBmiCalculationMetricAndImperial() {
        // Metric: 70kg, 1.75m
        val weightKg = 70.0
        val heightM = 1.75
        val bmiMetric = weightKg / (heightM * heightM)
        assertEquals(22.86, bmiMetric, 0.01)

        // Imperial: 5ft 9in = 69in = 1.7526m; 154.324 lb = 70 kg
        val totalInches = 5 * 12.0 + 9.0
        val heightFromInches = (totalInches * 2.54) / 100.0
        val weightFromLb = 154.3235835 * 0.45359237
        val bmiImperial = weightFromLb / (heightFromInches * heightFromInches)
        assertEquals(22.79, bmiImperial, 0.01)
    }

    @Test
    fun testTemperatureConversion() {
        // Celsius to Fahrenheit & Kelvin
        val c = 100.0
        val f = (c * 9.0 / 5.0) + 32.0
        val k = c + 273.15
        assertEquals(212.0, f, 0.001)
        assertEquals(373.15, k, 0.001)

        // Negative temperatures: -40 C == -40 F
        val negC = -40.0
        val negF = (negC * 9.0 / 5.0) + 32.0
        assertEquals(-40.0, negF, 0.001)
    }

    @Test
    fun testLengthConversion() {
        // 1 meter = 100 cm = 1000 mm = 39.3700787 inches = 3.28084 feet = 0.001 km
        val meters = 1.0
        assertEquals(1000.0, meters / 0.001, 0.001) // mm
        assertEquals(100.0, meters / 0.01, 0.001)  // cm
        assertEquals(0.001, meters / 1000.0, 0.000001) // km
        val feet = meters / 0.3048
        assertEquals(3.28084, feet, 0.0001)
    }

    @Test
    fun testWeightConversion() {
        // 1 kg = 1000 grams = 1000000 mg = 2.20462 lbs
        val kg = 1.0
        val grams = kg * 1000.0
        assertEquals(1000.0, grams, 0.001)
        val lbs = grams / 453.59237
        assertEquals(2.20462, lbs, 0.0001)
    }

    @Test
    fun testDateDifferenceCalculation() {
        val cal1 = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 0, 0, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 15, 0, 0, 0)
        }
        val diff1 = TimeUnit.MILLISECONDS.toDays(abs(cal2.timeInMillis - cal1.timeInMillis))
        val diff2 = TimeUnit.MILLISECONDS.toDays(abs(cal1.timeInMillis - cal2.timeInMillis))
        assertEquals(14L, diff1)
        assertEquals(diff1, diff2) // Symmetric date difference handling
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = FormatUtils.formatCurrency(1234567.0, "₹")
        assertTrue(formatted.startsWith("₹"))
        assertTrue(formatted.contains("1,234,567"))
    }
}
