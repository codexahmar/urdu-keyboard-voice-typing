import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:urdu_keyboard/core/localization/app_localizations.dart';
import 'package:urdu_keyboard/core/theme/app_theme.dart';
import 'package:urdu_keyboard/features/onboarding/onboarding_screen.dart';
import 'package:urdu_keyboard/state/app_state.dart';

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    GoogleFonts.config.allowRuntimeFetching = false;
    SharedPreferences.setMockInitialValues({});
    const channel = MethodChannel('com.codexahmar.urdu_keyboard/keyboard');
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      return true;
    });
  });

  testWidgets('OnboardingScreen renders and navigates cleanly',
      (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.0;

    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });

    final appState = AppState();
    bool completed = false;

    await tester.pumpWidget(
      ChangeNotifierProvider<AppState>.value(
        value: appState,
        child: MaterialApp(
          locale: const Locale('ur'),
          supportedLocales: AppLocalizations.supportedLocales,
          localizationsDelegates: AppLocalizations.localizationsDelegates,
          theme: AppTheme.theme,
          home: OnboardingScreen(
            onComplete: () {
              completed = true;
            },
          ),
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Verify Urdu title is present on Page 1
    expect(find.text('انگلش میں لکھیں، خودکار اردو بن جائے گی'), findsOneWidget);

    // Verify Directionality is LTR even when locale is Urdu
    expect(
      Directionality.of(tester.element(find.text('انگلش میں لکھیں، خودکار اردو بن جائے گی'))),
      TextDirection.ltr,
    );

    // Verify Skip button is present
    expect(find.text('اسکپ'), findsOneWidget);

    // Tap Next to navigate to Page 2 (Voice Typing)
    final nextFinder = find.text('نیکسٹ');
    expect(nextFinder, findsOneWidget);
    await tester.tap(nextFinder);
    await tester.pumpAndSettle();

    // Verify Page 2 content
    expect(find.text('بول کر اردو لکھیں (وائس ٹائپنگ)'), findsOneWidget);

    // Tap Next to navigate to Page 3 (Enable Keyboard)
    await tester.tap(nextFinder);
    await tester.pumpAndSettle();

    // Verify Page 3 content
    expect(find.text('مرحلہ 1: سیٹنگز میں کی بورڈ آن کریں'), findsOneWidget);
    expect(find.text('سیٹنگز کھولیں'), findsOneWidget);

    // Tap Next to navigate to Page 4 (Select Keyboard & Tips)
    await tester.tap(nextFinder);
    await tester.pumpAndSettle();

    // Verify Page 4 content: text conversion TextField removed, tip card present
    expect(find.text('مرحلہ 2: کی بورڈ سلیکٹ کریں اور شروع کریں'), findsOneWidget);
    expect(find.byType(TextField), findsNothing);
    expect(find.text('اہم ٹپ'), findsOneWidget);
    expect(find.text('شروع کریں'), findsOneWidget);

    // Tap "شروع کریں" to complete
    await tester.tap(find.text('شروع کریں'));
    await tester.pumpAndSettle();

    expect(completed, isTrue);
  });

  testWidgets('OnboardingScreen switches language to English dynamically',
      (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.0;

    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });

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
              home: OnboardingScreen(onComplete: () {}),
            );
          },
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Starts in Urdu
    expect(find.text('انگلش میں لکھیں، خودکار اردو بن جائے گی'), findsOneWidget);

    // Switch state to English
    await appState.setSelectedLanguage('en');
    await tester.pumpAndSettle();

    // Verify content dynamically updated to English
    expect(find.text('Type Urdu Using English (Phonetic)'), findsOneWidget);
    expect(find.text('Skip'), findsOneWidget);
    expect(find.text('Next'), findsOneWidget);
  });

  testWidgets('OnboardingScreen 1-tap language switch pill works via UI tap',
      (WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.0;

    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });

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
              home: OnboardingScreen(onComplete: () {}),
            );
          },
        ),
      ),
    );

    await tester.pumpAndSettle();

    // Default is Urdu
    expect(find.text('انگلش میں لکھیں، خودکار اردو بن جائے گی'), findsOneWidget);

    // Tap "English" on the top bar segmented pill
    await tester.tap(find.text('English'));
    await tester.pumpAndSettle();

    // Now in English
    expect(find.text('Type Urdu Using English (Phonetic)'), findsOneWidget);

    // Tap "اردو" to switch back
    await tester.tap(find.text('اردو'));
    await tester.pumpAndSettle();

    // Back in Urdu
    expect(find.text('انگلش میں لکھیں، خودکار اردو بن جائے گی'), findsOneWidget);
  });
}
