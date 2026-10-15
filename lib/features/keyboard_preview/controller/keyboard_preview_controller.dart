import 'package:flutter/material.dart';
import '../../../state/app_state.dart';

/// Controller managing text testing state and input method switching.
class KeyboardPreviewController extends ChangeNotifier {
  final TextEditingController textController = TextEditingController();

  KeyboardPreviewController() {
    textController.addListener(_onTextChanged);
  }

  bool get hasText => textController.text.isNotEmpty;

  void _onTextChanged() {
    notifyListeners();
  }

  /// Clears text input.
  void clearText() {
    textController.clear();
  }

  /// Opens system input method picker via AppState.
  void switchKeyboard(AppState appState) {
    appState.openInputMethodPicker();
  }

  @override
  void dispose() {
    textController.removeListener(_onTextChanged);
    textController.dispose();
    super.dispose();
  }
}
