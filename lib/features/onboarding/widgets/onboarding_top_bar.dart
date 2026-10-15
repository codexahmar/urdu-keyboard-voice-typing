import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../state/app_state.dart';

/// Sleek top bar with 1-tap segmented language switcher and skip button
class OnboardingTopBar extends StatelessWidget {
  final AppState appState;
  final int currentPage;
  final int totalPages;
  final VoidCallback onSkip;

  const OnboardingTopBar({
    super.key,
    required this.appState,
    required this.currentPage,
    required this.totalPages,
    required this.onSkip,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // 1-Tap Modern Segmented Language Pill
          Container(
            padding: const EdgeInsets.all(3),
            decoration: BoxDecoration(
              color: AppColors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AppColors.border, width: 1.2),
              boxShadow: const [
                BoxShadow(
                  color: Color(0x0A000000),
                  blurRadius: 4,
                  offset: Offset(0, 1),
                ),
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _buildLangButton(
                  label: 'اردو',
                  isSelected: appState.selectedLanguage == 'ur',
                  onTap: () => appState.setSelectedLanguage('ur'),
                ),
                _buildLangButton(
                  label: 'English',
                  isSelected: appState.selectedLanguage == 'en',
                  onTap: () => appState.setSelectedLanguage('en'),
                ),
              ],
            ),
          ),

          // Skip Button (Visible on earlier slides)
          if (currentPage < totalPages - 1)
            TextButton(
              onPressed: onSkip,
              style: TextButton.styleFrom(
                foregroundColor: AppColors.primary,
                backgroundColor: AppColors.white,
                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(24),
                  side: const BorderSide(color: AppColors.border, width: 1.2),
                ),
              ),
              child: Text(
                l10n.skip,
                style: const TextStyle(
                  fontSize: 13.5,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
            )
          else
            const SizedBox(width: 48),
        ],
      ),
    );
  }

  Widget _buildLangButton({
    required String label,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          boxShadow: isSelected
              ? const [
                  BoxShadow(
                    color: Color(0x2401411C),
                    blurRadius: 4,
                    offset: Offset(0, 1.5),
                  ),
                ]
              : null,
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: isSelected ? AppColors.white : AppColors.textSecondary,
            fontFamilyFallback: AppTheme.urduFontFallbacks,
          ),
        ),
      ),
    );
  }
}
