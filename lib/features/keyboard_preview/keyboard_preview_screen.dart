import 'package:flutter/material.dart';
import '../../core/localization/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_theme.dart';

/// Clean KeyboardPreviewScreen in Pakistan Green & White theme.
/// Provides an interactive phonetic playground and preview of keyboard layouts.
class KeyboardPreviewScreen extends StatefulWidget {
  const KeyboardPreviewScreen({super.key});

  @override
  State<KeyboardPreviewScreen> createState() => _KeyboardPreviewScreenState();
}

class _KeyboardPreviewScreenState extends State<KeyboardPreviewScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _testInputController = TextEditingController();

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
    _tabController = TabController(length: 3, vsync: this);
    _testInputController.addListener(_onTextChanged);
  }

  @override
  void dispose() {
    _tabController.dispose();
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
    final l10n = context.l10n;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(l10n.keyboardPreviewTitle),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppColors.primary,
          indicatorWeight: 3,
          labelColor: AppColors.primary,
          unselectedLabelColor: AppColors.textMuted,
          labelStyle: const TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 14,
            fontFamilyFallback: AppTheme.urduFontFallbacks,
          ),
          tabs: [
            Tab(text: l10n.keyboardPreviewTabPhonetic),
            Tab(text: l10n.keyboardPreviewTabAlphabetic),
            Tab(text: l10n.keyboardPreviewTabEnglish),
          ],
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Interactive Typing Test Area
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Container(
                decoration: BoxDecoration(
                  color: AppColors.surface,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.greenBorder, width: 1.2),
                ),
                child: Column(
                  children: [
                    TextField(
                      controller: _testInputController,
                      maxLines: 2,
                      style: const TextStyle(
                        fontSize: 16,
                        color: AppColors.textPrimary,
                        fontFamilyFallback: AppTheme.urduFontFallbacks,
                      ),
                      decoration: InputDecoration(
                        hintText: l10n.homeTestHint,
                        contentPadding: const EdgeInsets.all(12),
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                      ),
                    ),
                    const Divider(height: 1),
                    // Suggestion Strip
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      child: Row(
                        children: [
                          const Icon(Icons.auto_awesome, size: 16, color: AppColors.primary),
                          const SizedBox(width: 8),
                          ActionChip(
                            backgroundColor: AppColors.greenLight,
                            side: const BorderSide(color: AppColors.greenBorder),
                            label: Text(
                              _currentPhoneticSuggestion,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: AppColors.primary,
                                fontFamilyFallback: AppTheme.urduFontFallbacks,
                              ),
                            ),
                            onPressed: _applySuggestion,
                          ),
                          const Spacer(),
                          IconButton(
                            icon: const Icon(Icons.backspace_outlined, size: 20, color: AppColors.primary),
                            onPressed: _backspace,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // Tab Views (Layouts)
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildKeyboardGrid([
                    ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'],
                    ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'],
                    ['z', 'x', 'c', 'v', 'b', 'n', 'm'],
                  ], isUrduPhonetic: true),
                  _buildKeyboardGrid([
                    ['آ', 'ا', 'ب', 'پ', 'ت', 'ٹ', 'ث', 'ج', 'چ'],
                    ['ح', 'خ', 'د', 'ڈ', 'ذ', 'ر', 'ڑ', 'ز', 'ژ'],
                    ['س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف'],
                    ['ق', 'ک', 'گ', 'ل', 'م', 'ن', 'و', 'ہ', 'ی'],
                  ]),
                  _buildKeyboardGrid([
                    ['Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'],
                    ['A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'],
                    ['Z', 'X', 'C', 'V', 'B', 'N', 'M'],
                  ]),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildKeyboardGrid(List<List<String>> rows, {bool isUrduPhonetic = false}) {
    return Container(
      color: AppColors.keyboardBackground,
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: rows.map((row) {
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: row.map((char) {
                return Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 3),
                    child: InkWell(
                      onTap: () => _keyTap(char),
                      borderRadius: BorderRadius.circular(10),
                      child: Container(
                        height: 48,
                        decoration: BoxDecoration(
                          color: AppColors.keyBackground,
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(color: AppColors.border, width: 1),
                          boxShadow: const [
                            BoxShadow(
                              color: Color(0x08000000),
                              blurRadius: 2,
                              offset: Offset(0, 1),
                            ),
                          ],
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          char,
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: AppColors.textPrimary,
                            fontFamilyFallback: AppTheme.urduFontFallbacks,
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          );
        }).toList(),
      ),
    );
  }
}
