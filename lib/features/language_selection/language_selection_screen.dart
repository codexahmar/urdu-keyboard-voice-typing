import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/widgets/language_tile.dart';
import '../../state/app_state.dart';
import '../../services/platform_channel_service.dart';

/// Language selection screen
/// Redesigned with modern Material 3 layout, feedback toasts, and persistent storage synchronization
class LanguageSelectionScreen extends StatefulWidget {
  const LanguageSelectionScreen({Key? key}) : super(key: key);

  @override
  State<LanguageSelectionScreen> createState() =>
      _LanguageSelectionScreenState();
}

class _LanguageSelectionScreenState extends State<LanguageSelectionScreen> {
  final PlatformChannelService _platformService = PlatformChannelService();
  String? _selectedLanguage;

  @override
  void initState() {
    super.initState();
    // Initialize app state language value
    _selectedLanguage = context.read<AppState>().selectedLanguage;
  }

  void _selectLanguage(String code) {
    setState(() {
      _selectedLanguage = code;
    });
  }

  Future<void> _saveLanguage() async {
    if (_selectedLanguage == null) return;

    final appState = context.read<AppState>();
    await appState.setSelectedLanguage(_selectedLanguage!);

    // Attempt to communicate with native keyboard channel
    await _platformService.setKeyboardLanguage(_selectedLanguage!);

    if (mounted) {
      final isDark = Theme.of(context).brightness == Brightness.dark;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              const Icon(Icons.check_circle, color: Colors.white, size: 20),
              const SizedBox(width: 12),
              Text(
                'Language set to ${appState.getLanguageName(_selectedLanguage!)}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
            ],
          ),
          backgroundColor: isDark ? AppColors.primaryLight : AppColors.primary,
          duration: const Duration(seconds: 2),
        ),
      );
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: isDark ? AppColors.backgroundDark : AppColors.background,
      appBar: AppBar(
        title: const Text(AppStrings.languageSelectionTitle),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Top Header Card
            Padding(
              padding: const EdgeInsets.all(20),
              child: Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.08),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(
                    color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.2),
                    width: 1,
                  ),
                ),
                child: Row(
                  children: [
                    Icon(
                      Icons.translate_rounded,
                      color: isDark ? AppColors.primaryLight : AppColors.primary,
                      size: 28,
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Text(
                        AppStrings.languageSelectionDescription,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              fontWeight: FontWeight.w500,
                              color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                            ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // Language Options List
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                physics: const BouncingScrollPhysics(),
                itemCount: AppConstants.supportedLanguages.length,
                itemBuilder: (context, index) {
                  final lang = AppConstants.supportedLanguages[index];
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 16),
                    child: LanguageTile(
                      name: lang['name'],
                      nativeName: lang['nativeName'],
                      description: lang['description'],
                      isSelected: _selectedLanguage == lang['code'],
                      isRecommended: lang['isRecommended'],
                      onTap: () => _selectLanguage(lang['code']),
                    ),
                  );
                },
              ),
            ),

            // Save Action Button Area
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: isDark ? AppColors.surfaceDark : Colors.white,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(isDark ? 0.2 : 0.04),
                    blurRadius: 10,
                    offset: const Offset(0, -4),
                  ),
                ],
              ),
              child: SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: _selectedLanguage != null ? _saveLanguage : null,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: isDark ? AppColors.primaryLight : AppColors.primary,
                    foregroundColor: isDark ? Colors.black : Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                  ),
                  child: const Text(
                    AppStrings.save,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                      letterSpacing: 0.3,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
