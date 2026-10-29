# Vantage - Auto Camera for Mounts

Mod ID: `vantage`. Fabric, Minecraft 26.3, client-only. Technical facts: [research/fabric-26.3.md](research/fabric-26.3.md).

## When it is active

- While riding anything: horses, boats, minecarts, pigs, striders, camels, modded mounts, furniture seats (chairs, cushions).
- Never on foot, never in spectator.

## Ranges (mounted only)

Angles in the settings: +90° straight up, -90° straight down (Minecraft's pitch is the inverse).

A range is: view (first person / third person / selfie), from and to angle, layer (1-5). Up to 8 ranges, zero allowed.

- **Same layer, latching:** the range you are in holds while you stay inside it, even where another range overlaps. Overlaps are the buffers.
- **Ties** (nothing held yet on that layer, e.g. on mount): the range held last on that layer if it still covers the angle, else the one higher in the list.
- **Higher layer wins** while you are inside one of its ranges, no buffer. Lower layers keep tracking underneath.
- **Angle covered by no range:** leave the view as it is.
- Acts only on changes; never forces a view continuously. Manual F5 sticks until the active range changes.

Defaults (list order matters for ties):

| # | View | From | To | Layer |
|---|---|---|---|---|
| 1 | Third person | 60 | -90 | 1 |
| 2 | Selfie | 90 | 20 | 1 |
| 3 | First person | 20 | -20 | 2 |

So selfie switches in above 60 and releases below 20 through the overlap, and looking straight ahead gives first person (chosen on purpose).

## Dismount and edge cases

- Restore the view from before the first mount (also across mount-to-mount switches, death, being thrown off).
- Disabling the mod mid-ride restores that view at once.

## Tight spaces

- Camera snaps in instantly when blocked, eases back out over the configured time. Leaves block as in vanilla.

## Settings (own screen, Mod Menu optional)

- Fixed, readable width; values update instantly; no collapsible groups. YACL dropped.
- Mod on/off (no keybind).
- Radar (display only for now): head on the left, half circle +90..-90, one arc per range in its view's colour, one ring per layer (layer 1 innermost), a beam for the current angle.
- One row per range with add and delete.
- Ease-out time 0-2 s, default 0.5 s, 0 = off. Snaps to 0.2 / 0.5 / 0.667 / 1 when close; hold Shift for any value (hint shown under the slider).

## Out of scope

Whitelists, keybind, camera through leaves, auto first person in tight spaces.

## Prototype result (branch `prototype/hooks`)

Confirmed in game on 26.3: mount detection by polling `getVehicle()` each tick, switching via `Options#setCameraType` + `checkEntityPostEffect`, pitch from `getXRot()`, and an `@Inject` at RETURN of `Camera#getMaxZoom` all work. Mount/dismount/restore held on donkey, horse, boat and minecart, including being thrown off.

Findings:
- The default angles felt bad to play with (selfie at -75° needs looking almost straight up). Defaults stay as a starting point; the settings screen is where they get tuned.
- A quick mouse flick briefly flashes the other view. Not addressed yet.
- Forest easing and manual F5 were not judged.

## Feedback on the first build (replaced by the range model above)

- Fixed up/middle/down zones were too rigid: no way to delete a zone, no angles past 0 in the up/down zones.
- Saving a corrected value did not refresh YACL's displayed value; the screen was too wide on big monitors.

## Open

- Radar editing by dragging arc ends, if read-only feels limiting.

- **Elytra**: should gliding trigger it like a mount? Decide after the mount version works. [Auto Third Person](https://modrinth.com/mod/auto-third-person-official) already does mounts + elytra, so elytra is expected by users.
