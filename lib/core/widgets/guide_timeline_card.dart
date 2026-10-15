import 'package:flutter/material.dart';
import '../theme/app_colors.dart';
import '../theme/app_theme.dart';

/// Data model representing a single step in the timeline guide.
class GuideTimelineStep {
  final int stepNumber;
  final String title;
  final String description;

  const GuideTimelineStep({
    required this.stepNumber,
    required this.title,
    required this.description,
  });
}

/// Global reusable timeline guide card.
/// Features clean vertical connecting line, numbered badges, and clean typography
/// with no icons on the left of step titles.
class GuideTimelineCard extends StatelessWidget {
  final String title;
  final List<GuideTimelineStep> steps;

  const GuideTimelineCard({
    super.key,
    required this.title,
    required this.steps,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.border, width: 1.2),
        boxShadow: const [
          BoxShadow(
            color: Color(0x0A01411C),
            blurRadius: 12,
            offset: Offset(0, 3),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header without icon
          Text(
            title,
            style: const TextStyle(
              fontSize: 15.5,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 18),

          // Connected Timeline Steps
          for (int i = 0; i < steps.length; i++)
            _buildTimelineStep(
              step: steps[i],
              isLast: i == steps.length - 1,
            ),
        ],
      ),
    );
  }

  Widget _buildTimelineStep({
    required GuideTimelineStep step,
    required bool isLast,
  }) {
    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Step Number Circle & Connecting Vertical Line
          Column(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: const BoxDecoration(
                  color: AppColors.primary,
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: Text(
                  '${step.stepNumber}',
                  style: const TextStyle(
                    color: AppColors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                ),
              ),
              if (!isLast)
                Expanded(
                  child: Container(
                    width: 2,
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    color: const Color(0xFFE2E8F0),
                  ),
                ),
            ],
          ),
          const SizedBox(width: 14),

          // Content - NO icon next to title
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(bottom: isLast ? 0 : 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    step.title,
                    style: const TextStyle(
                      fontSize: 14.5,
                      fontWeight: FontWeight.bold,
                      color: AppColors.textPrimary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    step.description,
                    style: const TextStyle(
                      fontSize: 13,
                      color: AppColors.textSecondary,
                      height: 1.4,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Global reusable tips card with pure white background and clear checkmark badges.
class GuideTipsCard extends StatelessWidget {
  final String title;
  final List<String> tips;

  const GuideTipsCard({
    super.key,
    required this.title,
    required this.tips,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.white, // Pure white background for maximum visibility
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border, width: 1.2),
        boxShadow: const [
          BoxShadow(
            color: Color(0x0A01411C),
            blurRadius: 10,
            offset: Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: const Color(0xFFE8F5E9),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.lightbulb_outline_rounded,
                  size: 18,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 10),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 14.5,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          for (int i = 0; i < tips.length; i++) ...[
            if (i > 0) const SizedBox(height: 10),
            _buildTipItem(tips[i]),
          ],
        ],
      ),
    );
  }

  Widget _buildTipItem(String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          margin: const EdgeInsets.only(top: 3),
          width: 16,
          height: 16,
          decoration: const BoxDecoration(
            color: Color(0xFFE8F5E9),
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.check_rounded,
            size: 11,
            color: AppColors.primary,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textPrimary,
              height: 1.4,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
        ),
      ],
    );
  }
}
