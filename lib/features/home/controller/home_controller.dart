import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/widgets/app_permission_dialog.dart';
import '../../../state/app_state.dart';

/// Controller handling Home screen business logic, permissions, and navigation.
class HomeController {
  /// Checks initial microphone permission and prompts dialog if denied.
  Future<void> checkInitialPermissions(BuildContext context) async {
    final status = await Permission.microphone.status;
    if (status.isDenied && context.mounted) {
      final l10n = context.l10n;
      await AppPermissionDialog.show(
        context,
        icon: Icons.mic_rounded,
        title: l10n.permissionMicTitle,
        description: l10n.permissionMicDesc,
        allowText: l10n.permissionAllow,
        cancelText: l10n.permissionNotNow,
        onAllow: () async {
          await Permission.microphone.request();
        },
      );
    }
  }

  /// Refreshes keyboard status via AppState.
  void refreshKeyboardStatus(AppState appState) {
    appState.checkKeyboardStatus();
  }

  /// Opens Android keyboard system settings.
  void openKeyboardSettings(AppState appState) {
    appState.openKeyboardSettings();
  }

  /// Opens Android input method picker dialog.
  void openInputMethodPicker(AppState appState) {
    appState.openInputMethodPicker();
  }

  /// Navigates to Settings screen.
  void openSettings(BuildContext context) {
    Navigator.pushNamed(context, AppConstants.settingsRoute);
  }

  /// Navigates to Voice Typing Guide screen.
  void openVoiceTypingGuide(BuildContext context) {
    Navigator.pushNamed(context, AppConstants.voiceTypingGuideRoute);
  }

  /// Navigates to Keyboard Preview screen.
  void openKeyboardPreview(BuildContext context) {
    Navigator.pushNamed(context, AppConstants.keyboardPreviewRoute);
  }
}
