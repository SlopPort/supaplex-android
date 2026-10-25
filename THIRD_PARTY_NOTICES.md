# Authorship and third-party notices

## Android adaptation

The new Android adaptation, touch interface, continuation integration, test automation, and documentation in this fork were produced by **Codex, an OpenAI large language model (LLM)**. This attribution applies to the fork's additions, not to the original game or inherited code. The publishing account does not replace the authors credited below.

The fork's C/Java integration code is distributed under GPL-3.0, consistent with OpenSupaplex. Existing copyright headers, licenses, and upstream Git history are retained.

## Game and engine

- **Supaplex and its original data:** the original developers and other credited contributors retain their rights. This repository's GPL license for code does not relicense the game graphics, levels, or other data. The upstream OpenSupaplex README describes the original resources as freeware belonging to their original authors. Original data documentation is retained in `resources` and in `android/app/src/main/assets/game`; preserve its notices when redistributing.
- **[OpenSupaplex](https://github.com/sergiou87/open-supaplex):** C reimplementation by Sergio Padrino and contributors, GPL-3.0. Base commit: `2ae9e5815c508c15e405892db486495d28f74f6d`. See [LICENSE](LICENSE), original source headers, and the [archived upstream README](docs/upstream/README.md).
- **[cilliemalan/supaplex](https://github.com/cilliemalan/supaplex):** the disassembly underlying OpenSupaplex; the included assembly tree has its own MIT license at `asm/LICENSE`.
- **Audio:** MOD/WAV files are inherited from the OpenSupaplex resource distribution, rather than newly composed or extracted from a commercial Android APK. Original resource credits remain applicable.
- **Replay fixtures:** recorded solutions remain attributed to their authors as described in `tests/README.md`; the Android adaptation does not claim authorship of them.

The Android asset directory includes the original 111-level set and additional sets already provided by OpenSupaplex. Selected original DOS data files were matched/imported for this port; [evidence/dos-assets.json](evidence/dos-assets.json) records hashes. A commercial Android APK is not part of the published source or runtime.

## Runtime and build dependencies

| Component | License / record |
| --- | --- |
| [SDL2](https://github.com/libsdl-org/SDL) | zlib; `vendor/SDL/LICENSE.txt` |
| [SDL_mixer](https://github.com/libsdl-org/SDL_mixer) | zlib; `vendor/SDL_mixer/LICENSE.txt` |
| [libxmp](https://github.com/libsdl-org/libxmp) | MIT; included notices under its source tree and `android/app/src/main/assets/licenses/libxmp.txt` |
| Gradle wrapper | Apache-2.0; wrapper script headers and [Gradle license](https://github.com/gradle/gradle/blob/v8.10.2/LICENSE) |
| INI parser in `src/lib/ini` | Its included license: `src/lib/ini/LICENSE` |

Exact dependency revisions are in [evidence/source-commits.json](evidence/source-commits.json) and the Git submodule entries. Dependency licenses are also bundled with the APK under `assets/licenses`.

## Analysis tooling

[morluto/rea](https://github.com/morluto/rea) was used to inspect Android packages during development. It is an analysis tool, not this game's engine or a runtime dependency. Its source is not bundled here. The public APK inspection summary is [evidence/apk-inspection-1.2.json](evidence/apk-inspection-1.2.json).
