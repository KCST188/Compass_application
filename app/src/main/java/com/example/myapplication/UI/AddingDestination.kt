package com.example.myapplication.UI

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.myapplication.R
import com.example.myapplication.data.Destination

class AddingDestination (context: Context, private var addDialogListener: AddDialogListener): AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.destination_coordinates)
        //get latitude and longitude given by user after clicking Add button
        findViewById<TextView>(R.id.tvAdd)?.setOnClickListener {
            val longitudeCheck =  findViewById<TextView>(R.id.longitude)?.text.toString()
            val latitudeCheck =  findViewById<TextView>(R.id.latitude)?.text.toString()
            val longitude: Double = longitudeCheck.toDouble()
            val latitude: Double = latitudeCheck.toDouble()
            //check if given data is not in range and if it is empty
            if(longitudeCheck.isEmpty() || latitudeCheck.isEmpty() || longitude > 180 || longitude < -180 || latitude > 180 || latitude < -180)
            {
                //if data is out of range or is empty make Toast and let write it again
                Toast.makeText(context, "Enter all information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //write data to variables
            val item = Destination(longitude, latitude)
            addDialogListener.onAddButtonClicked(item)
            dismiss()
        }
        //cancel view after clicking Cancel button
        findViewById<TextView>(R.id.tvCancel)?.setOnClickListener {
            cancel()
        }
    }
}