package com.example.urdu_keyboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.example.urdu_keyboard.channel.FlutterChannelHandler
import com.example.urdu_keyboard.utils.PreferenceHelper

class MainActivity : FlutterActivity() {
    
    private lateinit var channelHandler: FlutterChannelHandler
    private lateinit var preferenceHelper: PreferenceHelper
    
    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 100
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Initialize preference helper
        preferenceHelper = PreferenceHelper(this)
        
        // Setup Flutter method channel
        channelHandler = FlutterChannelHandler(this, preferenceHelper)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            FlutterChannelHandler.CHANNEL_NAME
        ).setMethodCallHandler(channelHandler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request microphone permission for voice input
        requestMicrophonePermission()
    }

    /**
     * Request microphone permission for voice input
     */
    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_CODE -> {
                // Permission result handled
                // Voice input will check permission status when used
            }
        }
    }
}
