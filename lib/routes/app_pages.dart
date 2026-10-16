import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../features/home/home_screen.dart';
import '../features/keyboard_preview/keyboard_preview_screen.dart';
import '../features/language_selection/language_selection_screen.dart';
import '../features/onboarding/onboarding_screen.dart';
import '../features/settings/settings_screen.dart';
import '../features/splash/splash_screen.dart';
import '../features/voice_typing/voice_typing_guide_screen.dart';
import '../state/app_state.dart';
import 'app_routes.dart';

/// Centralized route table and page navigation mapping.
abstract class AppPages {
  static const String initial = AppRoutes.splash;

  static final Map<String, WidgetBuilder> routes = {
    AppRoutes.splash: (context) => const SplashScreen(),
    AppRoutes.onboarding: (context) => OnboardingScreen(
          onComplete: () {
            context.read<AppState>().completeOnboarding();
            Navigator.pushReplacementNamed(context, AppRoutes.home);
          },
        ),
    AppRoutes.home: (context) => const HomeScreen(),
    AppRoutes.languageSelection: (context) => const LanguageSelectionScreen(),
    AppRoutes.keyboardPreview: (context) => const KeyboardPreviewScreen(),
    AppRoutes.voiceTypingGuide: (context) => const VoiceTypingGuideScreen(),
    AppRoutes.settings: (context) => const SettingsScreen(),
  };
}
