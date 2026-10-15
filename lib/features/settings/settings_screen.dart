import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/constants/app_constants.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../state/app_state.dart';

/// Application settings and preferences screen.
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  Future<void> _launchUrl(String urlString, {String? marketUrl}) async {
    try {
      if (marketUrl != null) {
        final marketUri = Uri.parse(marketUrl);
        if (await canLaunchUrl(marketUri)) {
          await launchUrl(marketUri, mode: LaunchMode.externalApplication);
          return;
        }
      }
      final uri = Uri.parse(urlString);
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (_) {
      try {
        final uri = Uri.parse(urlString);
        await launchUrl(uri, mode: LaunchMode.platformDefault);
      } catch (_) {}
    }
  }

  Future<void> _shareApp(String message) async {
    try {
      await SharePlus.instance.share(ShareParams(text: message));
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();
    final l10n = context.l10n;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(l10n.settingsTitle),
      ),
      body: SafeArea(
        child: ListView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          children: [
            // Section 1: General Preferences (Language Dropdown)
            _buildSectionHeader(l10n.settingsGeneralSection),
            _buildCard([
              _buildTile(
                icon: Icons.translate_rounded,
                title: l10n.settingsLanguage,
                subtitle: appState.selectedLanguage == 'ur'
                    ? l10n.urduNative
                    : l10n.english,
                trailing: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.white,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.primary, width: 1.2),
                  ),
                  child: DropdownButtonHideUnderline(
                    child: DropdownButton<String>(
                      value: appState.selectedLanguage,
                      icon: const Icon(
                        Icons.keyboard_arrow_down_rounded,
                        color: AppColors.primary,
                        size: 20,
                      ),
                      isDense: true,
                      elevation: 2,
                      borderRadius: BorderRadius.circular(14),
                      dropdownColor: AppColors.white,
                      items: const [
                        DropdownMenuItem(
                          value: 'ur',
                          child: Text(
                            'اردو (Urdu)',
                            style: TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                              fontFamilyFallback: AppTheme.urduFontFallbacks,
                            ),
                          ),
                        ),
                        DropdownMenuItem(
                          value: 'en',
                          child: Text(
                            'English',
                            style: TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.bold,
                              color: AppColors.primary,
                            ),
                          ),
                        ),
                      ],
                      onChanged: (String? newLang) {
                        if (newLang != null && newLang != appState.selectedLanguage) {
                          appState.setSelectedLanguage(newLang);
                        }
                      },
                    ),
                  ),
                ),
              ),
            ]),
            const SizedBox(height: 24),

            // Section 2: Guides & Typing Tools
            _buildSectionHeader(l10n.settingsHelpSection),
            _buildCard([
              _buildTile(
                icon: Icons.tune_rounded,
                title: l10n.settingsSetupGuide,
                subtitle: l10n.settingsSetupGuideSubtitle,
                onTap: () => appState.openKeyboardSettings(),
              ),
              const Divider(height: 1, indent: 70),
              _buildTile(
                icon: Icons.keyboard_alt_rounded,
                title: l10n.homeCardPreviewTitle,
                subtitle: l10n.homeCardPreviewSubtitle,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.keyboardPreviewRoute,
                  );
                },
              ),
              const Divider(height: 1, indent: 70),
              _buildTile(
                icon: Icons.mic_rounded,
                title: l10n.settingsVoiceGuide,
                subtitle: l10n.settingsVoiceGuideSubtitle,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.voiceTypingGuideRoute,
                  );
                },
              ),
            ]),
            const SizedBox(height: 24),

            // Section 3: About & Support
            _buildSectionHeader(l10n.settingsAboutSection),
            _buildCard([
              _buildTile(
                icon: Icons.star_rate_rounded,
                title: l10n.settingsRateApp,
                subtitle: l10n.settingsRateAppSubtitle,
                onTap: () => _launchUrl(
                  AppConstants.playStoreAppUrl,
                  marketUrl: AppConstants.playStoreMarketUrl,
                ),
              ),
              const Divider(height: 1, indent: 70),
              _buildTile(
                icon: Icons.share_rounded,
                title: l10n.settingsShareApp,
                subtitle: l10n.settingsShareAppSubtitle,
                onTap: () => _shareApp(l10n.settingsShareMessage),
              ),
              const Divider(height: 1, indent: 70),
              _buildTile(
                icon: Icons.apps_rounded,
                title: l10n.settingsMoreApps,
                subtitle: l10n.settingsMoreAppsSubtitle,
                onTap: () => _launchUrl(AppConstants.developerAppsUrl),
              ),
              const Divider(height: 1, indent: 70),
              _buildTile(
                icon: Icons.verified_user_outlined,
                title: l10n.settingsPrivacyPolicy,
                subtitle: l10n.settingsPrivacyPolicySubtitle,
                onTap: () => _launchUrl(AppConstants.privacyPolicyUrl),
              ),
            ]),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, right: 4, bottom: 8),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.bold,
          color: AppColors.primary,
          letterSpacing: 0.5,
          fontFamilyFallback: AppTheme.urduFontFallbacks,
        ),
      ),
    );
  }

  Widget _buildCard(List<Widget> children) {
    return Material(
      color: AppColors.white,
      clipBehavior: Clip.antiAlias,
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: const BorderSide(color: AppColors.border, width: 1.2),
      ),
      child: Column(
        children: children,
      ),
    );
  }

  Widget _buildTile({
    required IconData icon,
    required String title,
    required String subtitle,
    VoidCallback? onTap,
    Widget? trailing,
  }) {
    return InkWell(
      onTap: onTap,
      splashColor: AppColors.primary.withValues(alpha: 0.08),
      highlightColor: AppColors.primary.withValues(alpha: 0.04),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(12),
                boxShadow: const [
                  BoxShadow(
                    color: Color(0x2201411C),
                    blurRadius: 6,
                    offset: Offset(0, 2),
                  ),
                ],
              ),
              child: Icon(icon, color: AppColors.white, size: 20),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 12.5,
                      color: AppColors.textMuted,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ],
              ),
            ),
            if (trailing != null)
              trailing
            else if (onTap != null)
              const Icon(
                Icons.arrow_forward_ios_rounded,
                size: 14,
                color: AppColors.textMuted,
              ),
          ],
        ),
      ),
    );
  }
}
