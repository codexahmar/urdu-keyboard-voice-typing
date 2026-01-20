# Urdu Smart Keyboard - Flutter UI

A modern, elder-friendly Flutter application for managing an Urdu keyboard on Android. This app provides a clean interface for onboarding, language selection, keyboard preview, and settings management.

## 🎯 Project Overview

This is the **UI-only** Flutter application. The actual keyboard implementation is done separately in native Android (Kotlin) using `InputMethodService`. This app communicates with the native keyboard via Platform Channels.

### Key Features

- ✅ **Multi-step Onboarding** - Guides users through keyboard setup
- ✅ **Language Selection** - Support for Urdu, English, and Arabic
- ✅ **Keyboard Preview** - Visual representation of the keyboard
- ✅ **Voice Typing Guide** - Instructions for voice-to-text
- ✅ **Settings** - Theme, text size, accessibility options
- ✅ **Material 3 Design** - Modern, accessible UI
- ✅ **Elder-Friendly** - Large text, high contrast, simple navigation
- ✅ **Zero Ads** - Clean, distraction-free experience

## 🏗️ Architecture

### Folder Structure

```
lib/
├── core/
│   ├── constants/
│   │   ├── app_constants.dart      # App-wide constants
│   │   └── app_strings.dart        # Localized strings
│   ├── theme/
│   │   ├── app_colors.dart         # Color palette
│   │   └── app_theme.dart          # Material 3 theme
│   └── widgets/
│       ├── action_card.dart        # Reusable action card
│       ├── language_tile.dart      # Language selection tile
│       └── step_indicator.dart     # Onboarding progress
├── features/
│   ├── splash/
│   │   └── splash_screen.dart
│   ├── onboarding/
│   │   └── onboarding_screen.dart  # 4-step onboarding flow
│   ├── home/
│   │   └── home_screen.dart        # Main dashboard
│   ├── language_selection/
│   │   └── language_selection_screen.dart
│   ├── keyboard_preview/
│   │   └── keyboard_preview_screen.dart
│   ├── voice_typing/
│   │   └── voice_typing_guide_screen.dart
│   └── settings/
│       └── settings_screen.dart
├── services/
│   ├── platform_channel_service.dart  # Native Android bridge (STUB)
│   └── preferences_service.dart       # Local storage
├── state/
│   └── app_state.dart                 # Global app state (Provider)
└── main.dart                          # App entry point
```

### State Management

**Provider** is used for state management:

- Simple and suitable for this app size
- Efficient change notifications
- Easy integration with Material 3

### Design Principles

1. **Clean Architecture** - Separation of concerns
2. **Feature-First** - Each feature in its own folder
3. **Reusable Components** - Core widgets shared across features
4. **Material 3** - Modern Android design language
5. **Accessibility First** - Elder-friendly sizing and contrast

## 📱 Screens

### 1. Splash Screen
- Animated app logo
- Auto-navigates after 2 seconds
- Checks onboarding status

### 2. Onboarding Flow (4 Steps)
- **Step 1:** Welcome message
- **Step 2:** Enable keyboard instructions
- **Step 3:** Select default keyboard guide
- **Step 4:** Setup complete

### 3. Home Screen
- Current language display
- Action cards for:
  - Language selection
  - Keyboard preview
  - Voice typing guide
  - Settings (via AppBar)

### 4. Language Selection
- Visual language tiles
- Urdu marked as "Recommended"
- Shows native script and description
- Saves to preferences and native keyboard

### 5. Keyboard Preview
- Static visual keyboard layout
- Phonetic typing guide
- Step-by-step instructions

### 6. Voice Typing Guide
- How-to instructions
- Supported languages
- Tips for better results

### 7. Settings
- **Appearance**
  - Theme mode (Light/Dark/System)
  - Text size slider
- **Accessibility**
  - High contrast mode (placeholder)
  - Haptic feedback (placeholder)
- **About**
  - Version info
  - Developer info
  - Open source licenses

## 🔌 Platform Channels (Preparation)

The `PlatformChannelService` provides stubs for native Android communication:

```dart
// Open Android keyboard settings
await platformService.openKeyboardSettings();

// Check if keyboard is enabled
bool isEnabled = await platformService.isKeyboardEnabled();

// Set active language
await platformService.setKeyboardLanguage('ur');

// Get selected language
String? lang = await platformService.getSelectedLanguage();
```

**Note:** These methods are stubs and will need implementation on the Android side using MethodChannel handlers.

## 🎨 Theme & Design

### Colors
- **Primary:** Green tones (#2E7D32) - Fresh, accessible
- **Secondary:** Teal tones (#00796B)
- **Urdu Accent:** Purple (#7B1FA2)

### Typography
- Large, readable fonts
- Elder-friendly sizing (18-28pt for body/titles)
- Support for Urdu script rendering

### Material 3 Components
- Elevated buttons with large touch targets (56dp min height)
- Rounded cards (16dp radius)
- Soft shadows and elevation
- Segmented buttons for settings

## 📦 Dependencies

```yaml
dependencies:
  provider: ^6.1.1              # State management
  shared_preferences: ^2.2.2    # Local storage
```

## 🚀 Getting Started

### 1. Install Dependencies

```bash
flutter pub get
```

### 2. Run the App

```bash
flutter run
```

### 3. Build for Android

```bash
flutter build apk --release
```

## 🔧 Configuration

### Onboarding Reset

To reset onboarding (for testing):

```dart
final prefs = PreferencesService();
await prefs.init();
await prefs.setOnboardingComplete(false);
```

### Change Default Language

Edit `app_constants.dart`:

```dart
String getSelectedLanguage() {
  return _prefs.getString(_selectedLanguageKey) ?? 'ur'; // Change 'ur' to 'en' or 'ar'
}
```

## 🔮 Next Steps (Native Integration)

To complete the keyboard functionality, implement in Android (Kotlin):

1. **Create InputMethodService**
   - Extend `InputMethodService`
   - Implement keyboard view
   - Handle key events

2. **Setup MethodChannel Handler**
   - Listen for Flutter method calls
   - Implement platform-specific logic
   - Return results to Flutter

3. **Manifest Configuration**
   - Register InputMethodService
   - Add permissions
   - Configure keyboard settings activity

## 📝 Code Standards

- **Dart style guide** followed
- **Material 3 guidelines** implemented
- **Accessibility** prioritized
- **Comments** on complex logic
- **const constructors** where possible

## 🌐 Localization (Future)

Currently uses inline English and Urdu strings. For full localization:

1. Use `flutter_localizations`
2. Create `.arb` files
3. Generate localization classes
4. Update `MaterialApp` with `localizationsDelegates`

## 🧪 Testing (Future Enhancement)

Add tests for:
- Widget tests for UI components
- Unit tests for state management
- Integration tests for navigation flow

## 📄 License

This project is for educational/personal use. Update license as needed.

## 👤 Developer

Created by: Ahmaryar Khan
Version: 1.0.0

---

**Built with ❤️ for the Urdu-speaking community**
