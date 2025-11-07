package com.jiashuaixu.diting

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var recordButton: Button
    private lateinit var statusText: TextView
    
    private var recordingService: RecordingService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RecordingService.RecordingBinder
            recordingService = binder.getService()
            isServiceBound = true
            updateUI()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
            isServiceBound = false
        }
    }
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        recordButton = findViewById(R.id.recordButton)
        statusText = findViewById(R.id.statusText)
        
        recordButton.setOnClickListener {
            if (checkPermissions()) {
                toggleRecording()
            } else {
                requestPermissions()
            }
        }
        
        // Check and request permissions on startup
        if (!checkPermissions()) {
            requestPermissions()
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Bind to service if it's running
        val intent = Intent(this, RecordingService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_required),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun toggleRecording() {
        if (recordingService?.isRecording() == true) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    
    private fun startRecording() {
        val intent = Intent(this, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START_RECORDING
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        // Bind to service to get updates
        bindService(
            Intent(this, RecordingService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        
        updateUI()
    }
    
    private fun stopRecording() {
        val intent = Intent(this, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        startService(intent)
        
        updateUI()
    }
    
    private fun updateUI() {
        val isRecording = recordingService?.isRecording() ?: false
        
        runOnUiThread {
            if (isRecording) {
                recordButton.text = getString(R.string.stop_recording)
                statusText.text = getString(R.string.status_recording)
            } else {
                recordButton.text = getString(R.string.start_recording)
                statusText.text = getString(R.string.status_idle)
            }
        }
    }
}
