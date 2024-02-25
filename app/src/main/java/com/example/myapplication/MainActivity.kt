package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import `in`.museinc.android.surr_core.recorder.OnInfoListener
import `in`.museinc.android.surr_core.recorder.OnLiveStreamListener
import `in`.museinc.android.surr_core.recorder.TaalRecorder
import `in`.museinc.android.surr_core.recorder.TaalRecorderState
import `in`.museinc.android.surr_core.taalConnectionUtils.TaalConnectionBroadcastReceiver
import `in`.museinc.android.surr_core.taalConnectionUtils.TaalConnectionListener
import `in`.museinc.android.surr_core.utils.PreFilter
import java.io.File

class MainActivity : AppCompatActivity(), OnInfoListener, OnLiveStreamListener {

    private lateinit var binding: ActivityMainBinding
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private val WRITE_EXTERNAL_STORAGE = 102
    private val USB_PERMISSION_CODE = 102
    private lateinit var taalRecorder: TaalRecorder
    private val TAG = "stethoscope"
    private val storageDir = "/storage/emulated/0/Android/data/com.example.myapplication/files/Stethoscope Data"
    private lateinit var onInfoListener: OnInfoListener
    private lateinit var onLiveStreamListener: OnLiveStreamListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        taalConnectionBroadcastManager.register(this)
        createDirectory()

        taalRecorder = TaalRecorder(this)
        taalRecorder.setRawAudioFilePath(storageDir)
        taalRecorder.setPreFilteredAudioFilePath(storageDir)
        taalRecorder.setPlayback(true)
        taalRecorder.setRecordingTime(30)
        taalRecorder.setPreFilter(PreFilter.HEART)
        taalRecorder.setPreAmplification(5)
        taalRecorder.setOnInfoListener(onInfoListener)
        taalRecorder.setOnLiveStreamListener(onLiveStreamListener = onLiveStreamListener)

        binding.discover.setOnClickListener {
            if (binding.status.text.equals("connected")) {
                taalRecorder.start()
            } else {
                Toast.makeText(this, "Device is not connected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createDirectory(){
        val directory = File(getExternalFilesDir(null), "Stethoscope Data")
        if (!directory.exists()) {
            directory.mkdir()
            Log.d(TAG, "Directory created successfully at: ${directory.absolutePath}")
        } else {
            Log.d(TAG, "Directory already exist at : ${directory.absolutePath}")
        }
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

    override fun onStateChange(state: TaalRecorderState) {
        Log.d(TAG, state.toString())
        binding.status.text = state.toString()
    }

    override fun onProgressUpdate(
        sampleRate: Int,
        bufferSize: Int,
        timeStamp: Double,
        data: FloatArray,
    ) {
        Log.d(TAG, "Sample Rate : $sampleRate" +
                "Buffer Size : $bufferSize" +
                "Time Stamp : $timeStamp" +
                "Data : $data")
    }

    override fun onNewStream(stream: ByteArray) {
        Log.d(TAG, "Stream $stream")
    }
}
