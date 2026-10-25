"""Extract original Sound Blaster effects from a player's own BLASTER.SND.

GPL-3.0; implementation by Codex (LLM). No original audio or driver is bundled.
Recognizes the DOS driver's seven-entry embedded Creative VOC sample table.
It parses data only; it never executes the DOS driver or game executable.
"""
import argparse
import hashlib
import io
import json
import struct
import wave
import zipfile
from pathlib import Path

EFFECTS = ("explosion", "infotron", "push", "fall", "bug", "base", "exit")
SUFFIXES = ("blaster", "beep", "sample", "adlib", "roland")
MAGIC = b"Creative Voice File\x1a"
REQUIRED = {"BACK.DAT": 32000, "CHARS6.DAT": 512, "CHARS8.DAT": 512,
            "CONTROLS.DAT": 32000, "FIXED.DAT": 5120, "GFX.DAT": 32000,
            "MENU.DAT": 32000, "MOVING.DAT": 73920, "PALETTES.DAT": 256,
            "PANEL.DAT": 3840, "TITLE.DAT": 32000, "TITLE1.DAT": 32000,
            "TITLE2.DAT": 32000, "LEVEL.LST": 3108, "LEVELS.DAT": 170496}

def decode_voc(data, start):
    if data[start:start + 20] != MAGIC or start + 26 > len(data):
        raise ValueError("Invalid Creative VOC header")
    offset, version, checksum = struct.unpack_from("<HHH", data, start + 20)
    if offset < 26 or checksum != ((~version + 0x1234) & 0xffff):
        raise ValueError("Invalid Creative VOC version/checksum")
    pos, rate, pcm = start + offset, None, bytearray()
    for _ in range(128):
        if pos >= len(data):
            raise ValueError("Truncated VOC stream")
        kind = data[pos]
        pos += 1
        if kind == 0:
            if not rate or not pcm:
                raise ValueError("Empty VOC sample")
            return rate, bytes(pcm), pos
        if pos + 3 > len(data):
            raise ValueError("Truncated VOC block")
        size = int.from_bytes(data[pos:pos + 3], "little")
        pos += 3
        block = data[pos:pos + size]
        if len(block) != size:
            raise ValueError("Truncated VOC payload")
        pos += size
        if kind == 1:
            if size < 2 or block[1] != 0 or block[0] == 255:
                raise ValueError("Unsupported VOC codec")
            current = round(1_000_000 / (256 - block[0]))
            if rate is not None and current != rate:
                raise ValueError("Changing sample rate is unsupported")
            rate = current
            pcm.extend(block[2:])
        elif kind == 2:
            if rate is None:
                raise ValueError("VOC continuation before sound data")
            pcm.extend(block)
        elif kind == 3:
            if size != 3 or rate != round(1_000_000 / (256 - block[2])):
                raise ValueError("Unsupported VOC silence block")
            pcm.extend(b"\x80" * (int.from_bytes(block[:2], "little") + 1))
        elif kind not in (4, 5):
            raise ValueError("Unsupported VOC block type: " + str(kind))
        if len(pcm) > 1_000_000:
            raise ValueError("VOC sample exceeds size limit")
    raise ValueError("Too many VOC blocks")

def extract(data):
    if not 1024 <= len(data) <= 65535:
        raise ValueError("Unsupported BLASTER.SND size")
    offsets, cursor = [], 0
    while True:
        cursor = data.find(MAGIC, cursor)
        if cursor < 0:
            break
        offsets.append(cursor)
        cursor += len(MAGIC)
    if len(offsets) != 7:
        raise ValueError("Expected exactly seven embedded VOC samples")
    table = struct.pack("<7H", *offsets)
    table_offset = data.find(table)
    if table_offset < offsets[-1] or data.find(table, table_offset + 1) != -1:
        raise ValueError("Unsupported or ambiguous effect pointer table")
    if b"\x8b\xbf" + struct.pack("<H", table_offset) not in data[:offsets[0]]:
        raise ValueError("Driver does not reference the expected sample table")
    effects = {}
    for index, (name, offset) in enumerate(zip(EFFECTS, offsets)):
        rate, pcm, end = decode_voc(data, offset)
        boundary = offsets[index + 1] if index < 6 else table_offset
        if end > boundary:
            raise ValueError("Overlapping VOC samples")
        output = io.BytesIO()
        with wave.open(output, "wb") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(1)
            wav.setframerate(rate)
            wav.writeframes(pcm)
        effects[name] = (output.getvalue(), rate, len(pcm))
    return effects

def read_source(source, include_game):
    wanted = {"BLASTER.SND"} | (set(REQUIRED) if include_game else set())
    found = {}
    if source.is_dir():
        entries = list(source.rglob("*"))
        if len(entries) > 4096:
            raise ValueError("Too many source entries; select one game folder")
        for path in entries:
            name = path.name.upper()
            if path.is_file() and name in wanted:
                if name in found or path.stat().st_size > 8 * 1024 * 1024:
                    raise ValueError("Duplicate or oversized source file: " + name)
                found[name] = path.read_bytes()
    elif source.suffix.lower() == ".zip":
        with zipfile.ZipFile(source) as archive:
            if len(archive.infolist()) > 4096:
                raise ValueError("Too many ZIP entries")
            for entry in archive.infolist():
                name = entry.filename.replace("\\", "/").rsplit("/", 1)[-1].upper()
                if name in wanted and not entry.is_dir():
                    if name in found or entry.file_size > 8 * 1024 * 1024:
                        raise ValueError("Duplicate or oversized source file: " + name)
                    found[name] = archive.read(entry)
    elif source.name.upper() == "BLASTER.SND":
        if source.stat().st_size > 65535:
            raise ValueError("Unsupported driver size")
        found["BLASTER.SND"] = source.read_bytes()
    else:
        raise ValueError("Select a PC/DOS folder, ZIP, or BLASTER.SND")
    if "BLASTER.SND" not in found:
        raise ValueError("BLASTER.SND is missing from your copy")
    if include_game:
        for name, size in REQUIRED.items():
            if not size <= len(found.get(name, b"")) <= size + 1:
                raise ValueError("Missing or invalid game data: " + name)
    return found

def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="Your own PC/DOS folder, ZIP or BLASTER.SND")
    parser.add_argument("--output", type=Path, required=True, help="New private output directory outside this repository")
    parser.add_argument("--all-modes", action="store_true", help="Use the Sound Blaster samples for all five compatibility filename sets")
    parser.add_argument("--make-game-zip", action="store_true", help="Include your required game data in a private importable ZIP")
    args = parser.parse_args(argv)
    try:
        root = Path(__file__).resolve().parents[1]
        output = args.output.resolve()
        if output.is_relative_to(root):
            raise ValueError("Keep extracted game audio outside the source repository")
        if output.exists():
            raise ValueError("Output already exists; choose a new directory")
        source = read_source(args.source, args.make_game_zip)
        effects = extract(source["BLASTER.SND"])
        aliases = SUFFIXES if args.all_modes else SUFFIXES[:1]
        (output / "audio").mkdir(parents=True)
        for name, (wav, rate, frames) in effects.items():
            for suffix in aliases:
                (output / "audio" / (name + "-" + suffix + ".wav")).write_bytes(wav)
        manifest = {"source": "BLASTER.SND", "source_sha256": hashlib.sha256(source["BLASTER.SND"]).hexdigest(),
                    "format": "Original unsigned 8-bit mono PCM, converted from embedded Creative VOC to WAV; no resampling",
                    "rights": "Extracted original audio retains its original rights. The new generated-audio CC0 dedication does not apply.",
                    "compatibility_aliases": list(aliases),
                    "effects": {name: {"sample_rate": rate, "frames": frames} for name, (_, rate, frames) in effects.items()}}
        (output / "EXTRACTION.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
        if args.make_game_zip:
            with zipfile.ZipFile(output / "Supaplex-your-data.zip", "w", zipfile.ZIP_DEFLATED) as archive:
                for name in REQUIRED:
                    archive.writestr(name, source[name])
                for path in sorted((output / "audio").glob("*.wav")):
                    archive.write(path, "audio/" + path.name)
        print("Extracted 7 original Sound Blaster effects to", output)
        if args.make_game_zip:
            print("Import Supaplex-your-data.zip with the Android app's Choose ZIP option.")
        return 0
    except (OSError, ValueError, zipfile.BadZipFile, struct.error) as error:
        parser.exit(1, "Extraction failed: " + str(error) + "\n")

if __name__ == "__main__":
    raise SystemExit(main())
