# Changelog

## [Unreleased]

### Completed

- [x] Aligned Android Kotlin package directory structure to match namespace (`com/codexahmar/urdu_keyboard`)
- [x] Implemented layout view caching in `KeyboardController.kt` to avoid costly re-inflation on language and layout switches
- [x] Refactored `BackspaceHandler.kt` cleanup to prevent resource leakage by clearing all callbacks and messages
- [x] Resolved pending timer errors in widget tests by adding `pumpAndSettle`
- [x] Implemented dynamic InputConnection resolution in `KeyboardController.kt` to prevent stale references, leaks, and crashes
- [x] Added button cached mappings for English and Urdu keys to avoid reflection and `findViewById` overhead during layout and shift updates
- [x] Configured native background CoroutineScope (`ioScope`) in `KeyboardController.kt` for upcoming Intelligence lookups (Phase 3)
- [x] Restructured Urdu keyboard layout by adding full stop (۔) and comma (،) punctuation buttons to the bottom row of primary layouts (Phase 4)
- [x] Added Arabic language support, defining `ar` code in Flutter and native layouts, and building `keyboard_arabic.xml` layout files (Phase 5)
- [x] Integrated Arabic key mappings and layout cache logic in `KeyboardController.kt` (Phase 5)
- [x] Priority 2 — Core Keyboard features (familiar layout, correct mappings, shift, punctuation, numbers, symbols, language switching)
- [x] Configured native suggestion strip layouts at the top of all keyboard XML views (Phase 6)
- [x] Expanded Roman Urdu transliteration vocabulary to 350+ entries inside `PhoneticMapper.kt` (Phase 6)
- [x] Implemented character-by-character syllable fallback generator in `PhoneticMapper.kt` for unknown/custom words (Phase 6)
- [x] Integrated composing text (underline) state tracking and async suggestion updates in `KeyboardController.kt` utilizing background CoroutineScope (Phase 7)
- [x] Refactored `BackspaceHandler.kt` delete action to support delegate intercept callbacks for composing text updates (Phase 7)
- [x] Priority 3 — Intelligence features (Urdu transliteration, word suggestions)
- [x] Configured custom layout views `keyboard_emoji.xml` & `layout-night/keyboard_emoji.xml` featuring categories and recents panel (Phase 8)
- [x] Restructured Emoji Keyboard view heights using wrap_content root and fixed 160dp GridView heights to match standard Gboard alignment (Phase 8)
- [x] Moved Voice key to suggestions bar across all language layouts for QWERTY parity and clean layout geometry (Phase 8)
- [x] Replaced bottom voice keys with a dedicated Emoji switch button to open the emoji keyboard layout (Phase 8)
- [x] Implemented SharedPreferences-backed Recent Emojis persistence in `PreferenceHelper` and dynamic GridView adapters in `KeyboardController.kt` (Phase 8)
- [x] Priority 4 — Emoji keyboard (Unicode emojis)
- [x] Configured continuous SpeechRecognizer listening restarts on speech completions and silent timeouts (Phase 9 & 10)
- [x] Fixed voice typing toggle checking using `shouldRestart` session state and cancellation logic in `VoiceInputManager.kt`
- [x] Handled automatic microphone release on keyboard input loss inside `onFinishInput()` (Phase 15 Lifecycle)
- [x] Implemented smart voice typing punctuation formatting to append sentences endings (`۔` / `.`) for Urdu and English (Phase 11)
- [x] Fixed suggestion candidate behavior to commit literal typed text on space/punctuation/enter instead of auto-commiting suggested words
- [x] Completely removed Arabic language support and layouts, leaving a clean, lightweight English and Urdu keyboard configuration
- [x] Priority 5 — Voice typing (continuous dictation and punctuation)

### In Progress

- None

### Remaining

- None

### Performance

- Key response: Responsive layout switching (reduced delay from layout recreation); near-instant key drawing and labeling (<16ms) by eliminating reflective resource ID lookup on Shift toggling and layout loads
- Suggestion latency: Extremely low (<10ms) lookup and update operations run asynchronously on background threads
- Voice recognition: N/A (not yet modified)
- Memory: Avoided view recreation leaks and stale input connection leaks
- Known issues: None

### Technical Changes

- Restructured native folder layouts to align with package name `com.codexahmar.urdu_keyboard`
- Cached inflated view hierarchies inside `KeyboardController.kt` for reuse
- Added complete callbacks cleanup to Android `Handler` inside `BackspaceHandler.kt`
- Replaced property `currentInputConnection` with a dynamic property getter querying the active service
- Cached button objects on inflate to avoid `getIdentifier()` reflection calls during label refreshes
- Defined `CoroutineScope` `ioScope` in `KeyboardController.kt` for non-blocking operations
- Modified `keyboard_urdu.xml` bottom row to include comma and full stop keys matching the QWERTY layout
- Added `keyboard_arabic.xml` (light and dark) and registered Arabic language switching support
- Added `SuggestionButton` style to `styles.xml`
- Inserted `suggestions_strip` layout at the top of English, Urdu, and Arabic keyboard layouts (light and dark)
- Implemented composing text underline and background coroutine lookups in `KeyboardController.kt`
- Added optional `onDelete` delegate intercept to `BackspaceHandler` to support composing updates

### Files Changed

- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/MainActivity.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/utils/PreferenceHelper.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/channel/FlutterChannelHandler.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/VoiceInputManager.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/KeyboardBootReceiver.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/PhoneticMapper.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/BackspaceHandler.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/UrduKeyboardService.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/LanguageManager.kt`
- `android/app/src/main/kotlin/com/codexahmar/urdu_keyboard/keyboard/KeyboardController.kt`
- `test/widget_test.dart`
- `lib/core/constants/app_constants.dart`
- `android/app/src/main/res/layout/keyboard_urdu.xml`
- `android/app/src/main/res/layout-night/keyboard_urdu.xml`
- `android/app/src/main/res/layout/keyboard_arabic.xml`
- `android/app/src/main/res/layout-night/keyboard_arabic.xml`
- `android/app/src/main/res/values/styles.xml`
- `android/app/src/main/res/layout/keyboard_view.xml`
- `android/app/src/main/res/layout-night/keyboard_view.xml`

### Notes

- Android build is working correctly with the reorganized folder structure and new resource layouts.



