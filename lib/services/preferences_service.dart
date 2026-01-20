import 'package:shared_preferences/shared_preferences.dart';

/// Service for managing app preferences and settings
class PreferencesService {
  static const String _onboardingKey = 'onboarding_complete';
  static const String _selectedLanguageKey = 'selected_language';
  static const String _themeModeKey = 'theme_mode';
  static const String _textSizeKey = 'text_size_scale';

  late SharedPreferences _prefs;
  bool _initialized = false;

  /// Initialize the service
  Future<void> init() async {
    if (!_initialized) {
      _prefs = await SharedPreferences.getInstance();
      _initialized = true;
    }
  }

  /// Onboarding
  Future<bool> setOnboardingComplete(bool value) async {
    await init();
    return await _prefs.setBool(_onboardingKey, value);
  }

  bool isOnboardingComplete() {
    return _prefs.getBool(_onboardingKey) ?? false;
  }

  /// Language
  Future<bool> setSelectedLanguage(String languageCode) async {
    await init();
    return await _prefs.setString(_selectedLanguageKey, languageCode);
  }

  String getSelectedLanguage() {
    return _prefs.getString(_selectedLanguageKey) ?? 'ur'; // Default to Urdu
  }

  /// Theme Mode
  Future<bool> setThemeMode(String mode) async {
    await init();
    return await _prefs.setString(_themeModeKey, mode);
  }

  String getThemeMode() {
    return _prefs.getString(_themeModeKey) ?? 'system';
  }

  /// Text Size
  Future<bool> setTextSize(double scale) async {
    await init();
    return await _prefs.setDouble(_textSizeKey, scale);
  }

  double getTextSize() {
    return _prefs.getDouble(_textSizeKey) ?? 1.0;
  }

  /// Clear all preferences
  Future<bool> clearAll() async {
    await init();
    return await _prefs.clear();
  }
}
