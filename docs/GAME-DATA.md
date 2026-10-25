# Bring your own PC/DOS game data

Version 1.4 and later ship the open-source Android port without Supaplex graphics, levels or audio. Supply your own legally obtained PC/DOS copy. This does not change the rights or license of those files. The Google Play Android app is a separate product and is not a compatible data source.

## Import on Android

1. Copy your PC/DOS game folder or ZIP onto your phone, for example into Downloads.
2. Open **Supaplex**. On the data setup screen, choose **Choose folder / Wybierz folder** or **Choose ZIP / Wybierz ZIP**. You can switch the setup language with **Polski / English**.
3. In Android's system picker, select the game folder and grant access, or select its ZIP. If Android prevents selecting Downloads itself, select a game subfolder or use ZIP instead.
4. The app checks the files, copies supported resources into its private storage, then opens the game. No broad storage permission is required. Future launches use that private copy.

Import is also required after upgrading from a version that bundled data. Existing saves, players, and touch settings are preserved. This importer does not copy player records, configuration, executables or saves from the selected source. There is no automatic download, and this guide does not provide game files.

The first-run screen explains replacement audio and links to the [original-effects extraction guide](EXTRACT-DOS-AUDIO.md). Existing configured installations see this information once after updating.

## Required files

Provide these files from the original 111-level PC/DOS distribution:

```text
BACK.DAT       CHARS6.DAT     CHARS8.DAT     CONTROLS.DAT
FIXED.DAT      GFX.DAT        MENU.DAT       MOVING.DAT
PALETTES.DAT   PANEL.DAT      TITLE.DAT      TITLE1.DAT
TITLE2.DAT     LEVEL.LST      LEVELS.DAT
```

Names are case-insensitive. A ZIP may contain a surrounding game folder. The importer checks expected file lengths (including known one-byte graphics variants); it does not certify ownership or guarantee that arbitrary modified data is compatible. Missing or invalid required files are listed on the setup screen. Select a complete compatible copy and retry. Nested folders may be searched, but multiple copies of the same resource are rejected; select one game copy rather than a collection of versions.

Optional `DEMO0.BIN` through `DEMO9.BIN`, extra `LEVEL.Lnn` / `LEVELS.Dnn` sets and compatible `audio/*.wav` / `audio/*.xm` files may also be imported.

## Sound effects and music

Standard DOS files alone do not provide converted WAV/XM audio. The port instead generates seven new procedural effects for explosions, infotrons, pushing, falling objects, bugs, base tiles and exits. No original samples or melodies are used; the generator is GPL-3.0 code. The fallback effects use the same timbres across the original sound-device options. Compatible user-supplied WAV files take priority.

Original music is neither included nor recreated. Five new CC0 tracks are generated and randomly selected at each level start, restart or saved-level load. To hear the original music, supply your own compatible XM files. See [generated music and licensing](GENERATED-AUDIO.md). Existing sound-effects volume and mute options still apply. Playback and acceptable volume were confirmed during gameplay on the tested Huawei; preferences on other devices may differ.

## Import limits and stored data

Imports are limited to 4,096 entries, 8 MiB per accepted file and 32 MiB total; folder nesting is limited to eight levels.

Uninstalling or clearing app storage removes the imported copy and saves. Keep your original files separately. To replace data later, use **Pause → Import game files / original sounds**, save/close, then reopen the app. Existing saves/settings are kept. **Keep installed files** cancels a requested re-import without replacing data. Use a compatible game copy for existing saved levels.

## Source and licensing

The port code is available under GPL-3.0, with inherited authorship preserved. Dependencies retain their own compatible licenses; see [third-party notices](../THIRD_PARTY_NOTICES.md). Original game data is excluded, not relicensed as open source. Do not commit your game files, converted audio, screenshots, or archives containing them. Earlier releases that bundled data have been withdrawn.
