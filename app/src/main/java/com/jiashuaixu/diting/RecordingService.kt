package com.jiashuaixu.diting

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class RecordingService : Service() {
    
    private val binder = RecordingBinder()
    private var audioRecorder: AudioRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    inner class RecordingBinder : Binder() {
        fun getService(): RecordingService = this@RecordingService
    }
    
    companion object {
        private const val TAG = "RecordingService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "audio_recording_channel"
        const val ACTION_START_RECORDING = "com.jiashuaixu.diting.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.jiashuaixu.diting.STOP_RECORDING"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        
        // Initialize audio recorder
        val outputDir = File(getExternalFilesDir(null), "recordings")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        audioRecorder = AudioRecorder(outputDir)
        
        // Acquire wake lock to keep CPU running
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DiTing::RecordingWakeLock"
        )
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                startForegroundService()
                startRecording()
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    fun startRecording() {
        Log.i(TAG, "Starting recording")
        wakeLock?.acquire()
        audioRecorder?.startRecording()
    }
    
    fun stopRecording() {
        Log.i(TAG, "Stopping recording")
        audioRecorder?.stopRecording()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
    
    fun isRecording(): Boolean {
        return audioRecorder?.isRecording() ?: false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
        stopRecording()
        audioRecorder = null
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
}
