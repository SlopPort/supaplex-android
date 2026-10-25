"""Reject original-game payloads in tracked port files and an optional APK.

Dependency submodules retain their own licensed test assets. This is a mechanical
packaging guard, not a legal audit of every source line or dependency.
"""
import re
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATTERN = re.compile(r"\.(?:dat|bin|snd|sp|wav|xm|ogg|at3|png|jpe?g|bmp|gif|ico|icns|xcf|i64|zip)$", re.I)

def forbidden(name):
    return (name.startswith(("resources/", "android/app/src/main/assets/game/"))
            or bool(PATTERN.search(name))
            or name in ("open-supaplex.svg", "linux/assets/io.github.sergiou87.open-supaplex.svg"))

names = subprocess.check_output(["git", "ls-files", "-z"], cwd=ROOT).decode().split("\0")
bad = [name for name in names if name and forbidden(name)]
if bad:
    raise SystemExit("Forbidden game-data/artwork/archive paths:\n" + "\n".join(bad))
if len(sys.argv) > 1:
    with zipfile.ZipFile(sys.argv[1]) as apk:
        assets = [n for n in apk.namelist() if n.startswith("assets/") and not n.endswith("/")]
        if any(not n.startswith("assets/licenses/") for n in assets):
            raise SystemExit("APK contains assets other than dependency licenses")
        if not assets:
            raise SystemExit("Dependency license assets are missing")
print("PASS: tracked port has no forbidden game payloads; optional APK contains license assets only")
