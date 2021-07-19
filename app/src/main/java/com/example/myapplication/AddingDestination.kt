package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.myapplication.data.Destination

class AddingDestination (context: Context, private var addDialogListener: AddDialogListener): AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.destination_coordinates)

        findViewById<TextView>(R.id.tvAdd)?.setOnClickListener {
            val longitudeCheck =  findViewById<TextView>(R.id.longitude)?.text.toString()
            val latitudeCheck =  findViewById<TextView>(R.id.latitude)?.text.toString()
            val longitude: Double = longitudeCheck.toDouble()
            val latitude: Double = latitudeCheck.toDouble()
            if(longitudeCheck.isEmpty() || latitudeCheck.isEmpty() || longitude > 180 || longitude < -180 || latitude > 180 || latitude < -180)
            {
                Toast.makeText(context, "Enter all information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val item = Destination(longitude, latitude)
            addDialogListener.onAddButtonClicked(item)
            dismiss()
        }
        findViewById<TextView>(R.id.tvCancel)?.setOnClickListener {
            cancel()
        }
    }
}