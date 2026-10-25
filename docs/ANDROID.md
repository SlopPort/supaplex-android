# Android play guide

Start with the [installation instructions](../README.md#install-and-start-playing). This guide refers to version 1.2. Its Android dialogs are Polish; the table gives their English meanings.

## Pause menu labels

Open the menu with pause at the top right, the three dots at the top left, or START on a gamepad. Gameplay and its timer stop while the menu is open. Scroll inside the menu to reach the lower settings.

| Label in the APK | Meaning |
| --- | --- |
| Pauza i zapis gry | Pause and save game |
| Zapisz postęp poziomu | Save the current level progress; keep the game open |
| Zapisz i zakończ | Save the current level progress and close the game |
| Wznów zapisany poziom | Load the last manual save; its date and time are shown |
| Wstecz | Send Escape to the original game; this can leave the level |
| Wróć do gry | Close the menu and continue playing |
| Ukryj przyciski dotykowe | Hide touch controls |
| Rozmiar sterowania | Touch-control size |
| Widoczność przycisków | Touch-control opacity |
| Układ dla leworęcznych | Left-handed layout |
| Zachowaj proporcje obrazu | Preserve original picture proportions |
| Ukrywaj dotyk po użyciu pada | Hide touch controls after controller input |
| Tak / Y; Nie / N | Answer Yes or No to a prompt in the original game |

**Wstecz is not the resume button.** Choose **Wróć do gry** to close the pause menu safely.

## Saving and resuming

Save during a live level with **Zapisz i zakończ**. After opening the app again, choose **Wznów** at the startup question. **Menu gry** opens the original menu instead; it does not delete the save.

**Zapisz postęp poziomu** writes the same slot without closing the app. The menu remains open, so the game stays paused. Close it with **Wróć do gry** to continue. If the resume button was disabled before your first save, reopen the menu to refresh it.

Loading with **Wznów zapisany poziom** during a live game asks for confirmation: **Wczytaj** means Load, **Anuluj** means Cancel. Loading replaces changes made since your last save. The slot is shared by all players and levels, so saving a different run overwrites it.

This is a manual continuation, not autosave or cloud sync. The state includes the board and objects, Murphy's position and animation state, items, time, random state, level, and player. It uses `CONTINUE.SAV` in the app's private data, separate from upstream's debug `SAVEGAME.SAV`. Loading a continuation does not itself set the cheating flag; a run already marked as cheating keeps that status.

The previous save is replaced only after the new temporary file has been written, flushed, and synchronized successfully. A failed save reports an error and keeps the previous slot. Do not uninstall to troubleshoot a save problem.

## Gamepads

Connect a controller in Android's Bluetooth settings or through a supported USB connection. The game uses Android/SDL mappings; behavior can vary by controller and firmware.

| Logical button | Action |
| --- | --- |
| D-pad / left stick | Move; navigate the original menu |
| X | Action / collect beside Murphy; hold to place a red disk |
| A | Confirm |
| B | Original engine secondary/back action; effect depends on context |
| START | Open or close the Android pause/save menu |
| Capture / MODE or hardware MENU | Open or close the same menu, when Android delivers this key |
| BACK / SELECT | Leave the level; this does not save it |
| Y | Toggle the original status panel |
| L1 / R1 | Change speed during play, or level set in the original menu |
| HOME | Android system home screen |

Letters are logical Android/SDL names, not a guarantee about the printed letters. Nintendo layouts can differ from Xbox layouts. Stick movement and action were manually checked with a controller reported as **Pro Controller (057e:2009)**; automated input tests cover the Android pause-menu handling.

Touch controls hide after gamepad input when that option is enabled. A small button remains for restoring touch controls. Disconnecting the final controller restores touch mode. Android HOME cannot be repurposed as an in-game menu key.

## Updates and save data

Updating the same package (`pl.offline.supaplex`) with the **same signing key** preserves private save data and settings. Uninstalling or using Android's **Clear storage / Clear data** removes them. There is no in-app export or cloud backup feature.

The downloadable 1.2 APK uses an Android debug key. A build made on another computer normally has a different key even if the package name matches. Android may reject that update with a signature mismatch. Keep the existing installation and its data; do not uninstall unless you accept losing that data. Developers should use a stable private release key for future distribution and never commit it.

Returning through the launcher after HOME is different from a persistent save: Android can terminate an app in the background. Make a manual save before relying on a later return.

## Troubleshooting

- **Save buttons disabled:** start a live level. Menus, demos, and death/end sequences cannot be saved.
- **No resume prompt:** no manual continuation was found. Check that saving succeeded and app data was not cleared.
- **Stretched picture:** enable **Zachowaj proporcje obrazu** in the pause menu.
- **Controller letters differ:** test logical positions; report the model, Android version, and working controls. Do not post Bluetooth addresses or serial numbers.
- **Install/update fails:** check the Android version, storage, and signing-key explanation before removing anything.

For bug reports, include app version, device model, Android version, and reproduction steps. Avoid names, device identifiers, personal saves, and unredacted logs.
