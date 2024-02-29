package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import `in`.museinc.android.surr_core.recorder.OnInfoListener
import `in`.museinc.android.surr_core.recorder.OnLiveStreamListener
import `in`.museinc.android.surr_core.recorder.TaalRecorder
import `in`.museinc.android.surr_core.recorder.TaalRecorderState
import `in`.museinc.android.surr_core.utils.PreFilter
import `in`.museinc.android.surr_core.utils.SurrUtils
import `in`.museinc.android.surr_core.utils.TaalConnectivityStatus
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), OnInfoListener, OnLiveStreamListener{

    private lateinit var binding: ActivityMainBinding
    private val TAG = "stethoscope"
    private lateinit var taalRecorder: TaalRecorder
    private var recordingTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taalRecorder = TaalRecorder(this)
        val recordedFilesList = mutableListOf<String>()


        binding.recordingTime.setOnClickListener {
            showTimeOptionsDialog()
        }

        binding.recordView.setOnClickListener {
            if (SurrUtils.isTaalDeviceConnected(this) == TaalConnectivityStatus.CONNECTED) {
                val rawFile = createFile("raw")
                val filterFile = createFile("filter")
                Log.d(TAG, rawFile.absolutePath + filterFile.absolutePath)
                if (rawFile.exists() && filterFile.exists()) {
                    recordedFilesList.add(rawFile.absolutePath)
                    recordedFilesList.add(filterFile.absolutePath)
                    if (recordingTime > 0) {
                        taalRecording(rawFile.absolutePath, filterFile.absolutePath, recordingTime)
                    } else {
                        Toast.makeText(this, "Select Recording Time", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "Files not exist")
                    Toast.makeText(this, "File not exist", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Taal is not connected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.importView.setOnClickListener {
            val path = getExternalFilesDir(null)?.absolutePath
            if (path != null) {
                showFilesInDirectory(path)
            }
        }

    }


    private fun showFilesInDirectory(directoryPath: String) {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null && files.isNotEmpty()) {
                val fileNamesList = files.map { it.name }
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Files in Directory")

                dialogBuilder.setItems(fileNamesList.toTypedArray()) { dialog, which ->
                    val selectedFile = files[which]
                    val selectedFilePath = selectedFile.absolutePath
                    Log.d(TAG, "Selected file: $selectedFilePath")
                    playFile(selectedFilePath)
                }
                val dialog = dialogBuilder.create()
                dialog.show()
            } else {
                Log.d(TAG, "Directory is empty")
                Toast.makeText(this, "Directory is empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Directory does not exist or is not a directory")
            Toast.makeText(this, "Directory does not exist or is not a directory", Toast.LENGTH_SHORT).show()
        }
    }

    fun playFile(filePath: String) {
        val intent = Intent(this, MediaPlayerActivity::class.java)
        intent.putExtra("url", filePath)
        startActivity(intent)
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val selectedFileUri = data?.data
            if (selectedFileUri != null) {
                val selectedFilePath = selectedFileUri.path
                Toast.makeText(this, "Selected file: $selectedFilePath", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createFile(extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timeStamp\\_$extension.wav"
        val path = getExternalFilesDir(null)?.absolutePath
        val file = File(path, fileName)

        try {
            if (file.createNewFile()) {
                Log.d(TAG, "File created successfully at: ${file.absolutePath}")
            } else {
                Log.e(TAG, "File creation failed: ${file.absolutePath}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "IOException occurred: ${e.message}")
        }

        return file
    }

    private fun showTimeOptionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recording_time, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn30Seconds).setOnClickListener {
            binding.tvRecordingTime.text = "00:00:30"
            recordingTime = 30
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn15Seconds).setOnClickListener {
            binding.tvRecordingTime.text = "00:00:15"
            recordingTime = 15
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun taalRecording(rawAudioFilePath: String, preFilterAudioFilePath: String, recordingTime: Int) {
        taalRecorder = TaalRecorder(this)
        taalRecorder.setRawAudioFilePath(rawAudioFilePath)
        taalRecorder.setPreFilteredAudioFilePath(preFilterAudioFilePath)
        taalRecorder.setRecordingTime(recordingTime)
        taalRecorder.setPlayback(true)
        taalRecorder.setPreAmplification(5)
        taalRecorder.setPreFilter(PreFilter.HEART)
        taalRecorder.setOnInfoListener(this)
        taalRecorder.setOnLiveStreamListener(this)
        taalRecorder.start()
    }

    override fun onProgressUpdate(
        sampleRate: Int,
        bufferSize: Int,
        timeStamp: Double,
        data: FloatArray,
    ) {
        Log.d("progress", "$sampleRate $bufferSize $timeStamp $data")
    }

    override fun onStateChange(state: TaalRecorderState) {
        if (state == TaalRecorderState.RECORDING) {
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Recording state")
        } else if (state == TaalRecorderState.INITIAL) {
            Log.d(TAG, "Initial state")
        } else {
            Log.d(TAG, "Stopped state")
        }
    }

    override fun onNewStream(stream: ByteArray) {
        Log.d("stream", stream.toString())
    }

    companion object {
        private const val FILE_REQUEST_CODE = 123
    }

}
