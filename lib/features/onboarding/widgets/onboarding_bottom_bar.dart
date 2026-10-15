import 'package:flutter/material.dart';
import '../../../core/localization/app_localizations.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/step_indicator.dart';

/// Standard mobile onboarding bottom bar with Back, Dot Indicators, and Next/Start button
class OnboardingBottomBar extends StatelessWidget {
  final int currentPage;
  final int totalPages;
  final VoidCallback onPrevious;
  final VoidCallback onNext;

  const OnboardingBottomBar({
    super.key,
    required this.currentPage,
    required this.totalPages,
    required this.onPrevious,
    required this.onNext,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final isLast = currentPage == totalPages - 1;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 18),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Back Button or Spacer (Smooth fade in without layout shift)
          SizedBox(
            width: 80,
            child: AnimatedOpacity(
              duration: const Duration(milliseconds: 200),
              opacity: currentPage > 0 ? 1.0 : 0.0,
              child: TextButton(
                onPressed: currentPage > 0 ? onPrevious : null,
                style: TextButton.styleFrom(
                  foregroundColor: AppColors.textSecondary,
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 8),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(
                      Icons.arrow_back_rounded,
                      size: 16,
                      color: AppColors.textSecondary,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      l10n.back,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textSecondary,
                        fontFamilyFallback: AppTheme.urduFontFallbacks,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),

          // Center Page Indicator Dots
          StepIndicator(
            currentStep: currentPage,
            totalSteps: totalPages,
          ),

          // Next / Get Started Action Button
          ElevatedButton(
            onPressed: onNext,
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: AppColors.white,
              elevation: 2,
              shadowColor: const Color(0x3301411C),
              padding: EdgeInsets.symmetric(
                horizontal: isLast ? 24 : 20,
                vertical: 12,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(24),
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  isLast ? l10n.getStarted : l10n.next,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    color: AppColors.white,
                    fontFamilyFallback: AppTheme.urduFontFallbacks,
                  ),
                ),
                const SizedBox(width: 6),
                Icon(
                  isLast ? Icons.check_circle_rounded : Icons.arrow_forward_rounded,
                  size: 18,
                  color: AppColors.white,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
