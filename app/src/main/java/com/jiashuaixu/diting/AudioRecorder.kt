package com.jiashuaixu.diting

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs

class AudioRecorder(private val outputDir: File) {
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    
    // Audio configuration
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    
    // Noise gate configuration
    private val noiseThreshold = 500 // Threshold for noise gating (adjust as needed)
    
    companion object {
        private const val TAG = "AudioRecorder"
    }
    
    fun startRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            recordingThread = Thread { recordAudio() }
            recordingThread?.start()
            
            Log.i(TAG, "Recording started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording: ${e.message}")
        }
    }
    
    fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return
        }
        
        isRecording = false
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            
            recordingThread?.join()
            recordingThread = null
            
            Log.i(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}")
        }
    }
    
    private fun recordAudio() {
        val audioData = ShortArray(bufferSize)
        val timestamp = System.currentTimeMillis()
        val outputFile = File(outputDir, "recording_$timestamp.pcm")
        
        var outputStream: FileOutputStream? = null
        
        try {
            outputStream = FileOutputStream(outputFile)
            Log.i(TAG, "Writing to file: ${outputFile.absolutePath}")
            
            while (isRecording) {
                val readSize = audioRecord?.read(audioData, 0, audioData.size) ?: 0
                
                if (readSize > 0) {
                    // Apply noise gating
                    val processedData = applyNoiseGate(audioData, readSize)
                    
                    // Convert short array to byte array and write
                    val byteData = shortArrayToByteArray(processedData, readSize)
                    outputStream.write(byteData)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error writing audio data: ${e.message}")
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing output stream: ${e.message}")
            }
        }
    }
    
    /**
     * Apply noise gate - filter out audio below threshold
     */
    private fun applyNoiseGate(audioData: ShortArray, size: Int): ShortArray {
        val result = ShortArray(size)
        
        for (i in 0 until size) {
            val amplitude = abs(audioData[i].toInt())
            result[i] = if (amplitude > noiseThreshold) {
                audioData[i]
            } else {
                0 // Silence samples below threshold
            }
        }
        
        return result
    }
    
    /**
     * Convert short array to byte array
     */
    private fun shortArrayToByteArray(shorts: ShortArray, size: Int): ByteArray {
        val bytes = ByteArray(size * 2)
        for (i in 0 until size) {
            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = ((shorts[i].toInt() shr 8) and 0xFF).toByte()
        }
        return bytes
    }
    
    fun isRecording(): Boolean = isRecording
}
