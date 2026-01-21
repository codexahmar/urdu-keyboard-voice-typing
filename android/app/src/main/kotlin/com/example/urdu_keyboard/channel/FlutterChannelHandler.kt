package com.example.urdu_keyboard.channel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import com.example.urdu_keyboard.utils.PreferenceHelper

/**
 * Handles communication between Flutter and native Android code
 * via Platform Channels
 */
class FlutterChannelHandler(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) : MethodChannel.MethodCallHandler {

    companion object {
        const val CHANNEL_NAME = "com.example.urdu_keyboard/keyboard"
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "openKeyboardSettings" -> {
                openKeyboardSettings()
                result.success(true)
            }
            
            "isKeyboardEnabled" -> {
                val isEnabled = isKeyboardEnabled()
                result.success(isEnabled)
            }
            
            "isDefaultKeyboard" -> {
                val isDefault = isDefaultKeyboard()
                result.success(isDefault)
            }
            
            "openInputMethodPicker" -> {
                openInputMethodPicker()
                result.success(null)
            }
            
            "setKeyboardLanguage" -> {
                val languageCode = call.argument<String>("languageCode")
                if (languageCode != null) {
                    preferenceHelper.setSelectedLanguage(languageCode)
                    result.success(true)
                } else {
                    result.error("INVALID_ARGUMENT", "Language code is required", null)
                }
            }
            
            "getSelectedLanguage" -> {
                val language = preferenceHelper.getSelectedLanguage()
                result.success(language)
            }
            
            "requestPermissions" -> {
                // For keyboard, we primarily need RECORD_AUDIO for voice input
                // This will be handled in the main activity
                result.success(true)
            }
            
            else -> {
                result.notImplemented()
            }
        }
    }

    /**
     * Open system keyboard settings page
     */
    private fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Check if our keyboard is enabled in system settings
     */
    private fun isKeyboardEnabled(): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledIMEs = imm.enabledInputMethodList
        val packageName = context.packageName
        
        return enabledIMEs.any { 
            it.packageName == packageName 
        }
    }

    /**
     * Check if our keyboard is the default/current keyboard
     */
    private fun isDefaultKeyboard(): Boolean {
        val currentIME = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        val packageName = context.packageName
        
        return currentIME?.contains(packageName) == true
    }

    /**
     * Open the input method picker dialog
     */
    private fun openInputMethodPicker() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }
}
