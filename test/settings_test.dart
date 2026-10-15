import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:urdu_keyboard/core/localization/app_localizations.dart';
import 'package:urdu_keyboard/core/theme/app_theme.dart';
import 'package:urdu_keyboard/features/settings/settings_screen.dart';
import 'package:urdu_keyboard/state/app_state.dart';

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    GoogleFonts.config.allowRuntimeFetching = false;
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('SettingsScreen renders without ListTile exceptions and handles inline language dropdown',
      (WidgetTester tester) async {
    final appState = AppState();

    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: appState,
        child: Consumer<AppState>(
          builder: (context, state, _) {
            return MaterialApp(
              locale: Locale(state.selectedLanguage),
              supportedLocales: AppLocalizations.supportedLocales,
              localizationsDelegates: AppLocalizations.localizationsDelegates,
              theme: AppTheme.theme,
              home: const SettingsScreen(),
            );
          },
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Verify title and language dropdown exist
    expect(find.text('سیٹنگز'), findsOneWidget);
    expect(find.byType(DropdownButton<String>), findsOneWidget);

    // Verify text size functionality is removed
    expect(find.text('Text Size'), findsNothing);
    expect(find.text('Small'), findsNothing);
    expect(find.text('Large'), findsNothing);

    // Tap dropdown and switch language to English inline
    await tester.tap(find.byType(DropdownButton<String>));
    await tester.pumpAndSettle();

    final englishItemFinder = find.text('English').last;
    expect(englishItemFinder, findsOneWidget);
    await tester.tap(englishItemFinder);
    await tester.pumpAndSettle();

    // Verify language changed to English inline without opening new screen
    expect(appState.selectedLanguage, 'en');
    expect(find.text('Settings'), findsOneWidget);
    expect(find.text('General Preferences'), findsOneWidget);
    expect(find.text('Guides & Setup'), findsOneWidget);
    expect(find.text('About & Support'), findsOneWidget);

    // Verify More Apps, Rate, Share, and Privacy Policy exist
    expect(find.text('Rate on Google Play'), findsOneWidget);
    expect(find.text('Share App'), findsOneWidget);
    expect(find.text('More Apps'), findsOneWidget);
    expect(find.text('Privacy Policy'), findsOneWidget);

    // Verify raw URL is not shown in tile subtitle
    expect(find.textContaining('https://docs.google.com'), findsNothing);

    // Verify Version tile is removed
    expect(find.textContaining('Version 1.0.0'), findsNothing);
  });
}
