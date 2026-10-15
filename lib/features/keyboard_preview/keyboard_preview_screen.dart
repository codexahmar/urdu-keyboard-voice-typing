import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/guide_timeline_card.dart';
import '../../state/app_state.dart';

/// Clean, simple KeyboardPreviewScreen.
/// 1. Subtitle outside container on the left
/// 2. Standalone TextField without wrapping container
/// 3. Global GuideTimelineCard (steps without left icons)
/// 4. 1-Tap Switch Keyboard action button
class KeyboardPreviewScreen extends StatefulWidget {
  const KeyboardPreviewScreen({super.key});

  @override
  State<KeyboardPreviewScreen> createState() => _KeyboardPreviewScreenState();
}

class _KeyboardPreviewScreenState extends State<KeyboardPreviewScreen> {
  final TextEditingController _testInputController = TextEditingController();

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
              const SizedBox(height: 18),

              // Standalone, clean TextField (no wrapping Container)
              _buildTypingField(l10n),
              const SizedBox(height: 20),

              // The One Guide Card Container (global common widget, no left icons)
              GuideTimelineCard(
                title: l10n.keyboardPreviewTitle,
                steps: [
                  GuideTimelineStep(
                    stepNumber: 1,
                    title: l10n.voiceGuideStep1Title,
                    description: l10n.voiceGuideStep1Desc,
                  ),
                  GuideTimelineStep(
                    stepNumber: 2,
                    title: l10n.onboardingPhoneticTitle,
                    description: l10n.onboardingPhoneticSubtitle,
                  ),
                  GuideTimelineStep(
                    stepNumber: 3,
                    title: l10n.onboardingSwitchTitle,
                    description: l10n.onboardingSwitchSubtitle,
                  ),
                  GuideTimelineStep(
                    stepNumber: 4,
                    title: l10n.onboardingVoiceFeatureTitle,
                    description: l10n.onboardingVoiceFeatureSubtitle,
                  ),
                ],
              ),
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

  Widget _buildTypingField(AppLocalizations l10n) {
    return TextField(
      controller: _testInputController,
      minLines: 4,
      maxLines: 6,
      style: const TextStyle(
        fontSize: 16,
        color: AppColors.textPrimary,
        fontFamilyFallback: AppTheme.urduFontFallbacks,
      ),
      decoration: InputDecoration(
        filled: true,
        fillColor: AppColors.white,
        hintText: l10n.homeTestHint,
        hintStyle: const TextStyle(
          color: AppColors.textSecondary,
          fontSize: 14,
          fontFamilyFallback: AppTheme.urduFontFallbacks,
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.border, width: 1.2),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.border, width: 1.2),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.primary, width: 1.5),
        ),
        suffixIcon: _testInputController.text.isNotEmpty
            ? IconButton(
                icon: const Icon(Icons.clear_rounded,
                    size: 18, color: AppColors.textSecondary),
                onPressed: _clearText,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                tooltip: 'Clear',
              )
            : null,
      ),
    );
  }
}
