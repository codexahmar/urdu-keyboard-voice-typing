import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/app_button.dart';
import '../../../state/app_state.dart';

/// Onboarding slide explaining how to switch the active keyboard.
class SlideSwitchGuide extends StatelessWidget {
  final AppState appState;

  const SlideSwitchGuide({
    super.key,
    required this.appState,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final isActive = appState.isKeyboardActive;

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
              Icons.check_circle_outline_rounded,
              size: 36,
              color: AppColors.white,
            ),
          ),
          const SizedBox(height: 20),

          // Headline
          Text(
            l10n.onboardingPage4Title,
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
            l10n.onboardingPage4Subtitle,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 14,
              height: 1.45,
              color: AppColors.textSecondary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 20),

          // How-to Step 1: Select keyboard
          _buildInstructionStep(
            number: '1',
            icon: Icons.radio_button_checked_rounded,
            text: l10n.onboardingSelectStep1,
          ),
          const SizedBox(height: 10),

          // How-to Step 2: Open any app to type
          _buildInstructionStep(
            number: '2',
            icon: Icons.chat_bubble_outline_rounded,
            text: l10n.onboardingSelectStep2,
          ),
          const SizedBox(height: 16),

          // Visual Mockup of Input Method Picker
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
                        l10n.onboardingSimulatedPickerHint,
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.textMuted,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: isActive ? AppColors.primary : AppColors.white,
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(
                      color: isActive ? AppColors.primary : AppColors.border,
                      width: 1.2,
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        isActive ? Icons.check_circle_rounded : Icons.radio_button_checked_rounded,
                        size: 14,
                        color: isActive ? AppColors.white : AppColors.primary,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        l10n.active,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: isActive ? AppColors.white : AppColors.primary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Active Status & Switch Button
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isActive ? AppColors.primary : AppColors.border,
                width: isActive ? 1.5 : 1.2,
              ),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: isActive ? AppColors.primary : const Color(0xFF757575),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    isActive ? Icons.check_rounded : Icons.radio_button_unchecked_rounded,
                    color: AppColors.white,
                    size: 16,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    isActive
                        ? l10n.onboardingKeyboardAlreadyActive
                        : l10n.onboardingKeyboardNotYetActive,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: isActive ? AppColors.primary : AppColors.textSecondary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                AppButton.text(
                  label: l10n.onboardingSelectKeyboard,
                  onPressed: () => appState.openInputMethodPicker(),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),

          // Educational Tip Card: Teaching users how to easily switch languages while typing
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border, width: 1.2),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
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
                    Icons.lightbulb_rounded,
                    color: AppColors.white,
                    size: 20,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.onboardingSwitchTipTitle,
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: AppColors.primary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        l10n.onboardingSwitchTipSubtitle,
                        style: const TextStyle(
                          fontSize: 12.5,
                          height: 1.45,
                          color: AppColors.textSecondary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildInstructionStep({
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
