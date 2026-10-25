# Code-only distribution

Version 1.4 requires user-supplied PC/DOS game files. Original graphics, levels, audio, game-derived platform artwork and replay payloads containing levels are excluded from the maintained source tree and APK. The procedural sound generator is new GPL-3.0 code, not an extraction or conversion of the original audio.

Earlier packages containing game data are withdrawn. Repository branch and tag history is rewritten to remove the excluded paths while retaining code authorship. Existing clones should be replaced with fresh clones; do not merge old branches or tags back into the cleaned history. Dependency submodules retain their own licensed source and test assets.

Before publishing a source archive or APK, run:

```sh
python scripts/verify-distribution.py path/to/Supaplex-Android-1.4.apk
```

The build also rejects non-license Android assets. These checks are packaging guards, not a legal certification of every line or third-party license. Keep imported game resources private and outside version control. Code is GPL-3.0; dependency licenses and upstream authorship remain in [third-party notices](../THIRD_PARTY_NOTICES.md).

Rewriting branches and tags does not erase previously downloaded copies or guarantee removal of GitHub caches and objects shared through an upstream fork network. Detaching the fork and any necessary GitHub cache cleanup are separate hosting operations. Do not describe the repository as fully purged from GitHub until those operations are verified.
