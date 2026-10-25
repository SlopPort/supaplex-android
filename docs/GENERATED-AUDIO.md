# New generated music and effects

The Android port creates its own replacement audio in private app storage. It does not include or convert Supaplex recordings, samples or melodies. The first launch may take a few seconds to prepare five tracks; later launches reuse the files.

## Music

Each track is a new 16-bar instrumental loop with synthesized lead, bass, arpeggios and percussion:

| Track | Tempo | Approximate loop | Character |
| --- | --- | --- | --- |
| Circuit Garden | 112 BPM | 34 seconds | Bright, playful arcade synth |
| Crystal Steps | 104 BPM | 37 seconds | More spacious, gently moving melody |
| Clockwork Rush | 132 BPM | 29 seconds | Fast, busy rhythmic pattern |
| Neon Maze | 120 BPM | 32 seconds | Syncopated, darker arcade mood |
| Quiet Terminal | 96 BPM | 40 seconds | Softer percussion and a calmer melody |

At the start or restart of a level, including loading a saved level, the port randomly selects one of the five generated tracks and loops it. Independent audio randomness never consumes the gameplay/replay random generator. A random selection may repeat the previous track. There is no track-selection menu.

Existing music mute and volume controls still apply. If compatible `audio/music-*.xm` files are supplied for the selected original sound-device mode, those take priority and are not randomized. Missing original music uses the new generated tracks instead. The five variants are different compositions, not attempts to reproduce the original score or emulate each DOS sound card exactly.

## Sound effects

Seven short generated effects cover explosions, infotrons, pushing, falling objects, bugs, base tiles and exits. Compatible supplied WAV files take priority. Missing effects use the new generated sounds across all existing sound-device options.

## License and source

The new composition data and audio rendered by `GeneratedAudio.java` and `GeneratedMusic.java` are dedicated under **CC0 1.0 Universal**, to the extent any copyright or related rights exist. They may be reused without royalties, including commercially. This dedication applies only to the newly generated music/effects, never to imported game files or third-party audio.

The generator implementation and port integration remain **GPL-3.0**. New composition and synthesis work was produced by **Codex, an OpenAI LLM**. See [the CC0 text](../LICENSE-CC0.txt), [code license](../LICENSE), and [Creative Commons' CC0 description](https://creativecommons.org/publicdomain/zero/1.0/).
