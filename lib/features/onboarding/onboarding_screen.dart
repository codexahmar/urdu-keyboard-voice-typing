import 'package:flutter/material.dart';
import '../../core/constants/app_strings.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/step_indicator.dart';

/// Onboarding screen with multi-step flow
/// Overhauled to follow top-tier Material 3 design and immersive UX guidelines
class OnboardingScreen extends StatefulWidget {
  final VoidCallback onComplete;

  const OnboardingScreen({Key? key, required this.onComplete})
      : super(key: key);

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  void _nextPage() {
    if (_currentPage < AppConstants.onboardingSteps - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 400),
        curve: Curves.fastOutSlowIn,
      );
    } else {
      widget.onComplete();
    }
  }

  void _skipOnboarding() {
    widget.onComplete();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: isDark ? AppColors.backgroundDark : AppColors.background,
      body: SafeArea(
        child: Column(
          children: [
            // Top Navigation Bar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Logo tag
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(6),
                        decoration: BoxDecoration(
                          color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Icon(
                          Icons.keyboard_alt_outlined,
                          size: 20,
                          color: isDark ? AppColors.primaryLight : AppColors.primary,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'Urdu Smart',
                        style: Theme.of(context).textTheme.titleSmall?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                            ),
                      ),
                    ],
                  ),

                  // Skip Button
                  AnimatedOpacity(
                    opacity: _currentPage < AppConstants.onboardingSteps - 1 ? 1.0 : 0.0,
                    duration: const Duration(milliseconds: 200),
                    child: IgnorePointer(
                      ignoring: _currentPage >= AppConstants.onboardingSteps - 1,
                      child: TextButton(
                        onPressed: _skipOnboarding,
                        style: TextButton.styleFrom(
                          foregroundColor: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        ),
                        child: Text(
                          AppStrings.skip,
                          style: TextStyle(
                            fontWeight: FontWeight.w600,
                            color: isDark ? AppColors.primaryLight : AppColors.primary,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),

            // Page View
            Expanded(
              child: PageView(
                controller: _pageController,
                onPageChanged: (index) {
                  setState(() {
                    _currentPage = index;
                  });
                },
                children: const [
                  OnboardingPage1(),
                  OnboardingPage2(),
                  OnboardingPage3(),
                  OnboardingPage4(),
                ],
              ),
            ),

            // Step indicator & Actions Panel
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 20.0),
              decoration: BoxDecoration(
                color: isDark ? AppColors.surfaceDark : AppColors.surface,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(isDark ? 0.2 : 0.03),
                    blurRadius: 10,
                    offset: const Offset(0, -4),
                  ),
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Dot Indicator
                  StepIndicator(
                    currentStep: _currentPage,
                    totalSteps: AppConstants.onboardingSteps,
                  ),
                  const SizedBox(height: 24),

                  // Next Button
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: ElevatedButton(
                      onPressed: _nextPage,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: isDark ? AppColors.primaryLight : AppColors.primary,
                        foregroundColor: isDark ? Colors.black : Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        elevation: 0,
                      ),
                      child: Text(
                        _currentPage == AppConstants.onboardingSteps - 1
                            ? AppStrings.getStarted
                            : AppStrings.next,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 0.3,
                        ),
                      ),
                    ),
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

/// Step 1: Welcome Screen
class OnboardingPage1 extends StatelessWidget {
  const OnboardingPage1({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Illustration Box
          Container(
            padding: const EdgeInsets.all(40),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: isDark
                    ? [const Color(0xFF144B27), AppColors.surfaceVariantDark]
                    : [const Color(0xFFE2F0D9), AppColors.surfaceVariant],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.waving_hand_outlined,
              size: 80,
              color: isDark ? AppColors.primaryLight : AppColors.primary,
            ),
          ),
          const SizedBox(height: 40),

          // Title
          Text(
            AppStrings.onboardingWelcomeTitle,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.5,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),

          // Urdu Welcome
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 6),
            decoration: BoxDecoration(
              color: AppColors.urduAccent.withOpacity(0.08),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text(
              AppStrings.onboardingWelcomeUrdu,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    color: AppColors.urduAccent,
                    fontWeight: FontWeight.bold,
                    fontFamilyFallback: AppTheme.urduFontFallbacks,
                  ),
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(height: 20),

          // Description
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Text(
              AppStrings.onboardingWelcomeDescription,
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                  ),
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
    );
  }
}

/// Step 2: Enable Keyboard
class OnboardingPage2 extends StatelessWidget {
  const OnboardingPage2({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Simulated Toggle Setting Card
          Card(
            elevation: 0,
            color: isDark ? AppColors.surfaceVariantDark : Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20),
              side: BorderSide(
                color: isDark ? AppColors.dividerDark : AppColors.divider,
                width: 1.5,
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.settings_outlined,
                        color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        size: 20,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'Virtual Keyboard Settings',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                  const Divider(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.12),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.keyboard_alt,
                              color: isDark ? AppColors.primaryLight : AppColors.primary,
                              size: 20,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Urdu Smart Keyboard',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                                ),
                              ),
                              const Text(
                                'Enabled',
                                style: TextStyle(
                                  fontSize: 11,
                                  color: Colors.green,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                      Switch(
                        value: true,
                        activeColor: isDark ? AppColors.primaryLight : AppColors.primary,
                        onChanged: (_) {},
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // Title
          Text(
            AppStrings.onboardingEnableTitle,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w900,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),

          // Instruction Steps
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.05),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              children: [
                _buildInstructionRow(context, '1', 'Open Settings'),
                _buildInstructionRow(context, '2', 'Go to System & Input'),
                _buildInstructionRow(context, '3', 'Select On-Screen Keyboard'),
                _buildInstructionRow(context, '4', 'Toggle ON "Urdu Smart Keyboard"'),
              ],
            ),
          ),
          const SizedBox(height: 16),

          Text(
            AppStrings.onboardingEnableDescription,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                  height: 1.4,
                ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildInstructionRow(BuildContext context, String step, String text) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6.0),
      child: Row(
        children: [
          Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: isDark ? AppColors.primaryLight : AppColors.primary,
              shape: BoxShape.circle,
            ),
            child: Center(
              child: Text(
                step,
                style: TextStyle(
                  color: isDark ? Colors.black : Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 12,
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              text,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Step 3: Select Default Keyboard
class OnboardingPage3 extends StatelessWidget {
  const OnboardingPage3({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Simulated Keyboard Picker Dialog
          Card(
            elevation: 0,
            color: isDark ? AppColors.surfaceVariantDark : Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20),
              side: BorderSide(
                color: isDark ? AppColors.dividerDark : AppColors.divider,
                width: 1.5,
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Choose Input Method',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                    ),
                  ),
                  const Divider(height: 20),
                  _buildSimulatedOption(context, 'English (US) Keyboard', false),
                  _buildSimulatedOption(context, 'Urdu Smart Keyboard', true),
                ],
              ),
            ),
          ),
          const SizedBox(height: 32),

          // Title
          Text(
            AppStrings.onboardingSelectTitle,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w900,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),

          // Description
          Text(
            AppStrings.onboardingSelectDescription,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),

          // Interactive Helper Card
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.secondary.withOpacity(0.08),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: AppColors.secondary.withOpacity(0.24),
                width: 1.5,
              ),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(
                  Icons.info_outline,
                  color: AppColors.secondary,
                  size: 24,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Tap any text field inside other apps to open options and select the keyboard.',
                    style: TextStyle(
                      fontSize: 13,
                      height: 1.4,
                      color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSimulatedOption(BuildContext context, String title, bool isSelected) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected
              ? (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.1)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          border: isSelected
              ? Border.all(
                  color: isDark ? AppColors.primaryLight : AppColors.primary,
                  width: 1.5,
                )
              : null,
        ),
        child: Row(
          children: [
            Icon(
              isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
              color: isSelected
                  ? (isDark ? AppColors.primaryLight : AppColors.primary)
                  : Colors.grey,
              size: 20,
            ),
            const SizedBox(width: 12),
            Text(
              title,
              style: TextStyle(
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Step 4: Finish Screen
class OnboardingPage4 extends StatelessWidget {
  const OnboardingPage4({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Double pulsing ring check icon
          Stack(
            alignment: Alignment.center,
            children: [
              Container(
                width: 130,
                height: 130,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.08),
                ),
              ),
              Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.12),
                ),
              ),
              Icon(
                Icons.check_circle,
                size: 72,
                color: isDark ? AppColors.primaryLight : AppColors.primary,
              ),
            ],
          ),
          const SizedBox(height: 40),

          // Title
          Text(
            AppStrings.onboardingCompleteTitle,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.5,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),

          // Description
          Text(
            AppStrings.onboardingCompleteDescription,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
                ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),

          // Celebration badge
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            decoration: BoxDecoration(
              color: AppColors.urduAccent.withOpacity(0.08),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(
                  Icons.celebration_outlined,
                  color: AppColors.urduAccent,
                  size: 24,
                ),
                const SizedBox(width: 8),
                Text(
                  'Enjoy ad-free fast Urdu typing!',
                  style: TextStyle(
                    fontWeight: FontWeight.w600,
                    color: AppColors.urduAccent,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
