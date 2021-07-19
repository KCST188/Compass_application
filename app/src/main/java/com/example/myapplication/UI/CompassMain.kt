package com.example.myapplication.UI


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
import com.example.myapplication.R
import com.example.myapplication.calculations.Calculations
import com.example.myapplication.data.Destination
import kotlin.math.*

class CompassMain : AppCompatActivity(), LocationListener, SensorEventListener {
    //number of required permissions
    private val requestLocation = 2

    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLocation()
        //get sensor service
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startCompass()
        //after clicking the button do event from AddingDestination class
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
    //set location when it changes
    override fun onLocationChanged(p0: Location) {
        setLocation()
    }
    private var destinationDistance: Int = 0
    private var destinationAzimuth: Int = 0
    var destLatitude = 36.45773
    var destLongitude = -113.32985
    val calculations = Calculations()

    @SuppressLint("SetTextI18n")
    fun setLocation() {
        //check if permissions are not granted and if they are not give them
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            requestLocation)
        }else {
            //getting system service
            val locationManger = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManger.getBestProvider(criteria, false)
            val location = provider?.let { locationManger.getLastKnownLocation(it) }
            //request location update every second with no minimal distance
            locationManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
            if (location != null) {
                val locLati = location.latitude
                val locLong = location.longitude
                destinationAzimuth = calculations.calculateAzimuth(locLati, locLong, destLatitude, destLongitude)
                destinationDistance = calculations.calculateDistance()
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
    //variables for onSensorChanged function
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var azimuth: Int = 0
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            //get azimuth from rotation Matrix and orientation of phone
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0].toDouble())+360).toInt()%360
            }
            //check if both accelerometer and magnetometer sensors are set
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                lastAccelerometerSet = true
            }else if(event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                lastMagnetometerSet = true
            }
            //if both are set get azimuth from their data
            if(lastAccelerometerSet && lastMagnetometerSet){
                SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth = (orientation[0] * 180/ PI).roundToInt()
            }
            //rotate images with given angle
            val compassImage = findViewById<ImageView>(R.id.compass)
            compassImage.rotation = (-azimuth).toFloat()
            val arrow = findViewById<ImageView>(R.id.arrow)
            arrow.rotation = (destinationAzimuth - azimuth).toFloat()
            setLocation()
        }
    }
    //variables for startCompass and stopCompass functions
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var rotationVector: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var haveSensorRotation = false

    private fun startCompass() {
        //check if sensors are enabled if any of them is disabled send alert message
        if(sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
            if(sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null
                || sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                noSensorAlert()
                //if sensors are enabled register data with UI delay
            }else{
                accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer = sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                haveSensorMagnetometer = sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            }
            //if rotation vector is enabled register data with UI delay
        }else {
            rotationVector = sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotation = sensorManager!!.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopCompass() {
        //stop registering data
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
    //start compass on resume
    override fun onResume() {
        super.onResume()
        startCompass()
    }
    //stop compass on pause
    override fun onPause() {
        super.onPause()
        stopCompass()
    }

    //empty function which is mandatory
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}