import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:urdu_keyboard/core/constants/app_constants.dart';
import 'package:urdu_keyboard/core/localization/app_localizations.dart';
import 'package:urdu_keyboard/core/theme/app_theme.dart';
import 'package:urdu_keyboard/features/home/home_screen.dart';
import 'package:urdu_keyboard/state/app_state.dart';

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    GoogleFonts.config.allowRuntimeFetching = false;
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('HomeScreen renders with clean hierarchy, AppBar settings button, and no test keyboard',
      (WidgetTester tester) async {
    final appState = AppState();
    bool settingsNavigated = false;

    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: appState,
        child: MaterialApp(
          locale: const Locale('en'),
          supportedLocales: AppLocalizations.supportedLocales,
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          theme: AppTheme.theme,
          routes: {
            AppConstants.settingsRoute: (context) {
              settingsNavigated = true;
              return const Scaffold(body: Text('Settings Screen Mock'));
            },
          },
          home: const HomeScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Verify title and subtitle exist
    expect(find.text('Urdu Smart Keyboard'), findsOneWidget);
    expect(find.text('Your complete Urdu & English typing companion'), findsOneWidget);

    // Verify test keyboard TextField is completely removed
    expect(find.byType(TextField), findsNothing);
    expect(find.text('Test Keyboard'), findsNothing);

    // Verify prominent Settings button exists in AppBar and works
    final settingsIconFinder = find.byIcon(Icons.settings_outlined);
    expect(settingsIconFinder, findsOneWidget);

    await tester.tap(settingsIconFinder);
    await tester.pumpAndSettle();
    expect(settingsNavigated, isTrue);

    // Verify no RefreshIndicator or refresh icon button
    expect(find.byType(RefreshIndicator), findsNothing);
    expect(find.byIcon(Icons.refresh), findsNothing);
  });

  testWidgets('HomeScreen renders vibrant hero card when keyboard is active without number 2',
      (WidgetTester tester) async {
    const channel = MethodChannel('com.codexahmar.urdu_keyboard/keyboard');
    tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      if (methodCall.method == 'isKeyboardEnabled') return true;
      if (methodCall.method == 'isDefaultKeyboard') return true;
      return null;
    });

    final appState = AppState();
    await appState.checkKeyboardStatus();

    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: appState,
        child: MaterialApp(
          locale: const Locale('en'),
          supportedLocales: AppLocalizations.supportedLocales,
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          theme: AppTheme.theme,
          home: const HomeScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Verify Active badge and vibrant card elements
    expect(find.text('Active'), findsOneWidget);
    expect(find.text('Urdu Smart Keyboard is now active'), findsOneWidget);

    // Verify subtitle text has NO number 2
    expect(find.text('Open WhatsApp or any app to start typing in Urdu!'), findsOneWidget);
    expect(find.textContaining('2. Open'), findsNothing);

    // Verify switch keyboard button
    expect(find.text('Switch Keyboard'), findsOneWidget);

    // Verify no RefreshIndicator or refresh icon
    expect(find.byType(RefreshIndicator), findsNothing);
    expect(find.byIcon(Icons.refresh), findsNothing);
  });
}

