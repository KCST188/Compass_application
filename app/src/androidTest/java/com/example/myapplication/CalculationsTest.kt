package com.example.myapplication

import com.example.myapplication.calculations.Calculations
import junit.framework.TestCase
import org.junit.Test
import kotlin.math.roundToInt

class CalculationsTest : TestCase() {
    private val calculations = Calculations()
    @Test

    fun testCalculations() {
        //values taken from site: https://www.omnicalculator.com/other/azimuth
        val expectedAzimuth = 94
        val startLati = 37.3565
        val startLong = -121.9618
        val finalLati = 36.45773
        val finalLong = -113.32985
        val calculatedAzimuth = calculations.calculateAzimuth(startLati, startLong, finalLati, finalLong)
        assertEquals(expectedAzimuth, calculatedAzimuth)
        val expectedDistance = 774
        val calculatedDistance = (calculations.calculateDistance() * 0.001).roundToInt()
        assertEquals(expectedDistance, calculatedDistance)
    }


}