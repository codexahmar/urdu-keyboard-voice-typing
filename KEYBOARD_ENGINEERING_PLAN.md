# Urdu Keyboard — Flutter + Native Kotlin Engineering Specification

## Project Goal

This project is an Android Urdu keyboard application built using a **Flutter UI layer and a native Kotlin keyboard engine**.

The Flutter application is responsible for:

* Onboarding
* Settings
* Language selection
* Keyboard configuration
* Keyboard preview
* Voice typing instructions
* User preferences

The native Kotlin side is responsible for:

* Android `InputMethodService`
* Keyboard rendering
* Key input
* Text insertion
* Backspace
* Enter
* Shift
* Language switching
* Emoji keyboard
* Word suggestions
* Transliteration
* Voice typing
* Keyboard performance
* Android-specific functionality

The primary goal of this development phase is to make the keyboard feel **fast, responsive, lightweight, reliable, and natural to use**, similar to the responsiveness users expect from modern Android keyboards.

---

# Important Instructions for AI / Antigravity

This README is a **living engineering specification**.

Before making any changes:

1. Read this entire README.
2. Read `CHANGELOG.md` if it exists.
3. Inspect the complete Flutter and Kotlin implementation.
4. Understand the existing architecture before modifying it.
5. Identify redundant, duplicated, inefficient, or broken code.
6. Do not blindly rewrite working code.
7. Preserve existing functionality unless it conflicts with the requirements in this document.
8. Prefer simple, maintainable solutions over unnecessarily complex architectures.
9. Avoid introducing unnecessary dependencies.
10. Do not duplicate functionality that already exists.
11. After completing a task, update `CHANGELOG.md`.
12. Mark completed requirements clearly.
13. If a requirement cannot be fully implemented, document:

* What was attempted
* Why it could not be completed
* What remains
* What should be done next

### Important

Do not assume that something works because the code compiles.

The keyboard must be evaluated from the perspective of an actual user:

* How quickly does a key respond?
* Does typing feel instantaneous?
* Does the keyboard lag during rapid typing?
* Does backspace respond immediately?
* Does language switching feel instant?
* Does the keyboard remain responsive during suggestions?
* Does voice typing continue naturally?
* Does the keyboard behave correctly during fast typing?
* Are there crashes, race conditions, or memory leaks?

---

# Development Strategy

Work in this order:

### Phase 1 — Audit

Inspect the complete project.

Identify:

* Redundant Kotlin code
* Duplicate classes
* Unused functions
* Unused imports
* Unused dependencies
* Inefficient loops
* Unnecessary object creation
* Main-thread blocking operations
* Excessive view recreation
* Unnecessary Android IPC
* Slow text processing
* Inefficient suggestion generation
* Memory leaks
* Lifecycle problems
* Coroutine misuse
* Threading problems
* Flutter ↔ Kotlin communication problems
* Voice recognition issues
* Emoji implementation problems
* Keyboard layout problems

Do not start with a large rewrite.

First understand the existing implementation.

---

# Phase 2 — Keyboard Performance

The keyboard must feel extremely responsive.

The target experience is:

> User presses a key → visual feedback appears immediately → character is inserted immediately.

Avoid noticeable delays between:

* Touch
* Key visual feedback
* Key processing
* Text insertion

## Performance requirements

### Key input

Key input should:

* Process immediately
* Avoid unnecessary allocations
* Avoid expensive calculations on every key press
* Avoid unnecessary Flutter communication
* Avoid blocking the main/UI thread
* Reuse keyboard objects where possible
* Avoid recreating the entire keyboard for every key press

### Text insertion

Use Android's `InputConnection` efficiently.

Avoid:

* Recreating `InputConnection`
* Excessive `getTextBeforeCursor()` calls
* Excessive `getTextAfterCursor()` calls
* Large text retrieval operations for every keystroke
* Unnecessary text transformations
* Blocking operations before committing text

Text should be committed as directly as possible.

### Backspace

Backspace must feel instantaneous.

Optimize:

* Single-character deletion
* Long-press deletion
* Repeated deletion
* Word deletion where supported

Do not perform expensive suggestion calculations before processing backspace.

---

# Phase 3 — Threading

The keyboard UI thread must remain lightweight.

Never perform expensive operations directly inside key event handling.

Potentially expensive operations include:

* Dictionary lookup
* Suggestion generation
* Transliteration
* Fuzzy matching
* Voice processing
* Large text processing
* File/database operations
* Network operations

Use appropriate background execution where necessary.

However:

> Do not move tiny operations to background threads unnecessarily.

The goal is low latency, not excessive concurrency.

Avoid creating a new thread/coroutine for every key press.

Use reusable coroutine scopes, executors, or appropriate lifecycle-aware mechanisms.

---

# Phase 4 — Keyboard Layout

The keyboard should use a **familiar modern Android keyboard layout** so users can rely on existing muscle memory.

The layout should follow the standard arrangement users expect from keyboards such as Gboard:

* Familiar QWERTY letter positions
* Familiar number/symbol access
* Familiar backspace position
* Familiar enter/return position
* Familiar shift position
* Familiar spacebar placement
* Familiar emoji access
* Familiar language-switching behavior
* Familiar punctuation placement
* Familiar bottom-row structure

The goal is:

> A user who already knows Gboard should immediately understand this keyboard without relearning key positions.

### Important

Match the **functional layout, key positioning, interaction patterns, and muscle-memory expectations**.

Do not copy proprietary Gboard assets, branding, logos, or proprietary implementation.

The keyboard should have its own visual identity while maintaining a familiar layout.

---

# Phase 5 — Key Mapping

Review every key mapping.

Verify:

* Urdu characters
* English characters
* Arabic characters
* Numbers
* Punctuation
* Symbols
* Shift
* Caps behavior
* Backspace
* Enter
* Space
* Emoji
* Language switch
* Keyboard settings

Every key must insert the expected character.

Test:

* Normal tap
* Rapid tapping
* Long press
* Repeated tap
* Shift + key
* Language switching
* Backspace
* Enter
* Space

No key should produce unexpected characters.

---

# Phase 6 — Urdu Transliteration

Implement a professional transliteration system.

Example:

```text
salam → سلام
assalam → السلام
assalamualaikum → السلام علیکم
allah → اللہ
muhammad → محمد
dua → دعا
quran → قرآن
masjid → مسجد
namaz → نماز
roza → روزہ
```

These are only examples.

The system should support a broad Urdu transliteration vocabulary rather than hardcoding only the examples above.

## Important behavior

The user should be able to type Urdu phonetically using English characters.

For example:

```text
mera naam ahmar hai
```

should produce an appropriate Urdu suggestion such as:

```text
میرا نام احمر ہے
```

The system should understand common transliteration patterns and variations.

For example:

```text
salam
salaam
salaam
```

should be handled intelligently.

Do not implement this as a massive chain of `if/else` statements.

Use a maintainable dictionary/tokenization approach.

---

# Phase 7 — Word Suggestions

Implement a proper suggestion system.

The keyboard should provide suggestions while the user types.

Example:

```text
mera
```

Suggestions might include:

```text
میرا
میری
میرے
```

Another example:

```text
salam
```

Suggestions could include:

```text
سلام
السلام
السلام علیکم
```

## Requirements

Suggestions should:

* Appear quickly
* Not block typing
* Update as the user types
* Respect the current language
* Handle Urdu
* Handle English
* Handle Arabic
* Work with transliteration
* Prioritize relevant words
* Avoid obviously incorrect suggestions
* Avoid excessive memory usage

Do not make suggestions so computationally expensive that typing becomes slower.

### Critical rule

**Typing responsiveness has priority over suggestion generation.**

If suggestion generation is still processing while the user presses another key, the user must still be able to type normally.

---

# Phase 8 — Emoji Keyboard

Use a reliable emoji library/package on the Kotlin/Android side where appropriate.

The emoji keyboard should provide access to a comprehensive set of modern Unicode emojis supported by the Android environment.

Requirements:

* Emoji categories
* Recently used emojis
* Smileys
* People
* Animals
* Food
* Travel
* Activities
* Objects
* Symbols
* Flags

Emoji selection should insert emojis through `InputConnection`.

Avoid manually maintaining a huge emoji list unless required by the selected library.

The implementation should be:

* Fast
* Memory-conscious
* Easy to update
* Compatible with modern Android versions

---

# Phase 9 — Voice Typing

Voice typing needs significant improvement.

The current behavior stops too quickly when the user pauses.

This must be changed.

## Desired behavior

Voice typing should remain active while the user is naturally speaking.

If the user:

* Pauses
* Takes a breath
* Thinks
* Briefly stops speaking

the system should **not immediately terminate the voice input session**.

Use appropriate Android speech recognition APIs and configuration to provide a more natural continuous dictation experience.

### Important

Android speech recognition services have their own limitations and timeout behavior.

Do not implement a fake infinite recognition session.

Instead:

* Handle partial results
* Handle final results
* Detect temporary pauses
* Restart recognition when appropriate
* Preserve already recognized text
* Avoid duplicate text
* Avoid visible interruptions
* Keep the microphone state clear to the user

The user should experience this as one continuous voice typing session.

---

# Phase 10 — Voice Recognition Accuracy

Voice typing currently produces incorrect words in some cases.

Investigate the entire pipeline.

Review:

* Recognition language
* Locale
* Partial results
* Final results
* Audio configuration
* Recognition restart behavior
* Text normalization
* Urdu language support
* Microphone lifecycle
* Duplicate result handling

Use the correct Urdu locale where supported.

Do not silently replace recognized words with random dictionary values.

Corrections should be context-aware.

---

# Phase 11 — Natural Punctuation

Voice typing should produce readable sentences.

The current behavior may produce:

```text
میرا نام احمر ہے میں آج یونیورسٹی گیا
```

Instead of a completely flat transcription, the system should intelligently handle punctuation where recognition support allows it.

Desired result:

```text
میرا نام احمر ہے۔ میں آج یونیورسٹی گیا۔
```

Potential punctuation:

* `۔`
* `,`
* `؟`
* `!`
* `:`
* `;`

For English:

```text
My name is Ahmar. I went to university today.
```

Do not blindly add punctuation after every phrase.

Use the speech recognition capabilities available on the device/API first.

If punctuation post-processing is required, implement conservative rules.

Never make punctuation correction so aggressive that it changes the user's intended meaning.

---

# Phase 12 — Flutter ↔ Kotlin Communication

Review the Platform Channel implementation.

The Flutter side should communicate with Kotlin only when necessary.

Avoid sending every keystroke through Flutter.

### Important principle

The actual keyboard input loop should remain **native Kotlin**.

Flutter should not sit in the critical path of:

```text
Touch → Key → Text insertion
```

The preferred flow is:

```text
User Touch
    ↓
Native Kotlin Keyboard
    ↓
Key Processing
    ↓
InputConnection
    ↓
Target Application
```

Flutter should primarily handle:

```text
Settings
   ↓
Platform Channel
   ↓
Native Keyboard Configuration
```

Examples:

* Selected language
* Theme
* Text size
* Keyboard preferences
* User configuration

This reduces IPC overhead and improves responsiveness.

---

# Phase 13 — Flutter UI Synchronization

The Flutter UI has already been updated.

Now review the Kotlin implementation against the current Flutter UI.

Make sure:

* Language settings match
* Theme settings match
* Text-size settings match
* Keyboard configuration matches
* Platform Channel method names match
* Method arguments match
* Kotlin handlers exist for every required Flutter method
* Flutter does not call obsolete native methods
* Kotlin does not expose obsolete methods

Remove obsolete Flutter ↔ Kotlin communication code.

Do not keep compatibility code unless it is actually required.

---

# Phase 14 — Code Cleanup

Clean the Kotlin codebase thoroughly.

Remove:

* Unused imports
* Unused variables
* Dead functions
* Duplicate functions
* Duplicate constants
* Duplicate layouts
* Unused classes
* Commented-out production code
* Old implementations
* Debug logging that is no longer required
* Temporary workarounds
* Unnecessary dependencies

Improve:

* Naming
* Class responsibilities
* Function size
* Error handling
* Lifecycle management
* Coroutine usage
* Memory management

Do not sacrifice readability for micro-optimizations.

---

# Phase 15 — Lifecycle & Stability

Review the complete `InputMethodService` lifecycle.

Verify correct handling of:

* `onCreate()`
* `onStartInput()`
* `onStartInputView()`
* `onFinishInput()`
* `onFinishInputView()`
* `onDestroy()`

Ensure:

* No leaked references
* No stale `InputConnection`
* No orphaned coroutine jobs
* No microphone resources left running
* No unnecessary keyboard recreation
* No crashes when switching applications
* No crashes when switching languages
* No crashes when opening emoji keyboard
* No crashes during voice typing

---

# Phase 16 — Memory Optimization

The keyboard is expected to run continuously.

Therefore memory usage must be minimized.

Avoid:

* Loading huge datasets into memory unnecessarily
* Recreating dictionaries
* Recreating emoji lists
* Recreating views unnecessarily
* Keeping references to destroyed activities/views
* Memory-heavy caches without limits

Prefer:

* Lazy initialization
* Reusable objects
* Appropriate caching
* Immutable data where practical
* Lifecycle-aware cleanup

---

# Phase 17 — Error Handling

The keyboard must fail gracefully.

Handle:

* Missing `InputConnection`
* Speech recognition failure
* Speech recognition timeout
* Unsupported locale
* Missing emoji support
* Invalid configuration
* Platform Channel errors
* Keyboard lifecycle changes
* Target application changes

The keyboard should never crash because a feature fails.

For example:

If voice recognition fails:

```text
Voice typing unavailable
```

should be handled gracefully instead of crashing the keyboard service.

---

# Phase 18 — Testing

After implementation, test the keyboard manually.

## Typing Performance

Test:

* Slow typing
* Fast typing
* Extremely fast typing
* Repeated letters
* Continuous sentences
* Rapid backspace
* Long press
* Shift
* Numbers
* Symbols

## Language

Test:

* Urdu
* English
* Arabic
* Urdu transliteration
* Language switching

## Suggestions

Test:

```text
salam
salaam
allah
muhammad
mera
meri
mere
aap
ap
ka
ki
ke
```

Also test normal English words.

## Voice

Test:

* Short sentence
* Long sentence
* Pause
* Breath
* Multiple sentences
* Urdu
* English
* Mixed Urdu/English
* Background noise
* Recognition failure
* Restart after timeout

## Emoji

Test:

* Every category
* Recent emojis
* Emoji insertion
* Switching between emoji and letters
* Backspace after emoji
* Emoji keyboard reopening

---

# Performance Targets

These are engineering goals rather than promises that must be achieved on every device.

### Key response

Target:

**< 16 ms for normal key interaction where practical**

The keyboard should feel immediate at 60 FPS.

### Main thread

Avoid expensive work on the main/UI thread.

### Suggestion generation

Suggestions should not visibly delay text insertion.

### Voice

Recognition should feel continuous and should not unnecessarily stop after short pauses.

### Memory

Avoid unnecessary persistent allocations and large in-memory datasets.

---

# Architecture Target

The desired architecture is:

```text
                         ┌──────────────────────┐
                         │      Flutter UI      │
                         │                      │
                         │ Settings             │
                         │ Onboarding           │
                         │ Language             │
                         │ Preferences          │
                         └──────────┬───────────┘
                                    │
                           Platform Channels
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Native Kotlin     │
                         │                      │
                         │ InputMethodService   │
                         │ Keyboard UI          │
                         │ Key Processing       │
                         │ InputConnection      │
                         │ Suggestions          │
                         │ Transliteration      │
                         │ Emoji                │
                         │ Voice Typing         │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   Android System     │
                         │                      │
                         │ Input Framework      │
                         │ Speech Recognition   │
                         └──────────────────────┘
```

---

# Critical Performance Rule

The native keyboard must remain independent from Flutter during actual typing.

Do NOT create this architecture:

```text
Key Press
   ↓
Flutter
   ↓
Platform Channel
   ↓
Kotlin
   ↓
InputConnection
```

Prefer:

```text
Key Press
   ↓
Kotlin
   ↓
InputConnection
```

Flutter should configure the keyboard, not process every key.

---

# Implementation Priorities

Implement improvements in this order:

## Priority 1 — Critical

* [ ] Audit entire Kotlin keyboard
* [ ] Fix crashes
* [ ] Fix lifecycle issues
* [ ] Fix key mappings
* [ ] Fix InputConnection handling
* [ ] Remove blocking operations
* [ ] Optimize key response
* [ ] Remove unnecessary Flutter communication

## Priority 2 — Core Keyboard

* [ ] Standard familiar keyboard layout
* [ ] Correct Urdu mappings
* [ ] English mappings
* [ ] Arabic mappings
* [ ] Shift
* [ ] Backspace
* [ ] Enter
* [ ] Space
* [ ] Numbers
* [ ] Symbols
* [ ] Language switching

## Priority 3 — Intelligence

* [ ] Urdu transliteration
* [ ] Word suggestions
* [ ] Context-aware suggestions
* [ ] Common Urdu vocabulary
* [ ] English suggestions
* [ ] Arabic suggestions

## Priority 4 — Emoji

* [ ] Integrate emoji library
* [ ] Emoji categories
* [ ] Recent emojis
* [ ] Emoji insertion
* [ ] Backspace support
* [ ] Performance optimization

## Priority 5 — Voice

* [ ] Continuous voice typing
* [ ] Better pause handling
* [ ] Partial results
* [ ] Final results
* [ ] Recognition restart
* [ ] Urdu locale
* [ ] English locale
* [ ] Better error handling
* [ ] Natural punctuation
* [ ] Duplicate result prevention

## Priority 6 — Cleanup

* [ ] Remove redundant Kotlin code
* [ ] Remove obsolete Flutter code
* [ ] Remove unused dependencies
* [ ] Improve naming
* [ ] Improve architecture
* [ ] Improve error handling
* [ ] Improve documentation

## Priority 7 — Validation

* [ ] Rapid typing test
* [ ] Backspace test
* [ ] Language test
* [ ] Suggestion test
* [ ] Transliteration test
* [ ] Emoji test
* [ ] Voice test
* [ ] Lifecycle test
* [ ] Memory test
* [ ] Crash test

---

# CHANGELOG.md Requirement

Create a file at the project root:

```text
CHANGELOG.md
```

This file must be maintained throughout development.

The purpose of this file is to provide **persistent project context between AI sessions**.

Before making changes, always read:

```text
README.md
CHANGELOG.md
```

After making changes, update:

```text
CHANGELOG.md
```

---

# CHANGELOG Format

Use this structure:

```markdown
# Changelog

## [Unreleased]

### Completed

- [ ] Example completed task

### In Progress

- [ ] Example task currently being worked on

### Remaining

- [ ] Example remaining task

### Performance

- Key response:
- Suggestion latency:
- Voice recognition:
- Memory:
- Known issues:

### Technical Changes

- Example architecture change

### Files Changed

- `path/to/file.kt`
- `path/to/file.dart`

### Notes

Additional information for the next development session.
```

---

# Changelog Rules

Every completed task must be recorded.

For example:

```markdown
### Completed

- [x] Removed redundant keyboard initialization
- [x] Optimized InputConnection usage
- [x] Added emoji keyboard
- [x] Added Urdu transliteration
- [x] Added word suggestions
```

If something is partially completed:

```markdown
### In Progress

- [ ] Voice recognition continuous mode
  - Partial results implemented
  - Automatic restart implemented
  - Natural punctuation still needs improvement
```

If something is blocked:

```markdown
### Blocked

- Voice punctuation quality depends on the Android speech recognition service.
- Further testing is required across different Android devices.
```

---

# AI Session Workflow

Every future AI development session should follow this workflow.

## Step 1

Read:

```text
README.md
CHANGELOG.md
```

## Step 2

Determine:

* What is already completed?
* What is currently in progress?
* What remains?
* What known issues exist?

## Step 3

Inspect the actual source code.

Do not trust the changelog blindly.

Verify that the implementation actually matches the documented status.

## Step 4

Choose the highest-priority unfinished task.

## Step 5

Implement the smallest clean change that solves the problem.

## Step 6

Test the implementation.

## Step 7

Review for:

* Performance
* Redundant code
* Bugs
* Lifecycle problems
* Memory issues
* Threading problems
* Regression risks

## Step 8

Update `CHANGELOG.md`.

## Step 9

Clearly record:

* What changed
* Why it changed
* Files modified
* Tests performed
* Remaining issues

---

# Definition of Done

A feature is not considered complete simply because the code compiles.

A task is complete only when:

* Implementation exists
* Existing functionality still works
* Code has been reviewed
* Redundant code has been removed where appropriate
* Performance has been considered
* Error handling exists
* Manual testing has been performed
* `CHANGELOG.md` has been updated

---

# Final Product Goal

The final keyboard should feel:

**Fast**

Key presses should feel immediate.

**Natural**

Users should not have to learn a new keyboard layout.

**Intelligent**

Suggestions and transliteration should help rather than interfere.

**Reliable**

Voice typing and keyboard input should not randomly stop or crash.

**Lightweight**

The keyboard should consume reasonable memory and CPU.

**Accessible**

The UI and keyboard should remain easy to use for elderly users.

**Professional**

The final product should feel like a real production Android keyboard rather than a prototype.

---

# Final Instruction

Do not optimize only for code quality.

Optimize for the **actual typing experience**.

A beautifully structured keyboard that feels slow is still a bad keyboard.

The highest priority is:

```text
Responsiveness
    ↓
Reliability
    ↓
Typing Accuracy
    ↓
Natural Interaction
    ↓
Features
    ↓
Code Quality
```

All improvements must preserve the existing Flutter application functionality while making the native Kotlin keyboard faster, cleaner, more reliable, and more natural to use.
