import 'package:flutter/material.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/guide_timeline_card.dart';
import 'controller/voice_typing_guide_controller.dart';

/// Screen guiding users on Urdu voice typing setup and usage.
class VoiceTypingGuideScreen extends StatefulWidget {
  final VoiceTypingGuideController? controller;

  const VoiceTypingGuideScreen({super.key, this.controller});

  @override
  State<VoiceTypingGuideScreen> createState() => _VoiceTypingGuideScreenState();
}

class _VoiceTypingGuideScreenState extends State<VoiceTypingGuideScreen>
    with WidgetsBindingObserver {
  late final VoiceTypingGuideController _controller;

  @override
  void initState() {
    super.initState();
    _controller = widget.controller ?? VoiceTypingGuideController();
    WidgetsBinding.instance.addObserver(this);
    _controller.checkPermissionStatus();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    if (widget.controller == null) {
      _controller.dispose();
    }
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _controller.handleResume();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return ListenableBuilder(
      listenable: _controller,
      builder: (context, _) {
        return Scaffold(
          backgroundColor: AppColors.background,
          appBar: AppBar(
            title: Text(
              l10n.voiceGuideTitle,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          body: SafeArea(
            child: SingleChildScrollView(
              physics: const BouncingScrollPhysics(),
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    l10n.voiceGuideSubtitle,
                    style: const TextStyle(
                      fontSize: 15.5,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textSecondary,
                      height: 1.45,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                  const SizedBox(height: 20),
                  GuideTimelineCard(
                    title: l10n.homeStatusTitle,
                    steps: [
                      GuideTimelineStep(
                        stepNumber: 1,
                        title: l10n.voiceGuideStep1Title,
                        description: l10n.voiceGuideStep1Desc,
                      ),
                      GuideTimelineStep(
                        stepNumber: 2,
                        title: l10n.voiceGuideStep2Title,
                        description: l10n.voiceGuideStep2Desc,
                      ),
                      GuideTimelineStep(
                        stepNumber: 3,
                        title: l10n.voiceGuideStep3Title,
                        description: l10n.voiceGuideStep3Desc,
                      ),
                      GuideTimelineStep(
                        stepNumber: 4,
                        title: l10n.voiceGuideStep4Title,
                        description: l10n.voiceGuideStep4Desc,
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  GuideTipsCard(
                    title: l10n.voiceGuideTipsTitle,
                    tips: [
                      l10n.voiceGuideTip1,
                      l10n.voiceGuideTip2,
                      l10n.voiceGuideTip3,
                    ],
                  ),
                  const SizedBox(height: 24),
                  if (!_controller.isPermissionGranted)
                    AppButton.primary(
                      label: l10n.voiceGuidePermissionPrompt,
                      icon: Icons.mic_rounded,
                      height: 52,
                      borderRadius: 16,
                      onPressed: () => _controller.handlePermissionAction(context),
                    )
                  else
                    Center(
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.check_circle_rounded,
                            color: AppColors.primary,
                            size: 18,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            l10n.onboardingPermissionGranted,
                            style: const TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.w600,
                              color: AppColors.primary,
                              fontFamilyFallback: AppTheme.urduFontFallbacks,
                            ),
                          ),
                        ],
                      ),
                    ),
                  const SizedBox(height: 24),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
