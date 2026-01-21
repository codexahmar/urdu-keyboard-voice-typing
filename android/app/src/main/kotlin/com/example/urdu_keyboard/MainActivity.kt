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

    // Microphone permission is now requested from Flutter side on home screen
    // This allows better UX flow: permission -> keyboard setup
}
