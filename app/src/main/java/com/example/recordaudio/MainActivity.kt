package com.example.recordaudio

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val READ_MEDIA_AUDIO_PERMISSION = Manifest.permission.READ_MEDIA_AUDIO
    private val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

    // ✅ Use registerForActivityResult to request permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[RECORD_AUDIO_PERMISSION] ?: false
        val readMediaAudioGranted = permissions[READ_MEDIA_AUDIO_PERMISSION] ?: false
        val writeStorageGranted = permissions[WRITE_EXTERNAL_STORAGE_PERMISSION] ?: false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (recordAudioGranted && readMediaAudioGranted) {
                startService(Intent(this, AudioRecordService::class.java))
            } else {
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show()
            }
        } else { // Android 12 and below
            if (recordAudioGranted && writeStorageGranted) {
                startService(Intent(this, AudioRecordService::class.java))
            } else {
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            if (checkPermissions()) {
                startService(Intent(this, AudioRecordService::class.java))
            } else {
                requestPermissions()
            }
        }

        stopButton.setOnClickListener {
            stopService(Intent(this, AudioRecordService::class.java))
        }
    }



    // ✅ Check permissions based on Android version
    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, READ_MEDIA_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED
        } else { // Android 12 and below
            ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ Request permissions dynamically
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissionLauncher.launch(arrayOf(RECORD_AUDIO_PERMISSION, READ_MEDIA_AUDIO_PERMISSION))
        } else { // Android 12 and below
            permissionLauncher.launch(arrayOf(RECORD_AUDIO_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSION))
        }
    }
}