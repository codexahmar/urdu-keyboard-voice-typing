import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/widgets/app_permission_dialog.dart';
import '../../../state/app_state.dart';

/// Controller managing onboarding slides, microphone permissions, and navigation.
class OnboardingController extends ChangeNotifier {
  final PageController pageController = PageController();

  int _currentPage = 0;
  int get currentPage => _currentPage;

  bool _micGranted = false;
  bool get micGranted => _micGranted;

  /// Updates current page index from PageView.
  void setPage(int index) {
    if (_currentPage != index) {
      _currentPage = index;
      notifyListeners();
    }
  }

  /// Checks current microphone permission status.
  Future<void> checkMicPermission() async {
    try {
      final status = await Permission.microphone.status;
      if (_micGranted != status.isGranted) {
        _micGranted = status.isGranted;
        notifyListeners();
      }
    } catch (_) {}
  }

  /// Displays permission dialog and requests microphone access.
  Future<void> requestMicPermission(BuildContext context) async {
    final l10n = context.l10n;
    await AppPermissionDialog.show(
      context,
      icon: Icons.mic_rounded,
      title: l10n.permissionMicTitle,
      description: l10n.permissionMicDesc,
      allowText: l10n.permissionAllow,
      cancelText: l10n.permissionNotNow,
      onAllow: () async {
        final result = await Permission.microphone.request();
        _micGranted = result.isGranted;
        notifyListeners();
        if (result.isPermanentlyDenied) {
          await openAppSettings();
        }
      },
    );
  }

  /// Navigates to the next onboarding slide or completes the flow.
  void nextPage(VoidCallback onComplete) {
    if (_currentPage < AppConstants.onboardingSteps - 1) {
      pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      onComplete();
    }
  }

  /// Navigates to the previous onboarding slide.
  void previousPage() {
    if (_currentPage > 0) {
      pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  /// Refreshes permissions and keyboard status on app resume.
  void handleResume(AppState appState) {
    checkMicPermission();
    appState.checkKeyboardStatus();
  }

  @override
  void dispose() {
    pageController.dispose();
    super.dispose();
  }
}
