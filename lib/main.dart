import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/constants/app_constants.dart';
import 'core/localization/app_localizations.dart';
import 'core/theme/app_theme.dart';
import 'features/home/home_screen.dart';
import 'features/keyboard_preview/keyboard_preview_screen.dart';
import 'features/language_selection/language_selection_screen.dart';
import 'features/onboarding/onboarding_screen.dart';
import 'features/settings/settings_screen.dart';
import 'features/splash/splash_screen.dart';
import 'features/voice_typing/voice_typing_guide_screen.dart';
import 'state/app_state.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize app state
  final appState = AppState();
  await appState.init();

  runApp(
    ChangeNotifierProvider<AppState>.value(
      value: appState,
      child: const UrduSmartKeyboardApp(),
    ),
  );
}

class UrduSmartKeyboardApp extends StatelessWidget {
  const UrduSmartKeyboardApp({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();

    return MaterialApp(
      title: AppConstants.appName,
      debugShowCheckedModeBanner: false,

      // Single Pakistan Flag Green & White Theme
      theme: AppTheme.theme,

      // Localization configuration
      locale: Locale(appState.selectedLanguage),
      supportedLocales: AppLocalizations.supportedLocales,
      localizationsDelegates: AppLocalizations.localizationsDelegates,

      // Enforce uniform LTR direction across English and Urdu
      builder: (context, child) {
        return Directionality(
          textDirection: TextDirection.ltr,
          child: child!,
        );
      },

      // Initial route
      initialRoute: AppConstants.splashRoute,

      // Routes
      routes: {
        AppConstants.splashRoute: (context) => const SplashWrapper(),
        AppConstants.onboardingRoute: (context) => OnboardingScreen(
              onComplete: () {
                appState.completeOnboarding();
                Navigator.pushReplacementNamed(context, AppConstants.homeRoute);
              },
            ),
        AppConstants.homeRoute: (context) => const HomeScreen(),
        AppConstants.languageSelectionRoute: (context) =>
            const LanguageSelectionScreen(),
        AppConstants.keyboardPreviewRoute: (context) =>
            const KeyboardPreviewScreen(),
        AppConstants.voiceTypingGuideRoute: (context) =>
            const VoiceTypingGuideScreen(),
        AppConstants.settingsRoute: (context) => const SettingsScreen(),
      },
    );
  }
}

/// Wrapper for splash screen with automatic navigation
class SplashWrapper extends StatefulWidget {
  const SplashWrapper({super.key});

  @override
  State<SplashWrapper> createState() => _SplashWrapperState();
}

class _SplashWrapperState extends State<SplashWrapper> {
  @override
  void initState() {
    super.initState();
    _navigateAfterDelay();
  }

  Future<void> _navigateAfterDelay() async {
    await Future.delayed(const Duration(seconds: 2));

    if (!mounted) return;

    final appState = context.read<AppState>();

    if (appState.onboardingComplete) {
      Navigator.pushReplacementNamed(context, AppConstants.homeRoute);
    } else {
      Navigator.pushReplacementNamed(context, AppConstants.onboardingRoute);
    }
  }

  @override
  Widget build(BuildContext context) {
    return const SplashScreen();
  }
}
