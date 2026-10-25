"""Generated fixtures only; no original game data. GPL-3.0."""
import importlib.util
import io
import struct
import tempfile
import unittest
import wave
import zipfile
from pathlib import Path

SPEC = importlib.util.spec_from_file_location("extractor", Path(__file__).with_name("extract-dos-audio.py"))
EXTRACTOR = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(EXTRACTOR)

def fixture():
    data = bytearray(2048)
    offsets, pos = [], 512
    for effect in range(7):
        offsets.append(pos)
        version = 0x10a
        pcm = bytes((128, 140 + effect, 160, 128, 96, 110, 128))
        payload = bytes((136, 0)) + pcm
        voc = EXTRACTOR.MAGIC + struct.pack("<HHH", 26, version, (~version + 0x1234) & 65535)
        voc += b"\x01" + len(payload).to_bytes(3, "little") + payload + b"\0"
        data[pos:pos + len(voc)] = voc
        pos += len(voc)
    data[pos:pos + 14] = struct.pack("<7H", *offsets)
    data[128:132] = b"\x8b\xbf" + struct.pack("<H", pos)
    return bytes(data)

class ExtractionTests(unittest.TestCase):
    def test_exact_unsigned_pcm(self):
        effects = EXTRACTOR.extract(fixture())
        self.assertEqual(len(effects), 7)
        for index, (name, (wav, rate, frames)) in enumerate(effects.items()):
            with wave.open(io.BytesIO(wav), "rb") as audio:
                self.assertEqual((audio.getnchannels(), audio.getsampwidth(), audio.getframerate()), (1, 1, 8333))
                self.assertEqual(audio.readframes(100), bytes((128, 140 + index, 160, 128, 96, 110, 128)))

    def test_rejects_unknown_driver(self):
        with self.assertRaises(ValueError):
            EXTRACTOR.extract(bytes(2048))

    def test_rejects_broken_dispatch_table(self):
        data = bytearray(fixture())
        data[128] = 0
        with self.assertRaises(ValueError):
            EXTRACTOR.extract(bytes(data))

    def test_rejects_compressed_voc(self):
        data = bytearray(fixture())
        data[512 + 26 + 5] = 1
        with self.assertRaises(ValueError):
            EXTRACTOR.extract(bytes(data))

    def test_rejects_truncated_voc(self):
        with self.assertRaises(ValueError):
            EXTRACTOR.decode_voc(fixture()[:540], 512)

    def test_rejects_bad_voc_checksum(self):
        data = bytearray(fixture())
        data[512 + 24] ^= 1
        with self.assertRaises(ValueError):
            EXTRACTOR.extract(bytes(data))

    def test_complete_private_import_zip(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source = root / "copy.zip"
            with zipfile.ZipFile(source, "w") as archive:
                archive.writestr("dos/BLASTER.SND", fixture())
                for name, size in EXTRACTOR.REQUIRED.items():
                    archive.writestr("dos/" + name, bytes(size))
            output = root / "converted"
            self.assertEqual(EXTRACTOR.main([str(source), "--output", str(output), "--all-modes", "--make-game-zip"]), 0)
            with zipfile.ZipFile(output / "Supaplex-your-data.zip") as archive:
                self.assertEqual(len(archive.namelist()), 15 + 35)
                self.assertNotIn("BLASTER.SND", archive.namelist())
                self.assertFalse(any(n.lower().endswith(".exe") for n in archive.namelist()))

    def test_rejects_duplicate_copy(self):
        with tempfile.TemporaryDirectory() as temp:
            source = Path(temp) / "copies.zip"
            with zipfile.ZipFile(source, "w") as archive:
                archive.writestr("one/BLASTER.SND", fixture())
                archive.writestr("two/BLASTER.SND", fixture())
            with self.assertRaises(ValueError):
                EXTRACTOR.read_source(source, False)

    def test_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source = root / "BLASTER.SND"
            source.write_bytes(fixture())
            output = root / "existing"
            output.mkdir()
            keep = output / "keep.txt"
            keep.write_text("unchanged")
            with self.assertRaises(SystemExit):
                EXTRACTOR.main([str(source), "--output", str(output)])
            self.assertEqual(keep.read_text(), "unchanged")

if __name__ == "__main__":
    unittest.main()
