package com.example.recordaudio

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startRecording()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        val channelId = "AudioRecordServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Audio Recording Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Audio Recording")
            .setContentText("Recording audio in the background...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)

        } else {
            startForeground(1, notification)

        }

    }

    private fun startRecording() {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Recordings"
        )
        if (!dir.exists()) dir.mkdirs() // Ensure the directory exists

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(dir, "recording_$timeStamp.mp3").absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile) // ✅ FIX: Use File Path instead of OutputStream

            try {
                prepare()
                start()
                Log.d("AudioRecordService", "Recording started: $outputFile")
            } catch (e: Exception) {
                Log.e("AudioRecordService", "Recording failed", e)
            }
        }
    }


    private fun stopRecording() {
        mediaRecorder?.apply {
            stopSelf()
            release()
        }
        mediaRecorder = null
        Log.d("AudioRecordService", "Recording stopped and saved: $outputFile")
    }
}