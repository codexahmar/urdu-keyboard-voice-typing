import 'package:flutter/material.dart';
import '../../core/constants/app_strings.dart';
import '../../core/theme/app_colors.dart';

/// Keyboard preview screen - visual representation only
class KeyboardPreviewScreen extends StatelessWidget {
  const KeyboardPreviewScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text(AppStrings.keyboardPreviewTitle)),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Description
              Text(
                AppStrings.keyboardPreviewDescription,
                style: Theme.of(context).textTheme.bodyLarge,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),

              // Keyboard Preview
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppColors.keyboardPreview,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.grey.shade300),
                ),
                child: Column(
                  children: [
                    // Row 1
                    _buildKeyRow([
                      'q',
                      'w',
                      'e',
                      'r',
                      't',
                      'y',
                      'u',
                      'i',
                      'o',
                      'p',
                    ]),
                    const SizedBox(height: 8),
                    // Row 2
                    _buildKeyRow(['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l']),
                    const SizedBox(height: 8),
                    // Row 3
                    _buildKeyRow(['z', 'x', 'c', 'v', 'b', 'n', 'm']),
                    const SizedBox(height: 8),
                    // Space bar row
                    Row(
                      children: [
                        _buildSpecialKey(
                          Icons.emoji_emotions_outlined,
                          flex: 1,
                        ),
                        const SizedBox(width: 8),
                        _buildSpaceBar(),
                        const SizedBox(width: 8),
                        _buildSpecialKey(Icons.backspace_outlined, flex: 1),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 32),

              // Phonetic Typing Guide
              Card(
                color: AppColors.urduAccent.withOpacity(0.1),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            Icons.lightbulb_outline,
                            color: AppColors.urduAccent,
                            size: 32,
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Text(
                              AppStrings.phoneticTyping,
                              style: Theme.of(context).textTheme.headlineSmall
                                  ?.copyWith(color: AppColors.urduAccent),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        AppStrings.phoneticDescription,
                        style: Theme.of(context).textTheme.bodyLarge,
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Quick Guide
              Text(
                AppStrings.quickGuide,
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 16),
              _buildGuideItem(
                context,
                'Type in Roman letters',
                'Use English letters to type Urdu words',
                Icons.keyboard,
              ),
              const SizedBox(height: 12),
              _buildGuideItem(
                context,
                'Suggestions appear',
                'Urdu words will appear as suggestions',
                Icons.format_list_bulleted,
              ),
              const SizedBox(height: 12),
              _buildGuideItem(
                context,
                'Select the word',
                'Tap the correct Urdu word from suggestions',
                Icons.touch_app,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildKeyRow(List<String> keys) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: keys.map((key) {
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 3),
          child: Container(
            width: 27,
            height: 44,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(6),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 2,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Center(
              child: Text(
                key.toUpperCase(),
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildSpecialKey(IconData icon, {int flex = 1}) {
    return Expanded(
      flex: flex,
      child: Container(
        height: 44,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(6),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 2,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Icon(icon, size: 20),
      ),
    );
  }

  Widget _buildSpaceBar() {
    return Expanded(
      flex: 4,
      child: Container(
        height: 44,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(6),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 2,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: const Center(
          child: Text('space', style: TextStyle(fontSize: 14)),
        ),
      ),
    );
  }

  Widget _buildGuideItem(
    BuildContext context,
    String title,
    String description,
    IconData icon,
  ) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.primary.withOpacity(0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: AppColors.primary, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 4),
                Text(
                  description,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: Theme.of(context).textTheme.bodySmall?.color,
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
