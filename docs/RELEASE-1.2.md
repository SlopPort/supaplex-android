# Supaplex for Android 1.2

This is an Android port of the original PC/DOS game Supaplex, based on OpenSupaplex. It is a separate project from the Android version available on Google Play, not a modification of that app.

A native Android adaptation of OpenSupaplex with original graphics and levels, configurable touch controls, gamepad input, and a manual mid-level continuation save.

## Download and play

Download **Supaplex-Offline-1.2.apk** from Assets below and open it on your Android phone. If prompted, allow the browser/file manager to install this app. Open **Supaplex Offline** from the launcher.

The Android dialogs are currently **Polish**. To save: pause (or START on a gamepad) → **Zapisz i zakończ** (Save and quit). On the next launch, choose **Wznów** (Resume). There is one manual slot shared across all players/levels; each successful save replaces it. There is no autosave.

Read the [play and save guide](https://github.com/SlopPort/supaplex-android/blob/main/docs/ANDROID.md) for the translated controls and labels.

## Requirements and limits

- Configured minimum: Android 5.0 / API 21, OpenGL ES 2.0. APK contains ARMv7, ARM64, x86, and x86_64 code.
- Physically tested: Huawei P9 lite / VNS-L21 with LineageOS Android 10 / API 29. Other devices and Android versions are not guaranteed.
- This APK is signed with an Android debug key, not a production release key. An update must use the same signing key to preserve an existing installation. **Do not uninstall or clear data to fix a signature mismatch unless you accept losing your saves.**

## What was checked

12 interface checks and 14 engine regression replays passed on the test phone. Mid-level save/relaunch and menu resume restored matching board, position, time, counters, level, and player. A failed new save preserved the previous slot. APK signature and 16 KB ZIP alignment checks passed; REA/JADX reported no requested permissions.

See [validation details](https://github.com/SlopPort/supaplex-android/blob/main/docs/VALIDATION.md) for scope and limitations.

## Source and attribution

**Supaplex-Offline-1.2-sources.zip** includes the pinned SDL2, SDL_mixer, and required libxmp sources. Extract it and follow [BUILDING.md](https://github.com/SlopPort/supaplex-android/blob/main/docs/BUILDING.md). GitHub's automatic Source code ZIP does not include submodules.

**SHA256SUMS.txt** lists hashes for the downloadable APK and bundled source archive.

The new Android work and documentation were produced by **Codex, an OpenAI LLM**. OpenSupaplex, the original game, and third-party libraries retain their own authorship and licenses. Fork code is GPL-3.0; original game data is separately credited. See [third-party notices](https://github.com/SlopPort/supaplex-android/blob/main/THIRD_PARTY_NOTICES.md).
