package com.example.myapplication.calculations

import junit.framework.TestCase
import org.junit.Test
import kotlin.math.roundToInt

class CalculationsTest : TestCase() {
    private val calculations = Calculations()
    @Test

    fun testCalculations() {
        //values taken from site: https://www.omnicalculator.com/other/azimuth
        val expectedAzimuth = 94
        val startLati = 36.45773
        val startLong = -113.32985
        val finalLati = 37.3565
        val finalLong = -121.9618
        val calculatedAzimuth = calculations.calculateAzimuth(startLati, startLong, finalLati, finalLong)
        assertEquals(expectedAzimuth, calculatedAzimuth)
        val expectedDistance = 774
        val calculatedDistance = (calculations.calculateDistance() * 0.001).roundToInt()
        assertEquals(expectedDistance, calculatedDistance)
    }


}