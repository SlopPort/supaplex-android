# Contributing

Start with the [README](README.md) to install/play, or [BUILDING.md](docs/BUILDING.md) to build the Android app. This fork focuses on Android; the engine and older platform ports come from OpenSupaplex.

## Report a bug

Open an issue with the app version, device model, Android version, and short steps to reproduce. Say what you expected and what happened. For controller problems, include the controller model and the controls that work or fail. For save problems, say whether you used Save, Save and quit, or Resume.

Do not post personal names, device serials, Bluetooth addresses, signing keys, or unredacted system logs. Do not upload game files, game-derived screenshots, audio, archives, personal saves, or replays containing level data. Use text reproduction steps and, when needed, synthetic fixtures that you have the right to license with the code.

## Submit a change

1. Follow [BUILDING.md](docs/BUILDING.md), including the pinned submodules.
2. Keep the original gameplay and attribution intact. Make presentation/control changes optional when they affect the original experience.
3. Run `assembleDebug assembleDebugAndroidTest lintDebug` from `android` using the wrapper. For input, lifecycle, or save changes, also test on a real Android device and describe the exact device/OS and steps.
4. Describe what changed, why, and which checks actually ran. Distinguish minimum supported versions from tested configurations.

Code contributions must be compatible with the existing GPL-3.0 license. Game data and third-party code keep their separate licenses. Disclose LLM-generated contributions accurately; do not claim authorship of upstream work.
