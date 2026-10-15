import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/app_button.dart';

/// Onboarding slide explaining voice typing.
class SlideVoiceGuide extends StatelessWidget {
  final bool micGranted;
  final VoidCallback onRequestPermission;

  const SlideVoiceGuide({
    super.key,
    required this.micGranted,
    required this.onRequestPermission,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          const SizedBox(height: 12),

          // Microphone Hero Demonstration Box
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 22, horizontal: 20),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(22),
              border: Border.all(color: AppColors.greenBorder, width: 1.2),
              boxShadow: const [
                BoxShadow(
                  color: Color(0x0801411C),
                  blurRadius: 10,
                  offset: Offset(0, 3),
                ),
              ],
            ),
            child: Column(
              children: [
                // Mic Icon Badge with animated audio waves
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _buildWaveBar(height: 14),
                    const SizedBox(width: 4),
                    _buildWaveBar(height: 24),
                    const SizedBox(width: 4),
                    _buildWaveBar(height: 38),
                    const SizedBox(width: 14),
                    Container(
                      width: 72,
                      height: 72,
                      decoration: const BoxDecoration(
                        color: AppColors.primary,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: Color(0x3301411C),
                            blurRadius: 14,
                            offset: Offset(0, 5),
                          ),
                        ],
                      ),
                      child: const Icon(
                        Icons.mic_rounded,
                        size: 36,
                        color: AppColors.white,
                      ),
                    ),
                    const SizedBox(width: 14),
                    _buildWaveBar(height: 38),
                    const SizedBox(width: 4),
                    _buildWaveBar(height: 24),
                    const SizedBox(width: 4),
                    _buildWaveBar(height: 14),
                  ],
                ),
                const SizedBox(height: 16),

                // Simulated Urdu Speech Bubble
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                  decoration: BoxDecoration(
                    color: AppColors.white,
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: AppColors.border, width: 1.2),
                  ),
                  child: Column(
                    children: [
                      Text(
                        l10n.onboardingSampleSpeech,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.bold,
                          color: AppColors.primary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        l10n.onboardingSpeechRecognized,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 12,
                          color: AppColors.textMuted,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 22),

          // Headline
          Text(
            l10n.onboardingPage2Title,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 8),

          // Subtitle
          Text(
            l10n.onboardingPage2Subtitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              color: AppColors.textSecondary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 20),

          // Step 1: Tap Mic
          _buildStepRow(
            number: '1',
            icon: Icons.touch_app_rounded,
            text: l10n.onboardingVoiceStep1,
          ),
          const SizedBox(height: 10),

          // Step 2: Speak clearly
          _buildStepRow(
            number: '2',
            icon: Icons.record_voice_over_rounded,
            text: l10n.onboardingVoiceStep2,
          ),
          const SizedBox(height: 10),

          // Step 3: Instant text
          _buildStepRow(
            number: '3',
            icon: Icons.auto_awesome_rounded,
            text: l10n.onboardingVoiceStep3,
          ),
          const SizedBox(height: 22),

          // Microphone Permission Action
          if (micGranted)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: AppColors.white,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.border, width: 1.2),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: AppColors.primary,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.check_rounded,
                      color: AppColors.white,
                      size: 16,
                    ),
                  ),
                  const SizedBox(width: 10),
                  Text(
                    l10n.onboardingPermissionGranted,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: AppColors.primary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ],
              ),
            )
          else
            AppButton.outlined(
              label: l10n.onboardingGrantPermission,
              icon: Icons.mic_none_rounded,
              onPressed: onRequestPermission,
            ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildStepRow({
    required String number,
    required IconData icon,
    required String text,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(9),
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(10),
              boxShadow: const [
                BoxShadow(
                  color: Color(0x2201411C),
                  blurRadius: 4,
                  offset: Offset(0, 2),
                ),
              ],
            ),
            child: Icon(icon, color: AppColors.white, size: 18),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: AppColors.textPrimary,
                fontFamilyFallback: AppTheme.urduFontFallbacks,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWaveBar({required double height}) {
    return Container(
      width: 4,
      height: height,
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.65),
        borderRadius: BorderRadius.circular(2),
      ),
    );
  }
}
