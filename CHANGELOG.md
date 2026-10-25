# Android changelog

These versions describe the Android adaptation, not OpenSupaplex's own release numbering.

## 1.2

- Added one persistent manual continuation slot during a live level.
- Added Save, Save and quit, startup Resume, and resume from the pause/original-menu flow.
- Preserved level/player context and random state; loading a continuation does not itself flag the run as cheating.
- Added checked temporary-file writes and replacement to preserve the previous save after a failed write.
- Made on-screen pause and controller START open the pause/save menu.
- Passed 12 interface checks and 14 regression replays on the documented Huawei P9 lite configuration.

## 1.1

- Added configurable touch controls, full-screen display with an aspect-ratio option, and controller/touch switching.
- Fixed Android HOME/resume rendering by keeping EGL rendering on the SDL thread.
- Added controller MODE/Capture menu handling and background timer handling.

## 1.0

- Introduced the native OpenSupaplex Android build, player-name keyboard integration, bundled game resources, and offline packaging without ads or purchases.
