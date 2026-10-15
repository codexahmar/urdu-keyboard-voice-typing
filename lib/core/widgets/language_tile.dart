import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

/// Reusable language selection tile widget in Pakistan Green & White theme
class LanguageTile extends StatelessWidget {
  final String name;
  final String nativeName;
  final String description;
  final bool isSelected;
  final bool isRecommended;
  final String? recommendedLabel;
  final VoidCallback onTap;

  const LanguageTile({
    super.key,
    required this.name,
    required this.nativeName,
    required this.description,
    required this.isSelected,
    required this.isRecommended,
    this.recommendedLabel,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: isSelected ? AppColors.greenLight : AppColors.surface,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(
          color: isSelected ? AppColors.primary : AppColors.border,
          width: isSelected ? 2.0 : 1.2,
        ),
        boxShadow: [
          BoxShadow(
            color: isSelected
                ? const Color(0x1801411C)
                : const Color(0x0601411C),
            blurRadius: isSelected ? 10 : 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(18),
          splashColor: AppColors.primary.withValues(alpha: 0.08),
          highlightColor: AppColors.primary.withValues(alpha: 0.04),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
            child: Row(
              children: [
                // Radio indicator
                Container(
                  width: 24,
                  height: 24,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: isSelected ? AppColors.primary : Colors.transparent,
                    border: Border.all(
                      color: isSelected ? AppColors.primary : AppColors.border,
                      width: 2,
                    ),
                  ),
                  child: isSelected
                      ? const Icon(
                          Icons.check,
                          size: 16,
                          color: AppColors.white,
                        )
                      : null,
                ),
                const SizedBox(width: 16),

                // Language Labels
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Row(
                        children: [
                          Text(
                            nativeName,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: AppColors.textPrimary,
                              fontFamilyFallback: [
                                'Noto Nastaliq Urdu',
                                'Jameel Noori Nastaleeq',
                                'sans-serif',
                              ],
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            '($name)',
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w500,
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 2),
                      Text(
                        description,
                        style: const TextStyle(
                          fontSize: 13,
                          color: AppColors.textMuted,
                          fontFamilyFallback: [
                            'Noto Nastaliq Urdu',
                            'Jameel Noori Nastaleeq',
                            'sans-serif',
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                // Recommended badge
                if (isRecommended)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.primary,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(
                      recommendedLabel ?? 'Recommended',
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: AppColors.white,
                        fontFamilyFallback: [
                          'Noto Nastaliq Urdu',
                          'Jameel Noori Nastaleeq',
                          'sans-serif',
                        ],
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
