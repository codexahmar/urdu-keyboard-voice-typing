import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Service to communicate with native Android keyboard implementation
class PlatformChannelService {
  static const MethodChannel _channel = MethodChannel(
    'com.codexahmar.urdu_keyboard/keyboard',
  );

  /// Opens Android keyboard settings
  Future<bool> openKeyboardSettings() async {
    try {
      final bool result = await _channel.invokeMethod('openKeyboardSettings');
      return result;
    } catch (e) {
      debugPrint('Error opening keyboard settings: $e');
      return false;
    }
  }

  /// Checks if the keyboard is enabled in system settings
  Future<bool> isKeyboardEnabled() async {
    try {
      final bool result = await _channel.invokeMethod('isKeyboardEnabled');
      return result;
    } catch (e) {
      debugPrint('Error checking keyboard status: $e');
      return false;
    }
  }

  /// Checks if the keyboard is selected as default
  Future<bool> isDefaultKeyboard() async {
    try {
      final bool result = await _channel.invokeMethod('isDefaultKeyboard');
      return result;
    } catch (e) {
      debugPrint('Error checking default keyboard: $e');
      return false;
    }
  }

  /// Opens input method picker
  Future<void> openInputMethodPicker() async {
    try {
      await _channel.invokeMethod('openInputMethodPicker');
    } catch (e) {
      debugPrint('Error opening input method picker: $e');
    }
  }

  /// Sets the active language for the keyboard
  Future<bool> setKeyboardLanguage(String languageCode) async {
    try {
      final bool result = await _channel.invokeMethod('setKeyboardLanguage', {
        'languageCode': languageCode,
      });
      return result;
    } catch (e) {
      debugPrint('Error setting keyboard language: $e');
      return false;
    }
  }

  /// Gets the currently selected language
  Future<String?> getSelectedLanguage() async {
    try {
      final String? result = await _channel.invokeMethod('getSelectedLanguage');
      return result;
    } catch (e) {
      debugPrint('Error getting selected language: $e');
      return null;
    }
  }

  /// Requests necessary permissions (if any)
  Future<bool> requestPermissions() async {
    try {
      final bool result = await _channel.invokeMethod('requestPermissions');
      return result;
    } catch (e) {
      debugPrint('Error requesting permissions: $e');
      return false;
    }
  }
}
