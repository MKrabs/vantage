# Fabric mod research: Minecraft Java 26.3 (client-only mount camera mod)

Researched 2026-10-29. Every claim cites a source. Abbreviations used below:

- **CLIENT** = `C:\Users\Video-gamer\AppData\Roaming\ModrinthApp\meta\versions\26.3-0.19.5\26.3-0.19.5.jar`. Its SHA-1 is `e877b6a07acd633fb3bb475002175cec036e7b87`, which matches `downloads.client.sha1` in `26.3-0.19.5.json` next to it, so it is Mojang's vanilla client jar. I inspected a copy with `C:\Program Files\Java\jdk-19\bin\javap -p -c -l`.
- **PROFILE** = `C:\Users\Video-gamer\AppData\Roaming\ModrinthApp\profiles\Fabric 26.3\mods`. I copied the jars to `C:\Users\Video-gamer\AppData\Local\Temp\fr263\mods` and only unzipped them there. Nothing from them was executed and the profile was not modified.
- **EXAMPLE** = `https://github.com/FabricMC/fabric-example-mod`, branch `26.3`, commit `91f19010a4e764f193fe50acfe2c8ee68f1f15cd` (2026-10-24).

---

## 1. Build setup for 26.3

| Item | Value | Source |
|---|---|---|
| Example mod / template | `FabricMC/fabric-example-mod`, branch `26.3`. This is also the repo's default branch. Its commits are titled "Deployment from commit FabricMC/fabricmc.net@…", so the branch is generated from the fabricmc.net generator. | GitHub API `repos/FabricMC/fabric-example-mod` (default_branch=26.3); commit message of EXAMPLE |
| Template generator | Exists at https://fabricmc.net/develop/template/. Fabric docs say you fill in mod name, package name and Minecraft version. Advanced options are Kotlin, Kotlin buildscripts and data generators. You then extract the zip and import it as a Gradle project. | https://fabricmc.net/develop/template/ ; https://docs.fabricmc.net/develop/getting-started/creating-a-project (page labeled "26.2") |
| Loom plugin | `id 'net.fabricmc.fabric-loom' version "${loom_version}"` with `loom_version=1.19-SNAPSHOT`. The newest release on Fabric maven is **1.19.3**. | EXAMPLE `build.gradle`, `gradle.properties`; https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml (`<release>1.19.3`) |
| Loom plugin IDs | `net.fabricmc.fabric-loom` is for non-obfuscated versions (26.1+). `net.fabricmc.fabric-loom-remap` is for obfuscated versions (≤1.21.11). Legacy `fabric-loom` is kept only for backwards compatibility. | https://docs.fabricmc.net/develop/loom/ (page "Loom 26.2") |
| Gradle | **9.8.2** (`distributionUrl=…gradle-9.8.2-bin.zip`) | EXAMPLE `gradle/wrapper/gradle-wrapper.properties` |
| Java | **Java 25**. EXAMPLE sets `options.release = 25` and `sourceCompatibility/targetCompatibility = JavaVersion.VERSION_25`. Its fabric.mod.json declares `"java": ">=25"`, and the client mixin config uses `"compatibilityLevel": "JAVA_25"`. The 26.3 version manifest asks for `"javaVersion":{"component":"java-runtime-epsilon","majorVersion":25}`. Fabric docs say "you will need JDK 25" (page written for 26.1/26.2). | EXAMPLE `build.gradle`, `src/main/resources/fabric.mod.json`, `src/client/resources/modid.client.mixins.json`; `meta\versions\26.3-0.19.5\26.3-0.19.5.json`; https://docs.fabricmc.net/develop/getting-started/setting-up |
| Fabric Loader | Latest stable for 26.3 is **0.20.1** (EXAMPLE `loader_version=0.20.1`; meta.fabricmc.net lists it first and `stable:true`). The local Modrinth profile runs **0.19.5**. Fabric API 0.164.0 requires `fabricloader >=0.19.3`. | EXAMPLE `gradle.properties`; https://meta.fabricmc.net/v2/versions/loader/26.3 ; `26.3-0.19.5.json` (`net.fabricmc:fabric-loader:0.19.5`); fabric.mod.json inside `fabric-api-0.164.0+26.3.jar` |
| Fabric API | EXAMPLE uses **0.165.0+26.3**, the newest 26.3 build on maven. The profile has **0.164.0+26.3**, which declares `minecraft: "~26.3-"`, `java: ">=25"`. | EXAMPLE `gradle.properties`; https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml ; fabric.mod.json in the PROFILE jar |
| Dependencies block | `minecraft "com.mojang:minecraft:${minecraft_version}"`, `implementation "net.fabricmc:fabric-loader:…"`, `implementation "net.fabricmc.fabric-api:fabric-api:…"`. There is **no `mappings` line**, and it uses `implementation`, not `modImplementation`. | EXAMPLE `build.gradle` |
| Source sets | `loom { splitEnvironmentSourceSets() … }`. Client code goes in `src/client/java`, with mixin config `modid.client.mixins.json` (`"client": [...]`, `defaultRequire: 1`). | EXAMPLE `build.gradle`, tree listing |
| fabric.mod.json | `schemaVersion: 1`, plus `id`, `version: "${version}"`, `environment`, `entrypoints.main` and `entrypoints.client`, and `mixins` (a string entry, or an object `{config, environment:"client"}`). `depends` is `fabricloader >=0.20.1`, `minecraft ~26.3`, `java >=25`, `fabric-api *`. For a client-only mod, use `"environment": "client"` (UNVERIFIED in docs; EXAMPLE uses `"*"`). | EXAMPLE `src/main/resources/fabric.mod.json` |
| Run the dev client | `./gradlew runClient` (`runServer` for a server). In IntelliJ, use the generated "Minecraft Client" run configuration, in normal or debug mode. Hotswap only works in debug mode. To hotswap mixins, add `-javaagent:<mixin jar>`. JetBrains Runtime with `-XX:+AllowEnhancedClassRedefinition` allows structural hotswap. | https://docs.fabricmc.net/develop/getting-started/intellij-idea/launching-the-game (page "26.2") |

The newest Fabric docs version is labeled **26.2**. I found no docs page labeled 26.3, so the docs pages above are cited as the nearest version.

## 2. Obfuscation and mappings

- **Mojang's announcement.** Mojang said the first snapshot after the full "Mounts of Mayhem" launch would be the first version without obfuscation. That build and all later builds would ship with original names, including variable names. Source: https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition (datePublished 2025-10-29).
- **Fabric's position.** Fabric docs say "Minecraft 26.1 is unobfuscated and includes parameter names". They also say Fabric no longer maintains third-party mappings from that version on, and that Yarn was deprecated in 2025. Intermediary is described as the mapping set used by compiled mods *for obfuscated releases*. Source: https://docs.fabricmc.net/develop/porting/mappings (page "26.2").
- **Verified locally.**
  - CLIENT contains classes under Mojang names, for example `net/minecraft/client/Camera.class` and `net/minecraft/client/CameraType.class`.
  - `javap -l` shows real local-variable names, for example in `Camera.getMaxZoom(float)`: `cameraDist`, `jitterScale`, `hitResult`.
  - `intermediary-26.3.jar` (`ModrinthApp\meta\libraries\net\fabricmc\intermediary\26.3\`) has a `mappings/mappings.tiny` that is a single header line `v1 official intermediary`, so it contains no mappings.
  - https://meta.fabricmc.net/v2/versions/loader/26.3 returns `"intermediary": {"maven": "net.fabricmc:intermediary:0.0.0"}`.
  - Every 26.3 mod jar in PROFILE that has a build manifest declares `Fabric-Mapping-Namespace: official`: fabric-api, sodium, iris, 3d-maps (their `META-INF/MANIFEST.MF`).
- **What this means for mixins.** Write `@Mixin(Camera.class)` and `method = "getMaxZoom"` with the plain Mojang names you see in the jar. No refmap or remapping is needed, and Yarn names (`Perspective`, `GameOptions`, `clipToSpace`) do not exist. The PROFILE mods do this: Sodium `CameraMixin` targets `Lnet/minecraft/client/Camera;` with `method=["createProjectionMatrixForCulling"]` (javap -v of the class in the sodium jar). Distant Horizons still ships a refmap whose entries are already Mojang names (`DistantHorizons.fabric.mixins-refmap.json`).

## 3. Camera hooks (verified against CLIENT)

**Perspective enum: `net.minecraft.client.CameraType`**
- Constants: `FIRST_PERSON`, `THIRD_PERSON_BACK`, `THIRD_PERSON_FRONT`.
- Methods: `isFirstPerson()`, `isMirrored()`, `cycle()`. `cycle()` returns `VALUES[(ordinal()+1) % VALUES.length]`.

**Get/set: `net.minecraft.client.Options`**
- `public CameraType getCameraType()` and `public void setCameraType(CameraType)`. The setter is a plain `putfield cameraType` with no side effects.
- Field: `private CameraType cameraType`.
- Keybind: `public final KeyMapping keyTogglePerspective`.
- No `"perspective"` string constant appears in `Options`, so the camera type does not seem to be saved to options.txt. This is an inference from `javap -constants`, so treat it as UNVERIFIED.

**Clipping method: `Camera.getMaxZoom(float)`**
- Signature: `private float getMaxZoom(float cameraDist)`.
- It casts 8 jittered rays (offset 0.1f) from `position` backwards along `forwards`, using `Level.clip(new ClipContext(from, to, ClipContext.Block, ClipContext.Fluid, entity))`. For each hit, it shrinks `cameraDist` to the hit distance if that is smaller, then returns `cameraDist`.
- There is no `clipToSpace` in 26.3.

**Per-frame setup: `Camera.alignWithEntity(float partialTicks)`** (private)
- Minecart passengers get special position lerp (`Minecart`, `NewMinecartBehavior.getCartLerpPosition`).
- It sets `detached = !options.getCameraType().isFirstPerson()`.
- If the type is mirrored, it calls `setRotation(yRot + 180, -xRot)`.
- If detached, it computes `cameraDistance = 4.0f` scaled by `LivingEntity.getScale()` and `Attributes.CAMERA_DISTANCE`. **When the camera entity `isPassenger()` and the vehicle is a `LivingEntity`**, it uses `max(cameraScale*cameraDistance, mountScale*mountDistance)`, so a horse's own `CAMERA_DISTANCE` attribute applies. It then calls `move(-getMaxZoom(dist), 0, 0)`.
- **Hook point for ease-out:** wrap or modify the return of `getMaxZoom` (instant snap-in, eased snap-out), or `@ModifyArg` the `move(FFF)` call inside `alignWithEntity`.

**Call chain each frame**
- `Minecraft.renderFrame(boolean)` → `GameRenderer.update(DeltaTracker)`, inside profiler section `"camera"` → `Camera.update(DeltaTracker)` → `getCameraEntityPartialTicks(DeltaTracker)` → `alignWithEntity(float)` → fov, culling and projection.
- Later in the frame, `GameRenderer.extract(DeltaTracker, boolean)` → `extractCamera(DeltaTracker, float)` → `Camera.extractRenderState(CameraRenderState, DeltaTracker)`.
- Per tick, `GameRenderer.tick()` calls `Camera.tick()`.
- Note: 26.3 has no `Camera.setup(...)`. The older-style entry point is now `update(DeltaTracker)`.
- Sources: `javap -c` of `Camera`, `GameRenderer` (`update`, `extract`, `extractCamera`, `tick`), `Minecraft` (`renderFrame`).

**F5 toggle path: `Minecraft.handleKeybinds()`** (private, called from `Minecraft.tick()`)
- Logic: `while (options.keyTogglePerspective.consumeClick()) { CameraType old = options.getCameraType(); options.setCameraType(options.getCameraType().cycle()); if (old.isFirstPerson() != options.getCameraType().isFirstPerson()) gameRenderer.checkEntityPostEffect(firstPerson ? getCameraEntity() : null); }`.
- How to tell a manual F5 press from your own change, either way works:
  - (a) Inject at `INVOKE Options.setCameraType` inside `handleKeybinds`, which catches only the keybind path.
  - (b) Remember the value you last set, and compare it with `getCameraType()` in `ClientTickEvents.END_CLIENT_TICK`. Fabric's lifecycle mixin injects at RETURN of `Minecraft.tick`, after `handleKeybinds`.
- **Gotcha:** when your mod switches perspective itself, it should mirror the `checkEntityPostEffect` call, or the spectator/creeper post-effect state will be wrong. That method is public on `GameRenderer`.

## 4. Mount detection

- Methods declared on `net.minecraft.world.entity.Entity` (javap of CLIENT):
  - `getVehicle()`, `isPassenger()`, `getRootVehicle()`, `getControlledVehicle()`
  - `getControllingPassenger()` (returns `LivingEntity`), `hasControllingPassenger()`, `isVehicle()`, `getPassengers()`
  - `startRiding(Entity)` (final), `startRiding(Entity, boolean, boolean)`, `stopRiding()`, `removeVehicle()`
- `LocalPlayer` overrides `startRiding(Entity, boolean, boolean)` and `removeVehicle()`.
- The client applies mounts from the server in `ClientPacketListener.handleSetEntityPassengersPacket(ClientboundSetPassengersPacket)`, which calls `Entity.startRiding(Entity, Z, Z)`.
- **Fabric API has no mount/dismount event.** Looking at 0.164.0+26.3: `fabric-entity-events-v1` exposes only Elytra, Sleep and Server* events. `fabric-lifecycle-events-v1` exposes `ClientEntityEvents.Load/Unload` and `ClientTickEvents` (`START_CLIENT_TICK`, `END_CLIENT_TICK`, `START_LEVEL_TICK`, `END_LEVEL_TICK`; `EndTick.onEndTick(Minecraft)`). Source: class listing and javap of the nested jars in the PROFILE fabric-api jar.
- **Recommendation:** poll `mc.player.getVehicle()` in `ClientTickEvents.END_CLIENT_TICK` and compare it with the previous tick. This catches every vehicle type, including modded seat entities, and needs no mixin. If you need the exact moment of the change, the alternative is a mixin on `LocalPlayer.startRiding`/`removeVehicle`. That choice is my reasoning, not something a doc states.

## 5. Config libraries for 26.3

All data comes from Modrinth `api.modrinth.com/v2/project/<slug>/version` (filtered on `game_versions=["26.3"]`) and the libraries' maven metadata, as of 2026-10-29.

| Library | 26.3 release | Maven repo + coordinates | Notes |
|---|---|---|---|
| Mod Menu | **21.0.0** (release, 2026-09-23; earlier 21.0.0-beta.1) | `https://maven.terraformersmc.com/` → `com.terraformersmc:modmenu:21.0.0` (POM returns HTTP 200) | Requires Fabric API and Text Placeholder API (Modrinth deps `P7dR8mSH`, `eXts2L7r`). README: use `implementation` from 26.1 on, and register a `"modmenu"` entrypoint implementing `com.terraformersmc.modmenu.api.ModMenuApi#getModConfigScreenFactory()` (README, and `src/main/java/com/terraformersmc/modmenu/api/ModMenuApi.java` on the default branch, which now targets 26.4-snapshot-6). |
| YACL | **3.9.7+26.3-fabric** (release, 2026-09-20; earlier 3.9.6, 3.9.5) | `https://maven.isxander.dev/releases` → `dev.isxander:yet-another-config-lib:3.9.7+26.3-fabric` (POM returns HTTP 200) | Requires Fabric API. Docs show `modImplementation`; on 26.x use `implementation` (inferred from the Loom docs, UNVERIFIED for YACL specifically). |
| Cloth Config | **26.3.159+fabric** (release, 2026-09-23; earlier 26.3.158) | `https://maven.shedaniel.me/` → `me.shedaniel.cloth:cloth-config-fabric:26.3.159` (POM depends on fabric-loader 0.19.5) | The GitHub default branch `v8` is 1.19-era; I could not locate the 26.3 source branch. |

**Best fit for a custom vertical pitch bar: YACL.**
- Its public API is `dev.isxander.yacl3.api.Controller<T>`, with `option()`, `formatValue()` and `AbstractWidget provideWidget(YACLScreen, Dimension<Integer>)`. You can plug in your own widget per option through `.controller(...)`.
- `OptionListWidget.OptionEntry` sizes each row from `widget.getDimension().height()`, so a taller-than-normal widget is supported.
- `CustomTabProvider.createTab(YACLScreen, ScreenRectangle)` allows a fully custom tab.
- It ships `FloatSliderController`, `EnumController`, `EnumDropdownControllerBuilder` and `DropdownStringControllerBuilder` for the sliders and dropdowns.
- Sources: YACL GitHub branch `port/26.3` (`src/main/java/dev/isxander/yacl3/api/Controller.java`, `gui/OptionListWidget.java` lines ~98 and ~398, `api/CustomTabProvider.java`); https://docs.isxander.dev/yet-another-config-lib/gui-builder/controllers.md. Caveat: that branch's `gradle.properties` says `modVersion=3.9.4`, and its last commit is "Port to 26.3-snapshot-1" (2026-06-23). It may not be the exact 3.9.7 source, and the docs' "Creating a custom Controller" section is empty.

**Cloth Config:** custom entries are done by subclassing `AbstractConfigListEntry`/`TooltipListEntry`. That is verified only on the old `v8` branch (`common/.../gui/entries/TooltipListEntry.java`). The 26.3 API shape is UNVERIFIED.

**Another option:** no library. Mod Menu's `ConfigScreenFactory` can return a plain vanilla `Screen` with your own widgets. That is my reasoning, not from docs.

## 6. Sodium, Iris and Distant Horizons conflicts

**Method.** I scanned all 803 classes carrying `@Mixin` across every PROFILE jar, including the nested Fabric API module jars, using `javap -v -p` (`C:\Users\Video-gamer\AppData\Local\Temp\fr263\mods\allmixins_v.txt`). I then kept the mixins whose `@Mixin` target is `Camera`, `CameraType`, `Options`, `Minecraft`, `GameRenderer` or `LocalPlayer`.

**Mixins that target `net.minecraft.client.Camera`:**
- Sodium 0.9.3-beta.1 `net.caffeinemc.mods.sodium.mixin.core.render.frustum.CameraMixin`: `@Inject method=createProjectionMatrixForCulling at=RETURN, cancellable`. This is the culling projection only. (Listed in `sodium-common.mixins.json`.)
- Sodium `net.caffeinemc.mods.sodium.mixin.features.render.world.sky.LevelRendererMixin`, which despite its name targets Camera: `@WrapOperation method=extractRenderState at INVOKE LivingEntity.hasEffect(Holder) ordinal 0`. This is sky/effect related.
- Iris 1.11.7 `net.irisshaders.iris.mixin.MixinCamera`: `@WrapOperation method=prepareCullFrustum at NEW Frustum`. (Listed in `mixins.iris.json`.)

**Mixins that target `GameRenderer`** (none touch `update`/camera setup):
- Sodium: `renderLevel` (ProjectionMatrixBuffer.getBuffer), `render` (GuiRenderer.render), `extractWindow` (Window.isIconified), plus an accessor.
- Iris: `render` HEAD and ModifyArgs; `<init>`; `renderItemInHand` (Redirect); `renderLevel` TAIL and several WrapOperations (`MixinModelViewBobbing`); `getProjectionMatrix` (Redirect getDepthFar); `nightVisionScale`.
- Distant Horizons 3.3.4: `renderLevel` HEAD and RETURN.
- Fabric rendering-v1: `<init>` RETURN, `extract` HEAD.

**Mixins that target `Minecraft` or `Options`:** none touch `handleKeybinds`'s perspective code, `setCameraType` or `getCameraType`.
- Fabric interaction-v0 injects into `handleKeybinds`, but at `LocalPlayer.isUsingItem()`.
- Iris injects at `Minecraft.tick` RETURN.
- Iris `MixinOptions_CloudsOverride` targets `Options.getCloudStatus`.

**Mixins that target `LocalPlayer`:** Fabric content-registries (water checks in `aiStep`, etc.), Fabric networking, Iris. None touch riding.

**Result: no local mod mixes into `Camera.getMaxZoom`, `Camera.alignWithEntity`, `Camera.update`, `Camera.move`/`setPosition`, `Options.setCameraType` or the F5 path.** A string grep for `getMaxZoom` and `alignWithEntity` across all extracted files found nothing. 3d-maps 0.8.2, veinminer 2.12.3 and fabric-language-kotlin have no camera-related mixins.

Side notes:
- Sodium 0.9.3-beta.1 declares `breaks: iris <=1.11.2, fabric-api <0.145.1`.
- Iris 1.11.7 `breaks: distanthorizons <=3.2.0`.
- DH 3.3.4 requires `minecraft 26.3`, `fabricloader >=0.19.5`, `java >=25`, and bundles fabric-api 0.160.5 nested.
- Sources: each jar's `fabric.mod.json`.

## 7. Local Java

- `java` on PATH is Oracle **19.0.2** (`java -version`; `/c/Program Files/Common Files/Oracle/Java/javapath/java`).
- `C:\Program Files\Java` contains only `jdk-11.0.12` and `jdk-19`.
- `ModrinthApp\meta\java_versions` contains only `zulu25.36.205-ca-jre25.0.4.1-win_x64`. Its `release` file says `JAVA_VERSION="25.0.4.1"`, Azul Zulu, and its `MODULES` list has **no `jdk.compiler`**. It is a JRE with no `javac`, so it can run the game but **cannot compile**.
- `~/.gradle/jdks` and `~/.jdks` are absent or empty. `.minecraft/runtime` has only `java-runtime-alpha/beta/gamma` and `jre-legacy`, which are older runtimes; the 26.3 manifest requests `java-runtime-epsilon`.
- **No JDK 25 is installed.** EXAMPLE compiles with `options.release = 25`, which needs a JDK ≥25 for the compiler. EXAMPLE `settings.gradle` has no toolchain-resolver plugin, so Gradle will not auto-download one with the stock template. That last point is inferred from the file contents.
- **Action:** install a JDK 25 (for example Temurin or Zulu JDK 25, not a JRE), and point Gradle or IntelliJ at it.

---

## Risks / open questions

1. **Docs lag:** the newest Fabric docs are labeled 26.2. 26.3 specifics come from the example mod, maven and the jars, not prose docs.
2. **Loom version:** EXAMPLE pins `1.19-SNAPSHOT`, a moving target. Pinning `1.19.3` (latest release) is reasonable, but whether 1.19.3 is what the template intends is UNVERIFIED.
3. **Loader drift:** dev uses 0.20.1 while the user profile runs 0.19.5. Keep `depends.fabricloader` at `>=0.19.5`, not the template's `>=0.20.1`, or the mod will refuse to load in the user's profile. Likewise, Fabric API 0.165 in dev vs 0.164 in the profile.
4. **YACL source:** the `port/26.3` branch is older (3.9.4) than the 3.9.7 release. Custom controller API stability on 3.9.7 is not verified from source.
5. **YACL and Mod Menu pull transitive deps:** Mod Menu needs Text Placeholder API, and both need Fabric API. Users must install them.
6. **Riding a LivingEntity** (horse, pig) uses the mount's `CAMERA_DISTANCE` attribute in `alignWithEntity`. The ease-out must operate on the value passed into or returned from `getMaxZoom`, so it respects that distance.
7. **Selfie mode:** `THIRD_PERSON_FRONT` is handled in `alignWithEntity` by a 180° yaw flip and pitch negation. Pitch-zone hysteresis should read the player's `getXRot()`, not the camera's `xRot()`, to avoid feedback.
8. **Not persisted:** CameraType seems not to be saved in options.txt (UNVERIFIED inference), so "restore previous perspective" only needs in-memory state.
9. **Out-of-profile conflicts:** other camera mods (Freelook, Better Third Person, Shoulder Surfing, etc.) are not in this profile and were not checked.
10. **Client-only flag:** whether `"environment": "client"` plus only a `client` entrypoint is the recommended setup for 26.3 is not stated in the docs consulted (UNVERIFIED).
11. **Modrinth requests:** the Modrinth API calls for Mod Menu and Cloth Config sent a User-Agent containing the user's contact email (my mistake). Later calls used a generic UA.
