import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/action_card.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/app_permission_dialog.dart';
import '../../state/app_state.dart';

/// Main home screen displaying keyboard activation status and quick actions.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppState>().checkKeyboardStatus();
      _checkInitialPermissions();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      context.read<AppState>().checkKeyboardStatus();
    }
  }

  Future<void> _checkInitialPermissions() async {
    final status = await Permission.microphone.status;
    if (status.isDenied && mounted) {
      final l10n = context.l10n;
      await AppPermissionDialog.show(
        context,
        icon: Icons.mic_rounded,
        title: l10n.permissionMicTitle,
        description: l10n.permissionMicDesc,
        allowText: l10n.permissionAllow,
        cancelText: l10n.permissionNotNow,
        onAllow: () async {
          await Permission.microphone.request();
        },
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final appState = context.watch<AppState>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(
          l10n.homeWelcome,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
            child: Material(
              color: AppColors.white,
              shape: const CircleBorder(
                side: BorderSide(color: AppColors.border, width: 1.2),
              ),
              clipBehavior: Clip.antiAlias,
              child: InkWell(
                onTap: () => Navigator.pushNamed(context, AppConstants.settingsRoute),
                child: const Padding(
                  padding: EdgeInsets.all(9),
                  child: Icon(
                    Icons.settings_outlined,
                    color: AppColors.primary,
                    size: 22,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Subtitle greeting
              Text(
                l10n.homeSubtitle,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: AppColors.textSecondary,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
              const SizedBox(height: 18),

              // Hero Keyboard Status Card
              _buildHeroStatusCard(context, l10n, appState),
              const SizedBox(height: 26),

              // Section Title: Quick Tools & Features
              Text(
                l10n.settingsHelpSection,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                  letterSpacing: 0.3,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
              const SizedBox(height: 12),

              // Card 1: Voice Typing Guide
              ActionCard(
                icon: Icons.mic_rounded,
                title: l10n.homeCardVoiceTitle,
                subtitle: l10n.homeCardVoiceSubtitle,
                onTap: () {
                  Navigator.pushNamed(context, AppConstants.voiceTypingGuideRoute);
                },
              ),
              const SizedBox(height: 12),

              // Card 2: Keyboard Layout Preview
              ActionCard(
                icon: Icons.keyboard_alt_rounded,
                title: l10n.homeCardPreviewTitle,
                subtitle: l10n.homeCardPreviewSubtitle,
                onTap: () {
                  Navigator.pushNamed(context, AppConstants.keyboardPreviewRoute);
                },
              ),
              const SizedBox(height: 12),

              // Card 3: App Settings & Language
              ActionCard(
                icon: Icons.tune_rounded,
                title: l10n.settingsTitle,
                subtitle: appState.selectedLanguage == 'ur'
                    ? 'اردو • سیٹنگز اور زبان تبدیل کریں'
                    : 'English • Settings, Language & Help',
                onTap: () {
                  Navigator.pushNamed(context, AppConstants.settingsRoute);
                },
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeroStatusCard(
    BuildContext context,
    AppLocalizations l10n,
    AppState appState,
  ) {
    final isEnabled = appState.isKeyboardEnabled;
    final isActive = appState.isKeyboardActive;
    final isReady = isEnabled && isActive;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border, width: 1.2),
        boxShadow: const [
          BoxShadow(
            color: Color(0x0A01411C),
            blurRadius: 12,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with status indicator dot and clean badge
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Row(
                  children: [
                    Container(
                      width: 9,
                      height: 9,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: isReady ? AppColors.primary : AppColors.warning,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Flexible(
                      child: Text(
                        l10n.homeStatusTitle,
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: AppColors.textPrimary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ),
              if (isReady) ...[
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: const Color(0xFFE8F5E9),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 6,
                        height: 6,
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          color: AppColors.primary,
                        ),
                      ),
                      const SizedBox(width: 5),
                      Text(
                        l10n.active,
                        style: const TextStyle(
                          fontSize: 11.5,
                          fontWeight: FontWeight.bold,
                          color: AppColors.primary,
                          fontFamilyFallback: AppTheme.urduFontFallbacks,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
          const SizedBox(height: 16),

          if (isReady) ...[
            // Refined, clean active status box
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: const Color(0xFFF6FAF7),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: const Color(0xFFE0EFE2)),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 38,
                    height: 38,
                    decoration: const BoxDecoration(
                      color: Color(0xFFE8F5E9),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.check_circle_rounded,
                      color: AppColors.primary,
                      size: 22,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n.onboardingKeyboardAlreadyActive,
                          style: const TextStyle(
                            fontSize: 14.5,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimary,
                            fontFamilyFallback: AppTheme.urduFontFallbacks,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          l10n.homeActiveHint,
                          style: const TextStyle(
                            fontSize: 12.5,
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
            ),
            const SizedBox(height: 12),
            InkWell(
              onTap: () => appState.openInputMethodPicker(),
              borderRadius: BorderRadius.circular(12),
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(vertical: 10),
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(
                      Icons.swap_horiz_rounded,
                      size: 18,
                      color: AppColors.primary,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      l10n.onboardingSelectKeyboard,
                      style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.primary,
                        fontFamilyFallback: AppTheme.urduFontFallbacks,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ] else ...[
            _buildStatusItem(
              isPositive: isEnabled,
              label: isEnabled ? l10n.homeStatusEnabled : l10n.homeStatusDisabled,
            ),
            const SizedBox(height: 10),
            _buildStatusItem(
              isPositive: isActive,
              label: isActive ? l10n.homeStatusActive : l10n.homeStatusNotActive,
            ),
            const SizedBox(height: 18),

            // Action Button
            if (!isEnabled)
              AppButton.primary(
                label: l10n.homeActionEnable,
                height: 48,
                borderRadius: 14,
                icon: Icons.tune_rounded,
                onPressed: () => appState.openKeyboardSettings(),
              )
            else
              AppButton.primary(
                label: l10n.homeActionSelect,
                height: 48,
                borderRadius: 14,
                icon: Icons.keyboard_alt_rounded,
                onPressed: () => appState.openInputMethodPicker(),
              ),
          ],
        ],
      ),
    );
  }

  Widget _buildStatusItem({required bool isPositive, required String label}) {
    return Row(
      children: [
        Container(
          width: 22,
          height: 22,
          decoration: BoxDecoration(
            color: isPositive ? AppColors.primary : const Color(0xFF888888),
            shape: BoxShape.circle,
          ),
          child: Icon(
            isPositive ? Icons.check_rounded : Icons.info_outline_rounded,
            color: AppColors.white,
            size: 14,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Text(
            label,
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w500,
              color: isPositive ? AppColors.textPrimary : AppColors.textSecondary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
        ),
      ],
    );
  }
}
