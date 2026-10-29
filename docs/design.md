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
- **Too close: first person** (optional, off by default): while riding in third person or selfie, if the camera is pushed closer than 1.5 blocks, force first person. Back to the wanted view once there is more than 3 blocks of room and at least 2 s have passed. All three values are settings.
  - In first person vanilla never measures, so `CameraMixin` calls `getMaxZoom` itself at the end of `alignWithEntity`.
  - Known limit: in first person it measures behind the head, also when the wanted view is selfie (which looks from the front). The minimum time covers any bouncing.
  - Manual F5 while forced is overridden until the force ends.

## Settings (own screen, Mod Menu optional)

- Fixed, readable width; values update instantly; no collapsible groups. YACL dropped.
- Everything above the bottom buttons scrolls (mouse wheel) when the window is too small; the bottom buttons stay pinned.
- Sections separated by horizontal rulers with a small title on the line, half a row of space below each title. Top to bottom:
  - **Vantage enabled** checkbox, alone on the first row (no keybind). When off, everything else is greyed out and does nothing; the radar fades. Cancel / Reset / Done stay usable.
  - **Ranges:** the radar, one row per range, add.
    - Radar (display only for now): head on the left, half circle +90..-90, one band per layer (layer 1 innermost), a beam with the current angle as a label.
    - Bands shown: layers 1 up to the highest layer in use, nothing beyond (only layer 3 used still shows 1, 2, 3). The bands share the full radius, so one layer fills the whole half circle.
    - Ranges are thick arcs in their view's colour, centred in their band with a margin; ranges sharing a band sit side by side.
    - Dark grey grid: the half circle's outline, a semicircle between bands, and spokes at +45, 0 and -45.
    - Height: 180 px for 1-2 bands, +20 px per extra band (240 px with 5).
  - **Tight spaces**, in rows:
    1. Too-close camera checkbox.
    2. Ease-out time (0-2 s, default 0.5 s, 0 shows Off; snaps to 0.2 / 0.5 / 0.667 / 1 when close, hold Shift for any value, hint under it) | stay at least (seconds).
    3. Switch below | back above (blocks, half-block steps).
    The three too-close values are greyed out while the checkbox is off; ease-out is its own feature and stays active.
- Sliders move in their steps: the handle jumps to the value, it does not glide between steps.
- Bottom: **Cancel** (revert everything since the screen opened), **Reset to defaults** (second click confirms), **Done** (saves; Esc too). No step-by-step undo: Cancel covers it, revisit if missed.

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

## Roadmap

1. **1.0 (released):** everything above.
2. **Next version:** shoulder view (left/right) as another view choice in the ranges; elytra (below).
3. **Later, each grilled on its own first:**
   - **Minecart camera follows the track.** Risk: quick successive turns cause motion sickness. Simple start: follow the travel direction heavily smoothed (1-2 s) so short wiggles barely move it. Complex: average the track direction a few blocks ahead. Open: turn only the camera, or also the player's look direction (that one fights the mouse).
   - **Fishing camera:** follow the bobber in flight like a goal replay, then pan to a view from the bobber looking back at the player ("second person") or a bird's-eye view. Check whether that view would be blocked before using it. After it lands in the water, a fun text with how far it landed from the player. Not tied to mounts, but the user first pictured it fishing from a boat.
   - **Boat camera:** third person over a shoulder (the user suggested a random shoulder), or the second-person view above; likely covered by the shoulder view.
   - The user's own words for these ideas: "QoL features" for minecart and boat, "fun idea" for the fishing camera, so it is lower priority.

## Open

- Radar editing by dragging arc ends, if read-only feels limiting.
- A quick mouse flick past a range edge briefly flashes the other view.
- **Elytra**: should gliding trigger it like a mount? Decide after the mount version works. [Auto Third Person](https://modrinth.com/mod/auto-third-person-official) already does mounts + elytra, so elytra is expected by users.
