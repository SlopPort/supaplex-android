# Comparison with related repositories

Review date: **2026-10-25**. This is a scoped comparison of public repositories, not an exhaustive search or a claim that this is the first Android Supaplex port.

## Repositories inspected

GitHub repository searches for Supaplex, an Android-specific search including forks, and the upstream fork listing were used to identify candidates. An Android-manifest code search returned no matches; that does not prove that other Android ports do not exist. The comparisons below use each repository's README, advertised platforms, and source/build tree at the linked commit.

| Repository and inspected revision | What it provides | Difference relevant to this fork |
| --- | --- | --- |
| [sergiou87/open-supaplex](https://github.com/sergiou87/open-supaplex/tree/2ae9e5815c508c15e405892db486495d28f74f6d) | The C engine this project directly builds on; original presentation, many platform ports, controller input, replay validation, and debug save/load. | Its platform list still describes Android as future work. This fork supplies Android packaging, touch UI, lifecycle handling, and a player-facing continuation flow. |
| [Mr-Precise/open-supaplex-cmake](https://github.com/Mr-Precise/open-supaplex-cmake/tree/dcb3f50176dfec9470eea2ef834b6c12354d24e9) | An OpenSupaplex CMake adaptation described as PC-only. | CMake itself is not new here. This fork's build targets an Android APK through Gradle/NDK. |
| [Will40/OpenSupaplex-port](https://github.com/Will40/OpenSupaplex-port/tree/1b990b4ffb1470bb1172555511f3d843144a86a6) | A PortMaster package for RG351-family Linux handhelds with controller instructions. | It targets a different runtime and deployment method, rather than Android's touch interface and Activity lifecycle. |
| [anight/supaplexSDL](https://github.com/anight/supaplexSDL/tree/db77babb9c0945de4a9276f258fe02e76eec3b54) | A separate C/SDL2 reimplementation, software rendering, controller input, replay mode, and a related Pico port. It asks for separately supplied game data. | This project uses the OpenSupaplex engine instead and bundles an Android app with touch controls and a continuation menu. |
| [niximor/supaplex](https://github.com/niximor/supaplex/tree/204cfe890d9d34d9958c63f6c8766aa4c7729f96) | An SDL clone with original graphics/levels; its tree includes a Makefile and Visual Studio project. | No Android Gradle/manifest packaging was found in the inspected tree. |
| [radu124/splexhd](https://github.com/radu124/splexhd/tree/ef8a992b52f4cb6a1a8a36c33f394ff45f54ad50) | A rewrite with redrawn graphics. | This fork keeps the original graphics and OpenSupaplex gameplay rather than adopting that visual remake. |

These comparisons describe documented or observed source-tree differences. A missing README feature is not proof that an implementation cannot support it. No other project's APK was installed as part of this repository review.

## Inherited work versus Android additions

The engine, object physics, original-style rendering, replay support, controller foundations, and most snapshot serialization are inherited from OpenSupaplex. Its [v7.1 release notes](https://github.com/sergiou87/open-supaplex/releases/tag/v7.1) already describe gamepad improvements and save/load states. This fork does not claim those concepts as original work.

The practical additions in the published Android source are:

- `android/`: Android application, Gradle build, native integration, touch UI, and device interface tests.
- `src/android/display.c`: aspect setting and Android overlay pause integration.
- `src/android/virtualKeyboard.c`: native-to-Android player-name entry.
- `src/android/progress.c`: save requests on the engine thread, slot availability, and continuation callbacks.
- Changes in `src/savegame.c`, `src/config.c`, and `src/supaplex.c`: a separate continuation file, level/player metadata, checked temporary-file writes and replacement, plus restoration without newly flagging the run as cheating.
- Android changes in SDL-facing game code: input routing, display lifecycle handling, and timer handling after backgrounding.
- Android CMake configuration and device validation instructions.

The combination is sufficient to publish a distinct Android adaptation. No claims are made about global uniqueness, superiority over every commercial port, universal Android compatibility, or perfect equivalence to every original DOS release.
