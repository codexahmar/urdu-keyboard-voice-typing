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
                  LanguageTile(
                    name: 'Urdu',
                    nativeName: l10n.languageUrduTitle,
                    description: l10n.languageUrduSubtitle,
                    isSelected: _controller.selectedCode == 'ur',
                    isRecommended: true,
                    recommendedLabel: l10n.recommended,
                    onTap: () => _controller.selectLanguage('ur'),
                  ),
                  const SizedBox(height: 14),
                  LanguageTile(
                    name: 'English',
                    nativeName: l10n.languageEnglishTitle,
                    description: l10n.languageEnglishSubtitle,
                    isSelected: _controller.selectedCode == 'en',
                    isRecommended: false,
                    onTap: () => _controller.selectLanguage('en'),
                  ),
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
