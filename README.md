# Urdu Smart Keyboard

A production-grade **Flutter + Native Android (Kotlin) hybrid application** designed to provide a modern, accessible, and intelligent Urdu keyboard experience for Android users.

This project demonstrates advanced mobile engineering capabilities, including **cross-platform UI development, native Android integration, and real-world system architecture design**.

> **Live on Google Play** — The application is publicly available and actively deployed.

---

## Project Overview

Urdu Smart Keyboard is a **hybrid mobile system** composed of:

* A **Flutter-based application layer** for UI, onboarding, and user configuration
* A **native Android keyboard engine (Kotlin)** built using `InputMethodService`
* A **communication bridge using Platform Channels** to connect both layers

This architecture allows Flutter to handle **user experience and state management**, while Kotlin handles **low-level system keyboard operations**, which Flutter cannot directly access.

This project reflects real-world mobile engineering practices used in production-grade applications.

---

## Engineering Highlights

This project demonstrates strong proficiency in:

### Cross-Platform + Native Integration

* Flutter ↔ Kotlin communication using Platform Channels
* Bi-directional messaging between UI layer and system keyboard service
* Clean separation between presentation layer and OS-level functionality

### Native Android System Development

* Custom keyboard built using `InputMethodService`
* Direct integration with Android input framework
* System-level keyboard lifecycle management

### Scalable Architecture Design

* Feature-first modular structure
* Separation of core, features, services, and state layers
* Maintainable and production-ready code organization

### UI/UX Engineering

* Material 3 design implementation
* Elder-friendly accessibility system (large text, high contrast, simplified flows)
* Multi-step onboarding experience optimized for non-technical users

### State & Data Management

* Provider-based state management
* Persistent local storage using SharedPreferences
* Clean separation of UI and business logic

---

## Key Features

* Multi-step onboarding system with guided setup flow
* Multi-language support (Urdu, English, Arabic)
* Keyboard preview and usage guidance system
* Voice typing instruction module
* Theme system (Light / Dark / System)
* Adjustable accessibility text scaling
* Elder-friendly UX design principles
* Zero-ad experience (clean production UI)
* Native Android keyboard integration (Kotlin)
* Real-time Flutter ↔ Android communication

---

## System Architecture

### High-Level Architecture

```text
┌──────────────────────────────┐
│        Flutter App           │
│                              │
│  UI Layer                    │
│  Onboarding Flow             │
│  Settings & Preferences      │
│  State Management (Provider) │
└──────────────┬───────────────┘
               │
     Platform Channels (IPC)
               │
               ▼
┌──────────────────────────────┐
│     Native Android Layer     │
│          (Kotlin)            │
│                              │
│  InputMethodService          │
│  Keyboard Engine             │
│  System Input Handling       │
└──────────────────────────────┘
```

---

## Project Structure

```text
lib/
├── core/
│   ├── constants/
│   ├── theme/
│   └── widgets/
├── features/
│   ├── splash/
│   ├── onboarding/
│   ├── home/
│   ├── language_selection/
│   ├── keyboard_preview/
│   ├── voice_typing/
│   └── settings/
├── services/
│   ├── platform_channel_service.dart
│   └── preferences_service.dart
├── state/
│   └── app_state.dart
└── main.dart
```

This structure follows a **feature-first scalable architecture**, making the codebase easy to extend, test, and maintain.

---

## Flutter + Kotlin Integration

A key highlight of this project is the **hybrid architecture combining Flutter and native Android**.

### Why this approach?

Flutter alone cannot implement system-level keyboard functionality. Therefore:

* Flutter handles UI, onboarding, and configuration
* Kotlin handles keyboard engine and system input
* Platform Channels act as a communication bridge

This enables:

* Real-time language switching
* Keyboard configuration updates from Flutter
* Native system-level input handling

---

## State Management

* Provider is used for scalable and reactive state management
* Ensures separation of UI and business logic
* Lightweight and production-appropriate for this architecture

---

## Design Principles

* Clean Architecture with strict separation of concerns
* Feature-first modular design
* Accessibility-driven UX for elderly users
* Native-first integration using Android system APIs
* Production-ready code quality and maintainability

---

## Application Modules

### Onboarding System

Guided multi-step setup flow for first-time users.

### Home Dashboard

Central navigation hub for all keyboard features.

### Language Management

Dynamic language selection with native script support.

### Keyboard Preview

Visual representation and usage guidance.

### Voice Typing Guide

Instructional module for voice input usage.

### Settings System

* Theme control
* Accessibility scaling
* App configuration

---

## Dependencies

```yaml
dependencies:
  provider:
  shared_preferences:
```

---

## Getting Started

### Requirements

* Flutter SDK
* Android Studio
* Kotlin support enabled
* Android device or emulator

### Setup

```bash
git clone https://github.com/codexahmar/urdu-keyboard-voice-typing
flutter pub get
flutter run
```

Note: Full functionality requires the native Android Kotlin keyboard module.

---

## Deployment

The application is successfully deployed on the Google Play Store, demonstrating real-world production readiness.

Google Play Store:
https://play.google.com/store/apps/details?id=com.codexahmar.urdu_keyboard

---

## Developer

Ahmaryar Khan
Flutter & Android Engineer

Version: 1.0.0

---

## Summary

This project demonstrates the ability to:

* Build production-level Flutter applications
* Integrate with native Android systems using Kotlin
* Design scalable mobile architectures
* Implement real-world cross-platform communication systems
* Deliver user-focused, accessible mobile experiences

---

Built with engineering precision for real-world mobile systems.
