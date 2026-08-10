package com.codexahmar.urdu_keyboard.keyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper

/**
 * Initializes keyboard state on device boot/package update so IME is ready
 * without requiring the app to be opened manually.
 */
class KeyboardBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "KeyboardBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        // Warm up persisted settings used by the IME service.
        val preferenceHelper = PreferenceHelper(context)
        val lang = preferenceHelper.getSelectedLanguage()
        Log.d(TAG, "Boot init complete, language=$lang")
    }
}
