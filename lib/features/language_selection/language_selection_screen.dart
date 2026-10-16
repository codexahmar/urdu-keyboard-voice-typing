import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/language_tile.dart';
import '../../state/app_state.dart';
import 'controller/language_selection_controller.dart';

/// Screen for selecting the application language.
class LanguageSelectionScreen extends StatefulWidget {
  const LanguageSelectionScreen({super.key});

  @override
  State<LanguageSelectionScreen> createState() => _LanguageSelectionScreenState();
}

class _LanguageSelectionScreenState extends State<LanguageSelectionScreen> {
  late final LanguageSelectionController _controller;

  @override
  void initState() {
    super.initState();
    _controller = LanguageSelectionController(
      initialCode: context.read<AppState>().selectedLanguage,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final appState = context.read<AppState>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(l10n.languageSelectionTitle),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: ListenableBuilder(
            listenable: _controller,
            builder: (context, _) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
                    child: Text(
                      l10n.languageSelectionSubtitle,
                      style: const TextStyle(
                        fontSize: 14,
                        color: AppColors.textSecondary,
                        height: 1.4,
                        fontFamilyFallback: AppTheme.urduFontFallbacks,
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  ...AppLocalizations.supportedLanguages.map((lang) {
                    final isUrdu = lang.code == 'ur';
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 14),
                      child: LanguageTile(
                        name: lang.name,
                        nativeName: isUrdu
                            ? l10n.languageUrduTitle
                            : l10n.languageEnglishTitle,
                        description: isUrdu
                            ? l10n.languageUrduSubtitle
                            : l10n.languageEnglishSubtitle,
                        isSelected: _controller.selectedCode == lang.code,
                        isRecommended: lang.isRecommended,
                        recommendedLabel: l10n.recommended,
                        onTap: () => _controller.selectLanguage(lang.code),
                      ),
                    );
                  }),
                  const Spacer(),
                  AppButton.primary(
                    label: l10n.save,
                    icon: Icons.check_circle_outline_rounded,
                    onPressed: () => _controller.applyLanguage(context, appState),
                  ),
                  const SizedBox(height: 12),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}
