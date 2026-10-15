import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/widgets/app_permission_dialog.dart';
import '../../state/app_state.dart';
import 'widgets/onboarding_bottom_bar.dart';
import 'widgets/onboarding_top_bar.dart';
import 'widgets/slide_enable_guide.dart';
import 'widgets/slide_phonetic_guide.dart';
import 'widgets/slide_switch_guide.dart';
import 'widgets/slide_voice_guide.dart';

/// Onboarding flow guiding users through setup and key features.
class OnboardingScreen extends StatefulWidget {
  final VoidCallback onComplete;

  const OnboardingScreen({super.key, required this.onComplete});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen>
    with WidgetsBindingObserver {
  final PageController _pageController = PageController();

  int _currentPage = 0;
  bool _micGranted = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkMicPermission();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppState>().checkKeyboardStatus();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _pageController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkMicPermission();
      context.read<AppState>().checkKeyboardStatus();
    }
  }

  Future<void> _checkMicPermission() async {
    try {
      final status = await Permission.microphone.status;
      if (mounted) {
        setState(() {
          _micGranted = status.isGranted;
        });
      }
    } catch (_) {}
  }

  Future<void> _requestMicPermission() async {
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
        if (mounted) {
          setState(() {
            _micGranted = result.isGranted;
          });
          if (result.isPermanentlyDenied) {
            await openAppSettings();
          }
        }
      },
    );
  }

  void _nextPage() {
    if (_currentPage < AppConstants.onboardingSteps - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      widget.onComplete();
    }
  }

  void _previousPage() {
    if (_currentPage > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();

    return Directionality(
      textDirection: TextDirection.ltr,
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: SafeArea(
          child: Column(
            children: [
              // Standard Top Bar with Language Dropdown & Skip button
              OnboardingTopBar(
                appState: appState,
                currentPage: _currentPage,
                totalPages: AppConstants.onboardingSteps,
                onSkip: widget.onComplete,
              ),

              // Main PageView with 4 educational slides
              Expanded(
                child: PageView(
                  controller: _pageController,
                  physics: const ClampingScrollPhysics(),
                  onPageChanged: (index) {
                    setState(() {
                      _currentPage = index;
                    });
                  },
                  children: [
                    const SlidePhoneticGuide(),
                    SlideVoiceGuide(
                      micGranted: _micGranted,
                      onRequestPermission: _requestMicPermission,
                    ),
                    SlideEnableGuide(appState: appState),
                    SlideSwitchGuide(appState: appState),
                  ],
                ),
              ),

              // Standard Bottom Bar with Back, Dots, and Next/Start button
              OnboardingBottomBar(
                currentPage: _currentPage,
                totalPages: AppConstants.onboardingSteps,
                onPrevious: _previousPage,
                onNext: _nextPage,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
