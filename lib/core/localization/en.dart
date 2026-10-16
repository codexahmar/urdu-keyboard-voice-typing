import '../constants/app_strings.dart';

/// English translations mapped from AppStrings constant definitions
const Map<String, String> enTranslations = {
  // App & Common Actions
  'appName': AppStrings.appName,
  'next': AppStrings.next,
  'back': AppStrings.back,
  'skip': AppStrings.skip,
  'getStarted': AppStrings.getStarted,
  'save': AppStrings.save,
  'active': AppStrings.active,
  'recommended': AppStrings.recommended,
  'english': AppStrings.english,
  'urduNative': AppStrings.urduNative,

  // Onboarding Page 1: Phonetic Typing
  'onboardingPage1Title': AppStrings.onboardingPage1Title,
  'onboardingPage1Subtitle': AppStrings.onboardingPage1Subtitle,
  'onboardingPhoneticTitle': AppStrings.onboardingPhoneticTitle,
  'onboardingPhoneticSubtitle': AppStrings.onboardingPhoneticSubtitle,
  'onboardingSwitchTitle': AppStrings.onboardingSwitchTitle,
  'onboardingSwitchSubtitle': AppStrings.onboardingSwitchSubtitle,
  'onboardingVoiceFeatureTitle': AppStrings.onboardingVoiceFeatureTitle,
  'onboardingVoiceFeatureSubtitle': AppStrings.onboardingVoiceFeatureSubtitle,

  // Onboarding Page 2: Voice Typing
  'onboardingPage2Title': AppStrings.onboardingPage2Title,
  'onboardingPage2Subtitle': AppStrings.onboardingPage2Subtitle,
  'onboardingVoiceStep1': AppStrings.onboardingVoiceStep1,
  'onboardingVoiceStep2': AppStrings.onboardingVoiceStep2,
  'onboardingVoiceStep3': AppStrings.onboardingVoiceStep3,
  'onboardingSampleSpeech': AppStrings.onboardingSampleSpeech,
  'onboardingSpeechRecognized': AppStrings.onboardingSpeechRecognized,
  'onboardingGrantPermission': AppStrings.onboardingGrantPermission,
  'onboardingPermissionGranted': AppStrings.onboardingPermissionGranted,

  // Onboarding Page 3: Enable in Settings
  'onboardingPage3Title': AppStrings.onboardingPage3Title,
  'onboardingPage3Subtitle': AppStrings.onboardingPage3Subtitle,
  'onboardingEnableStep1': AppStrings.onboardingEnableStep1,
  'onboardingEnableStep2': AppStrings.onboardingEnableStep2,
  'onboardingOpenKeyboardSettings': AppStrings.onboardingOpenKeyboardSettings,
  'onboardingKeyboardAlreadyEnabled':
      AppStrings.onboardingKeyboardAlreadyEnabled,
  'onboardingKeyboardNotYetEnabled': AppStrings.onboardingKeyboardNotYetEnabled,
  'onboardingSimulatedToggleHint': AppStrings.onboardingSimulatedToggleHint,

  // Onboarding Page 4: Select Active Keyboard
  'onboardingPage4Title': AppStrings.onboardingPage4Title,
  'onboardingPage4Subtitle': AppStrings.onboardingPage4Subtitle,
  'onboardingSelectStep1': AppStrings.onboardingSelectStep1,
  'onboardingSelectStep2': AppStrings.onboardingSelectStep2,
  'onboardingSelectKeyboard': AppStrings.onboardingSelectKeyboard,
  'onboardingKeyboardAlreadyActive': AppStrings.onboardingKeyboardAlreadyActive,
  'onboardingKeyboardNotYetActive': AppStrings.onboardingKeyboardNotYetActive,
  'onboardingSimulatedPickerHint': AppStrings.onboardingSimulatedPickerHint,
  'onboardingSwitchTipTitle': AppStrings.onboardingSwitchTipTitle,
  'onboardingSwitchTipSubtitle': AppStrings.onboardingSwitchTipSubtitle,

  // Home Screen
  'homeWelcome': AppStrings.homeWelcome,
  'homeSubtitle': AppStrings.homeSubtitle,
  'homeStatusTitle': AppStrings.homeStatusTitle,
  'homeStatusEnabled': AppStrings.homeStatusEnabled,
  'homeStatusDisabled': AppStrings.homeStatusDisabled,
  'homeStatusActive': AppStrings.homeStatusActive,
  'homeStatusNotActive': AppStrings.homeStatusNotActive,
  'homeActionEnable': AppStrings.homeActionEnable,
  'homeActionSelect': AppStrings.homeActionSelect,
  'homeTestHint': AppStrings.homeTestHint,
  'homeActiveHint': AppStrings.homeActiveHint,
  'homeCardPreviewTitle': AppStrings.homeCardPreviewTitle,
  'homeCardPreviewSubtitle': AppStrings.homeCardPreviewSubtitle,
  'homeCardVoiceTitle': AppStrings.homeCardVoiceTitle,
  'homeCardVoiceSubtitle': AppStrings.homeCardVoiceSubtitle,

  // Settings
  'settingsTitle': AppStrings.settingsTitle,
  'settingsGeneralSection': AppStrings.settingsGeneralSection,
  'settingsLanguage': AppStrings.settingsLanguage,
  'settingsHelpSection': AppStrings.settingsHelpSection,
  'settingsSetupGuide': AppStrings.settingsSetupGuide,
  'settingsSetupGuideSubtitle': AppStrings.settingsSetupGuideSubtitle,
  'settingsVoiceGuide': AppStrings.settingsVoiceGuide,
  'settingsVoiceGuideSubtitle': AppStrings.settingsVoiceGuideSubtitle,
  'settingsAboutSection': AppStrings.settingsAboutSection,
  'settingsRateApp': AppStrings.settingsRateApp,
  'settingsRateAppSubtitle': AppStrings.settingsRateAppSubtitle,
  'settingsShareApp': AppStrings.settingsShareApp,
  'settingsShareAppSubtitle': AppStrings.settingsShareAppSubtitle,
  'settingsShareMessage': AppStrings.settingsShareMessage,
  'settingsMoreApps': AppStrings.settingsMoreApps,
  'settingsMoreAppsSubtitle': AppStrings.settingsMoreAppsSubtitle,
  'settingsPrivacyPolicy': AppStrings.settingsPrivacyPolicy,
  'settingsPrivacyPolicySubtitle': AppStrings.settingsPrivacyPolicySubtitle,

  // Language Selection
  'languageSelectionTitle': AppStrings.languageSelectionTitle,
  'languageSelectionSubtitle': AppStrings.languageSelectionSubtitle,
  'languageUrduTitle': AppStrings.languageUrduTitle,
  'languageUrduSubtitle': AppStrings.languageUrduSubtitle,
  'languageEnglishTitle': AppStrings.languageEnglishTitle,
  'languageEnglishSubtitle': AppStrings.languageEnglishSubtitle,

  // Voice Typing Guide
  'voiceGuideTitle': AppStrings.voiceGuideTitle,
  'voiceGuideSubtitle': AppStrings.voiceGuideSubtitle,
  'voiceGuideStep1Title': AppStrings.voiceGuideStep1Title,
  'voiceGuideStep1Desc': AppStrings.voiceGuideStep1Desc,
  'voiceGuideStep2Title': AppStrings.voiceGuideStep2Title,
  'voiceGuideStep2Desc': AppStrings.voiceGuideStep2Desc,
  'voiceGuideStep3Title': AppStrings.voiceGuideStep3Title,
  'voiceGuideStep3Desc': AppStrings.voiceGuideStep3Desc,
  'voiceGuideStep4Title': AppStrings.voiceGuideStep4Title,
  'voiceGuideStep4Desc': AppStrings.voiceGuideStep4Desc,
  'voiceGuideTipsTitle': AppStrings.voiceGuideTipsTitle,
  'voiceGuideTip1': AppStrings.voiceGuideTip1,
  'voiceGuideTip2': AppStrings.voiceGuideTip2,
  'voiceGuideTip3': AppStrings.voiceGuideTip3,
  'voiceGuidePermissionPrompt': AppStrings.voiceGuidePermissionPrompt,

  // Keyboard Preview
  'keyboardPreviewTitle': AppStrings.keyboardPreviewTitle,
  'keyboardPreviewSubtitle': AppStrings.keyboardPreviewSubtitle,

  // Permission Dialog
  'permissionMicTitle': AppStrings.permissionMicTitle,
  'permissionMicDesc': AppStrings.permissionMicDesc,
  'permissionAllow': AppStrings.permissionAllow,
  'permissionNotNow': AppStrings.permissionNotNow,
};
