# Supaplex Offline for Android 1.3

This update adds language selection and more precise touch-control choices to the native OpenSupaplex Android port. No ads, purchases, accounts, or network permission.

## New in 1.3

- Choose **Polski** or **English** at first launch; change it later through **Pause → Language / Język**. Original DOS menus stay English.
- Choose one of five layouts in **Pause → Touch controls**: four separate arrows (default), fixed joystick, floating joystick, swipe-and-hold direction lock, or split up/down on one side and left/right on the other.
- The split layout has ACTION on both sides. The most recently pressed direction wins; releasing it resumes another direction still held.
- Tune joystick dead zone and direction stability. Size, opacity, left-handed layout, and reset are on the same settings page.

The original engine/save code is unchanged. A movement animation already started may finish after release; this is not a guaranteed one-tile-per-tap mode.

## Install or update

Download **Supaplex-Offline-1.3.apk** from Assets below and open it on the phone. It uses the same debug signing key as the published 1.2 APK and can update that installation without clearing saves. Do not uninstall or clear data to fix a signature mismatch unless you accept losing your saves. Builds from other computers can use a different debug key.

Configured minimum: Android 5.0 / API 21 and OpenGL ES 2.0. One APK contains ARMv7, ARM64, x86, and x86_64 libraries. Physical testing used Huawei P9 lite / VNS-L21 with LineageOS Android 10 / API 29; other devices and versions are not guaranteed.

## Checks

45 automated interface checks passed on the test phone, including split-axis priority, two-finger direction/action combinations on both sides, joystick jitter, cancellation, handedness, language resources, and gamepad pause behavior. First-launch language selection, persistence after restart, and English resume/settings were also exercised. See [validation](https://github.com/SlopPort/supaplex-android/blob/main/docs/VALIDATION.md) for scope and historical engine-test results.

## Files and guides

- **Supaplex-Offline-1.3.apk:** installable debug-signed game.
- **Supaplex-Offline-1.3-sources.zip:** source including pinned SDL2, SDL_mixer, and required libxmp, under `supaplex-android/`.
- **SHA256SUMS.txt:** download checksums.

Read the [touch/language guide](https://github.com/SlopPort/supaplex-android/blob/main/docs/TOUCH-CONTROLS.md) and [build guide](https://github.com/SlopPort/supaplex-android/blob/main/docs/BUILDING.md). GitHub automatic source ZIPs do not include submodules.

New Android work and documentation were produced by **Codex, an OpenAI LLM**. Original game, OpenSupaplex engine, and dependency authors retain their credits and licenses. Fork code is GPL-3.0; game data is separately credited.
