# Vantage

**Client side · Visual only · Does not affect gameplay**

Vantage improves camera control so you don't have to keep pressing F5. Right now it handles perspective changes while riding an animal, boat or minecart. Quality of life features too! More is planned: see Future plans below.

## Features

**Camera angles:** set up your own ranges of look angles, each with its own camera perspective. Where two ranges overlap, the current view holds until you have clearly left it, so it doesn't flicker at the border. Ranges on a higher layer overrule the ones on lower layers.

Default setup: look down for third person view, look straight ahead for first person and look up for selfie mode!

**Quality of life:**

- **Smooth glide back:** when a wall or tree pushes the camera close to you, it glides back out once there is room again instead of snapping. You choose how long the glide takes.
- **Auto first person:** when there is barely any room behind you, Vantage switches to first person. Once there is space again, your view comes back.

## Future plans

- Over-the-shoulder view
- Elytra flight

## Requirements

- Minecraft 26.3 with Fabric Loader and Fabric API
- Mod Menu (optional)

## Known issues

- Flicking the mouse quickly across the border between two ranges can flash the other view for a moment.

Found a bug? Report it on [GitHub](https://github.com/MKrabs/vantage/issues).

Request a feature? Ask on [GitHub](https://github.com/MKrabs/vantage/issues).

Wanna talk about anything else? You guessed it, [GitHub](https://github.com/MKrabs/vantage/issues).

Made with ❤️ by MKrabs

<!-- Modrinth description ends here; the rest is for GitHub only. -->

## Building

Want to build the mod yourself? Install JDK 25 (the Java Development Kit), then run `./gradlew build` in the project folder. The finished mod is `build/libs/vantage-<version>.jar` (not the `-sources` one); put it in your `mods` folder.

## License

MIT, see [LICENSE](LICENSE).
