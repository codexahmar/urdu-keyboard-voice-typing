/// App-wide constants and configuration values
class AppConstants {
  // App Info
  static const String appName = 'Urdu Smart Keyboard';
  static const String appVersion = '1.0.0';
  static const String appDescription =
      'A simple, ad-free keyboard app for Urdu typing';

  // Onboarding
  static const int onboardingSteps = 4;
  static const String onboardingCompleteKey = 'onboarding_complete';

  // Preferences Keys
  static const String selectedLanguageKey = 'selected_language';
  static const String themeKey = 'theme_mode';
  static const String textSizeKey = 'text_size_scale';

  // Platform Channel
  static const String platformChannel = 'com.codexahmar.urdu_keyboard/keyboard';

  // Supported Languages
  static const List<Map<String, dynamic>> supportedLanguages = [
    {
      'code': 'ur',
      'name': 'Urdu',
      'nativeName': 'اردو',
      'isRecommended': true,
      'description': 'Type in Urdu using Roman letters (phonetic typing)',
    },
    {
      'code': 'en',
      'name': 'English',
      'nativeName': 'English',
      'isRecommended': false,
      'description': 'Standard English QWERTY keyboard',
    },
    {
      'code': 'ar',
      'name': 'Arabic',
      'nativeName': 'العربية',
      'isRecommended': false,
      'description': 'Standard Arabic keyboard layout',
    },
  ];

  // Routes
  static const String splashRoute = '/';
  static const String onboardingRoute = '/onboarding';
  static const String homeRoute = '/home';
  static const String languageSelectionRoute = '/language-selection';
  static const String keyboardPreviewRoute = '/keyboard-preview';
  static const String voiceTypingGuideRoute = '/voice-typing-guide';
  static const String settingsRoute = '/settings';
}
