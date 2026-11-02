package com.codexahmar.urdu_keyboard

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.codexahmar.urdu_keyboard.channel.FlutterChannelHandler
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper

class MainActivity : FlutterActivity() {
    
    private lateinit var channelHandler: FlutterChannelHandler
    private lateinit var preferenceHelper: PreferenceHelper

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
}
