package com.example.myapplication.calculations

import android.location.Location
import kotlin.math.*

class Calculations {
    //variables needed for calculation functions
    private var a: Double = 0.0
    private var c: Double = 0.0
    private var d: Double = 0.0
    private var deltaFi: Double = 0.0
    private var deltaLambda: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var destLatitudeRad: Double = 0.0
    private var destLongitudeRad: Double = 0.0

    fun calculateAzimuth(locLati: Double, locLong: Double, x: Double, y: Double): Int {
        //change latitude and longitude of 2 coordinates to radians
        destLongitudeRad = Math.toRadians(y)
        destLatitudeRad = Math.toRadians(x)
        latitude = Math.toRadians(locLati)
        longitude = Math.toRadians(locLong)
        deltaLambda = destLongitudeRad - longitude
        deltaFi = destLatitudeRad - latitude
        //calculate destination point azimuth from given function
        var destAzi = (Math.toDegrees(
            atan2(
                sin(deltaLambda) * cos(destLatitudeRad), (cos(latitude) * sin(destLatitudeRad) -
                        sin(latitude) * cos(destLatitudeRad) * cos(deltaLambda)))
        )).toInt()
        destAzi = destAzi.toFloat().roundToInt()
        return destAzi
    }
    fun calculateDistance(): Int {
        //calculate destination as 3 variables and in the end changed result from Double to Int
        a =
            ((sin(deltaFi / 2)).pow(2) + (cos(latitude) * cos(destLatitudeRad) * sin(deltaLambda / 2).pow(
                2
            )))
        c = 2 * atan2(sqrt(a), sqrt(1 - a))
        d = 6371 * c * 1000
        return d.toInt()
    }

}