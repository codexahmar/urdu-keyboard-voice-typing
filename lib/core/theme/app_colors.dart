import 'package:flutter/material.dart';

/// Single unified Pakistan Flag Green & White color palette.
/// No dark/light duality - strictly pure white and authentic Pakistani flag green (#01411C).
class AppColors {
  // Official Pakistan Flag Green & Accents
  static const Color primary = Color(0xFF01411C); // Authentic Pakistan Flag Green
  static const Color primaryLight = Color(0xFF0B6623); // Vibrant Emerald Accent
  static const Color primaryDark = Color(0xFF002B11); // Deep Forest Green

  // Green Tints & Soft Surfaces
  static const Color greenLight = Color(0xFFEBF5EC); // Soft tint for selected chips/badges
  static const Color greenSubtle = Color(0xFFF2F8F3); // Gentle highlight tint
  static const Color greenBorder = Color(0xFFD2E4D5); // Green-tinted border

  // Pure White & Clean Surfaces
  static const Color white = Color(0xFFFFFFFF);
  static const Color background = Color(0xFFFFFFFF); // Pure white for the entire app
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceElevated = Color(0xFFFFFFFF);
  static const Color divider = Color(0xFFE8EFE8);
  static const Color border = Color(0xFFDFEADE);

  // Text Colors (High Contrast & Clear Readability)
  static const Color textPrimary = Color(0xFF0C1D10); // Deep charcoal-forest
  static const Color textSecondary = Color(0xFF384D3C); // Medium muted forest
  static const Color textMuted = Color(0xFF6E8372); // Subtle grey-green
  static const Color textWhite = Color(0xFFFFFFFF); // White text on green buttons

  // Status & Feedback
  static const Color success = Color(0xFF01411C);
  static const Color error = Color(0xFFC62828);
  static const Color warning = Color(0xFFE65100);
  static const Color disabled = Color(0xFFC7D3C9);

  // Keyboard Preview Specific
  static const Color keyBackground = Color(0xFFFFFFFF);
  static const Color keySpecialBackground = Color(0xFFE8F3E9);
  static const Color keyboardBackground = Color(0xFFEDF3ED);

  // Flag Gradient for prominent hero cards
  static const List<Color> heroGradient = [
    Color(0xFF01411C),
    Color(0xFF0B6623),
  ];
}
