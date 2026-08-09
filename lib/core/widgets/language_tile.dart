import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_theme.dart';

/// Reusable language selection tile widget
/// Upgraded to modern Material 3 specs with high accessibility and dynamic touch states
class LanguageTile extends StatelessWidget {
  final String name;
  final String nativeName;
  final String description;
  final bool isSelected;
  final bool isRecommended;
  final VoidCallback onTap;

  const LanguageTile({
    Key? key,
    required this.name,
    required this.nativeName,
    required this.description,
    required this.isSelected,
    required this.isRecommended,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final primaryColor = isDark ? AppColors.primaryLight : AppColors.primary;

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: isSelected
                ? primaryColor.withOpacity(isDark ? 0.2 : 0.1)
                : Colors.black.withOpacity(isDark ? 0.15 : 0.02),
            blurRadius: isSelected ? 12 : 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Card(
        elevation: 0,
        color: isDark ? AppColors.surfaceDark : Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: BorderSide(
            color: isSelected
                ? primaryColor
                : (isDark ? AppColors.dividerDark : AppColors.divider),
            width: isSelected ? 2 : 1.5,
          ),
        ),
        margin: EdgeInsets.zero,
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          splashColor: primaryColor.withOpacity(0.1),
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Row(
              children: [
                // Selection Radio Check Indicator
                AnimatedContainer(
                  duration: const Duration(milliseconds: 250),
                  width: 26,
                  height: 26,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: isSelected
                          ? primaryColor
                          : (isDark ? AppColors.textMutedDark : AppColors.textMuted),
                      width: 2,
                    ),
                    color: isSelected ? primaryColor : Colors.transparent,
                  ),
                  child: isSelected
                      ? Icon(
                          Icons.check_rounded,
                          size: 18,
                          color: isDark ? Colors.black : Colors.white,
                        )
                      : null,
                ),
                const SizedBox(width: 18),

                // Language Description Content
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(
                            name,
                            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                                ),
                          ),
                          const SizedBox(width: 10),
                          if (isRecommended)
                            Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 10,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: primaryColor.withOpacity(0.12),
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    Icons.star_rounded,
                                    size: 14,
                                    color: primaryColor,
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    'Recommended',
                                    style: TextStyle(
                                      fontSize: 11,
                                      fontWeight: FontWeight.bold,
                                      color: primaryColor,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        nativeName,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                          color: isDark ? AppColors.primaryLight : AppColors.primary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        description,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                              height: 1.3,
                            ),
                      ),
                    ],
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
