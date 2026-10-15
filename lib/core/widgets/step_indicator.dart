import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

/// Reusable step indicator widget for onboarding in Pakistan Green & White theme
class StepIndicator extends StatelessWidget {
  final int currentStep;
  final int totalSteps;

  const StepIndicator({
    super.key,
    required this.currentStep,
    required this.totalSteps,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        totalSteps,
        (index) => AnimatedContainer(
          duration: const Duration(milliseconds: 250),
          margin: const EdgeInsets.symmetric(horizontal: 4),
          width: currentStep == index ? 28 : 8,
          height: 7,
          decoration: BoxDecoration(
            color: currentStep == index
                ? AppColors.primary
                : AppColors.greenBorder,
            borderRadius: BorderRadius.circular(4),
          ),
        ),
      ),
    );
  }
}
