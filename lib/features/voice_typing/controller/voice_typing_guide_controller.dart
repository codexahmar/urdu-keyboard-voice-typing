import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/app_permission_dialog.dart';

/// Controller managing microphone permission checks and requests for voice typing.
class VoiceTypingGuideController extends ChangeNotifier {
  bool _isPermissionGranted = false;
  bool get isPermissionGranted => _isPermissionGranted;

  /// Checks the current microphone permission status.
  Future<void> checkPermissionStatus() async {
    try {
      final status = await Permission.microphone.status;
      if (_isPermissionGranted != status.isGranted) {
        _isPermissionGranted = status.isGranted;
        notifyListeners();
      }
    } catch (_) {}
  }

  /// Requests microphone permission or informs the user of active permission.
  Future<void> handlePermissionAction(BuildContext context) async {
    final l10n = context.l10n;
    final status = await Permission.microphone.status;

    if (status.isGranted) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: [
                const Icon(
                  Icons.check_circle_rounded,
                  color: AppColors.white,
                  size: 20,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    l10n.onboardingPermissionGranted,
                    style: const TextStyle(
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ),
              ],
            ),
            backgroundColor: AppColors.primary,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        );
      }
    } else {
      if (context.mounted) {
        await AppPermissionDialog.show(
          context,
          icon: Icons.mic_rounded,
          title: l10n.permissionMicTitle,
          description: l10n.permissionMicDesc,
          allowText: l10n.permissionAllow,
          cancelText: l10n.permissionNotNow,
          onAllow: () async {
            final res = await Permission.microphone.request();
            if (res.isPermanentlyDenied) {
              await openAppSettings();
            }
            await checkPermissionStatus();
          },
        );
      }
    }
  }

  /// Handles app resume lifecycle events.
  void handleResume() {
    checkPermissionStatus();
  }
}
