import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:permission_handler/permission_handler.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/widgets/action_card.dart';
import '../../state/app_state.dart';
import '../../services/platform_channel_service.dart';

/// Home screen - main dashboard of Urdu Smart Keyboard
/// Upgraded to modern Material 3 architecture with enhanced animations and layouts
class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final PlatformChannelService _platformService = PlatformChannelService();
  bool _hasCheckedOnThisLaunch = false;

  @override
  void initState() {
    super.initState();
    // Initialize app state and check settings automatically on first launch
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkPermissionsAndKeyboard();
    });
  }

  Future<void> _checkPermissionsAndKeyboard() async {
    if (_hasCheckedOnThisLaunch) return;
    _hasCheckedOnThisLaunch = true;

    // First, request microphone permission
    await _requestMicrophonePermission();

    // Small delay to ensure permission dialog is dismissed before showing keyboard dialog
    await Future.delayed(const Duration(milliseconds: 500));

    // Then check keyboard status
    if (mounted) {
      await _checkAndOpenKeyboardSettings();
    }
  }

  Future<void> _requestMicrophonePermission() async {
    final status = await Permission.microphone.status;

    if (status.isDenied || status.isPermanentlyDenied) {
      if (mounted) {
        final isDark = Theme.of(context).brightness == Brightness.dark;

        final shouldRequest = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            backgroundColor: isDark ? AppColors.surfaceDark : Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(24),
            ),
            title: Row(
              children: [
                Icon(
                  Icons.mic_none_rounded,
                  color: isDark ? AppColors.primaryLight : AppColors.primary,
                  size: 28,
                ),
                const SizedBox(width: 12),
                const Text('Microphone Permission'),
              ],
            ),
            content: const Text(
              'This keyboard uses voice typing feature. '
              'Please grant microphone permission to use voice input.\n\n'
              'You can enable it now or later from settings.',
              style: TextStyle(height: 1.4, fontSize: 15),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: Text(
                  'Later',
                  style: TextStyle(
                    color: isDark
                        ? AppColors.textSecondaryDark
                        : AppColors.textSecondary,
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(context, true),
                style: ElevatedButton.styleFrom(
                  backgroundColor: isDark
                      ? AppColors.primaryLight
                      : AppColors.primary,
                  foregroundColor: isDark ? Colors.black : Colors.white,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 12,
                  ),
                  minimumSize: const Size(100, 44),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: const Text('Grant'),
              ),
            ],
          ),
        );

        if (shouldRequest == true) {
          final result = await Permission.microphone.request();
          if (result.isPermanentlyDenied && mounted) {
            showDialog(
              context: context,
              builder: (context) => AlertDialog(
                backgroundColor: isDark ? AppColors.surfaceDark : Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(24),
                ),
                title: const Text('Permission Required'),
                content: const Text(
                  'Microphone permission is permanently denied. '
                  'Please enable it from app settings to use voice typing.',
                  style: TextStyle(height: 1.4, fontSize: 15),
                ),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: Text(
                      'Cancel',
                      style: TextStyle(
                        color: isDark
                            ? AppColors.textSecondaryDark
                            : AppColors.textSecondary,
                      ),
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      openAppSettings();
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: isDark
                          ? AppColors.primaryLight
                          : AppColors.primary,
                      foregroundColor: isDark ? Colors.black : Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: const Text('Open Settings'),
                  ),
                ],
              ),
            );
          }
        }
      }
    }
  }

  Future<void> _checkAndOpenKeyboardSettings() async {
    try {
      final isEnabled = await _platformService.isKeyboardEnabled();

      if (!isEnabled && mounted) {
        final isDark = Theme.of(context).brightness == Brightness.dark;

        showDialog(
          context: context,
          barrierDismissible: false,
          builder: (context) => AlertDialog(
            backgroundColor: isDark ? AppColors.surfaceDark : Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(24),
            ),
            title: Row(
              children: [
                Icon(
                  Icons.keyboard_alt_outlined,
                  color: isDark ? AppColors.primaryLight : AppColors.primary,
                  size: 28,
                ),
                const SizedBox(width: 12),
                const Text('Enable Keyboard'),
              ],
            ),
            content: const Text(
              'To use the Urdu Smart Keyboard, you need to enable it in your system settings.\n\n'
              'Tap OK to open settings, then:\n'
              '1. Find "Urdu Smart Keyboard"\n'
              '2. Toggle it ON\n'
              '3. Return to this app',
              style: TextStyle(height: 1.4, fontSize: 15),
            ),
            actions: [
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                child: Text(
                  'Later',
                  style: TextStyle(
                    color: isDark
                        ? AppColors.textSecondaryDark
                        : AppColors.textSecondary,
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () async {
                  Navigator.pop(context);
                  await _platformService.openKeyboardSettings();
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: isDark
                      ? AppColors.primaryLight
                      : AppColors.primary,
                  foregroundColor: isDark ? Colors.black : Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
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

  /// Helper to get greeting message based on local time
  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 5) {
      return 'Good night';
    } else if (hour < 12) {
      return 'Good morning';
    } else if (hour < 17) {
      return 'Good afternoon';
    } else {
      return 'Good evening';
    }
  }

  @override
  Widget build(BuildContext context) {
    // Initialize app state
    final appState = context.watch<AppState>();
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final isUrdu = appState.selectedLanguage == 'ur';

    return Scaffold(
      backgroundColor: isDark ? AppColors.backgroundDark : AppColors.background,
      appBar: AppBar(
        title: Text(
          AppStrings.splashTitle,
          style: Theme.of(context).appBarTheme.titleTextStyle?.copyWith(
            fontWeight: FontWeight.w800,
            letterSpacing: -0.5,
          ),
        ),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 12),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: isDark ? AppColors.surfaceVariantDark : Colors.white,
              border: Border.all(
                color: isDark ? AppColors.dividerDark : AppColors.divider,
                width: 1,
              ),
            ),
            child: IconButton(
              icon: const Icon(Icons.settings_outlined, size: 22),
              onPressed: () {
                Navigator.pushNamed(context, AppConstants.settingsRoute);
              },
              tooltip: AppStrings.settings,
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
              // Welcome Header Section
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Text(
                            _getGreeting(),
                            style: Theme.of(context).textTheme.bodyLarge
                                ?.copyWith(
                                  color: isDark
                                      ? AppColors.textSecondaryDark
                                      : AppColors.textSecondary,
                                  fontWeight: FontWeight.w500,
                                ),
                          ),
                          const SizedBox(width: 6),
                          const Icon(
                            Icons.wb_sunny_outlined,
                            size: 16,
                            color: Colors.amber,
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'As-salamu alaykum',
                        style: Theme.of(context).textTheme.headlineMedium
                            ?.copyWith(
                              fontWeight: FontWeight.w900,
                              color: isDark
                                  ? AppColors.textPrimaryDark
                                  : AppColors.textPrimary,
                              letterSpacing: -0.5,
                            ),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Modernised Current Language Card (Premium Emerald or Royal Purple Gradient)
              Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: isUrdu
                        ? AppColors.primaryGradient
                        : AppColors.urduGradient,
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: (isUrdu ? AppColors.primary : AppColors.urduAccent)
                          .withOpacity(isDark ? 0.3 : 0.15),
                      blurRadius: 20,
                      offset: const Offset(0, 8),
                    ),
                  ],
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(24),
                  child: Stack(
                    children: [
                      // Abstract decor circles in gradient card
                      Positioned(
                        right: -20,
                        bottom: -20,
                        child: Container(
                          width: 120,
                          height: 120,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.white.withOpacity(0.06),
                          ),
                        ),
                      ),
                      Positioned(
                        left: -40,
                        top: -40,
                        child: Container(
                          width: 150,
                          height: 150,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.white.withOpacity(0.04),
                          ),
                        ),
                      ),

                      Padding(
                        padding: const EdgeInsets.all(22),
                        child: Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.16),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: const Icon(
                                Icons.language_rounded,
                                size: 30,
                                color: Colors.white,
                              ),
                            ),
                            const SizedBox(width: 18),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    children: [
                                      Text(
                                        'ACTIVE LANGUAGE',
                                        style: TextStyle(
                                          fontSize: 11,
                                          fontWeight: FontWeight.w900,
                                          color: Colors.white.withOpacity(0.72),
                                          letterSpacing: 1.2,
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                          horizontal: 8,
                                          vertical: 2,
                                        ),
                                        decoration: BoxDecoration(
                                          color: Colors.white.withOpacity(0.24),
                                          borderRadius: BorderRadius.circular(
                                            10,
                                          ),
                                        ),
                                        child: const Text(
                                          'ACTIVE',
                                          style: TextStyle(
                                            fontSize: 9,
                                            fontWeight: FontWeight.bold,
                                            color: Colors.white,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                  const SizedBox(height: 6),
                                  Text(
                                    appState.getLanguageNativeName(
                                      appState.selectedLanguage,
                                    ),
                                    style: const TextStyle(
                                      fontSize: 24,
                                      fontWeight: FontWeight.bold,
                                      color: Colors.white,
                                      letterSpacing: 0.2,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    '${appState.getLanguageName(appState.selectedLanguage)} Mode Enabled',
                                    style: TextStyle(
                                      fontSize: 13,
                                      color: Colors.white.withOpacity(0.85),
                                    ),
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
              const SizedBox(height: 28),

              // Action Options Grid Header
              Text(
                'Tweak & Learn',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: isDark
                      ? AppColors.textPrimaryDark
                      : AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 14),

              // Redesigned Action Cards
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
                iconColor: isDark ? AppColors.primaryLight : AppColors.primary,
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
                iconColor: isDark
                    ? AppColors.secondaryLight
                    : AppColors.secondary,
              ),
              const SizedBox(height: 16),

              ActionCard(
                icon: Icons.mic_rounded,
                title: AppStrings.voiceTyping,
                subtitle: AppStrings.voiceTypingUrdu,
                onTap: () {
                  Navigator.pushNamed(
                    context,
                    AppConstants.voiceTypingGuideRoute,
                  );
                },
                iconColor: isDark
                    ? AppColors.urduAccentLight
                    : AppColors.urduAccent,
              ),
              const SizedBox(height: 28),

              // Help & Onboarding Replay Section
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: isDark
                      ? AppColors.surfaceVariantDark
                      : const Color(0xFFEFF6FF), // soft light blue
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: isDark
                        ? AppColors.dividerDark
                        : const Color(0xFFBFDBFE),
                    width: 1.5,
                  ),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.12),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.live_help_outlined,
                        color: Colors.blue,
                        size: 24,
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Need Help?',
                            style: TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 16,
                              color: isDark
                                  ? Colors.blue.shade300
                                  : Colors.blue.shade900,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Make sure "Urdu Smart Keyboard" is selected under your system Virtual Keyboard settings.',
                            style: TextStyle(
                              fontSize: 13,
                              height: 1.4,
                              color: isDark
                                  ? AppColors.textSecondaryDark
                                  : Colors.blue.shade800,
                            ),
                          ),
                          const SizedBox(height: 12),
                          Row(
                            children: [
                              InkWell(
                                onTap: () {
                                  // Re-launch Keyboard settings directly from platform helper
                                  _platformService.openKeyboardSettings();
                                },
                                child: Text(
                                  'Manage Keyboards',
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: FontWeight.bold,
                                    color: isDark
                                        ? Colors.blue.shade300
                                        : Colors.blue.shade900,
                                    decoration: TextDecoration.underline,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 16),
                              InkWell(
                                onTap: () {
                                  // Safely open the system input method picker directly
                                  _platformService.openInputMethodPicker();
                                },
                                child: Text(
                                  'Switch Keyboard',
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: FontWeight.bold,
                                    color: isDark
                                        ? Colors.blue.shade300
                                        : Colors.blue.shade900,
                                    decoration: TextDecoration.underline,
                                  ),
                                ),
                              ),
                            ],
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
