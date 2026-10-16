import 'package:flutter/material.dart';
import '../../../routes/app_routes.dart';

/// Controller managing splash screen animations and navigation lifecycle.
class SplashController {
  late final AnimationController entranceController;
  late final AnimationController loadingController;
  late final Animation<double> fadeAnimation;
  late final Animation<double> scaleAnimation;
  late final Animation<Offset> slideAnimation;
  late final Animation<double> footerFadeAnimation;
  late final Animation<double> loadingAnimation;

  /// Initializes splash entrance and loading animations.
  void initAnimations(TickerProvider vsync) {
    entranceController = AnimationController(
      vsync: vsync,
      duration: const Duration(milliseconds: 1200),
    );

    loadingController = AnimationController(
      vsync: vsync,
      duration: const Duration(milliseconds: 2600),
    );

    fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: entranceController,
        curve: const Interval(0.0, 0.65, curve: Curves.easeOut),
      ),
    );

    scaleAnimation = Tween<double>(begin: 0.88, end: 1.0).animate(
      CurvedAnimation(
        parent: entranceController,
        curve: const Interval(0.0, 0.8, curve: Curves.easeOutBack),
      ),
    );

    slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.10),
      end: Offset.zero,
    ).animate(
      CurvedAnimation(
        parent: entranceController,
        curve: const Interval(0.15, 0.85, curve: Curves.easeOutCubic),
      ),
    );

    footerFadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: entranceController,
        curve: const Interval(0.1, 0.6, curve: Curves.easeOut),
      ),
    );

    loadingAnimation = CurvedAnimation(
      parent: loadingController,
      curve: Curves.easeInOutCubic,
    );

    entranceController.forward();
    loadingController.forward();
  }

  /// Waits for splash duration and navigates to the next appropriate screen.
  Future<void> handleSplashNavigation(
    BuildContext context, {
    required bool isOnboardingComplete,
  }) async {
    await Future.delayed(const Duration(seconds: 3));
    if (!context.mounted) return;

    if (isOnboardingComplete) {
      Navigator.pushReplacementNamed(context, AppRoutes.home);
    } else {
      Navigator.pushReplacementNamed(context, AppRoutes.onboarding);
    }
  }

  /// Disposes active animation controllers.
  void dispose() {
    entranceController.dispose();
    loadingController.dispose();
  }
}
