# Vantage

Client-only Fabric mod for Minecraft 26.3: switches camera views while riding, by pitch ranges.

- **Design** (what the mod does and why, every agreed decision): [docs/design.md](docs/design.md). It is the source of truth; update it in the same change whenever a decision changes.
- **Platform facts** (versions, camera hooks, library availability, with sources): [docs/research/fabric-26.3.md](docs/research/fabric-26.3.md). Read before touching mixins or dependencies.

## Build, test, run

Gradle needs JDK 25, which is not on PATH. Prefix every Gradle call:

```bash
export JAVA_HOME="$(cygpath -w ~/.jdks/jdk-25)"; ./gradlew test --no-daemon -q
```

- `build` compiles and runs the tests; `runClient` opens a dev game with Mod Menu. Run `runClient` in the background and watch its log for `Sound engine started` (ready) and `Minecraft Crash` / `Mixin` errors.
- The user's real game is the Modrinth profile `Fabric 26.3` (Loader 0.19.5), so `fabric.mod.json` keeps `fabricloader >=0.19.5` even though the build uses a newer loader.

## Checking 26.3 APIs

26.3 is unobfuscated and many GUI names changed from older versions (`GuiGraphicsExtractor`, `extractRenderState`, `Minecraft.gui.setScreen`). Memory of older APIs is unreliable: confirm every class and method with `javap` before writing code against it.

```bash
~/.jdks/jdk-25/bin/javap -p -cp ~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar net.minecraft.client.Camera
```

Use `javap -c` to read what vanilla code actually calls. Library jars (Mod Menu) live under `~/.gradle/caches/modules-2`.

## Code map

`src/client/java/de/mkrabs/vantage/`:
- `ViewSwitcher` (range rules), `CloseCamera` (forced first person), `CameraEase` (distance smoothing): plain logic, no game state, fully unit-tested.
- `VantageConfig`: settings, `config/vantage.json` load/save, `sanitize()` for hand-edited files.
- `VantageClient`: per-tick glue that feeds the logic and applies views. `mixin/CameraMixin`: measures room behind the head and eases the distance.
- `config/`: own settings screen (`VantageScreen`, `RadarWidget`, sliders) and the optional Mod Menu entry.

Angles in config and UI are +90 up, -90 down; Minecraft's pitch is the inverse, flipped once in `VantageClient`.

## How this user works

- New features start with a **grill**: numbered question rounds, each with a recommended answer, until every decision is settled. The user decides; record the outcome in `docs/design.md` before coding.
- The user values the simplest design that covers the need and will push back on extra mechanisms; look for a solution inside the existing model first.
- Logic is built **test-first** at seams agreed with the user: one failing test, minimal code, repeat. UI and game hookup are checked by building and letting the user play; they report how it feels.
- Explain in plain language: no unexplained jargon, translate technical terms for a player, not a Java developer.
- Commit each finished step on `main`; throwaway experiments go on a `prototype/*` branch.
