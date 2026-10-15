import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../state/app_state.dart';

/// Clean, simple KeyboardPreviewScreen matching the VoiceTypingGuideScreen structure.
/// Eliminates all mock keyboard dead code and provides:
/// 1. Subtitle outside container on the left
/// 2. Simple real keyboard test canvas
/// 3. Connected step-by-step timeline guide
/// 4. Helpful keyboard tips card
/// 5. 1-Tap Switch Keyboard action button
class KeyboardPreviewScreen extends StatefulWidget {
  const KeyboardPreviewScreen({super.key});

  @override
  State<KeyboardPreviewScreen> createState() => _KeyboardPreviewScreenState();
}

class _KeyboardPreviewScreenState extends State<KeyboardPreviewScreen> {
  final TextEditingController _testInputController = TextEditingController();

  final List<String> _quickPhrases = [
    'السلام علیکم',
    'پاکستان زندہ باد',
    'شکریہ',
    'Good Morning',
  ];

  @override
  void initState() {
    super.initState();
    _testInputController.addListener(() {
      setState(() {});
    });
  }

  @override
  void dispose() {
    _testInputController.dispose();
    super.dispose();
  }

  void _insertPhrase(String phrase) {
    final currentText = _testInputController.text;
    if (currentText.isEmpty) {
      _testInputController.text = phrase;
    } else {
      _testInputController.text = '$currentText $phrase';
    }
    _testInputController.selection = TextSelection.fromPosition(
      TextPosition(offset: _testInputController.text.length),
    );
  }

  void _clearText() {
    _testInputController.clear();
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final appState = context.watch<AppState>();

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(
          l10n.keyboardPreviewTitle,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Clean subtitle outside container on the left
              Text(
                l10n.keyboardPreviewSubtitle,
                style: const TextStyle(
                  fontSize: 15.5,
                  fontWeight: FontWeight.w500,
                  color: AppColors.textSecondary,
                  height: 1.45,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
              ),
              const SizedBox(height: 20),

              // Simple Real Typing Test Canvas
              _buildTypingCanvas(l10n, appState),
              const SizedBox(height: 20),

              // Step-by-Step Connected Timeline Guide
              _buildTimelineGuideCard(l10n),
              const SizedBox(height: 20),

              // Helpful Tips Card
              _buildTipsCard(l10n),
              const SizedBox(height: 24),

              // Switch Keyboard Action Button
              AppButton.primary(
                label: l10n.onboardingSelectKeyboard,
                icon: Icons.keyboard_alt_outlined,
                height: 52,
                borderRadius: 16,
                onPressed: () => appState.openInputMethodPicker(),
              ),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTypingCanvas(AppLocalizations l10n, AppState appState) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
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
          // Header with Status Indicator & Clear Action
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: appState.isKeyboardActive
                          ? AppColors.primary
                          : AppColors.warning,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    appState.isKeyboardActive
                        ? l10n.active
                        : l10n.homeStatusDisabled,
                    style: TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.bold,
                      color: appState.isKeyboardActive
                          ? AppColors.primary
                          : AppColors.warning,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ],
              ),
              if (_testInputController.text.isNotEmpty)
                InkWell(
                  onTap: _clearText,
                  borderRadius: BorderRadius.circular(8),
                  child: const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.clear_rounded,
                            size: 16, color: AppColors.textSecondary),
                        SizedBox(width: 4),
                        Text(
                          'Clear',
                          style: TextStyle(
                            fontSize: 12.5,
                            color: AppColors.textSecondary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 12),

          // Real Input Area - User's actual keyboard opens on tap
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xFFF9FCFA),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: const Color(0xFFE0EFE2)),
            ),
            child: TextField(
              controller: _testInputController,
              minLines: 3,
              maxLines: 5,
              style: const TextStyle(
                fontSize: 16,
                color: AppColors.textPrimary,
                fontFamilyFallback: AppTheme.urduFontFallbacks,
              ),
              decoration: InputDecoration(
                hintText: l10n.homeTestHint,
                hintStyle: const TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 14,
                  fontFamilyFallback: AppTheme.urduFontFallbacks,
                ),
                border: InputBorder.none,
                isDense: true,
                contentPadding: EdgeInsets.zero,
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Quick Test Phrase Chips
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _quickPhrases.map((phrase) {
              return InkWell(
                onTap: () => _insertPhrase(phrase),
                borderRadius: BorderRadius.circular(20),
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: const Color(0xFFE8F5E9),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: const Color(0xFFC8E6C9)),
                  ),
                  child: Text(
                    phrase,
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.w600,
                      color: AppColors.primary,
                      fontFamilyFallback: AppTheme.urduFontFallbacks,
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineGuideCard(AppLocalizations l10n) {
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
          // Header without icon (matching VoiceTypingGuideScreen)
          Text(
            l10n.keyboardPreviewTitle,
            style: const TextStyle(
              fontSize: 15.5,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
          ),
          const SizedBox(height: 18),

          // Steps 1 to 4 connected timeline
          _buildTimelineStep(
            stepNumber: 1,
            icon: Icons.chat_bubble_outline_rounded,
            title: l10n.voiceGuideStep1Title,
            description: l10n.voiceGuideStep1Desc,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 2,
            icon: Icons.spellcheck_rounded,
            title: l10n.onboardingPhoneticTitle,
            description: l10n.onboardingPhoneticSubtitle,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 3,
            icon: Icons.swap_horiz_rounded,
            title: l10n.onboardingSwitchTitle,
            description: l10n.onboardingSwitchSubtitle,
            isLast: false,
          ),
          _buildTimelineStep(
            stepNumber: 4,
            icon: Icons.mic_rounded,
            title: l10n.onboardingVoiceFeatureTitle,
            description: l10n.onboardingVoiceFeatureSubtitle,
            isLast: true,
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineStep({
    required int stepNumber,
    required IconData icon,
    required String title,
    required String description,
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
                  '$stepNumber',
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

          // Content
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(bottom: isLast ? 0 : 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(icon, size: 16, color: AppColors.primary),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          title,
                          style: const TextStyle(
                            fontSize: 14.5,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimary,
                            fontFamilyFallback: AppTheme.urduFontFallbacks,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
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

  Widget _buildTipsCard(AppLocalizations l10n) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFF9FCFA),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFE0EFE2), width: 1.2),
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
                l10n.voiceGuideTipsTitle,
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
          _buildTipItem(l10n.keyboardPreviewPhoneticHint),
          const SizedBox(height: 10),
          _buildTipItem(l10n.onboardingSwitchTipSubtitle),
          const SizedBox(height: 10),
          _buildTipItem(l10n.voiceGuideTip2),
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
