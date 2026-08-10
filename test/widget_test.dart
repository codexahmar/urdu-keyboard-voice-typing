import 'dart:ui';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:urdu_keyboard/main.dart';
import 'package:urdu_keyboard/state/app_state.dart';

void main() {
  testWidgets('Urdu Smart Keyboard app smoke test', (WidgetTester tester) async {
    // Set a realistic phone screen size to prevent layout overflows in the test
    tester.view.physicalSize = const Size(1080, 1920);
    tester.view.devicePixelRatio = 1.0;

    // Reset view size at the end of the test
    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });

    // Initialize app state
    final appState = AppState();

    // Build app with Provider wrapper
    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: appState,
        child: const UrduSmartKeyboardApp(),
      ),
    );

    // Verify splash screen or title presence
    expect(find.text('Urdu Smart Keyboard'), findsOneWidget);

    // Let the splash screen timer complete to avoid pending timer exception
    await tester.pumpAndSettle(const Duration(seconds: 3));
  });
}
