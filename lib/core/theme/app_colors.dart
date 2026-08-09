import 'package:flutter/material.dart';

/// Modern Material 3 Color Scheme for Urdu Smart Keyboard
/// Designed for high readability, accessibility, and visual elegance
class AppColors {
  // Brand Colors - Premium Emerald Forest Palette
  static const Color primary = Color(0xFF0F5A2C); // Emerald Green (Strong & Accessible)
  static const Color primaryLight = Color(0xFF2E8B4F); // Vibrant Green
  static const Color primaryDark = Color(0xFF083B1C); // Deep Pine Green

  // Secondary/Accent Colors - Teal/Mint Fresh Tones
  static const Color secondary = Color(0xFF00796B); // Classic Teal
  static const Color secondaryLight = Color(0xFF33AB9F); // Bright Mint/Teal
  static const Color secondaryDark = Color(0xFF004D40); // Deep Teal

  // Urdu Accent Colors - Luxurious Purple Royal Tones
  static const Color urduAccent = Color(0xFF7B1FA2); // Royal Amethyst Purple
  static const Color urduAccentLight = Color(0xFFAB47BC); // Soft Amethyst
  static const Color urduAccentDark = Color(0xFF4A148C); // Deep Velvet Purple

  // Background and Surface Palettes
  static const Color background = Color(0xFFF8F9FA); // Ultra-light cool grey
  static const Color surface = Color(0xFFFFFFFF); // Pure white
  static const Color surfaceVariant = Color(0xFFF1F3F5); // Soft satin grey
  static const Color divider = Color(0xFFE9ECEF); // Light border divider

  // Dark Mode Palettes
  static const Color backgroundDark = Color(0xFF121416); // True deep charcoal
  static const Color surfaceDark = Color(0xFF1A1D20); // Premium slate dark grey
  static const Color surfaceVariantDark = Color(0xFF262A2E); // Lighter dark satin grey
  static const Color dividerDark = Color(0xFF2D3238); // Dark border divider

  // High Contrast Text Colors
  static const Color textPrimary = Color(0xFF1F2937); // Deep Charcoal Slate
  static const Color textSecondary = Color(0xFF4B5563); // Muted Cool Grey
  static const Color textMuted = Color(0xFF9CA3AF); // Quiet Grey
  static const Color textDisabled = Color(0xFFD1D5DB); // Disabled Key Grey

  static const Color textPrimaryDark = Color(0xFFF3F4F6); // Soft Ivory White
  static const Color textSecondaryDark = Color(0xFFD1D5DB); // Light Muted Grey
  static const Color textMutedDark = Color(0xFF6B7280); // Quiet Dark Grey

  // Status Colors (Soft and Modern)
  static const Color success = Color(0xFF10B981); // Emerald Green
  static const Color warning = Color(0xFFF59E0B); // Amber Yellow
  static const Color error = Color(0xFFEF4444); // Rose Red
  static const Color info = Color(0xFF3B82F6); // Royal Blue

  // Interactive UI Elements
  static const Color keyboardPreviewBackground = Color(0xFFEFEFEF);
  static const Color keyboardPreviewBackgroundDark = Color(0xFF2A2D30);

  // Gradient helper definitions for modern cards
  static const List<Color> primaryGradient = [
    Color(0xFF0F5A2C),
    Color(0xFF238243),
  ];

  static const List<Color> secondaryGradient = [
    Color(0xFF00796B),
    Color(0xFF009688),
  ];

  static const List<Color> urduGradient = [
    Color(0xFF7B1FA2),
    Color(0xFF9C27B0),
  ];

  static const List<Color> cardGradient = [
    Color(0xFFFFFFFF),
    Color(0xFFF8F9FA),
  ];

  static const List<Color> cardGradientDark = [
    Color(0xFF1A1D20),
    Color(0xFF121416),
  ];
}
