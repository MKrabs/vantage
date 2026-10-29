# Vantage - Auto Camera for Mounts

Mod ID: `vantage`. Fabric, Minecraft 26.3, client-only. Technical facts: [research/fabric-26.3.md](research/fabric-26.3.md).

## When it is active

- While riding anything: horses, boats, minecarts, pigs, striders, camels, modded mounts, furniture seats (chairs, cushions).
- Never on foot, never in spectator.

## Pitch zones (mounted only)

Pitch: -90° straight up, +90° straight down.

| Zone | Default view | Enter | Leave |
|---|---|---|---|
| Looking down | First person | past +65° | above +35° |
| Middle | Third person back | on mount | - |
| Looking up | Third person front (selfie) | past -75° | below -45° |

- Acts only when a line is crossed; never forces a view continuously. Manual F5 sticks until the next crossing.
- On mount, use the view of the zone the player is already looking into.
- Selfie is "off" when the up zone's view is set to third person back.

## Dismount and edge cases

- Restore the view from before the first mount (also across mount-to-mount switches, death, being thrown off).
- Disabling the mod mid-ride restores that view at once.

## Tight spaces

- Camera snaps in instantly when blocked, eases back out over the configured time. Leaves block as in vanilla.

## Settings (Mod Menu + YACL)

- Mod on/off (no keybind).
- View per zone (first person / third back / third front).
- The 4 angles (down enter/leave, up enter/leave).
- Pitch bar widget: three coloured zones, shaded buffers, live marker for the current pitch.
- Ease-out time 0-2 s, default 0.5 s, 0 = instant.

## Out of scope

Whitelists, keybind, camera through leaves, auto first person in tight spaces.

## Prototype result (branch `prototype/hooks`)

Confirmed in game on 26.3: mount detection by polling `getVehicle()` each tick, switching via `Options#setCameraType` + `checkEntityPostEffect`, pitch from `getXRot()`, and an `@Inject` at RETURN of `Camera#getMaxZoom` all work. Mount/dismount/restore held on donkey, horse, boat and minecart, including being thrown off.

Findings:
- The default angles felt bad to play with (selfie at -75° needs looking almost straight up). Defaults stay as a starting point; the settings screen is where they get tuned.
- A quick mouse flick briefly flashes the other view. Not addressed yet.
- Forest easing and manual F5 were not judged.

## Open

- **Elytra**: should gliding trigger it like a mount? Decide after the mount version works. [Auto Third Person](https://modrinth.com/mod/auto-third-person-official) already does mounts + elytra, so elytra is expected by users.
