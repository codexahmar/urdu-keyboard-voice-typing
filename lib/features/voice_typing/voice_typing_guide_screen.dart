import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/app_permission_dialog.dart';
import '../../core/widgets/guide_timeline_card.dart';

/// Clean, high-quality professional VoiceTypingGuideScreen.
/// Features:
/// 1. Subtitle outside container on the left
/// 2. Global GuideTimelineCard (steps without left icons)
/// 3. Global GuideTipsCard with pure white background
/// 4. Direct 1-tap microphone permission button
class VoiceTypingGuideScreen extends StatefulWidget {
  const VoiceTypingGuideScreen({super.key});

  @override
  State<VoiceTypingGuideScreen> createState() => _VoiceTypingGuideScreenState();
}

class _VoiceTypingGuideScreenState extends State<VoiceTypingGuideScreen>
    with WidgetsBindingObserver {
  bool _isPermissionGranted = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkPermissionStatus();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkPermissionStatus();
    }
  }

  Future<void> _checkPermissionStatus() async {
    final status = await Permission.microphone.status;
    if (mounted) {
      setState(() {
        _isPermissionGranted = status.isGranted;
      });
    }
  }

  Future<void> _handlePermissionAction(BuildContext context) async {
    final l10n = context.l10n;
    final status = await Permission.microphone.status;

    if (status.isGranted) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: [
                const Icon(Icons.check_circle_rounded, color: AppColors.white, size: 20),
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
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
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
            await _checkPermissionStatus();
          },
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

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
              // Clean subtitle outside of any container on the left with bigger font size
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

              // Step-by-Step Connected Timeline Guide (No left icons)
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

              // Accuracy Tips Card with Pure White Background
              GuideTipsCard(
                title: l10n.voiceGuideTipsTitle,
                tips: [
                  l10n.voiceGuideTip1,
                  l10n.voiceGuideTip2,
                  l10n.voiceGuideTip3,
                ],
              ),
              const SizedBox(height: 24),

              // Direct 1-Tap Microphone Permission Button
              if (!_isPermissionGranted)
                AppButton.primary(
                  label: l10n.voiceGuidePermissionPrompt,
                  icon: Icons.mic_rounded,
                  height: 52,
                  borderRadius: 16,
                  onPressed: () => _handlePermissionAction(context),
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
  }
}
