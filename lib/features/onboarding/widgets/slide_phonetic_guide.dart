import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';

/// Slide 1: Teaches users how phonetic Roman-to-Urdu typing works
class SlidePhoneticGuide extends StatelessWidget {
  const SlidePhoneticGuide({super.key});

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

          // Hero Icon Badge with solid background color
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
              Icons.keyboard_alt_rounded,
              size: 36,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 20),

          // Headline
          Text(
            l10n.onboardingPage1Title,
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
            l10n.onboardingPage1Subtitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              color: AppColors.textSecondary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 24),

          // Feature explanation 1: Roman phonetic
          _buildGuideCard(
            icon: Icons.spellcheck_rounded,
            title: l10n.onboardingPhoneticTitle,
            description: l10n.onboardingPhoneticSubtitle,
          ),
          const SizedBox(height: 12),

          // Feature explanation 2: 1-Tap Toggle
          _buildGuideCard(
            icon: Icons.swap_horizontal_circle_rounded,
            title: l10n.onboardingSwitchTitle,
            description: l10n.onboardingSwitchSubtitle,
          ),
          const SizedBox(height: 12),

          // Feature explanation 3: Voice typing intro
          _buildGuideCard(
            icon: Icons.mic_rounded,
            title: l10n.onboardingVoiceFeatureTitle,
            description: l10n.onboardingVoiceFeatureSubtitle,
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildGuideCard({
    required IconData icon,
    required String title,
    required String description,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border, width: 1.2),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
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
            child: Icon(icon, color: AppColors.white, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                    fontFamilyFallback: AppTheme.urduFontFallbacks,
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  description,
                  style: const TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                    height: 1.35,
                    fontFamilyFallback: AppTheme.urduFontFallbacks,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
