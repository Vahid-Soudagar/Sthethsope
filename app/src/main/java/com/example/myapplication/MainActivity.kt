package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import `in`.museinc.android.surr_core.taalConnectionUtils.TaalConnectionBroadcastReceiver
import `in`.museinc.android.surr_core.taalConnectionUtils.TaalConnectionListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private val USB_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        // Initialize and register TaalConnectionBroadcastReceiver
        taalConnectionBroadcastManager.register(this)
    }



    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }
    val taalConnectionBroadcastManager = TaalConnectionBroadcastReceiver(object :
        TaalConnectionListener {
        override fun onTaalConnect() {
            Log.d("myTag", "Taal Connected")
            binding.status.text = "Connected"
        }

        override fun onTaalDisconnect() {
            binding.status.text = "Disconnected"
        }
    })

    override fun onDestroy() {
        super.onDestroy()
        taalConnectionBroadcastManager.unregister(this)
    }
}
