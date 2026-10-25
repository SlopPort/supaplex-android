# Supaplex for Android

An Android port of the original PC/DOS game Supaplex, based on [OpenSupaplex](https://github.com/sergiou87/open-supaplex). It keeps the original graphics and levels and adds touch controls, gamepad support, and a manual save that lets you continue partway through a level.

This is a separate project from the Android version available on Google Play; it is not a modification of that app.

**Current version: 1.3.1.** Choose **Polski** or **English** on first launch (also after updating from 1.2). This changes Android buttons, dialogs, hints, and accessibility text. Original DOS artwork and menus remain in English. Change the choice later in **Pause → Language / Język**.

## Install and start playing

1. Open [Releases](https://github.com/SlopPort/supaplex-android/releases) and download `Supaplex-Offline-1.3.1.apk` from the **Assets** section. You do not need the source ZIP to play.
2. Open the APK on your phone. If Android asks, allow that browser or file manager to install this app. You can turn that permission off afterward.
3. Open **Supaplex Offline**, choose a language, and skip opening screens with the check-mark button if needed.
4. In the original game menu, use **NEW PLAYER** to create a player. The Android name dialog is titled **Player name** / **Imię gracza**. Enter a name, confirm with **OK**, then select a level and press the check-mark button to start.

The APK is a test build signed with an Android debug key. See [updating without losing saves](docs/ANDROID.md#updates-and-save-data) before replacing an existing installation. Android 5.0 or later and OpenGL ES 2.0 are required by the configuration; this is not a promise that every device works.

## Controls

| On screen | Action |
| --- | --- |
| Direction pad, bottom left | Move Murphy. Slide between directions without lifting your finger. |
| Lightning button (ACTION) | Action. Hold it with a direction to collect beside Murphy without moving; hold it to place a red disk when available. |
| Check-mark button | Confirm / Enter. |
| Pause, top right | Pause and open save options. |
| Three dots, top left | Open the same menu, including touch and display settings. |

The picture fills the screen by default, stretching its proportions. Enable **Preserve picture proportions / Zachowaj proporcje obrazu** in the pause menu for the original proportions. Open **Touch controls / Sterowanie dotykowe** for the movement style, button size, opacity, and left-handed layout; scroll down inside that page for all settings.

Direction size now ranges from **75–220%**, independently of **ACTION button size (75–160%)**. ACTION and confirm are compact by default in every layout. **Button opacity / Widoczność przycisków** ranges from **10–100%**; lower values make the overlay more transparent without changing touch input.

For a gamepad, use the D-pad or left stick to move, **X** for action, **A** to confirm, and **START** to open or close the pause menu. **HOME** goes to Android's home screen. Button letters describe Android/SDL positions; Nintendo controllers can have different printed letters. See the [full controller guide](docs/ANDROID.md#gamepads).

## Choose a movement style

Open **Pause → Touch controls**. All five styles send only the four original directions; they do not change the engine or game speed.

| Style | How it works |
| --- | --- |
| Four buttons — precise (default) | Separate arrow buttons. Tap briefly or hold; gaps and the centre are neutral. |
| Fixed joystick | Drag from a fixed base. Return to its centre or lift to stop. |
| Floating joystick | During a level, the base appears where the thumb touches the lower movement side; drag from there. |
| Swipe and hold — locked direction | Swipe once, keep the finger down to continue, and lift to stop. Choose a new direction with a new swipe. |
| Split: up/down &#124; left/right | Up/down on the left; left/right on the right. ACTION is duplicated so either direction pair can combine with action using two fingers. |

In the split style, the latest newly pressed direction wins. Releasing it returns to the other direction if that button is still held. Left-handed mode mirrors the layout. Joysticks also have **dead zone** and **direction stability** sliders to reduce accidental movement and diagonal jitter. Stopping input does not cancel a tile animation already in progress; this is not a guaranteed one-tile-per-tap mode.

See the [touch-control guide and design references](docs/TOUCH-CONTROLS.md), including the popular Android games reviewed for interaction ideas.

## Save halfway through a level

1. Press pause on screen or **START** on the gamepad.
2. Choose **Save and quit** / **Zapisz i zakończ**.
3. Next time you open the game, choose **Resume** / **Wznów**.

Your board, Murphy's position, collected items, level, player, and elapsed time are restored. To save while keeping the game open, choose **Save level progress / Zapisz postęp poziomu**. To load from the pause menu, choose **Resume saved level / Wznów zapisany poziom**.

There is **one manual save slot for the whole app**, shared across players and levels. Each successful save replaces it. **There is no autosave.** Uninstalling the app or clearing its data deletes the save. Saving is available during a live level, not in the original menu, a demo, or the death/end sequence.

## What this fork adds

OpenSupaplex supplies the engine, original presentation, controller foundations, and snapshot format. This fork adds Android packaging and an Android interface around that engine:

- A configurable touch overlay with simultaneous direction and action input.
- Full-screen display, optional original proportions, and controller/touch switching.
- A pause menu and a persistent continuation slot, separate from upstream's debug snapshot slot. Loading this continuation does not itself mark a run as cheating.
- Android keyboard entry for player names and handling for HOME/resume.
- One APK containing ARMv7, ARM64, x86, and x86_64 libraries, configured for 16 KB memory-page alignment.

The [repository comparison](docs/COMPARISON.md) explains what was checked and what is inherited. This is an Android adaptation, not a claim to have invented Supaplex saving or gamepad controls. It aims to preserve OpenSupaplex/SpeedFix behavior; complete equivalence to every historical DOS version has not been established.

## Build from source

Playing does not require Android Studio. Developers can follow the [build instructions](docs/BUILDING.md) for SDK versions, dependency setup, and commands. A normal GitHub source ZIP does not contain submodule sources; use the documented Git clone procedure or the bundled source archive attached to the release.

## Testing and limitations

Physical-device testing used a **Huawei P9 lite (VNS-L21), running LineageOS / Android 10, API 29**, not the phone's original Huawei firmware.

- Version 1.3.1: 57 automated touch/controller/language/size checks passed. Engine C code is unchanged from 1.2; its 14 regression results are historical, not rerun for this interface update.
- Continuation tests verified the board checksum, position, level, player, time, and frame counter after saving and reopening, plus resume from the original menu and preservation of the previous save when a new save fails.
- Earlier port validation replayed 334 solutions of the original levels on the same device. That full set was not rerun for 1.2.

See [validation notes](docs/VALIDATION.md). Other Android versions, phones, and all controller models have not been physically tested. Precision preferences are subjective and need play feedback; the save slot is manual. The APK is not a Play Store release.

## Authorship and licenses

The new Android adaptation, touch interface, continuation integration, test automation, and documentation in this fork were produced by **Codex, an OpenAI large language model (LLM)**. Original game development, reverse engineering, engine work, and third-party libraries belong to their respective authors; their attribution and history are retained.

The C reimplementation and this fork's code use **GPL-3.0**. Original game data is separate from that code license. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the original game, OpenSupaplex, SDL2, SDL_mixer, libxmp, and the analysis tool REA.

Bug reports and patches are welcome: [contribution guide](CONTRIBUTING.md).
