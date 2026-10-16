import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'en.dart';
import 'language_model.dart';
import 'ur.dart';

export 'language_model.dart';

/// Production-ready typed localization provider for Urdu Keyboard.
class AppLocalizations {
  final Locale locale;
  final Map<String, String> _localizedStrings;

  AppLocalizations(this.locale)
      : _localizedStrings =
            locale.languageCode == 'ur' ? urTranslations : enTranslations;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations) ??
        AppLocalizations(const Locale('ur'));
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  static const List<Locale> supportedLocales = [
    Locale('ur'),
    Locale('en'),
  ];

  /// Supported application languages metadata configuration.
  static const List<AppLanguage> supportedLanguages = [
    AppLanguage(
      code: 'ur',
      name: 'Urdu',
      nativeName: 'اردو',
      isRecommended: true,
      description: 'Type in Urdu using Roman letters (phonetic typing)',
    ),
    AppLanguage(
      code: 'en',
      name: 'English',
      nativeName: 'English',
      isRecommended: false,
      description: 'Standard English QWERTY keyboard',
    ),
  ];

  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates = [
    delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ];

  String _get(String key) =>
      _localizedStrings[key] ?? enTranslations[key] ?? key;

  // App & Common Actions
  String get appName => _get('appName');
  String get next => _get('next');
  String get back => _get('back');
  String get skip => _get('skip');
  String get getStarted => _get('getStarted');
  String get save => _get('save');
  String get active => _get('active');
  String get recommended => _get('recommended');
  String get english => _get('english');
  String get urduNative => _get('urduNative');

  // Onboarding Page 1
  String get onboardingPage1Title => _get('onboardingPage1Title');
  String get onboardingPage1Subtitle => _get('onboardingPage1Subtitle');
  String get onboardingPhoneticTitle => _get('onboardingPhoneticTitle');
  String get onboardingPhoneticSubtitle => _get('onboardingPhoneticSubtitle');
  String get onboardingSwitchTitle => _get('onboardingSwitchTitle');
  String get onboardingSwitchSubtitle => _get('onboardingSwitchSubtitle');
  String get onboardingVoiceFeatureTitle =>
      _get('onboardingVoiceFeatureTitle');
  String get onboardingVoiceFeatureSubtitle =>
      _get('onboardingVoiceFeatureSubtitle');

  // Onboarding Page 2
  String get onboardingPage2Title => _get('onboardingPage2Title');
  String get onboardingPage2Subtitle => _get('onboardingPage2Subtitle');
  String get onboardingVoiceStep1 => _get('onboardingVoiceStep1');
  String get onboardingVoiceStep2 => _get('onboardingVoiceStep2');
  String get onboardingVoiceStep3 => _get('onboardingVoiceStep3');
  String get onboardingSampleSpeech => _get('onboardingSampleSpeech');
  String get onboardingSpeechRecognized => _get('onboardingSpeechRecognized');
  String get onboardingGrantPermission => _get('onboardingGrantPermission');
  String get onboardingPermissionGranted =>
      _get('onboardingPermissionGranted');

  // Onboarding Page 3
  String get onboardingPage3Title => _get('onboardingPage3Title');
  String get onboardingPage3Subtitle => _get('onboardingPage3Subtitle');
  String get onboardingEnableStep1 => _get('onboardingEnableStep1');
  String get onboardingEnableStep2 => _get('onboardingEnableStep2');
  String get onboardingOpenKeyboardSettings =>
      _get('onboardingOpenKeyboardSettings');
  String get onboardingKeyboardAlreadyEnabled =>
      _get('onboardingKeyboardAlreadyEnabled');
  String get onboardingKeyboardNotYetEnabled =>
      _get('onboardingKeyboardNotYetEnabled');
  String get onboardingSimulatedToggleHint =>
      _get('onboardingSimulatedToggleHint');

  // Onboarding Page 4
  String get onboardingPage4Title => _get('onboardingPage4Title');
  String get onboardingPage4Subtitle => _get('onboardingPage4Subtitle');
  String get onboardingSelectStep1 => _get('onboardingSelectStep1');
  String get onboardingSelectStep2 => _get('onboardingSelectStep2');
  String get onboardingSelectKeyboard => _get('onboardingSelectKeyboard');
  String get onboardingKeyboardAlreadyActive =>
      _get('onboardingKeyboardAlreadyActive');
  String get onboardingKeyboardNotYetActive =>
      _get('onboardingKeyboardNotYetActive');
  String get onboardingSimulatedPickerHint =>
      _get('onboardingSimulatedPickerHint');
  String get onboardingSwitchTipTitle => _get('onboardingSwitchTipTitle');
  String get onboardingSwitchTipSubtitle =>
      _get('onboardingSwitchTipSubtitle');

  // Home
  String get homeWelcome => _get('homeWelcome');
  String get homeSubtitle => _get('homeSubtitle');
  String get homeStatusTitle => _get('homeStatusTitle');
  String get homeStatusEnabled => _get('homeStatusEnabled');
  String get homeStatusDisabled => _get('homeStatusDisabled');
  String get homeStatusActive => _get('homeStatusActive');
  String get homeStatusNotActive => _get('homeStatusNotActive');
  String get homeActionEnable => _get('homeActionEnable');
  String get homeActionSelect => _get('homeActionSelect');
  String get homeTestHint => _get('homeTestHint');
  String get homeActiveHint => _get('homeActiveHint');
  String get homeCardPreviewTitle => _get('homeCardPreviewTitle');
  String get homeCardPreviewSubtitle => _get('homeCardPreviewSubtitle');
  String get homeCardVoiceTitle => _get('homeCardVoiceTitle');
  String get homeCardVoiceSubtitle => _get('homeCardVoiceSubtitle');

  // Settings
  String get settingsTitle => _get('settingsTitle');
  String get settingsGeneralSection => _get('settingsGeneralSection');
  String get settingsLanguage => _get('settingsLanguage');
  String get settingsHelpSection => _get('settingsHelpSection');
  String get settingsSetupGuide => _get('settingsSetupGuide');
  String get settingsSetupGuideSubtitle =>
      _get('settingsSetupGuideSubtitle');
  String get settingsVoiceGuide => _get('settingsVoiceGuide');
  String get settingsVoiceGuideSubtitle =>
      _get('settingsVoiceGuideSubtitle');
  String get settingsAboutSection => _get('settingsAboutSection');
  String get settingsRateApp => _get('settingsRateApp');
  String get settingsRateAppSubtitle => _get('settingsRateAppSubtitle');
  String get settingsShareApp => _get('settingsShareApp');
  String get settingsShareAppSubtitle => _get('settingsShareAppSubtitle');
  String get settingsShareMessage => _get('settingsShareMessage');
  String get settingsMoreApps => _get('settingsMoreApps');
  String get settingsMoreAppsSubtitle => _get('settingsMoreAppsSubtitle');
  String get settingsPrivacyPolicy => _get('settingsPrivacyPolicy');
  String get settingsPrivacyPolicySubtitle =>
      _get('settingsPrivacyPolicySubtitle');

  // Language Selection
  String get languageSelectionTitle => _get('languageSelectionTitle');
  String get languageSelectionSubtitle => _get('languageSelectionSubtitle');
  String get languageUrduTitle => _get('languageUrduTitle');
  String get languageUrduSubtitle => _get('languageUrduSubtitle');
  String get languageEnglishTitle => _get('languageEnglishTitle');
  String get languageEnglishSubtitle => _get('languageEnglishSubtitle');

  // Voice Typing Guide
  String get voiceGuideTitle => _get('voiceGuideTitle');
  String get voiceGuideSubtitle => _get('voiceGuideSubtitle');
  String get voiceGuideStep1Title => _get('voiceGuideStep1Title');
  String get voiceGuideStep1Desc => _get('voiceGuideStep1Desc');
  String get voiceGuideStep2Title => _get('voiceGuideStep2Title');
  String get voiceGuideStep2Desc => _get('voiceGuideStep2Desc');
  String get voiceGuideStep3Title => _get('voiceGuideStep3Title');
  String get voiceGuideStep3Desc => _get('voiceGuideStep3Desc');
  String get voiceGuideStep4Title => _get('voiceGuideStep4Title');
  String get voiceGuideStep4Desc => _get('voiceGuideStep4Desc');
  String get voiceGuideTipsTitle => _get('voiceGuideTipsTitle');
  String get voiceGuideTip1 => _get('voiceGuideTip1');
  String get voiceGuideTip2 => _get('voiceGuideTip2');
  String get voiceGuideTip3 => _get('voiceGuideTip3');
  String get voiceGuidePermissionPrompt =>
      _get('voiceGuidePermissionPrompt');

  // Keyboard Preview
  String get keyboardPreviewTitle => _get('keyboardPreviewTitle');
  String get keyboardPreviewSubtitle => _get('keyboardPreviewSubtitle');

  // Permission Dialog
  String get permissionMicTitle => _get('permissionMicTitle');
  String get permissionMicDesc => _get('permissionMicDesc');
  String get permissionAllow => _get('permissionAllow');
  String get permissionNotNow => _get('permissionNotNow');
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) => ['ur', 'en'].contains(locale.languageCode);

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(AppLocalizations(locale));
  }

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

/// Convenience BuildContext extensions for UI code
extension AppLocalizationsX on BuildContext {
  AppLocalizations get l10n => AppLocalizations.of(this);
  bool get isUrdu => Localizations.localeOf(this).languageCode == 'ur';
}
