import 'package:flutter/material.dart';
import '../services/preferences_service.dart';
import '../core/constants/app_constants.dart';

/// Global app state using ChangeNotifier
/// Manages theme, language, text size, and other app-wide settings
class AppState extends ChangeNotifier {
  final PreferencesService _prefsService = PreferencesService();

  // Theme Mode
  ThemeMode _themeMode = ThemeMode.system;
  ThemeMode get themeMode => _themeMode;

  // Text Size Scale
  double _textSizeScale = 1.0;
  double get textSizeScale => _textSizeScale;

  // Selected Language
  String _selectedLanguage = 'ur';
  String get selectedLanguage => _selectedLanguage;

  // Onboarding Status
  bool _onboardingComplete = false;
  bool get onboardingComplete => _onboardingComplete;

  /// Initialize app state from preferences
  Future<void> init() async {
    await _prefsService.init();

    _onboardingComplete = _prefsService.isOnboardingComplete();
    _selectedLanguage = _prefsService.getSelectedLanguage();
    final supportedCodes = AppConstants.supportedLanguages
        .map((lang) => lang['code'] as String)
        .toSet();
    if (!supportedCodes.contains(_selectedLanguage)) {
      _selectedLanguage = 'ur';
      await _prefsService.setSelectedLanguage(_selectedLanguage);
    }
    _textSizeScale = _prefsService.getTextSize();

    final themeModeString = _prefsService.getThemeMode();
    _themeMode = _getThemeModeFromString(themeModeString);

    notifyListeners();
  }

  /// Set theme mode
  Future<void> setThemeMode(ThemeMode mode) async {
    _themeMode = mode;
    await _prefsService.setThemeMode(_getStringFromThemeMode(mode));
    notifyListeners();
  }

  /// Set text size scale
  Future<void> setTextSizeScale(double scale) async {
    _textSizeScale = scale;
    await _prefsService.setTextSize(scale);
    notifyListeners();
  }

  /// Set selected language
  Future<void> setSelectedLanguage(String languageCode) async {
    _selectedLanguage = languageCode;
    await _prefsService.setSelectedLanguage(languageCode);
    notifyListeners();
  }

  /// Mark onboarding as complete
  Future<void> completeOnboarding() async {
    _onboardingComplete = true;
    await _prefsService.setOnboardingComplete(true);
    notifyListeners();
  }

  /// Get language name from code
  String getLanguageName(String code) {
    final lang = AppConstants.supportedLanguages.firstWhere(
      (l) => l['code'] == code,
      orElse: () => {'name': 'Unknown'},
    );
    return lang['name'];
  }

  /// Get language native name from code
  String getLanguageNativeName(String code) {
    final lang = AppConstants.supportedLanguages.firstWhere(
      (l) => l['code'] == code,
      orElse: () => {'nativeName': 'Unknown'},
    );
    return lang['nativeName'];
  }

  // Helper methods
  ThemeMode _getThemeModeFromString(String mode) {
    switch (mode) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  String _getStringFromThemeMode(ThemeMode mode) {
    switch (mode) {
      case ThemeMode.light:
        return 'light';
      case ThemeMode.dark:
        return 'dark';
      default:
        return 'system';
    }
  }
}
