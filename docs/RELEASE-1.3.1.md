# Supaplex Offline for Android 1.3.1

Smaller action buttons and independent touch sizing in all five movement layouts.

Open **Pause → Touch controls** (**Pauza → Sterowanie dotykowe**) and scroll down:

- **Direction size / Rozmiar kierunków:** 75–220%, independently of ACTION.
- **ACTION button size / Rozmiar przycisku AKCJI:** 75–160% of a new compact default. Both split-layout copies use this value.
- **Button opacity / Widoczność przycisków:** 10–100%. Lower values make the controls more transparent; touch input is unchanged.

ACTION is smaller by default, including on both sides of the split layout. Confirm also stays compact when movement controls are enlarged. Existing movement style, direction scale, opacity, language, and saved game are retained. Native engine/save code is unchanged.

57 automated interface checks passed on Huawei P9 lite / VNS-L21 with LineageOS Android 10, including independent sizing and small-ACTION input/release in every style at 220% direction size and low opacity. Configured minimum Android 5.0 / API21 does not mean all devices were tested.

Download **Supaplex-Offline-1.3.1.apk** from Assets to install/update. It uses the same debug signing key as published 1.2/1.3 APKs. Do not uninstall or clear data to work around a signature mismatch unless you accept losing saves. The bundled **Supaplex-Offline-1.3.1-sources.zip** includes the pinned required dependencies; **SHA256SUMS.txt** contains download hashes.

Android UI can be Polish or English; DOS menus remain English. No ads, purchases, accounts, or network permission. Android additions and documentation were produced by **Codex, an OpenAI LLM**; upstream authors and licenses remain credited. Fork code is GPL-3.0, with game data separately credited.

[Touch guide](https://github.com/SlopPort/supaplex-android/blob/main/docs/TOUCH-CONTROLS.md) · [Build guide](https://github.com/SlopPort/supaplex-android/blob/main/docs/BUILDING.md)
