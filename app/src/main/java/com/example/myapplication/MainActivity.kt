package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.myapplication.data.Destination
import kotlin.math.*

class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {
    private val requestLocation = 2

    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocation()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startCompass()
        val destination = findViewById<Button>(R.id.destiantion)
        destination.setOnClickListener {
            setLocation()
            AddingDestination(this, object : AddDialogListener {
                override fun onAddButtonClicked(item: Destination) {
                    destLongitude = item.longitude
                    destLatitude = item.latitude
                }
            }).show()
        }
    }

    override fun onLocationChanged(p0: Location) {
        setLocation()
    }
    @SuppressLint("SetTextI18n")
    private fun setLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            requestLocation)
        }else {
            val locationManger = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManger.getBestProvider(criteria, false)
            val location = provider?.let { locationManger.getLastKnownLocation(it) }

            locationManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
            if (location != null) {
                destLongitudeRad = Math.toRadians(destLongitude)
                destLatitudeRad = Math.toRadians(destLatitude)
                latitude = Math.toRadians(location.latitude)
                longitude = Math.toRadians(location.longitude)
                deltaLambda = destLongitudeRad - longitude
                deltaFi = destLatitudeRad - latitude
                destinationAzimuth = (Math.toDegrees(atan2(sin(deltaLambda)* cos(destLatitudeRad), (cos(latitude)* sin(destLatitudeRad) -
                        sin(latitude)* cos(destLatitudeRad)* cos(deltaLambda))))).toInt()
                destinationAzimuth = destinationAzimuth.toFloat().roundToInt()

                a = ((sin(deltaFi/2)).pow(2) + (cos(latitude) * cos(destLatitudeRad) * sin(deltaLambda/2).pow(2)))
                c = 2 * atan2(sqrt(a), sqrt(1-a))
                d = 6371 * c *1000
                destinationDistance = d.toInt()
                val distance = findViewById<TextView>(R.id.Distance)
                distance.text = "Distance to the destination: $destinationDistance m"
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == requestLocation) setLocation()
    }
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var azimuth: Int = 0
    private var destinationAzimuth: Int = 0
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private var destLatitude = 51.45773
    private var destLongitude = 22.32985
    private var destinationDistance: Int = 0
    private var a: Double = 0.0
    private var c: Double = 0.0
    private var d: Double = 0.0
    private var deltaFi: Double = 0.0
    private var deltaLambda: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var destLatitudeRad: Double = 0.0
    private var destLongitudeRad: Double = 0.0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble())+360).toInt()%360
            }
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                lastAccelerometerSet = true
            }else if(event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                lastMagnetometerSet = true
            }
            if(lastAccelerometerSet && lastMagnetometerSet){
                SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = (orientation[0] * 180/ PI).roundToInt()
            }
            val compassImage = findViewById<ImageView>(R.id.compass)
            compassImage.rotation = (-azimuth).toFloat()
            val arrow = findViewById<ImageView>(R.id.arrow)
            arrow.rotation = (destinationAzimuth - azimuth).toFloat()
            setLocation()
        }
    }
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var rotationVector: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var haveSensorRotation = false

    private fun startCompass() {
        if(sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
            if(sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null
                || sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                noSensorAlert()
            }else{
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer = sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                haveSensorMagnetometer = sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            }
        }else {
            rotationVector = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotation = sensorManager!!.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }
    private fun stopCompass() {
        if(haveSensorAccelerometer) sensorManager!!.unregisterListener(this, accelerometer)
        if(haveSensorMagnetometer) sensorManager!!.unregisterListener(this, magnetometer)
        if(haveSensorRotation) sensorManager!!.unregisterListener(this, rotationVector)
    }
    private fun noSensorAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Your device does not support a compass")
            .setCancelable(false)
            .setNegativeButton("Close"){_,_ -> finish()}
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        startCompass()
    }

    override fun onPause() {
        super.onPause()
        stopCompass()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}