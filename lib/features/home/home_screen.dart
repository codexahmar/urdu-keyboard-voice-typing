import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/widgets/action_card.dart';
import '../../state/app_state.dart';
import '../../services/platform_channel_service.dart';

/// Home screen - main dashboard
class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final PlatformChannelService _platformService = PlatformChannelService();
  bool _hasCheckedKeyboard = false;

  @override
  void initState() {
    super.initState();
    // Automatically check and open keyboard settings on first launch
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAndOpenKeyboardSettings();
    });
  }

  Future<void> _checkAndOpenKeyboardSettings() async {
    if (_hasCheckedKeyboard) return;
    _hasCheckedKeyboard = true;

    try {
      final isEnabled = await _platformService.isKeyboardEnabled();

      if (!isEnabled && mounted) {
        // Show dialog and open settings
        showDialog(
          context: context,
          barrierDismissible: false,
          builder: (context) => AlertDialog(
            title: const Text('Enable Urdu Keyboard'),
            content: const Text(
              'To use the Urdu Smart Keyboard, you need to enable it in your device settings.\n\n'
              'Tap OK to open settings, then:\n'
              '1. Find "Urdu Smart Keyboard"\n'
              '2. Toggle it ON\n'
              '3. Return to this app',
            ),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                child: const Text('Later'),
              ),
              ElevatedButton(
                onPressed: () async {
                  Navigator.pop(context);
                  await _platformService.openKeyboardSettings();
                },
                child: const Text('Open Settings'),
              ),
            ],
          ),
        );
      }
    } catch (e) {
      print('Error checking keyboard status: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = context.watch<AppState>();

    return Scaffold(
      appBar: AppBar(
        title: const Text(AppStrings.splashTitle),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () {
              Navigator.pushNamed(context, AppConstants.settingsRoute);
            },
            tooltip: AppStrings.settings,
          ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Welcome message
              Text(
                AppStrings.homeWelcome,
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 8),
              Text(
                AppStrings.homeSubtitle,
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: Theme.of(context).textTheme.bodySmall?.color,
                ),
              ),
              const SizedBox(height: 24),

              // Current language card
              Card(
                color: Theme.of(
                  context,
                ).colorScheme.primaryContainer.withOpacity(0.1),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      Icon(
                        Icons.language,
                        size: 32,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Current Language',
                              style: Theme.of(context).textTheme.bodyMedium,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              appState.getLanguageNativeName(
                                appState.selectedLanguage,
                              ),
                              style: Theme.of(context).textTheme.headlineSmall,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Action cards
              ActionCard(
                icon: Icons.translate,
                title: AppStrings.selectLanguage,
                subtitle: AppStrings.selectLanguageUrdu,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.languageSelectionRoute,
                  );
                },
              ),
              const SizedBox(height: 16),

              ActionCard(
                icon: Icons.keyboard_alt_outlined,
                title: AppStrings.keyboardPreview,
                subtitle: AppStrings.keyboardPreviewUrdu,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.keyboardPreviewRoute,
                  );
                },
                iconColor: Theme.of(context).colorScheme.secondary,
              ),
              const SizedBox(height: 16),

              ActionCard(
                icon: Icons.mic_outlined,
                title: AppStrings.voiceTyping,
                subtitle: AppStrings.voiceTypingUrdu,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.voiceTypingGuideRoute,
                  );
                },
                iconColor: Colors.purple,
              ),
              const SizedBox(height: 32),

              // Help section
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.blue.shade200),
                ),
                child: Row(
                  children: [
                    Icon(
                      Icons.help_outline,
                      color: Colors.blue.shade700,
                      size: 32,
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Need Help?',
                            style: Theme.of(context).textTheme.titleMedium
                                ?.copyWith(color: Colors.blue.shade900),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Visit settings to enable the keyboard',
                            style: Theme.of(context).textTheme.bodyMedium
                                ?.copyWith(color: Colors.blue.shade700),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
