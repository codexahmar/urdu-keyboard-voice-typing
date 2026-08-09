import 'package:flutter/material.dart';
import '../../core/constants/app_strings.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';

/// Keyboard preview screen - featuring an interactive phonetic typing playground
/// and hyper-realistic native keyboard layout visualizer
class KeyboardPreviewScreen extends StatefulWidget {
  const KeyboardPreviewScreen({Key? key}) : super(key: key);

  @override
  State<KeyboardPreviewScreen> createState() => _KeyboardPreviewScreenState();
}

class _KeyboardPreviewScreenState extends State<KeyboardPreviewScreen> {
  final TextEditingController _testInputController = TextEditingController();

  // Simple Phonetic dictionary for live test preview demo
  final Map<String, String> _phoneticMap = {
    'salam': 'سلام',
    'pakistan': 'پاکستان',
    'urdu': 'اردو',
    'shukriya': 'شکریہ',
    'kia': 'کیا',
    'hal': 'حال',
    'ap': 'آپ',
    'khush': 'خوش',
    'amdeed': 'آمدید',
    'a': 'ا',
    'b': 'ب',
    'p': 'پ',
    't': 'ت',
    's': 'س',
    'm': 'م',
    'k': 'ک',
    'g': 'گ',
  };

  String _currentPhoneticSuggestion = 'سلام';

  @override
  void initState() {
    super.initState();
    _testInputController.addListener(_onTextChanged);
  }

  @override
  void dispose() {
    _testInputController.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    final text = _testInputController.text.trim().toLowerCase();
    if (text.isEmpty) {
      setState(() {
        _currentPhoneticSuggestion = 'سلام';
      });
      return;
    }

    final words = text.split(' ');
    final lastWord = words.isNotEmpty ? words.last : '';

    if (_phoneticMap.containsKey(lastWord)) {
      setState(() {
        _currentPhoneticSuggestion = _phoneticMap[lastWord]!;
      });
    } else {
      // Basic fallback mock phonetic
      setState(() {
        _currentPhoneticSuggestion = '${_testInputController.text} (Urdu)';
      });
    }
  }

  void _keyTap(String char) {
    _testInputController.text = _testInputController.text + char;
    _testInputController.selection = TextSelection.fromPosition(
      TextPosition(offset: _testInputController.text.length),
    );
  }

  void _backspace() {
    if (_testInputController.text.isNotEmpty) {
      _testInputController.text = _testInputController.text.substring(
        0,
        _testInputController.text.length - 1,
      );
      _testInputController.selection = TextSelection.fromPosition(
        TextPosition(offset: _testInputController.text.length),
      );
    }
  }

  void _applySuggestion() {
    if (_currentPhoneticSuggestion.isNotEmpty) {
      _testInputController.text = '$_currentPhoneticSuggestion ';
      _testInputController.selection = TextSelection.fromPosition(
        TextPosition(offset: _testInputController.text.length),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: isDark ? AppColors.backgroundDark : AppColors.background,
      appBar: AppBar(title: const Text(AppStrings.keyboardPreviewTitle)),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: (isDark ? AppColors.secondaryLight : AppColors.secondary).withOpacity(0.08),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: (isDark ? AppColors.secondaryLight : AppColors.secondary).withOpacity(0.2),
                  ),
                ),
                child: Row(
                  children: [
                    Icon(
                      Icons.touch_app_rounded,
                      color: isDark ? AppColors.secondaryLight : AppColors.secondary,
                      size: 28,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Test the interactive keyboard below! Type Roman letters to see live Urdu suggestions.',
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w500,
                          color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Playground Text Input Box
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                decoration: BoxDecoration(
                  color: isDark ? AppColors.surfaceDark : Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: isDark ? AppColors.primaryLight : AppColors.primary,
                    width: 2,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.08),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: TextField(
                  controller: _testInputController,
                  decoration: InputDecoration(
                    border: InputBorder.none,
                    enabledBorder: InputBorder.none,
                    focusedBorder: InputBorder.none,
                    hintText: 'Type here (e.g. salam)...',
                    hintStyle: TextStyle(
                      color: isDark ? AppColors.textMutedDark : AppColors.textMuted,
                      fontSize: 15,
                    ),
                    suffixIcon: _testInputController.text.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear_rounded, size: 20),
                            onPressed: () => _testInputController.clear(),
                          )
                        : null,
                  ),
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Hyper-Realistic Keyboard Preview Card
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: isDark
                      ? AppColors.keyboardPreviewBackgroundDark
                      : AppColors.keyboardPreviewBackground,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(isDark ? 0.4 : 0.1),
                      blurRadius: 15,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    // Suggestions Bar
                    Container(
                      height: 42,
                      // margin: const EdgeInsets.only(bottom: 10)),
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                      decoration: BoxDecoration(
                        color: isDark ? AppColors.surfaceDark : Colors.white,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          _buildSuggestionChip('پاکستان', () {
                            _testInputController.text = 'پاکستان ';
                          }),
                          const VerticalDivider(indent: 8, endIndent: 8),
                          _buildSuggestionChip(_currentPhoneticSuggestion, _applySuggestion, isHighlighted: true),
                          const VerticalDivider(indent: 8, endIndent: 8),
                          _buildSuggestionChip('شکریہ', () {
                            _testInputController.text = 'شکریہ ';
                          }),
                        ],
                      ),
                    ),

                    // Key Row 1
                    _buildKeyRow(['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p']),
                    const SizedBox(height: 6),
                    // Key Row 2
                    _buildKeyRow(['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l']),
                    const SizedBox(height: 6),
                    // Key Row 3
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        _buildSpecialKey(Icons.north_rounded, flex: 15, onTap: () {}),
                        const SizedBox(width: 4),
                        Expanded(
                          flex: 70,
                          child: _buildKeyRow(['z', 'x', 'c', 'v', 'b', 'n', 'm']),
                        ),
                        const SizedBox(width: 4),
                        _buildSpecialKey(Icons.backspace_outlined, flex: 15, onTap: _backspace),
                      ],
                    ),
                    const SizedBox(height: 6),

                    // Space Bar & Function keys row
                    Row(
                      children: [
                        _buildSpecialKeyText('?123', flex: 15, onTap: () {}),
                        const SizedBox(width: 6),
                        _buildSpecialKey(Icons.language_rounded, flex: 12, onTap: () {}),
                        const SizedBox(width: 6),
                        Expanded(
                          flex: 50,
                          child: GestureDetector(
                            onTap: () => _keyTap(' '),
                            child: Container(
                              height: 46,
                              decoration: BoxDecoration(
                                color: isDark ? AppColors.surfaceDark : Colors.white,
                                borderRadius: BorderRadius.circular(8),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.black.withOpacity(0.1),
                                    blurRadius: 2,
                                    offset: const Offset(0, 1),
                                  ),
                                ],
                              ),
                              child: const Center(
                                child: Text(
                                  'اردو / English',
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w600,
                                    color: Colors.grey,
                                    fontFamilyFallback: AppTheme.urduFontFallbacks,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 6),
                        _buildSpecialKey(Icons.mic_none_rounded, flex: 12, onTap: () {}),
                        const SizedBox(width: 6),
                        _buildSpecialKey(Icons.keyboard_return_rounded, flex: 15, isPrimary: true, onTap: () {
                          _keyTap('\n');
                        }),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 28),

              // Phonetic Guide Card
              Card(
                color: AppColors.urduAccent.withOpacity(0.08),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                  side: BorderSide(
                    color: AppColors.urduAccent.withOpacity(0.24),
                    width: 1.5,
                  ),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: AppColors.urduAccent.withOpacity(0.12),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(
                              Icons.lightbulb_outline_rounded,
                              color: AppColors.urduAccent,
                              size: 24,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Text(
                            AppStrings.phoneticTyping,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                  color: AppColors.urduAccent,
                                  fontWeight: FontWeight.bold,
                                ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Text(
                        AppStrings.phoneticDescription,
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                              height: 1.5,
                              color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                            ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Quick Step Guide
              Text(
                AppStrings.quickGuide,
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 14),

              _buildGuideItem(
                context,
                '1. Type in Roman letters',
                'Use standard English alphabet keys (e.g. "salam")',
                Icons.keyboard_alt_outlined,
              ),
              const SizedBox(height: 10),
              _buildGuideItem(
                context,
                '2. Instant Urdu Suggestions',
                'Correct Urdu words like "سلام" appear above the keys',
                Icons.smart_button_outlined,
              ),
              const SizedBox(height: 10),
              _buildGuideItem(
                context,
                '3. One-Tap Selection',
                'Tap the word to insert it directly into any app',
                Icons.touch_app_outlined,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSuggestionChip(String text, VoidCallback onTap, {bool isHighlighted = false}) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          alignment: Alignment.center,
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Text(
            text,
            style: TextStyle(
              fontSize: 16,
              fontWeight: isHighlighted ? FontWeight.bold : FontWeight.w500,
              color: isHighlighted
                  ? (isDark ? AppColors.primaryLight : AppColors.primary)
                  : (isDark ? AppColors.textPrimaryDark : AppColors.textPrimary),
              fontFamilyFallback: AppTheme.urduFontFallbacks,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ),
    );
  }

  Widget _buildKeyRow(List<String> keys) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: keys.map((key) {
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 2),
            child: _buildKey(key),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildKey(String label) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () => _keyTap(label),
        borderRadius: BorderRadius.circular(8),
        child: Container(
          height: 46,
          decoration: BoxDecoration(
            color: isDark ? AppColors.surfaceDark : Colors.white,
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.12),
                blurRadius: 2,
                offset: const Offset(0, 1),
              ),
            ],
          ),
          child: Center(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSpecialKey(IconData icon, {required int flex, required VoidCallback onTap, bool isPrimary = false}) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Expanded(
      flex: flex,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          height: 46,
          decoration: BoxDecoration(
            color: isPrimary
                ? (isDark ? AppColors.primaryLight : AppColors.primary)
                : (isDark ? AppColors.surfaceVariantDark : Colors.blueGrey.shade100),
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.12),
                blurRadius: 2,
                offset: const Offset(0, 1),
              ),
            ],
          ),
          child: Center(
            child: Icon(
              icon,
              size: 20,
              color: isPrimary
                  ? (isDark ? Colors.black : Colors.white)
                  : (isDark ? AppColors.textPrimaryDark : AppColors.textPrimary),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSpecialKeyText(String label, {required int flex, required VoidCallback onTap}) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Expanded(
      flex: flex,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          height: 46,
          decoration: BoxDecoration(
            color: isDark ? AppColors.surfaceVariantDark : Colors.blueGrey.shade100,
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.12),
                blurRadius: 2,
                offset: const Offset(0, 1),
              ),
            ],
          ),
          child: Center(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.bold,
                color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
              ),
            ),
          ),
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
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isDark ? AppColors.surfaceDark : Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isDark ? AppColors.dividerDark : AppColors.divider,
        ),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: (isDark ? AppColors.primaryLight : AppColors.primary).withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              icon,
              color: isDark ? AppColors.primaryLight : AppColors.primary,
              size: 22,
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                    color: isDark ? AppColors.textPrimaryDark : AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 13,
                    color: isDark ? AppColors.textSecondaryDark : AppColors.textSecondary,
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
