# Universal Adaptive Backporter (UAB)

**Modern Minecraft. Classic Foundations.**

Universal Adaptive Backporter (UAB) adapts content from Minecraft 1.8 through Java Edition 1.21.11 for **Minecraft Forge 1.7.10 and 1.12.2**.

Originally forked from Et Futurum Requiem, UAB focuses on native integration, survival functionality, configurability, and compatibility with established legacy Minecraft environments.

The project retains the original engines, world-height limitations, and save formats of its target versions. Modern features are adapted to those environments rather than relying on extensive engine replacements.

## Project Overview

UAB provides:

- Modern blocks, items, entities, biomes, structures, equipment, and gameplay mechanics.
- A unified configuration system organized by Minecraft update, with over 350 feature switches.
- Modern world generation adapted to legacy terrain and structure systems.
- Optional integrations with other Forge mods.
- Shared feature definitions with version-specific implementations.
- Registry and saved-world compatibility with existing Et Futurum installations.

**Implementation coverage differs between 1.7.10 and 1.12.2.** The 1.7.10 target currently has broader content coverage, while 1.12.2 continues to receive native implementations and compatibility improvements.

Detailed documentation:

- [Content Catalog](CONTENT_CATALOG.md) — Features, implementation status, and target coverage.
- [Mob Coverage](MOB_COVERAGE.md) — Entity implementation and behavior.
- [World Generation](WORLD_GENERATION.md) — Biomes, structures, generation rules, and limitations.
- [Configuration Reference](CONFIGURATION.md) — Settings, migration, and configuration behavior.

### Structure Locating

UAB includes `/uab_locate <structure>` for supported structures.

The command searches saved structure locations or potential positions in unexplored terrain without generating chunks during the search. Tab completion provides supported structure identifiers.

See [World Generation](WORLD_GENERATION.md#locating-structures) for details.

## Installation

Both supported versions require Java 8 at runtime.

| Minecraft | Forge | Required Dependency |
|---|---|---|
| 1.7.10 | 10.13.4.1614 | [UniMixins](https://modrinth.com/mod/unimixins), including GTNH Mixins |
| 1.12.2 | 14.23.5.2859 | [MixinBooter 11.18+](https://github.com/CleanroomMC/MixinBooter) |

Install the appropriate UAB JAR and dependencies in the `mods` directory. Dedicated servers require the same version-specific dependencies.

**Remove existing Et Futurum Requiem or Et Futurum Requiem Reforged installations before installing UAB.** These projects share technical identities and cannot coexist.

Some modern assets are supplied using the bundled MCLib AssetDirector integration and may require an initial download.

For offline Minecraft 1.12.2 environments, the official Minecraft 1.21.11 client JAR can be supplied using:

`-Duab.assets=<path>`

Back up existing worlds before upgrading.

UAB preserves the historical `etfuturum` registry namespace, resource domain, entity identities, and saved-data keys. Existing biome, potion, and enchantment IDs should remain consistent.

On 1.7.10, potion ID `-1` reuses the allocation in `config/legacy-potion-ids.properties`, or chooses an unused ID after mod initialization on a first launch. Explicit IDs such as `78` work within the native 0–127 range; larger IDs require the enabled EndlessIDs potion module. An explicit ID cannot override a different saved allocation. Keep the allocation file with the modpack and use the same file on clients and servers; existing worlds also retain a snapshot to detect accidental changes.

Before a new pack's first launch, reserve a late-registering mod's potion slot in that properties file with an entry such as `modpack\:reserved_lotr_effect=30` and `schema=1` (the colon must be escaped in Java properties files). Reservations affect automatic allocation without excluding ID 30 globally. Do not change allocations already used by saved worlds without a separate migration.

## Configuration

UAB uses a unified configuration file:

`config/uab.cfg`

Configuration categories are organized by Minecraft update, with separate controls for content families, gameplay behavior, spawning, world generation, and compatibility.

The configuration system supports:

- Individual feature and update-category toggles.
- Configurable mob spawn weights and group sizes.
- Structure and biome generation controls.
- Optional mod integrations.
- Client-specific preferences.
- Automatic migration of recognized legacy Et Futurum settings.

Disabling content generally prevents new acquisition, generation, or spawning without deleting existing registry identities or saved objects.

Configuration changes affecting gameplay require restarting the game or dedicated server. Selected client preferences can update immediately.

Both target versions validate shared gameplay settings during multiplayer connections.

Legacy configuration migration preserves original files and creates backups where applicable.

See [CONFIGURATION.md](CONFIGURATION.md) for the full configuration reference.

## Optional Mod Compatibility

UAB provides optional integrations for recipes, equipment, world generation, modded materials, rendering, and gameplay behavior.

**Gameplay & Equipment:** Not Enough Items (NEI), WAILA, MineTweaker 3, Iron Chests, Ender IO, Baubles, Baubles Expanded, Tinkers' Construct, Thaumcraft, Forbidden Magic, Mine & Blade: Battlegear 2, CustomNPCs.

**World Generation & Materials:** Biomes O' Plenty, Witchery, Underground Biomes Constructs, NetheritePlus, Enderlicious, Netherlicious, Hardcore Ender Expansion, Applied Energistics 2, IndustrialCraft 2, Ars Magica 2, Big Reactors, BluePower, Botania, Draconic Evolution, Dragon Block C, Dragon Quest (DQM III Next), Fisk's Superheroes, ProjectRed, SimpleOres, Natura.

**Additional Compatibility:** MineFactory Reloaded, Campfire Backport, Back in Slime, Village Names, UpToDate, GregTech, Extra Utilities, RPLE (Realistic Lighting), LWJGL3ify, EndlessIDs, Forge Multipart.

See [content compatibility providers](COMPATIBILITY.md) for BOP kelp/bamboo, modded signs and independent provider settings. The [experimental offline importer](tools/world-import/README.md) supports a reviewed subset of Java 1.13.2 worlds; 1.18.2+ worlds are excluded.

See [content compatibility providers](COMPATIBILITY.md) for BOP kelp/bamboo, modded signs and independent provider settings. The [experimental offline importer](tools/world-import/README.md) supports a reviewed subset of Java 1.13.2 worlds; 1.18.2+ worlds are excluded.

**All integrations are optional and vary in scope.** Some provide dedicated functionality, while others handle specific blocks, recipes, ore mappings, or engine interactions. Support also varies between Minecraft versions.

Compatibility with replacement dimension or terrain providers is not guaranteed. UAB's Nether provider remains incompatible with replacement Nether terrain generators such as Netherlicious.

UAB does not require HogUtils, FastUtil, GTNHLib, or GTNHExtLib.

## Project Structure

| Directory | Description |
|---|---|
| `common/src/main/java` | Platform-independent metadata and utilities |
| `common/src/forge` | Shared Forge configuration implementation |
| `common/src/main/resources` | Shared resources and assets |
| `versions/1.7.10` | Forge 1.7.10 implementation |
| `versions/1.12.2` | Forge 1.12.2 implementation |
| `devmods/` | Local development mod dependencies |
| `tools/` | Verification and development utilities |

Both versions maintain their own gameplay, rendering, and platform-specific implementations while sharing common feature and configuration definitions.

Technical identifiers inherited from Et Futurum are intentionally preserved for compatibility.

## Building

The repository uses a multi-version Gradle build coordinated from the root project.

### Build Requirements

| Component | JDK |
|---|---|
| Root Gradle | 25 |
| Minecraft 1.7.10 (GTNHGradle/Jabel) | 25 |
| Minecraft 1.12.2 (Unimined) | 21 |
| Production runtime and native testing | 8 |

Configure the required JDK installations or use `JAVA8_HOME`, `JAVA21_HOME`, and `JAVA25_HOME`.

### Build Commands

```bash
./gradlew buildAll
./gradlew testAll
./gradlew cleanAll
```

Each directory under `versions/` is also an independent Gradle project with its own wrapper.

Development run tasks:

```text
runClient1_7_10
runServer1_7_10
runClient1_12_2
runServer1_12_2
```

### Production Artifacts

The root `version.properties` defines the release version.

```text
versions/1.7.10/build/libs/uab-<modVersion>+mc1.7.10.jar
versions/1.12.2/build/libs/uab-<modVersion>+mc1.12.2.jar
```

Both production artifacts target Java 8.

The aggregate build verifies target metadata, packaged resources, and bytecode compatibility.

Successful compilation does not guarantee complete gameplay or mod compatibility.

## Releases

Release tags use the format `v<modVersion>` and must match `version.properties`.

GitHub Actions builds both targets and attaches their production JARs and update metadata.

Normal builds do not increment the project version.

See [RELEASING.md](RELEASING.md) for release procedures and publishing requirements.

## Scope & Limitations

UAB is a content backporter, not an engine replacement.

It does not attempt to provide a universal modern swimming or waterlogging engine, expanded world height, a complete cave-generation replacement, or a general combat rewrite.

Feature implementations may differ from modern Minecraft where required by legacy engine limitations.

Existing world data and registry identities are preserved wherever possible. Disabling content is not equivalent to removing previously generated structures, items, or entities.

Optional integrations and specific feature implementations may remain incomplete or require further testing.

## Origins & Credits

UAB began as a fork of **Et Futurum Requiem (EFR)**, initially maintained as **Et Futurum Requiem Reforged**.

EFR itself builds on ganymedes01's Et Futurum and contributions from KryptonCaptain, jss2a98aj, Roadhog360, embeddedt, makamys, and other developers.

UAB retains this lineage while maintaining its own architecture, feature scope, and development direction.

All inherited contributions retain attribution.

The project uses the **LGPL-3.0** license. Minecraft assets and other third-party resources retain their respective licenses.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for licensing and attribution details.

## Reporting Issues

Report bugs through the [GitHub Issue Tracker](https://github.com/RagexPrince683/UniversalAdaptiveBackporter/issues).

Include the exact UAB JAR filename, Minecraft/Forge/Java versions, reproduction steps, relevant configuration settings, and logs.

For world-generation problems, include the world seed, dimension, and coordinates.

Reproducible issues and compatibility reports are welcome.

<!-- UAB_ISSUE_LEADERBOARD_START -->
## Top Issue Submitters

Recognizing community members whose bug reports and enhancement suggestions have been completed. Thank you for helping us maintain this mod!.

| Rank | Issue Submitter | Bugs Reported | Enhancements Suggested | Total |
|---|---|---|---|---|
| 1 | [@branman5949](https://github.com/branman5949) | 16 | 12 | 28 |
| 2 | [@GlowingFederal](https://github.com/GlowingFederal) | 5 | 0 | 5 |
| 3 | [@UristMcFarmer](https://github.com/UristMcFarmer) | 2 | 0 | 2 |
| 4 | [@DDRAIGOCH117](https://github.com/DDRAIGOCH117) | 1 | 0 | 1 |
| 5 | [@VioletWyvern](https://github.com/VioletWyvern) | 1 | 0 | 1 |
<!-- UAB_ISSUE_LEADERBOARD_END -->
