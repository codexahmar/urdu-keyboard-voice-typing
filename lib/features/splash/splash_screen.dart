import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/constants/app_constants.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../state/app_state.dart';
import 'controller/splash_controller.dart';

/// Initial splash screen with entrance animation.
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with TickerProviderStateMixin {
  late final SplashController _controller;

  @override
  void initState() {
    super.initState();
    _controller = SplashController();
    _controller.initAnimations(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final appState = context.read<AppState>();
      _controller.handleSplashNavigation(
        context,
        isOnboardingComplete: appState.onboardingComplete,
      );
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: Stack(
        children: [
          _buildBackgroundAura(),
          Center(child: _buildAnimatedHeroContent()),
          _buildAnimatedFooter(),
        ],
      ),
    );
  }

  /// Builds ambient background gradient and lighter corner bubbles.
  Widget _buildBackgroundAura() {
    return SizedBox.expand(
      child: DecoratedBox(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [AppColors.white, Color(0xFFF7FAF8), Color(0xFFEDF5EF)],
            stops: [0.0, 0.55, 1.0],
          ),
        ),
        child: Stack(
          children: [
            Positioned(
              top: -100,
              right: -80,
              child: Container(
                width: 260,
                height: 260,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: const Color.fromARGB(255, 94, 142, 103),
                ),
              ),
            ),
            Positioned(
              bottom: -90,
              left: -80,
              child: Container(
                width: 240,
                height: 240,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: const Color.fromARGB(255, 94, 142, 103),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Builds the central hero brand content with icon, titles, and loader.
  Widget _buildAnimatedHeroContent() {
    return FadeTransition(
      opacity: _controller.fadeAnimation,
      child: ScaleTransition(
        scale: _controller.scaleAnimation,
        child: SlideTransition(
          position: _controller.slideAnimation,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Image.asset(
                AppConstants.appIconPath,
                width: 110,
                height: 110,
                fit: BoxFit.contain,
              ),
              const SizedBox(height: 28),
              const Text(
                AppConstants.appName,
                style: TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                  letterSpacing: -0.5,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 6),
              const Text(
                'اردو کی بورڈ',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                  height: 1.5,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 36),
              _buildLoadingBar(),
            ],
          ),
        ),
      ),
    );
  }

  /// Builds a smooth animated progress bar filling over the splash duration.
  Widget _buildLoadingBar() {
    return AnimatedBuilder(
      animation: _controller.loadingAnimation,
      builder: (context, child) {
        return SizedBox(
          width: 130,
          height: 4,
          child: ClipRRect(
            borderRadius: BorderRadius.circular(2),
            child: LinearProgressIndicator(
              value: _controller.loadingAnimation.value,
              backgroundColor: const Color(0xFFE2E8F0),
              valueColor: const AlwaysStoppedAnimation<Color>(
                AppColors.primary,
              ),
            ),
          ),
        );
      },
    );
  }

  /// Builds visible version tag anchored at the bottom.
  Widget _buildAnimatedFooter() {
    return SafeArea(
      child: Align(
        alignment: Alignment.bottomCenter,
        child: Padding(
          padding: const EdgeInsets.only(bottom: 24),
          child: FadeTransition(
            opacity: _controller.footerFadeAnimation,
            child: const Text(
              'v${AppConstants.appVersion}',
              style: TextStyle(
                fontSize: 13,
                color: AppColors.textSecondary,
                fontWeight: FontWeight.w600,
                letterSpacing: 0.5,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
