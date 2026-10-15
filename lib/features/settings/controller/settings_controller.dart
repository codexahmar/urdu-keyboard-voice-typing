import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../../core/constants/app_constants.dart';
import '../../../state/app_state.dart';

/// Controller handling external links, sharing, and navigation for Settings.
class SettingsController {
  const SettingsController();

  /// Opens external URLs with fallback options.
  Future<void> launchUrlString(String urlString, {String? marketUrl}) async {
    try {
      if (marketUrl != null) {
        final marketUri = Uri.parse(marketUrl);
        if (await canLaunchUrl(marketUri)) {
          await launchUrl(marketUri, mode: LaunchMode.externalApplication);
          return;
        }
      }
      final uri = Uri.parse(urlString);
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (_) {
      try {
        final uri = Uri.parse(urlString);
        await launchUrl(uri, mode: LaunchMode.platformDefault);
      } catch (_) {}
    }
  }

  /// Opens the system share sheet with application invitation message.
  Future<void> shareApp(String message) async {
    try {
      await SharePlus.instance.share(ShareParams(text: message));
    } catch (_) {}
  }

  /// Opens Google Play Store page for rating the app.
  Future<void> rateApp() => launchUrlString(
        AppConstants.playStoreAppUrl,
        marketUrl: AppConstants.playStoreMarketUrl,
      );

  /// Opens developer portfolio in Google Play Store.
  Future<void> openDeveloperApps() =>
      launchUrlString(AppConstants.developerAppsUrl);

  /// Opens application privacy policy web page.
  Future<void> openPrivacyPolicy() =>
      launchUrlString(AppConstants.privacyPolicyUrl);

  /// Updates current active locale via AppState.
  void updateLanguage(AppState appState, String newLang) {
    if (newLang != appState.selectedLanguage) {
      appState.setSelectedLanguage(newLang);
    }
  }

  /// Navigates to keyboard preview screen.
  void navigateToKeyboardPreview(BuildContext context) {
    Navigator.pushNamed(context, AppConstants.keyboardPreviewRoute);
  }

  /// Navigates to voice typing guide screen.
  void navigateToVoiceTypingGuide(BuildContext context) {
    Navigator.pushNamed(context, AppConstants.voiceTypingGuideRoute);
  }
}
