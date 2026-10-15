import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/app_permission_dialog.dart';

/// Clean, high-quality professional VoiceTypingGuideScreen.
/// Features:
/// 1. Hero Microphone header card with live permission status badge
/// 2. Clean connected visual timeline walkthrough (Steps 1 to 4)
/// 3. Best practices & accuracy tips card
/// 4. Direct 1-tap microphone permission tester button
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

              // Step-by-Step Connected Timeline Guide
              _buildTimelineGuideCard(l10n),
              const SizedBox(height: 20),

              // Accuracy Tips Card
              _buildTipsCard(l10n),
              const SizedBox(height: 24),

              // Direct 1-Tap Microphone Permission Button (only shown when needed to avoid repetition)
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

  Widget _buildTimelineGuideCard(AppLocalizations l10n) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border, width: 1.2),
        boxShadow: const [
          BoxShadow(
            color: Color(0x0A01411C),
            blurRadius: 12,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header without icon
          Text(
            l10n.homeStatusTitle,
            style: const TextStyle(
              fontSize: 15.5,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 18),

          // Steps 1 to 4
          _buildTimelineStep(
            stepNumber: 1,
            icon: Icons.chat_bubble_outline_rounded,
            title: l10n.voiceGuideStep1Title,
            description: l10n.voiceGuideStep1Desc,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 2,
            icon: Icons.mic_rounded,
            title: l10n.voiceGuideStep2Title,
            description: l10n.voiceGuideStep2Desc,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 3,
            icon: Icons.record_voice_over_rounded,
            title: l10n.voiceGuideStep3Title,
            description: l10n.voiceGuideStep3Desc,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 4,
            icon: Icons.check_circle_outline_rounded,
            title: l10n.voiceGuideStep4Title,
            description: l10n.voiceGuideStep4Desc,
            isLast: true,
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineStep({
    required int stepNumber,
    required IconData icon,
    required String title,
    required String description,
    required bool isLast,
  }) {
    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Step Indicator & Connecting Line
          Column(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: const BoxDecoration(
                  color: AppColors.primary,
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: Text(
                  '$stepNumber',
                  style: const TextStyle(
                    color: AppColors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                ),
              ),
              if (!isLast)
                Expanded(
                  child: Container(
                    width: 2,
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    color: const Color(0xFFE2E8F0),
                  ),
                ),
            ],
          ),
          const SizedBox(width: 14),

          // Content
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(bottom: isLast ? 0 : 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(icon, size: 16, color: AppColors.primary),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          title,
                          style: const TextStyle(
                            fontSize: 14.5,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimary,
                            fontFamilyFallback: AppTheme.urduFontFallbacks,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: const TextStyle(
                      fontSize: 13,
                      color: AppColors.textSecondary,
                      height: 1.4,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTipsCard(AppLocalizations l10n) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFF9FCFA),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE0EFE2), width: 1.2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: const Color(0xFFE8F5E9),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.lightbulb_outline_rounded,
                  size: 18,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 10),
              Text(
                l10n.voiceGuideTipsTitle,
                style: const TextStyle(
                  fontSize: 14.5,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          _buildTipItem(l10n.voiceGuideTip1),
          const SizedBox(height: 10),
          _buildTipItem(l10n.voiceGuideTip2),
          const SizedBox(height: 10),
          _buildTipItem(l10n.voiceGuideTip3),
        ],
      ),
    );
  }

  Widget _buildTipItem(String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          margin: const EdgeInsets.only(top: 3),
          width: 16,
          height: 16,
          decoration: const BoxDecoration(
            color: Color(0xFFE8F5E9),
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.check_rounded,
            size: 11,
            color: AppColors.primary,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textPrimary,
              height: 1.4,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
        ),
      ],
    );
  }
}
