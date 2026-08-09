import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../state/app_state.dart';

/// Settings screen
/// Upgraded with modern Material 3 grouped settings cards and clean control elements
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Initialize app state
    final appState = context.watch<AppState>();
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final primaryColor = isDark ? AppColors.primaryLight : AppColors.primary;

    return Scaffold(
      backgroundColor: isDark ? AppColors.backgroundDark : AppColors.background,
      appBar: AppBar(title: const Text(AppStrings.settingsTitle)),
      body: SafeArea(
        child: ListView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.all(20),
          children: [
            // Appearance Section Header
            Padding(
              padding: const EdgeInsets.only(left: 4, bottom: 12),
              child: Text(
                AppStrings.appearance,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: primaryColor,
                  letterSpacing: 1.0,
                ),
              ),
            ),

            // Theme Mode Card
            Card(
              elevation: 0,
              color: isDark ? AppColors.surfaceDark : Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(
                  color: isDark ? AppColors.dividerDark : AppColors.divider,
                ),
              ),
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                            color: primaryColor.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Icon(
                            Icons.brightness_6_rounded,
                            color: primaryColor,
                            size: 22,
                          ),
                        ),
                        const SizedBox(width: 14),
                        Text(
                          AppStrings.themeMode,
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    SizedBox(
                      width: double.infinity,
                      child: SegmentedButton<ThemeMode>(
                        segments: const [
                          ButtonSegment(
                            value: ThemeMode.light,
                            label: Text(AppStrings.light),
                            icon: Icon(Icons.light_mode_outlined, size: 18),
                          ),
                          ButtonSegment(
                            value: ThemeMode.dark,
                            label: Text(AppStrings.dark),
                            icon: Icon(Icons.dark_mode_outlined, size: 18),
                          ),
                          ButtonSegment(
                            value: ThemeMode.system,
                            label: Text(AppStrings.system),
                            icon: Icon(Icons.settings_suggest_outlined, size: 18),
                          ),
                        ],
                        selected: {appState.themeMode},
                        onSelectionChanged: (Set<ThemeMode> newSelection) {
                          appState.setThemeMode(newSelection.first);
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Text Size Slider Card
            Card(
              elevation: 0,
              color: isDark ? AppColors.surfaceDark : Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(
                  color: isDark ? AppColors.dividerDark : AppColors.divider,
                ),
              ),
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                            color: primaryColor.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Icon(
                            Icons.text_fields_rounded,
                            color: primaryColor,
                            size: 22,
                          ),
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: Text(
                            AppStrings.textSize,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: primaryColor.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            '${(appState.textSizeScale * 100).round()}%',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: primaryColor,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Icon(
                          Icons.text_decrease_rounded,
                          size: 20,
                          color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        ),
                        Expanded(
                          child: Slider(
                            value: appState.textSizeScale,
                            min: 0.8,
                            max: 1.5,
                            divisions: 7,
                            label: '${(appState.textSizeScale * 100).round()}%',
                            onChanged: (value) {
                              appState.setTextSizeScale(value);
                            },
                          ),
                        ),
                        Icon(
                          Icons.text_increase_rounded,
                          size: 24,
                          color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        ),
                      ],
                    ),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 14),
                      decoration: BoxDecoration(
                        color: isDark ? AppColors.surfaceVariantDark : AppColors.surfaceVariant,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        'Preview Text: اردو کی بورڈ (Sample)',
                        style: TextStyle(
                          fontSize: 14 * appState.textSizeScale,
                          fontWeight: FontWeight.w500,
                          color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 28),

            // About Section Header
            Padding(
              padding: const EdgeInsets.only(left: 4, bottom: 12),
              child: Text(
                AppStrings.about,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: primaryColor,
                  letterSpacing: 1.0,
                ),
              ),
            ),

            // About Options List Group
            Card(
              elevation: 0,
              color: isDark ? AppColors.surfaceDark : Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(
                  color: isDark ? AppColors.dividerDark : AppColors.divider,
                ),
              ),
              child: Column(
                children: [
                  ListTile(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                    leading: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.info_outline_rounded, color: Colors.blue, size: 20),
                    ),
                    title: const Text(AppStrings.version, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    trailing: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: isDark ? AppColors.surfaceVariantDark : AppColors.surfaceVariant,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        AppConstants.appVersion,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                    ),
                  ),
                  Divider(height: 1, color: isDark ? AppColors.dividerDark : AppColors.divider),
                  ListTile(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                    leading: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: AppColors.urduAccent.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.person_outline_rounded, color: AppColors.urduAccent, size: 20),
                    ),
                    title: const Text(AppStrings.developer, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    trailing: Text(
                      AppStrings.developerName,
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                        color: primaryColor,
                      ),
                    ),
                  ),
                  Divider(height: 1, color: isDark ? AppColors.dividerDark : AppColors.divider),
                  ListTile(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                    leading: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.orange.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.description_outlined, color: Colors.orange, size: 20),
                    ),
                    title: const Text('Description', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    subtitle: Padding(
                      padding: const EdgeInsets.only(top: 4.0),
                      child: Text(
                        AppConstants.appDescription,
                        style: TextStyle(
                          fontSize: 13,
                          color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        ),
                      ),
                    ),
                  ),
                  Divider(height: 1, color: isDark ? AppColors.dividerDark : AppColors.divider),
                  ListTile(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                    leading: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.purple.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.privacy_tip_outlined, color: Colors.purple, size: 20),
                    ),
                    title: const Text('Privacy Policy', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    trailing: const Icon(Icons.chevron_right_rounded, size: 20),
                    onTap: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: const Text('Privacy Policy: No user data is ever collected or transmitted.'),
                          backgroundColor: primaryColor,
                        ),
                      );
                    },
                  ),
                  Divider(height: 1, color: isDark ? AppColors.dividerDark : AppColors.divider),
                  ListTile(
                    contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                    leading: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.teal.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.code_rounded, color: Colors.teal, size: 20),
                    ),
                    title: const Text('Open Source Licenses', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    trailing: const Icon(Icons.chevron_right_rounded, size: 20),
                    onTap: () {
                      showLicensePage(
                        context: context,
                        applicationName: AppConstants.appName,
                        applicationVersion: AppConstants.appVersion,
                      );
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
