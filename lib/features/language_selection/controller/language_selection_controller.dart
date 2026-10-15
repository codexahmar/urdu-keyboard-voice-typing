import 'package:flutter/material.dart';
import '../../../state/app_state.dart';

/// Controller managing language selection state and persistence.
class LanguageSelectionController extends ChangeNotifier {
  String _selectedCode;

  LanguageSelectionController({required String initialCode})
      : _selectedCode = initialCode;

  String get selectedCode => _selectedCode;

  /// Updates the currently selected language code.
  void selectLanguage(String code) {
    if (_selectedCode != code) {
      _selectedCode = code;
      notifyListeners();
    }
  }

  /// Persists selected language to AppState and pops the screen.
  Future<void> applyLanguage(BuildContext context, AppState appState) async {
    await appState.setSelectedLanguage(_selectedCode);
    if (context.mounted) {
      Navigator.pop(context);
    }
  }
}
