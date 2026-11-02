# Universal Adaptive Backporter (UAB)

UAB adapts modern Minecraft content to Forge. **Minecraft 1.7.10** and **Minecraft 1.12.2** are the versions it targets. Its content reference extends through **Java Edition 1.21.11**. Blocks, items, mobs, biomes, structures and selected gameplay conveniences use implementations suited to the legacy game.

UAB preserves each target version's vanilla textures and artwork. New content and separate variations supply their own assets; player resource packs retain their normal priority.

## What UAB supplies

- Building materials and wood families, functional workstations, copper and netherite progression, crops, equipment and modern mob families.
- Modern surface and ocean biomes within the native world height, optional Nether biomes, outer End islands and End Cities.
- Saved ocean monuments, bastions, outposts, woodland mansions, trail ruins, Ancient Cities and Trial Chambers, with native placement, encounters and loot adaptations.
- One configuration file organized by the update that introduced each feature. Feature families share controls; individual mobs have their own availability and applicable natural-spawning settings.

The feature list above describes 1.7.10. The 1.12.2 target currently contains selected materials, workstations, fish, drowned, phantoms, tridents and ocean structures; it does not yet provide the full catalog. See [target coverage](CONTENT_CATALOG.md#target-coverage). Some mechanics are adapted or incomplete. The [content catalog](CONTENT_CATALOG.md), [mob coverage](MOB_COVERAGE.md) and [world-generation reference](WORLD_GENERATION.md) describe those boundaries. Registration or a modern name does not imply complete parity with the modern game.

Operators can find structures with `/uab_locate <structure>`; press Tab for supported names. Searches report saved sites or potential new-terrain locations without generating terrain. See the [world-generation reference](WORLD_GENERATION.md#locating-structures) for coverage and search limits.

## Installation

For **1.7.10**, use Forge **10.13.4.1614** and Java **8**, with [UniMixins](https://modrinth.com/mod/unimixins), including its GTNH Mixins module. Place the UAB production JAR in the client and dedicated server `mods` folders. Remove the previous EFR/EFR-R JAR when upgrading; both use the same technical mod ID and cannot be installed together.

The development **1.12.2** target uses Forge **14.23.5.2859**, Java **8**, and [MixinBooter 11.18 or later](https://github.com/CleanroomMC/MixinBooter) on clients and servers. It provides the shared configuration screen and rejects gameplay-setting mismatches during connection. Its client prepares Minecraft 1.21.11 assets at startup; offline installations can supply the official 1.21.11 client JAR with `-Duab.assets=<path>`. Optional-mod integrations and much of the content catalog remain under development.

UAB 1.7.10 does not require HogUtils, FastUtil, GTNHLib or GTNHExtLib. Optional integrations include NEI, Waila, CraftTweaker and Iron Chests. Their presence is not required to load UAB. Sounds and some other Minecraft assets are supplied through the bundled MCLib AssetDirector integration; an initial asset download may be required.

Back up existing worlds before upgrading. UAB deliberately retains the `etfuturum` registry namespace, resource domain, Java package, entity identities and saved-data keys. Keep configured biome, potion and enchantment IDs stable.

## Configuration

Edit **`config/uab.cfg`** in each installation, or use the configuration screen in Forge's Mods menu on either target. Categories use player-facing names such as **Minecraft 1.16 - Nether Update**, with controls for feature families and their useful parameters. There is no separate experiments file or global experiments switch.

An update switch disables its features while remembering their individual preferences. Dependencies are checked before generation and acquisition. Disabling a feature retains its registry identities and existing saved objects; new recipes, acquisition, generation and spawning are controlled where applicable. It does not remove already generated terrain or erase existing inventories.

Restart Minecraft or the dedicated server after changing content, world generation, spawn parameters or compatibility settings. A small set of client preferences can be applied through the configuration screen without restarting. Both targets check common gameplay settings during connection. Client preferences may differ.

Existing recognized EFR configuration values are imported once, with existing UAB settings taking precedence. Original files and adjacent `.uab.bak` backups are retained. Read the startup log for obsolete or unrecognized values. See the [configuration reference](CONFIGURATION.md) for layout, migration and spawn semantics.

## Scope and approach

UAB is a content backporter. It retains the legacy world height, loader, save model and authoritative server behavior. It does not replace Minecraft's terrain engine, introduce a general combat rewrite or take over the responsibilities of separate cave-generation projects. Missing prerequisites are deferred rather than replaced with unrelated ingredients.

Feature support follows behavior and practical use on 1.7.10. UAB can use project-owned lifecycle tracking, retained model data, native structure assembly and bounded gameplay systems when those provide a coherent legacy implementation. Shared technical identifiers are retained for compatibility; internal architecture can change substantially.

## Lineage

UAB began as a fork of **Et Futurum Requiem (EFR)**, initially maintained as **Et Futurum Requiem Reforged**. EFR itself builds on ganymedes01's Et Futurum and the work of KryptonCaptain, jss2a98aj, Roadhog360, embeddedt, makamys and other contributors.

Its architecture and feature-support philosophy differ substantially from the original EFR project. Notably, this project intends for features to actually integrate into that version natively without rewriting the rest of the game's code, and promotes survival usability. 

The lineage and inherited contributions retain full credit. The project code uses the existing LGPL-3.0 license; Minecraft assets and other third-party material retain their own rights. See [third-party notices](THIRD_PARTY_NOTICES.md) and the asset credits included in the repository.

## Building and contributing

The root wrapper uses JDK 25 and coordinates independent builds. The 1.7.10 target uses GTNHGradle/Jabel with JDK 25; 1.12.2 uses Unimined with JDK 21. Both production artifacts target Java 8, which is also required for native configuration checks and game launches. Install these JDKs or set `JAVA8_HOME`, `JAVA21_HOME` and `JAVA25_HOME`.

```text
./gradlew buildAll
./gradlew testAll
./gradlew cleanAll
```

Each directory under `versions/` can also be opened as an independent IntelliJ Gradle project and built with its own wrapper. Its wrapper selects the appropriate build JDK. Root `runClient1_7_10`, `runServer1_7_10`, `runClient1_12_2` and `runServer1_12_2` launch the corresponding target. Existing root Gradle run-task names continue to delegate to 1.7.10.

The root **`version.properties`** supplies one release version. Production artifacts are **`versions/1.7.10/build/libs/uab-<modVersion>+mc1.7.10.jar`** and **`versions/1.12.2/build/libs/uab-<modVersion>+mc1.12.2.jar`**. Development and source artifacts have classifier suffixes. Normal builds and tests never increment or rewrite the release version. `buildAll` verifies each production JAR's target metadata and Java 8 bytecode.

`common/src/main/java` contains platform-neutral feature metadata and deterministic seed helpers. `common/src/forge` contains configuration logic compiled against each target's native Forge API. Shared resources live under `common/src/main/resources`; native gameplay, rendering and adapters remain under their respective `versions/` directories. Local 1.7.10 development mods follow [devmods/README.md](devmods/README.md).

Release tags use `v<modVersion>` and must match `version.properties`. GitHub Actions builds both targets and attaches their production JARs and root update metadata. A successful build does not certify complete 1.12.2 gameplay coverage. Modrinth and CurseForge publishing require this project's own hosting IDs and credentials; inherited upstream project IDs are not used. See [RELEASING.md](RELEASING.md).

Report reproducible problems in this repository's issue tracker with the exact JAR name, Forge/Java versions, relevant settings and logs. For world-generation issues include the seed, dimension and coordinates. Static checks, a successful build and a short runtime probe are separate evidence from a complete gameplay acceptance test.
