import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/language_tile.dart';
import '../../state/app_state.dart';
import '../../services/platform_channel_service.dart';

/// Language selection screen
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

    // Attempt to communicate with native keyboard (will fail gracefully if not implemented)
    await _platformService.setKeyboardLanguage(_selectedLanguage!);

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Language changed to ${appState.getLanguageName(_selectedLanguage!)}',
          ),
          backgroundColor: Theme.of(context).colorScheme.primary,
        ),
      );
      Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text(AppStrings.languageSelectionTitle)),
      body: SafeArea(
        child: Column(
          children: [
            // Description
            Padding(
              padding: const EdgeInsets.all(20),
              child: Text(
                AppStrings.languageSelectionDescription,
                style: Theme.of(context).textTheme.bodyLarge,
                textAlign: TextAlign.center,
              ),
            ),

            // Language list
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.symmetric(horizontal: 20),
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

            // Save button
            Padding(
              padding: const EdgeInsets.all(20),
              child: SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _selectedLanguage != null ? _saveLanguage : null,
                  child: const Text(AppStrings.save),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
