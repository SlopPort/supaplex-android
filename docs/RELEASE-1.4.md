# Supaplex for Android 1.4

An Android port of the original PC/DOS Supaplex, based on OpenSupaplex. This is a separate project from the Android version on Google Play.

**Game files are no longer included.** On first launch, select a folder or ZIP from your own legally obtained PC/DOS copy using Android's file picker. See [required files and setup](https://github.com/SlopPort/supaplex-android/blob/main/docs/GAME-DATA.md).

The port generates seven new procedural sound effects in code when matching WAV files are absent. These are original replacements, not recordings or a reconstruction of the DOS sound. Compatible user-supplied WAV/XM files take priority. Original music is not included or recreated. Five new CC0 music loops are generated instead, with a random choice at each level start; see [generated audio](https://github.com/SlopPort/supaplex-android/blob/main/docs/GENERATED-AUDIO.md).

The first-run screen links to the [original-effects extractor](https://github.com/SlopPort/supaplex-android/blob/main/docs/EXTRACT-DOS-AUDIO.md). It converts seven Sound Blaster effects from your own BLASTER.SND into WAV, without supplying game data or converting original music.

This requirement also applies when upgrading from older releases. Existing saves and control settings are preserved; executables, settings, players and saves from the chosen source are never imported. Earlier releases with bundled resources have been withdrawn.

The five touch layouts, independent direction/ACTION sizing, opacity, Polish/English Android interface, gamepad support and manual mid-level save remain available. Original DOS menus remain English.

Download **Supaplex-Android-1.4.apk**. It uses the same debug signing key as 1.3.1; install it as an update, without uninstalling or clearing data. **Supaplex-Android-1.4-sources.zip** contains code and required pinned dependencies, without game data. **SHA256SUMS.txt** lists download hashes.

Configured minimum Android 5.0 / API 21 and OpenGL ES 2.0; physical validation uses Huawei P9 lite with LineageOS Android 10. Other devices are not guaranteed. See [validation records](https://github.com/SlopPort/supaplex-android/blob/main/docs/VALIDATION.md) for completed checks.

The port code is GPL-3.0; dependencies retain their licenses. Original game data retains its owners' rights and is not covered by the port's license. Android additions and documentation were produced by **Codex, an OpenAI LLM**; upstream authors remain credited.
