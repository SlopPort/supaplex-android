# Extract original effects from your own DOS copy

The port's default music and effects are new generated replacements. If you own a legally obtained PC/DOS copy, the included tool can recover **seven original Sound Blaster effects** from its `BLASTER.SND` and turn them into WAV files accepted by this Android port. The first-run screen links to this guide.

This extracts explosions, infotrons, pushing, falling objects, bugs, base tiles and exits. It does **not** convert the original AdLib/PC Speaker/Roland music or synthesize their sound-card effects. The five new music loops remain available. Extracted original audio retains its original rights; the generated-audio CC0 dedication does not apply to it.

## Create a ready-to-import ZIP

You need Python 3.9 or later on Windows, macOS or Linux, plus a copy of this repository. The extraction script uses only Python's standard library; Android Studio and additional Python packages are not required.

From the repository folder, run this as one command, replacing `my-game.zip` with your own DOS ZIP or game-folder path:

```sh
python scripts/extract-dos-audio.py "my-game.zip" --output "../my-supaplex-data" --all-modes --make-game-zip
```

On Windows, `py -3` may be used instead of `python`. The output directory must be new and outside the repository; choose another name if it already exists.

1. The tool reads your `BLASTER.SND`, validates its embedded Creative VOC sample table and writes WAV files under `my-supaplex-data/audio`.
2. It creates **`my-supaplex-data/Supaplex-your-data.zip`**, containing your required graphics/level files and the extracted effects. It does not include the DOS executable or sound driver.
3. Copy that ZIP to the phone. On a new installation's data setup screen, choose **Choose ZIP / Wybierz ZIP** and select it. To use a folder instead, unzip it into a separate game folder and choose that folder.

`--all-modes` supplies the Sound Blaster recordings under all five compatibility filename sets, so the port can use them regardless of the existing DOS sound-device preset. These are still Sound Blaster samples; the aliases do not recreate AdLib, Roland, or PC Speaker sound. Without that option, only `*-blaster.wav` files are produced, and the original game's **SOUND BLASTER** or **COMBINED** sound setting is needed to use them.

For audio-only extraction, omit `--make-game-zip`. The source may then also be an individual `BLASTER.SND`. Copy the resulting `audio` folder into your own game folder before importing it.

## Existing installations

Open **Pause → Import game files / original sounds** (**Pauza → Import plików gry / oryginalnych dźwięków**). Confirm **Save and quit** when available, or **Close game** from the original menu. Reopen Supaplex and select your prepared ZIP/folder. Existing saves and settings are retained; no uninstall or storage clearing is needed. Use compatible data for the saved level you want to resume.

If you change your mind, **Keep installed files / Zachowaj obecne pliki** returns to the existing setup. Cancelling the Android picker leaves the import screen open so you can retry or keep installed files.

## Formats and limits

The tested DOS driver embeds seven uncompressed unsigned 8-bit mono VOC samples, playing at approximately 8,333 Hz. The tool preserves their PCM bytes and writes WAV headers without resampling. It recognizes the seven-entry dispatch table rather than blindly renaming arbitrary `.SND` files. Unsupported or modified drivers, compressed VOC blocks, duplicates and incomplete game data are rejected with an error.

The script never executes the DOS driver or game executable. Existing output directories are not overwritten. `EXTRACTION.json` records rates, sample counts, the source driver hash and the rights reminder. Keep all extracted files and the resulting game ZIP private; they are not part of the open-source distribution.

The extractor's code is GPL-3.0 and was produced by Codex (LLM). Tests generate synthetic VOC fixtures without original game audio:

```sh
python scripts/test-extract-dos-audio.py
```
