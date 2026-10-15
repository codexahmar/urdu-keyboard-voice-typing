import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/language_tile.dart';
import '../../state/app_state.dart';

/// Clean, professional LanguageSelectionScreen in Pakistan Green & White theme
class LanguageSelectionScreen extends StatefulWidget {
  const LanguageSelectionScreen({super.key});

  @override
  State<LanguageSelectionScreen> createState() => _LanguageSelectionScreenState();
}

class _LanguageSelectionScreenState extends State<LanguageSelectionScreen> {
  late String _selectedCode;

  @override
  void initState() {
    super.initState();
    _selectedCode = context.read<AppState>().selectedLanguage;
  }

  Future<void> _applyLanguage() async {
    final appState = context.read<AppState>();
    await appState.setSelectedLanguage(_selectedCode);
    if (mounted) {
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(l10n.languageSelectionTitle),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header description
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

              // Urdu Tile
              LanguageTile(
                name: 'Urdu',
                nativeName: l10n.languageUrduTitle,
                description: l10n.languageUrduSubtitle,
                isSelected: _selectedCode == 'ur',
                isRecommended: true,
                recommendedLabel: l10n.recommended,
                onTap: () {
                  setState(() {
                    _selectedCode = 'ur';
                  });
                },
              ),
              const SizedBox(height: 14),

              // English Tile
              LanguageTile(
                name: 'English',
                nativeName: l10n.languageEnglishTitle,
                description: l10n.languageEnglishSubtitle,
                isSelected: _selectedCode == 'en',
                isRecommended: false,
                onTap: () {
                  setState(() {
                    _selectedCode = 'en';
                  });
                },
              ),

              const Spacer(),

              // Save / Apply Button
              AppButton.primary(
                label: l10n.save,
                icon: Icons.check_circle_outline_rounded,
                onPressed: _applyLanguage,
              ),
              const SizedBox(height: 12),
            ],
          ),
        ),
      ),
    );
  }
}
