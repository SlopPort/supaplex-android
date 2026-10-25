# Touch controls and language — version 1.5

This guide includes development build 1.5. The published 1.4 APK has five layouts; the two-D-pad layout and position/spacing sliders require 1.5.

Choose **Polski** or **English** at first launch. The choice affects Android buttons, dialogs, hints, and the HUD description. It does not translate the original DOS menu artwork. To change it later, open pause and select **Language / Język**. Gameplay remains paused and the Activity is not recreated for a language change.

## Choose a layout

Open **Pause → Touch controls** (**Pauza → Sterowanie dotykowe**). Scroll within the settings page for all six choices and the tuning sliders. A selected style is saved immediately; close the settings page and return to the game to try it.

| English label | Polish label | Behavior |
| --- | --- | --- |
| Four buttons — precise | Cztery przyciski — precyzyjne | Four distinct arrows. Each button means exactly one direction. The centre and gaps do nothing, so sliding through a gap stops directional input. |
| Fixed joystick | Gałka stała | The base stays in place. Move beyond its dead zone, return to the centre to stop, or lift. |
| Floating joystick | Gałka pod palcem | During a live level, touch the lower movement side to anchor the base under the thumb, then drag. The base does not chase the thumb. In original menus, start inside the visible movement circle so menu taps outside it remain available. |
| Swipe and hold — locked direction | Gest i przytrzymanie — blokada kierunku | Start within the movement circle and swipe to choose one direction. Keep the finger down to continue; lift to stop. Thumb drift or returning to the origin does not change the chosen direction. Lift and swipe again to turn. |
| Split: up/down &#124; left/right | Osobno: góra/dół &#124; lewo/prawo | Up/down buttons on the left and left/right buttons on the right. Left-handed mode mirrors them. |
| Two D-pads — one on each side | Dwa krzyżaki — po jednym z każdej strony | Two complete four-direction pads. Either thumb can choose any direction; the latest direction press wins. ACTION is above each pad. |

**Four buttons** is the default. The split layout is an alternative for players who prefer to assign each axis to a different thumb.

## Split layout: two thumbs, one direction

Supaplex accepts four directions, not diagonal movement. If one thumb holds Up and the other then presses Right, **Right wins**. When Right is released, **Up resumes if it is still held**. Moving to a different button also counts as a new direction press. Centre gaps stop that side.

ACTION appears above the direction buttons on **both sides** in this layout. Hold the right ACTION with left-side Up/Down, or the left ACTION with right-side Left/Right. This avoids needing a third finger for collecting beside Murphy. The check-mark confirmation button remains available above the right-side directions in the standard layout.

## Position and space the controls

These settings are available in **Pause → Touch controls**:

- **Move controls inward from screen edges:** 0–100%, default 0%. Move the control groups away from the outer edges and toward the middle; reduce it to move them back toward the edges. In the two-pad layouts, both groups move symmetrically.
- **Left/right arrow spacing:** 90–180%, default 100%. Change the distance between the left and right arrows without changing button size.
- **Up/down arrow spacing:** 90–180%, default 100%. Independently change the distance between the top and bottom arrows.

Arrow spacing applies to the four-button, split, and two-D-pad layouts. It does not change joystick response. Layout limits can restrict further movement or spacing at large button sizes to keep the arrows on screen and the two movement groups apart. Reset touch settings also resets these three sliders.

## Two full D-pads

Choose **Two D-pads — one on each side** to give both thumbs access to all four directions. Centres and gaps are neutral. If you hold Up on one pad and then press Right on the other, Right wins; releasing Right resumes Up while it is still held. ACTION is above each pad, and the check-mark confirmation button is between them. Direction size, spacing, opacity, and edge position apply to both pads.

## Tune a joystick

- **Dead zone:** 8–38% of movement radius, default 20%. Larger values ignore more small movement near the origin. This affects both joysticks and the initial swipe, not the arrow/split buttons.
- **Direction stability:** 0–100%, default 45%. Larger values require a clearer change of axis before a joystick switches between horizontal and vertical. Setting zero gives immediate dominant-axis selection. It does not delay release or returning to centre. This setting affects fixed/floating joysticks only.
- **Direction size / Rozmiar kierunków:** 75–220%, default 100%. Resizes the movement arrows or joystick only.
- **ACTION button size / Rozmiar przycisku AKCJI:** 75–160% of the new compact default, independently of direction size. Both copies in the split and two-D-pad layouts use this setting. Confirm remains compact.
- **Button opacity / Widoczność przycisków:** 10–100%, default 64%. Lower values make the controls more transparent; touch behavior is unchanged. This is an overall visibility factor, not a promise that every painted pixel becomes fully opaque at 100%.
- Left-handed layout mirrors the positions; auto-hide can switch to the gamepad HUD after controller input.
- **Reset touch settings:** restores the default four-button layout and touch tuning. It preserves the language, picture proportions, and saved game.

Changes are remembered across app launches. The displayed percentages are settings values, not analog movement speeds. The original engine still determines movement and animation timing. A started tile movement can finish after release; these layouts do **not** guarantee exactly one tile per tap.

## Design references from Android games

Reviewed on 2026-10-25 using publisher/developer sources:

| Reference | Documented interaction idea | Adaptation here |
| --- | --- | --- |
| [Minecraft controls](https://www.minecraft.net/en-us/article/minecraft-controls) and [Bedrock control-scheme notes](https://www.minecraft.net/en-us/article/minecraft-preview-1-21-130-26) | Touch-control choices including directional-pad and joystick schemes. | Selectable arrow and joystick layouts rather than one mandatory scheme. |
| [Brawl Stars customizable controls](https://supercell.com/en/games/brawlstars/blog/release-notes/optional-update-customizable-controls/) | A dedicated control-settings interface. | A separate touch-settings page with saved style, size, opacity, and tuning. This is not a replica of its editor. |
| [Dead Cells developer listing on Google Play](https://play.google.com/store/apps/details?id=com.playdigious.deadcells.mobile&hl=en) | Custom touch-control sizing and gesture alternatives. | Optional swipe-and-hold movement and sizing. Supaplex does not gain a dodge, auto-hit, or other combat mechanics. |

These are interaction references, not copied assets or source code. Exact dead-zone/hysteresis values, the floating-anchor behavior, and the split-axis policy are implementation choices for this port. The other games were not installed or benchmarked during this review; comfort and precision here still require play feedback.
