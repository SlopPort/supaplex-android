# Android play guide

Start with the [installation instructions](../README.md#install-and-start-playing). This guide refers to version 1.3. On first launch, choose Polski or English. The choice is remembered and can be changed through Pause → Language / Język without restarting gameplay. Original DOS menus remain English. The table below pairs the Polish labels with their English meanings.

## Pause menu labels

Open the menu with pause at the top right, the three dots at the top left, or START on a gamepad. Gameplay and its timer stop while the menu is open. Scroll inside the menu to reach the lower settings.

| English button label | Polish button label | Purpose |
| --- | --- | --- |
| Pause and save | Pauza i zapis gry | Pause menu title |
| Save level progress | Zapisz postęp poziomu | Save and keep the app open |
| Save and quit | Zapisz i zakończ | Save and close the app |
| Resume saved level | Wznów zapisany poziom | Load the manual save, identified by its date/time |
| Back / leave level | Wstecz / opuść poziom | Send Escape to the original game; may leave the level |
| Return to game | Wróć do gry | Close the pause menu and continue |
| Hide touch controls | Ukryj przyciski dotykowe | Switch to the minimal touch HUD |
| Touch controls | Sterowanie dotykowe | Open movement styles and touch settings |
| Language / Język | Język / Language | Choose Android UI language |
| Control size | Rozmiar sterowania | Resize controls |
| Button opacity | Widoczność przycisków | Change visibility |
| Left-handed layout | Układ dla leworęcznych | Mirror controls |
| Preserve picture proportions | Zachowaj proporcje obrazu | Keep original aspect ratio |
| Hide touch after gamepad input | Ukrywaj dotyk po użyciu pada | Automatically switch HUD |
| Yes / Y; No / N | Tak / Y; Nie / N | Answer original-game prompts |

**Back / leave level (Wstecz / opuść poziom) is not the resume button.** Choose **Wróć do gry** to close the pause menu safely.

For movement styles and the split layout, see [TOUCH-CONTROLS.md](TOUCH-CONTROLS.md). Size, opacity, left-handed layout, auto-hide, dead zone, and stability are on that separate settings page.

## Saving and resuming

Save during a live level with **Save and quit / Zapisz i zakończ**. After opening the app again, choose **Resume / Wznów** at the startup question. **Game menu / Menu gry** opens the original menu instead; it does not delete the save.

**Save level progress / Zapisz postęp poziomu** writes the same slot without closing the app. The menu remains open, so the game stays paused. Close it with **Return to game / Wróć do gry** to continue. If the resume button was disabled before your first save, reopen the menu to refresh it.

Loading with **Resume saved level / Wznów zapisany poziom** during a live game asks for confirmation: choose **Load / Wczytaj** to confirm or **Cancel / Anuluj** to keep playing. Loading replaces changes made since your last save. The slot is shared by all players and levels, so saving a different run overwrites it.

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

The downloadable 1.2 and 1.3 APKs use the same Android debug key, so 1.3 can update 1.2 without clearing saves. A build made on another computer normally has a different key even if the package name matches. Android may reject that update with a signature mismatch. Keep the existing installation and its data; do not uninstall unless you accept losing that data. Developers should use a stable private release key for future distribution and never commit it.

Returning through the launcher after HOME is different from a persistent save: Android can terminate an app in the background. Make a manual save before relying on a later return.

## Troubleshooting

- **Save buttons disabled:** start a live level. Menus, demos, and death/end sequences cannot be saved.
- **No resume prompt:** no manual continuation was found. Check that saving succeeded and app data was not cleared.
- **Stretched picture:** enable **Preserve picture proportions / Zachowaj proporcje obrazu** in the pause menu.
- **Controller letters differ:** test logical positions; report the model, Android version, and working controls. Do not post Bluetooth addresses or serial numbers.
- **Install/update fails:** check the Android version, storage, and signing-key explanation before removing anything.

For bug reports, include app version, device model, Android version, and reproduction steps. Avoid names, device identifiers, personal saves, and unredacted logs.
