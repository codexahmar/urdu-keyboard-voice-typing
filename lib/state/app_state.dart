import 'package:flutter/material.dart';
import '../services/platform_channel_service.dart';
import '../services/preferences_service.dart';

/// State management provider for application preferences and keyboard status.
class AppState extends ChangeNotifier {
  final PreferencesService _prefsService = PreferencesService();
  final PlatformChannelService _platformService = PlatformChannelService();

  // Selected Language (Default: 'ur')
  String _selectedLanguage = 'ur';
  String get selectedLanguage => _selectedLanguage;

  // Text Size Scale (Default: 1.0)
  double _textSizeScale = 1.0;
  double get textSizeScale => _textSizeScale;

  // Onboarding Status
  bool _onboardingComplete = false;
  bool get onboardingComplete => _onboardingComplete;

  // Keyboard Status
  bool _isKeyboardEnabled = false;
  bool get isKeyboardEnabled => _isKeyboardEnabled;

  bool _isKeyboardActive = false;
  bool get isKeyboardActive => _isKeyboardActive;

  bool _isLoadingStatus = false;
  bool get isLoadingStatus => _isLoadingStatus;

  /// Initialize app state from preferences and check system status
  Future<void> init() async {
    await _prefsService.init();

    _onboardingComplete = _prefsService.isOnboardingComplete();
    _selectedLanguage = _prefsService.getSelectedLanguage();
    _textSizeScale = _prefsService.getTextSize();

    await checkKeyboardStatus();
    notifyListeners();
  }

  /// Change selected language and persist
  Future<void> setSelectedLanguage(String languageCode) async {
    if (_selectedLanguage == languageCode) return;
    _selectedLanguage = languageCode;
    notifyListeners();
    await _prefsService.setSelectedLanguage(languageCode);
    await _platformService.setKeyboardLanguage(languageCode);
  }

  /// Adjust text size scale
  Future<void> setTextSizeScale(double scale) async {
    _textSizeScale = scale;
    await _prefsService.setTextSize(scale);
    notifyListeners();
  }

  /// Mark onboarding as completed
  Future<void> completeOnboarding() async {
    _onboardingComplete = true;
    await _prefsService.setOnboardingComplete(true);
    notifyListeners();
  }

  /// Check keyboard status from Android system
  Future<void> checkKeyboardStatus() async {
    _isLoadingStatus = true;
    notifyListeners();

    try {
      _isKeyboardEnabled = await _platformService.isKeyboardEnabled();
      _isKeyboardActive = await _platformService.isDefaultKeyboard();
    } catch (_) {
      _isKeyboardEnabled = false;
      _isKeyboardActive = false;
    } finally {
      _isLoadingStatus = false;
      notifyListeners();
    }
  }

  /// Launch Android keyboard settings
  Future<void> openKeyboardSettings() async {
    await _platformService.openKeyboardSettings();
    await checkKeyboardStatus();
  }

  /// Open Android input method picker dialog
  Future<void> openInputMethodPicker() async {
    await _platformService.openInputMethodPicker();
    await checkKeyboardStatus();
  }

  /// Helper for testing keyboard status states
  @visibleForTesting
  void setKeyboardStatusForTesting({required bool enabled, required bool active}) {
    _isKeyboardEnabled = enabled;
    _isKeyboardActive = active;
    notifyListeners();
  }
}
