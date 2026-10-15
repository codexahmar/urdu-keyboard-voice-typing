import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import 'app_button.dart';

/// Modal dialog for requesting runtime permissions.
class AppPermissionDialog extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final String allowText;
  final String cancelText;
  final VoidCallback onAllow;
  final VoidCallback? onCancel;

  const AppPermissionDialog({
    super.key,
    required this.icon,
    required this.title,
    required this.description,
    required this.allowText,
    required this.cancelText,
    required this.onAllow,
    this.onCancel,
  });

  static Future<bool?> show(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String description,
    required String allowText,
    required String cancelText,
    required VoidCallback onAllow,
    VoidCallback? onCancel,
  }) {
    return showDialog<bool>(
      context: context,
      barrierDismissible: true,
      builder: (ctx) => AppPermissionDialog(
        icon: icon,
        title: title,
        description: description,
        allowText: allowText,
        cancelText: cancelText,
        onAllow: onAllow,
        onCancel: onCancel,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: AppColors.surface,
      elevation: 6,
      insetPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side: const BorderSide(color: AppColors.greenBorder, width: 1.2),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Icon Badge
            Container(
              width: 64,
              height: 64,
              decoration: BoxDecoration(
                color: AppColors.greenLight,
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.greenBorder, width: 1.5),
              ),
              child: Icon(
                icon,
                color: AppColors.primary,
                size: 30,
              ),
            ),
            const SizedBox(height: 18),

            // Title
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 19,
                fontWeight: FontWeight.bold,
                color: AppColors.textPrimary,
                fontFamilyFallback: [
                  'Noto Nastaliq Urdu',
                  'Jameel Noori Nastaleeq',
                  'sans-serif',
                ],
              ),
            ),
            const SizedBox(height: 10),

            // Description
            Text(
              description,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 14,
                height: 1.45,
                color: AppColors.textSecondary,
                fontFamilyFallback: [
                  'Noto Nastaliq Urdu',
                  'Jameel Noori Nastaleeq',
                  'sans-serif',
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Action Buttons
            AppButton.primary(
              label: allowText,
              onPressed: () {
                Navigator.of(context).pop(true);
                onAllow();
              },
            ),
            const SizedBox(height: 10),
            AppButton.text(
              label: cancelText,
              onPressed: () {
                Navigator.of(context).pop(false);
                if (onCancel != null) onCancel!();
              },
            ),
          ],
        ),
      ),
    );
  }
}
