# Validation and device tests

## 1.4 — user-supplied game data

- `assembleDebug assembleDebugAndroidTest lintDebug` passed for the import build.
- 25 generated-fixture import and procedural-audio checks passed on Huawei P9 lite / VNS-L21 with LineageOS Android 10. They cover absent/incomplete data, normalization, allowlisting, traversal, duplicates, size limits, the startup marker and preservation of an existing save. Audio checks cover seven distinct, bounded generated WAV effects, preservation of supplied audio, fallback files for the existing sound modes, absence of generated music, and acceptance of one generated WAV by Android MediaPlayer's decoder. Decoder acceptance is not a speaker or gameplay listening test.
- Fixtures are generated from zero-filled buffers; no original game resources are packaged with the test APK. These structural checks do not establish that arbitrary imported resources are playable or legally redistributable.
- The system ZIP picker imported a personal PC/DOS copy on Huawei; the saved level resumed successfully. After privately setting aside the old audio directory, a cold launch generated the replacement effects. Playback during gameplay and acceptable volume were confirmed on the phone. The folder-picker path and other Android versions have not been manually verified. Earlier interface/engine results below are historical, not rerun for this release.
- Updating the APK preserves existing private data. The new setup requires an explicit user import even if an older version left bundled resources in private storage.


## Version 1.3.1 size update

57 interface checks passed on the same Huawei device. In addition to the 1.3 checks below, the suite checks that direction and ACTION scaling are independent and that minimum-size ACTION remains touchable and releases correctly in all five layouts at 220% direction size and low opacity. Native engine/save code is unchanged. The three slider extremes were also exercised through the real settings UI, checked on disk, and the large-direction/small-ACTION HUD was visually inspected. Existing save data stayed unchanged; previous settings were restored after that check.

## Version 1.3 interface update

The 1.3 update changes Java touch input and Android language resources only. Native engine and save code match 1.2; engine replay tests were not rerun for this interface-only change.

45 automated checks passed on the same physical Huawei P9 lite configuration below. They cover the original HUD behavior plus neutral button gaps, movement-finger ownership, dead zones, joystick axis hysteresis and deliberate turns, floating origins, swipe locking, split-axis priority/release, action on either side, mirrored controls, language resources, and controller pause/menu behavior.

First-launch language selection, remembered English after a cold restart, original continuation resume, and English touch settings were exercised on the phone. Language changes are handled without Activity recreation. Old continuation data was fingerprinted privately before/after the UI tests to check it was not overwritten; no raw save or identifier is published.

The default buttons do not guarantee one tile per tap. Automated event checks establish input behavior, not subjective comfort for every player or device. See [TOUCH-CONTROLS.md](TOUCH-CONTROLS.md).

## Historical 1.2 validation

The physical test device was a **Huawei P9 lite / VNS-L21 running LineageOS, Android 10 (API 29)**. That is the device configuration tested, not an assertion that Huawei shipped Android 10 for this phone. Only this phone was used for Android port testing.

| Check | Result and scope |
| --- | --- |
| Version 1.2 build | `assembleDebug`, `assembleDebugAndroidTest`, and `lintDebug` passed locally. |
| Touch/controller interface | 12 automated checks passed: full surface, direction, simultaneous action/direction, sliding, independent releases, cancellation, left-handed controls, menu opening, gamepad HUD switching, MODE, START opening, and START returning. |
| Engine regression replays | 14 passed on the physical phone: expected-success and expected-failure cases. A replay expected to fail counts as passing only when the verifier returns that expected result. |
| Mid-level continuation | Saved after movement on level 4; save-and-quit closed the app; ordinary relaunch restored matching position, level, player, time, frame count, random state, item counts, and board checksum. Resume from the original menu also passed. |
| Failed save | Making the temporary-file path unwritable as a file caused a save error; the previous continuation remained byte-for-byte unchanged. |
| Cheating flag | Continuation preserved an unflagged run without setting the debug cheating flag. |
| APK inspection | REA/JADX reported version 1.2, minimum SDK 21, target SDK 35, and no requested permissions. |
| APK format | Android tools verified v1/v2 debug signatures and ZIP alignment for 16 KB pages. |

Earlier port testing replayed 334 original-level solutions on the phone. Version 1.1 also tested repeated HOME/return transitions and the fix for `EGL_BAD_ACCESS`. Those are historical results; the 334-solution suite was not rerun for 1.2. Controller stick/action were manually checked with a device reported as Pro Controller (057e:2009).

Public records are in [evidence](../evidence). Only selected summaries are published; personal paths, serial numbers, save files, and raw system logs are excluded. A summary is not a guarantee that every game state or Android configuration has been tested.

## Run the interface tests on a device

Build both APKs using [BUILDING.md](BUILDING.md), enable USB debugging, connect your intended phone, and authorize the computer. Run these commands from the repository root. Replace `DEVICE_ID` with the intended device ID from `adb devices`.

```sh
adb -s DEVICE_ID install -r android/app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_ID install -r android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s DEVICE_ID shell am force-stop pl.offline.supaplex
adb -s DEVICE_ID shell am instrument -w pl.offline.supaplex.test/pl.offline.supaplex.HudInstrumentation
```

Save any live play first: `force-stop` closes the app without making a save. The test injects input, opens menus, and changes transient HUD state. Use a test installation rather than an important unsaved run. Installing these APKs requires the same signing key as any existing installation.

A successful 1.3.1 run prints 57 `PASS` lines and `HUD checks complete`. Building the test APK alone does not run these tests, and the optional GitHub Actions template does not run physical-device tests.

## Repeat a continuation check

1. Start a level, move to a recognizable location, and record the displayed time and item counts.
2. Open pause, save, and quit. Open the app again and choose Resume.
3. Check the level, position, board, counters, and time. Repeat using Resume from the original menu.
4. For an engine-level comparison, the native log's `Progress saved` and `Progress restored` lines include the frame, random state, counters, and board CRC32. Compare those lines before further movement; redact any unrelated log content before sharing.

Do not deliberately fill storage or alter files in a personal installation to test failure handling. The published failure result was obtained in a controlled test of the temporary save path.

## Limits

Minimum SDK 21, four ABIs, and 16 KB alignment are build properties. They are not physical test coverage of Android 5 through all current versions, 16 KB hardware, or every CPU/GPU. Audio output has not had a full live listening comparison. Complete frame-by-frame equivalence to every historical DOS build has not been established.

## Run the 1.4 import and generated-audio checks

Build and install the app and test APK, then run:

```sh
adb -s DEVICE_ID shell am instrument -w -e importOnly true pl.offline.supaplex.test/pl.offline.supaplex.HudInstrumentation
```

This selects the generated-fixture import/audio checks through the existing runner; no game data is required. The older HUD checks (without `-e importOnly true`) require compatible game data to be imported first. Their 57-check results above refer to 1.3.1 and were not rerun for 1.4.
