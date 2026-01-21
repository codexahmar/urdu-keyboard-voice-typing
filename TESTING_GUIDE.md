# 🧪 Complete Testing Guide - Urdu Smart Keyboard

## Quick Test Procedure

### ⚡ Fast Track (5 minutes)

```bash
# 1. Build and install
cd "/Users/codexahmar/Desktop/Urdu Keyboard/urdu_keyboard"
flutter build apk --debug
flutter install

# 2. Enable keyboard on device
# Settings → System → Languages & Input → Virtual Keyboard → Manage Keyboards
# ✓ Enable "Urdu Smart Keyboard"

# 3. Test in any app
# Open Messages/Notes
# Tap text field → Select keyboard
# Type and test!
```

---

## 📋 Detailed Testing Steps

### Step 1: Build & Deploy

```bash
# Clean build
flutter clean
flutter pub get

# Build APK
flutter build apk --debug

# Install on connected device
flutter install

# OR use Android Studio
# Run → Flutter → Run 'main.dart'
```

**Expected Output:**
```
✓ Built build/app/outputs/flutter-apk/app-debug.apk
✓ Installing build/app/outputs/flutter-apk/app-debug.apk
Launched on [device name]
```

---

### Step 2: Enable Keyboard

**On Your Android Device:**

1. Open **Settings**
2. Navigate: **System** → **Languages & input** → **Virtual keyboard**
3. Tap **Manage keyboards**
4. Find **Urdu Smart Keyboard**
5. Toggle **ON**

**Screenshot verification:**
- You should see "Urdu Smart Keyboard" in the enabled list

---

### Step 3: Set as Default

1. Open any app with text input:
   - **Messages**
   - **Notes**
   - **WhatsApp**
   - **Google Keep**

2. Tap on a text field

3. Tap the **keyboard icon** (🎹) in navigation bar

4. Select **Urdu Smart Keyboard**

**What you'll see:**
- Custom keyboard with white keys
- Language button showing "EN"
- Microphone button
- Space bar
- Backspace and Enter keys

---

### Step 4: Test English Input

**Test Case:** Basic English typing

**Steps:**
1. Ensure language shows "EN"
2. Type: `hello world`

**Expected Result:**
```
Input: hello world
Output: hello world
```

✅ **Pass Criteria:** Text appears exactly as typed

---

### Step 5: Test Urdu Phonetic

**Test Case 1:** Common word conversion

**Steps:**
1. Switch to Urdu (tap language button until you see "اردو")
2. Type: `salam`
3. Press **space**

**Expected Result:**
```
Input: salam [space]
Output: سلام 
```

**Test Case 2:** Multiple words

**Steps:**
1. Type: `pakistan`
2. Press space
3. Type: `zindabad`
4. Press space

**Expected Result:**
```
Input: pakistan zindabad
Output: پاکستان زندہ باد
```

**Test Case 3:** Word list

Try these words:
```
mera → میرا
khuda → خدا
dost → دوست
kitab → کتاب
mohabbat → محبت
```

✅ **Pass Criteria:** All words convert to Urdu script

---

### Step 6: Test Language Switching

**Test Case:** Cycle through languages

**Steps:**
1. Tap **language button** (bottom left)
2. Note the label changes
3. Tap again
4. Tap again

**Expected Sequence:**
```
Press 1: EN → اردو (Urdu)
Press 2: اردو → عربي (Arabic)
Press 3: عربي → EN (English)
```

✅ **Pass Criteria:** Language cycles correctly, toast shows language name

---

### Step 7: Test Voice Input

**Test Case:** English voice input

**Steps:**
1. Switch to English (language button shows "EN")
2. Tap **microphone button** (🎤)
3. Grant microphone permission if prompted
4. Speak: "Hello this is a test"

**Expected Result:**
```
Voice: "Hello this is a test"
Output: Hello this is a test
```

**Test Case:** Urdu voice input

**Steps:**
1. Switch to Urdu (اردو)
2. Tap microphone
3. Speak in Urdu: "سلام دوستوں"

**Expected Result:**
- Urdu text appears in input field

✅ **Pass Criteria:** 
- Microphone icon shows listening state
- Toast appears: "🎤 Listening..."
- Recognized text is inserted
- Toast confirms: "✓ Text inserted"

---

### Step 8: Test Special Keys

**Backspace:**
```
Type: hello
Press backspace 3 times
Result: he
```

**Space:**
```
Type: hello
Press space
Type: world
Result: hello world
```

**Enter:**
```
Type: message
Press enter
Result: Text submitted/new line created
```

✅ **Pass Criteria:** All special keys function correctly

---

### Step 9: Test Platform Channel

**From Flutter App:**

1. Launch the Flutter app
2. Go to **Language Selection** screen
3. Select a language (e.g., Urdu)
4. Tap **Save**

**Expected:**
- Success message appears
- Language is saved

**Verification:**
1. Open keyboard in another app
2. Language should be set to selected language

---

### Step 10: Test Persistence

**Test Case:** Language persists after reboot

**Steps:**
1. Set keyboard language to Urdu
2. Close keyboard
3. Restart device
4. Open keyboard again

**Expected Result:**
- Language is still set to Urdu

✅ **Pass Criteria:** Settings persist across reboots

---

## 🐛 Known Issues & Workarounds

### Issue 1: Keyboard Not Showing

**Symptoms:**
- Keyboard doesn't appear in system settings
- Can't enable keyboard

**Solution:**
```bash
# Rebuild completely
flutter clean
rm -rf build/
flutter pub get
flutter build apk --debug
flutter install
```

**Then:**
- Restart device
- Check Settings again

---

### Issue 2: Voice Input Permission Denied

**Symptoms:**
- Microphone button doesn't work
- No permission dialog

**Solution:**
1. Settings → Apps → Urdu Keyboard
2. Permissions → Microphone → Allow

---

### Issue 3: Phonetic Not Converting

**Symptoms:**
- Typing "salam" stays as "salam"
- No Urdu conversion

**Checklist:**
- [ ] Language is set to Urdu (اردو)
- [ ] Pressed space after typing
- [ ] Word exists in dictionary

**Verify Dictionary:**
```bash
# Check PhoneticMapper.kt has the word
grep -r "salam" android/app/src/main/kotlin/
```

---

## 📊 Test Report Template

```
Date: ___________
Tester: ___________
Device: ___________
Android Version: ___________

┌─────────────────────────────────┬──────┬─────────┐
│ Test Case                       │ Pass │ Notes   │
├─────────────────────────────────┼──────┼─────────┤
│ Build & Install                 │ [ ]  │         │
│ Enable Keyboard                 │ [ ]  │         │
│ Set as Default                  │ [ ]  │         │
│ English Input                   │ [ ]  │         │
│ Urdu Phonetic                   │ [ ]  │         │
│ Language Switching              │ [ ]  │         │
│ Voice Input (EN)                │ [ ]  │         │
│ Voice Input (UR)                │ [ ]  │         │
│ Backspace                       │ [ ]  │         │
│ Space Key                       │ [ ]  │         │
│ Enter Key                       │ [ ]  │         │
│ Platform Channel                │ [ ]  │         │
│ Settings Persistence            │ [ ]  │         │
└─────────────────────────────────┴──────┴─────────┘

Overall Status: ________________
Comments: ______________________
```

---

## 🎯 Success Criteria

**Minimum Requirements for Release:**

- [x] ✅ Keyboard installs without errors
- [x] ✅ Appears in system keyboard list
- [x] ✅ Can be enabled and set as default
- [x] ✅ English input works flawlessly
- [x] ✅ At least 20 Urdu words convert correctly
- [x] ✅ Language switching functions
- [x] ✅ Voice input works for English
- [x] ✅ No crashes during normal usage
- [x] ✅ Settings persist after restart
- [x] ✅ Platform channel communicates

---

## 🚀 Performance Benchmarks

### Expected Performance:

| Metric | Target | Actual |
|--------|--------|--------|
| Key press latency | < 50ms | _____ |
| Language switch | < 100ms | _____ |
| Voice recognition start | < 500ms | _____ |
| Phonetic lookup | < 10ms | _____ |
| Memory footprint | < 20MB | _____ |

### Measuring Performance:

```bash
# Monitor app performance
adb shell dumpsys meminfo com.example.urdu_keyboard

# Check CPU usage
adb shell top | grep urdu_keyboard

# View logs
adb logcat | grep "UrduKeyboard"
```

---

## 📝 Testing Notes

### Tested Devices:

```
Device 1:
- Model: _____________
- Android: _____________
- Result: _____________

Device 2:
- Model: _____________
- Android: _____________
- Result: _____________
```

### Edge Cases Tested:

- [ ] Very long text input (1000+ characters)
- [ ] Rapid key presses
- [ ] Language switch during typing
- [ ] Voice input interruption
- [ ] Low memory scenario
- [ ] Multiple app switches

---

## ✅ Final Verification

**Before submitting for release:**

```bash
# 1. Clean build
flutter clean && flutter build apk --release

# 2. Test on minimum Android version (API 26)

# 3. Test on latest Android version

# 4. Verify all features work

# 5. Check APK size
ls -lh build/app/outputs/flutter-apk/app-release.apk

# 6. Sign APK for Play Store
```

---

**Testing Status:** Ready for QA ✅

Happy Testing! 🎉
