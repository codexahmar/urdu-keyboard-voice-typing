import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/app_button.dart';
import '../../../state/app_state.dart';

/// Onboarding slide explaining how to enable the keyboard.
class SlideEnableGuide extends StatelessWidget {
  final AppState appState;

  const SlideEnableGuide({super.key, required this.appState});

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final isEnabled = appState.isKeyboardEnabled;

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
              Icons.tune_rounded,
              size: 36,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 20),

          // Headline
          Text(
            l10n.onboardingPage3Title,
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
            l10n.onboardingPage3Subtitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              color: AppColors.textSecondary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 20),

          // Tutorial Step 1
          _buildTutorialStep(
            number: '1',
            icon: Icons.open_in_new_rounded,
            text: l10n.onboardingEnableStep1,
          ),
          const SizedBox(height: 10),

          // Tutorial Step 2
          _buildTutorialStep(
            number: '2',
            icon: Icons.toggle_on_rounded,
            text: l10n.onboardingEnableStep2,
          ),
          const SizedBox(height: 16),

          // Visual Mockup of Android Settings toggle
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: AppColors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border, width: 1.2),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
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
                  child: const Icon(
                    Icons.keyboard_alt_rounded,
                    color: AppColors.white,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Urdu Smart Keyboard',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        l10n.onboardingSimulatedToggleHint,
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.textMuted,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
                // Simulated Android Switch Toggle
                Container(
                  width: 46,
                  height: 26,
                  padding: const EdgeInsets.all(2),
                  decoration: BoxDecoration(
                    color: isEnabled ? AppColors.primary : const Color(0xFF007A33),
                    borderRadius: BorderRadius.circular(13),
                  ),
                  alignment: Alignment.centerRight,
                  child: Container(
                    width: 22,
                    height: 22,
                    decoration: const BoxDecoration(
                      color: AppColors.white,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.check,
                      size: 14,
                      color: AppColors.primary,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Live System Status Card
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isEnabled ? AppColors.primary : AppColors.border,
                width: isEnabled ? 1.5 : 1.2,
              ),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: isEnabled ? AppColors.primary : const Color(0xFF757575),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    isEnabled ? Icons.check_rounded : Icons.info_outline_rounded,
                    color: AppColors.white,
                    size: 16,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    isEnabled
                        ? l10n.onboardingKeyboardAlreadyEnabled
                        : l10n.onboardingKeyboardNotYetEnabled,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: isEnabled ? AppColors.primary : AppColors.textPrimary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 22),

          // Action Button
          AppButton.primary(
            label: l10n.onboardingOpenKeyboardSettings,
            icon: Icons.settings_rounded,
            onPressed: () => appState.openKeyboardSettings(),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildTutorialStep({
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
}
