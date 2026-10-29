# Vantage

Vantage switches your camera view automatically while you ride, based on where you look. Look straight ahead and you see through your own eyes, look down and the camera moves behind you, look up and it turns around for a selfie. Get off and your old view comes back.

It works on anything you can sit on: horses, donkeys, camels, pigs, striders, boats, minecarts, modded mounts and furniture seats. On foot nothing changes.

## Out of the box

- Looking roughly straight ahead (between +20° and -20°): first person.
- Looking down: third person, from behind.
- Looking up past +60°: selfie view. It stays until you look below +20° again, so it doesn't flicker at the edge.
- Pressing F5 yourself still works. Your choice sticks until you look into a different range.
- When you get off, die or get thrown off, you get back the view you had before you got on.

## Make it your own

The view is picked from **ranges**. A range says "between these two angles, use this view". You can have up to 8 of them, and a radar in the settings shows them as coloured arcs around a little head, with a line for where you are looking right now.

Ranges sit on **layers** 1 to 5. A range on a higher layer wins while you look into it. Ranges on the same layer can overlap: the one you are already in keeps going until you leave it, which stops the view from jumping back and forth at the border.

## Tight spaces

When a wall or a tree pushes the third person camera closer to you, vanilla snaps it back out the moment there is room again. Vantage lets it glide back out instead, over a time you choose (0 to 2 seconds, or off).

Optionally, Vantage can switch to first person when the camera gets squeezed closer than a set distance, and go back once there is room again. This is off by default.

## Settings

Open the settings through [Mod Menu](https://modrinth.com/mod/modmenu). Without Mod Menu there is no settings screen; you can still edit `config/vantage.json` by hand. Changes show up instantly; **Done** saves, **Cancel** undoes everything since you opened the screen.

## Requirements

- Minecraft 26.3 with Fabric Loader 0.19.5 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25
- Optional: [Mod Menu](https://modrinth.com/mod/modmenu) for the settings screen

Vantage runs only on your side (client only). It works on any server, also ones without the mod.

## Known issues

- Flicking the mouse quickly across a range border can flash the other view for a moment.

## Building

Needs JDK 25. `./gradlew build` puts the jar in `build/libs/`.

## License

MIT, see [LICENSE](LICENSE).
