import 'package:flutter/material.dart';
import '../../core/constants/app_strings.dart';
import '../../core/theme/app_colors.dart';

/// Splash screen - shown on app launch
/// Upgraded to a modern, high-fidelity Material 3 design with fluid animations
class SplashScreen extends StatefulWidget {
  const SplashScreen({Key? key}) : super(key: key);

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1600),
    );

    // Fade animation
    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(0.0, 0.7, curve: Curves.easeOut),
      ),
    );

    // Subtle scale animation for logo
    _scaleAnimation = Tween<double>(begin: 0.85, end: 1.0).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(0.0, 0.85, curve: Curves.easeOutBack),
      ),
    );

    _animationController.forward();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          // Modern Premium Gradient Background
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: isDark
                ? [
                    const Color(0xFF0C331A), // Ultra Dark Pine
                    AppColors.backgroundDark,
                  ]
                : [
                    const Color(0xFFE2F0D9), // Pastel Lime Mint
                    AppColors.background,
                  ],
          ),
        ),
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Abstract subtle grid/shape behind the logo
            Positioned(
              top: -100,
              right: -100,
              child: Container(
                width: 300,
                height: 300,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: (isDark ? AppColors.primaryLight : AppColors.primary)
                      .withOpacity(0.04),
                ),
              ),
            ),
            Positioned(
              bottom: -150,
              left: -150,
              child: Container(
                width: 350,
                height: 350,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: (isDark ? AppColors.urduAccentLight : AppColors.urduAccent)
                      .withOpacity(0.03),
                ),
              ),
            ),

            Center(
              child: FadeTransition(
                opacity: _fadeAnimation,
                child: ScaleTransition(
                  scale: _scaleAnimation,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      // Modernised Logo/Icon Card (M3 Premium Look)
                      Container(
                        width: 130,
                        height: 130,
                        decoration: BoxDecoration(
                          color: isDark ? AppColors.surfaceDark : Colors.white,
                          borderRadius: BorderRadius.circular(32), // M3 Extra Large shape
                          border: Border.all(
                            color: isDark ? AppColors.dividerDark : AppColors.divider,
                            width: 1.5,
                          ),
                          boxShadow: [
                            BoxShadow(
                              color: (isDark ? Colors.black : AppColors.primary)
                                  .withOpacity(isDark ? 0.4 : 0.1),
                              blurRadius: 30,
                              offset: const Offset(0, 12),
                            ),
                          ],
                        ),
                        child: Center(
                          child: Icon(
                            Icons.keyboard_alt_outlined,
                            size: 68,
                            color: isDark ? AppColors.primaryLight : AppColors.primary,
                          ),
                        ),
                      ),
                      const SizedBox(height: 36),

                      // App Name - English
                      Text(
                        AppStrings.splashTitle,
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                              color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                              fontWeight: FontWeight.bold,
                              letterSpacing: -0.5,
                            ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 10),

                      // App Name - Urdu
                      Text(
                        AppStrings.splashSubtitle,
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              color: isDark
                                  ? AppColors.primaryLight
                                  : AppColors.primary,
                              fontWeight: FontWeight.w600,
                              letterSpacing: 0.5,
                            ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // Modern, clean M3 Linear Progress Indicator at the bottom
            Positioned(
              bottom: 80,
              child: FadeTransition(
                opacity: _fadeAnimation,
                child: SizedBox(
                  width: 160,
                  child: Column(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          minHeight: 4,
                          backgroundColor: (isDark
                                  ? AppColors.primaryLight
                                  : AppColors.primary)
                              .withOpacity(0.12),
                          valueColor: AlwaysStoppedAnimation<Color>(
                            isDark ? AppColors.primaryLight : AppColors.primary,
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      Text(
                        'Loading secure assets...',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: isDark
                                  ? AppColors.textMutedDark
                                  : AppColors.textSecondary,
                              fontSize: 12,
                              letterSpacing: 0.2,
                            ),
                      ),
                    ],
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
