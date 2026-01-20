import 'package:flutter/material.dart';
import '../../core/constants/app_strings.dart';

/// Voice typing guide screen - informational only
class VoiceTypingGuideScreen extends StatelessWidget {
  const VoiceTypingGuideScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text(AppStrings.voiceTypingTitle)),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Description
              Text(
                AppStrings.voiceTypingDescription,
                style: Theme.of(context).textTheme.bodyLarge,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),

              // Microphone illustration
              Center(
                child: Container(
                  width: 120,
                  height: 120,
                  decoration: BoxDecoration(
                    color: Colors.purple.withOpacity(0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    Icons.mic,
                    size: 64,
                    color: Colors.purple.shade700,
                  ),
                ),
              ),
              const SizedBox(height: 32),

              // How to use section
              Text(
                AppStrings.howToUse,
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 16),

              _buildStep(
                context,
                1,
                AppStrings.voiceStep1,
                Icons.keyboard_voice,
              ),
              const SizedBox(height: 16),

              _buildStep(
                context,
                2,
                AppStrings.voiceStep2,
                Icons.record_voice_over,
              ),
              const SizedBox(height: 16),

              _buildStep(context, 3, AppStrings.voiceStep3, Icons.text_fields),
              const SizedBox(height: 32),

              // Supported languages
              Card(
                color: Colors.blue.shade50,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            Icons.language,
                            color: Colors.blue.shade700,
                            size: 32,
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Text(
                              AppStrings.supportedLanguages,
                              style: Theme.of(context).textTheme.headlineSmall
                                  ?.copyWith(color: Colors.blue.shade900),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        AppStrings.voiceLanguagesInfo,
                        style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          color: Colors.blue.shade900,
                        ),
                      ),
                      const SizedBox(height: 16),
                      _buildLanguageChip(context, 'اردو', 'Urdu'),
                      const SizedBox(height: 8),
                      _buildLanguageChip(context, 'English', 'English'),
                      const SizedBox(height: 8),
                      _buildLanguageChip(context, 'العربية', 'Arabic'),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Tips card
              Card(
                color: Colors.green.shade50,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            Icons.tips_and_updates,
                            color: Colors.green.shade700,
                            size: 28,
                          ),
                          const SizedBox(width: 12),
                          Text(
                            'Tips for Better Results',
                            style: Theme.of(context).textTheme.titleLarge
                                ?.copyWith(color: Colors.green.shade900),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      _buildTip(context, 'Speak clearly and naturally'),
                      _buildTip(context, 'Minimize background noise'),
                      _buildTip(context, 'Use punctuation commands'),
                      _buildTip(context, 'Check internet connection'),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStep(
    BuildContext context,
    int number,
    String description,
    IconData icon,
  ) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.purple.shade50,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.purple.shade200),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.purple.shade700,
              shape: BoxShape.circle,
            ),
            child: Center(
              child: Text(
                number.toString(),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(icon, color: Colors.purple.shade700, size: 24),
                const SizedBox(height: 8),
                Text(
                  description,
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: Colors.purple.shade900,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLanguageChip(
    BuildContext context,
    String nativeName,
    String name,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue.shade200),
      ),
      child: Row(
        children: [
          Icon(Icons.check_circle, color: Colors.blue.shade700, size: 20),
          const SizedBox(width: 12),
          Text(nativeName, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(width: 8),
          Text(
            '($name)',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).textTheme.bodySmall?.color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTip(BuildContext context, String tip) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.check_circle_outline,
            color: Colors.green.shade700,
            size: 20,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              tip,
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(color: Colors.green.shade900),
            ),
          ),
        ],
      ),
    );
  }
}
