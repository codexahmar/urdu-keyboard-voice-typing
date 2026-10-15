import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../state/app_state.dart';
import 'controller/onboarding_controller.dart';
import 'widgets/onboarding_bottom_bar.dart';
import 'widgets/onboarding_top_bar.dart';
import 'widgets/slide_enable_guide.dart';
import 'widgets/slide_phonetic_guide.dart';
import 'widgets/slide_switch_guide.dart';
import 'widgets/slide_voice_guide.dart';

/// Onboarding flow screen presenting educational guides.
class OnboardingScreen extends StatefulWidget {
  final VoidCallback onComplete;

  const OnboardingScreen({super.key, required this.onComplete});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen>
    with WidgetsBindingObserver {
  late final OnboardingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = OnboardingController();
    WidgetsBinding.instance.addObserver(this);
    _controller.checkMicPermission();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppState>().checkKeyboardStatus();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _controller.handleResume(context.read<AppState>());
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();

    return ListenableBuilder(
      listenable: _controller,
      builder: (context, _) {
        return Directionality(
          textDirection: TextDirection.ltr,
          child: Scaffold(
            backgroundColor: AppColors.background,
            body: SafeArea(
              child: Column(
                children: [
                  OnboardingTopBar(
                    appState: appState,
                    currentPage: _controller.currentPage,
                    totalPages: AppConstants.onboardingSteps,
                    onSkip: widget.onComplete,
                  ),
                  Expanded(
                    child: PageView(
                      controller: _controller.pageController,
                      physics: const ClampingScrollPhysics(),
                      onPageChanged: _controller.setPage,
                      children: [
                        const SlidePhoneticGuide(),
                        SlideVoiceGuide(
                          micGranted: _controller.micGranted,
                          onRequestPermission: () =>
                              _controller.requestMicPermission(context),
                        ),
                        SlideEnableGuide(appState: appState),
                        SlideSwitchGuide(appState: appState),
                      ],
                    ),
                  ),
                  OnboardingBottomBar(
                    currentPage: _controller.currentPage,
                    totalPages: AppConstants.onboardingSteps,
                    onPrevious: _controller.previousPage,
                    onNext: () => _controller.nextPage(widget.onComplete),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
