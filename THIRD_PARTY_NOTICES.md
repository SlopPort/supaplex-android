# Authorship and third-party notices

## Android adaptation

The new Android adaptation, touch interface, continuation integration, test automation, and documentation in this fork were produced by **Codex, an OpenAI large language model (LLM)**. This attribution applies to the fork's additions, not to the original game or inherited code. The publishing account does not replace the authors credited below.

The fork's C/Java integration code is distributed under GPL-3.0, consistent with OpenSupaplex. Existing copyright headers, licenses and upstream attribution are retained. Provenance is recorded in `evidence/source-commits.json`; history is sanitized to exclude game data.

## Game and engine

- **Supaplex and its original data:** original graphics, levels, audio and other game resources are not distributed with this port. Users must import files from their own legally obtained PC/DOS copy. Those files retain their owners' rights; GPL does not apply to them. Upstream describes its original resources as freeware, which is distinct from an open-source license.
- **[OpenSupaplex](https://github.com/sergiou87/open-supaplex):** C reimplementation by Sergio Padrino and contributors, GPL-3.0. Base commit: `2ae9e5815c508c15e405892db486495d28f74f6d`. See [LICENSE](LICENSE), original source headers, and the [archived upstream README](docs/upstream/README.md).
- **[cilliemalan/supaplex](https://github.com/cilliemalan/supaplex):** the disassembly underlying OpenSupaplex; the included assembly tree has its own MIT license at `asm/LICENSE`.
- **Audio:** no original game audio is bundled. `GeneratedAudio.java` and `GeneratedMusic.java` are new GPL-3.0 procedural generators by Codex (LLM); they create seven replacement effects and five new music compositions without original samples or melodies. New composition data and rendered audio are dedicated under CC0-1.0, to the extent rights exist; see [generated audio](docs/GENERATED-AUDIO.md) and [CC0](LICENSE-CC0.txt). Compatible user-supplied WAV/XM files may optionally be imported and retain their own rights. Original music is not recreated.
- **Replay fixtures:** historical community recordings that contained level data have been removed from this distribution. Historical validation summaries do not distribute those fixtures.

Earlier Android releases contained game resources; their downloadable packages have been withdrawn. Importing user data never imports executables, player lists, settings, or save files. Code and dependency licenses remain included.

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
