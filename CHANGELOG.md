# Changelog

## Fix spectator crosshair rendering with missing mouse-over state

- Handle a missing `Minecraft.objectMouseOver` safely while rendering the spectator crosshair.

## Fix Forge 1.7.10 spectator event-handler crash

- Make the client and common spectator event subscribers instance methods to match their Forge singleton registrations and prevent generated event wrappers from invoking static methods as instance methods.
- Keep spectator behavior, event priorities, conditional registration, and the statically called player-model restoration helper unchanged.

## Remove remaining external runtime utility mods

- Remove the GTNHLib runtime declaration and replace its annotation subscribers with explicit Forge event-bus registration.
- Track client armor-slot item changes with a weakly keyed, allocation-stable state cache and preserve the existing equip-sound rules.
- Replace boxed ocean-monument coordinates with project-owned packed-coordinate helpers and a pre-sized primitive open-addressing map.
- Document Reforged's self-contained RageCraft focus while retaining upstream attribution.

## Remove accidental fastutil runtime dependency

- Replace every direct fastutil collection, primitive map, function, and pair use with Java 8 collections/functions or the already bundled Apache Commons pair.
- Keep block and item metadata maps identity-keyed with wildcard fallback and lazy map views, and retain tag inheritance, reverse lookup, and metadata-aware registration.
- Make both Mixin loaders bootstrap-safe without fastutil while retaining UniMixins integration.
- At this stage of the migration, event registration, client equipment events, and coordinate packing still came from GTNHLib; the newer entry above completes their replacement.

## Fix GenericUtils filename validation overload

- Restore exception-aware filename validation so namespaced tag paths can allow `:` and `/` while retaining the remaining Windows filename restrictions.

## Fix Iron Chest compatibility after HogUtils removal

- Replace the last HogUtils-shaded reflection helper usage with self-contained Java reflection while retaining optional Iron Chest variant fallbacks.

## Remove the HogUtils hard dependency

- Internalize the metadata-aware block, item, and biome tag APIs, including wildcard matching, inheritance, reverse lookup, registry-ID helpers, and early Mixins on vanilla registry objects.
- Internalize block/metadata pair maps, recipe validation and priority recipes, xoshiro256** random generation, weighted lists, and the dummy world used by ore compatibility.
- Track nested chunk generation with an Et Futurum-owned thread-local early Mixin and provide action- and metadata-specific multi-block sounds through the existing client event handler.
- Remove the HogUtils runtime declaration, Gradle dependency, bundled JARs, and nightly-install documentation while retaining GTNHLib and optional integrations.
- Credit the exact upstream HogUtils reference revision in `THIRD_PARTY_NOTICES.md`.

## Fix spectator flight capability authority

- Resolve the local player's spectator state from its actual client or server game mode instead of the potentially delayed spectator DataWatcher.
- Keep spectator flight capability maintenance and ability synchronization server-authoritative, and explicitly restore the authoritative target game mode's capabilities on spectator exit.
- Preserve the DataWatcher for remote spectator state, entity-following behavior, and spectator-owned visual-effect cleanup.

## Protect game-mode flight and CustomNPC boat passengers

- Clear stale flying state whenever a newly configured game mode does not permit flight, while preserving Spectator and Creative flight.
- Add a default-enabled server-side option that keeps CustomNPCs and CustomNPC+ NPCs out of EFR boat seats, ejects existing NPC passengers, and prevents boat-hull collision movement.

## Fix BOP/Witchery compatibility compilation

- Initialize external wood pillars with the required wood material and restore the missing vanilla chunk-coordinate import used by entity replacement tracking.

## BOP and Witchery wood-family compatibility (GTNewHorizons/Et-Futurum-Requiem#35, #80)

- Add optional, fail-safe Biomes O' Plenty and Witchery external wood descriptors and modern log, fence, redstone, door, trapdoor, boat, chest-boat, and bamboo-raft derivatives.
- Preserve the current orientation-aware `StrippedLogRegistry`, use exact external plank metadata in recipes, and register generated log forms as `logWood`.
- Keep compatibility registry identities namespaced from EFR's native cherry, mangrove, and bamboo families.

## Port ridden horse buoyancy and pet death messages (GTNewHorizons/Et-Futurum-Requiem#95, #87)

- Add an independent backport option that gives ridden horses gentle upward buoyancy while they are in water.
- Notify an online owner with the vanilla CombatTracker death message when their owned mob or horse dies.

## Port modern lighting and End atmosphere (GTNewHorizons/Et-Futurum-Requiem#91, #104)

- Add independent client options for End flashes, modern End ambience, hue-preserving gamma, warm block light, and modern night vision.
- Add deterministic End flash state, directional delayed rumble audio, additive sky rendering, and a renderer-neutral client API.
- Restrict modern lightmap processing to vanilla providers and custom providers whose complete brightness table matches a vanilla table.
- Register the End flash sound and texture through the existing Minecraft AssetDirector namespace.

## Refine entity replacement chunk tracking (#701)

- Track server-loaded chunks by dimension and packed coordinates instead of retaining weak `Chunk` references.
- Avoid every loading or generating chunk lookup during entity replacement while preserving originals when safe insertion is unavailable.
- Respect canceled and dead entity joins, prevent duplicate tracker registration during saved-chunk loading, and clean tracking state on chunk, world, and server shutdown.

## [Memory-opti:fix leak] Fix two world leaks (#63)

- Release event-handler references to worlds, players, entities, and chunks during logout, world unload, and server shutdown.
- Stop the falling dripstone renderer from retaining its most recently rendered world.

## Fix copper door oxidation duplication (GTNewHorizons/Et-Futurum-Requiem#42)

- Synchronize copper door oxidation and wax-state changes with the correct opposite half, whether the changed block is the upper or lower half.
- Preserve each door half's metadata and avoid replacing unrelated blocks beside malformed doors.

## Fix furnace `BurnTime` overflow (GTNewHorizons/Et-Futurum-Requiem#113)

- Persist Blast Furnace and Smoker remaining burn times as NBT integers so long-burning modded fuels survive world reloads without signed-short truncation.
- Synchronize both halves of the full burn-time values to furnace GUIs instead of truncating their raw values to a 16-bit progress update.

## Add polar bear realism mode

- Add the `polarBearRealism` neutral entity option and enable it by default.
- Make adult polar bears target players by default while realism mode is enabled, while preserving cub-defence behavior when it is disabled.
- Document how to restore modern neutral polar bear behavior.

## Fix disabled boat recipe startup crash (Roadhog360/Et-Futurum-Requiem#590)

- Use EFR's enum-owned enabled state instead of probing Forge registry names for constructed but disabled boat items.
- Keep disabled boat and chest-boat variants out of recipes while allowing replacement oak recipes to use the vanilla boat item.
- Use the obtainable vanilla oak boat as the oak chest boat ingredient when old boats are replaced.

## Preserve vanilla functional-block identities (Roadhog360/Et-Futurum-Requiem#311)

- Augment vanilla enchanting tables and anvils with EFR's modern containers through narrowly gated block Mixins.
- Keep vanilla brewing-stand and beacon blocks while creating EFR's `TileEntityBrewingStand` and `TileEntityBeacon` subclasses for fuel and coloured-beam state.
- Retain every legacy replacement registration for old saves and make replacement blocks migration-only, hidden from creative tabs, NEI, and normal recipes.
- Make `tileReplacementMode` a legacy migration control, default new configurations to `-1`, and preserve the existing `-1`, `0`, and `1` directions.
- Preserve tile NBT while migrating and keep replacement maps server-local so worlds cannot leak migration directions into one another.

## Fix config toggle authority (Roadhog360/Et-Futurum-Requiem#643)

- Require bamboo content as well as new boats before registering bamboo raft items, and skip recipes for every disabled boat item.
- Keep disabled sign and dyed-bed content out of recipes, creative-tab sorting, and OreDictionary registration.
- Register the fancy inventory skull and inventory bow renderers only when their respective client options are enabled.
- Allow built-in texture and language overrides to be enabled independently, with identical language filtering in development folders and packaged JARs.

## Fix armor stand duplication (#699)

- Reject repeated server-side damage after an armor stand has died so its stand item and equipment cannot be dropped more than once.

## Fix spectator renderer state

- Restore the reusable player model as soon as the client changes from Spectator Mode to Survival, Creative, or Adventure.
- Balance spectator-only model hiding during rendering so repeated game mode transitions cannot leave body parts hidden.

## Add reusable devmods development loading

- Add drag-and-drop support for RFG-remapped production mod jars and already-deobfuscated development jars.
- Keep local compatibility-testing mods isolated from release artifacts and publication metadata.

## Update spectator mode

- Maintain ambient Night Vision and Invisibility potion effects while a player is in Et Futurum spectator mode without replacing pre-existing effects.
- Synchronize remote players' Et Futurum spectator state and hide those players from JourneyMap 5.1.4 radar when that optional client mod is present.
- Allow builds without the optional JourneyMap mod on the compile classpath while retaining strict validation for other mixins.

## Fix recursive chunk-loading `StackOverflowError` (#701)

- Guard entity replacement chunk access so `EntityJoinWorldEvent` never force-loads an absent neighboring chunk.
- Keep the original entity alive and its join event active when replacement cannot be completed.

## 2026-10-20 22:28 â€” Initial audit and correctness stabilization

- Validate armor-stand interaction identity, range, hit coordinates and entity state; validate boat movement and disabled-elytra requests while preserving existing packet layouts.
- Execute custom server packet actions on the server thread through a bounded lifecycle-managed queue, and isolate scheduled client packet callbacks behind the sided proxy.
- Conserve Mending's remaining XP budget across equipment and player XP, preserve existing repair order and rounding, and correct Frost Walker straight-line/negative-coordinate behavior.
- Correct brewing inventory extraction and dirty state, keep brewing server-authoritative, and reject invalid fuel consumption. Correct the documented blaze-powder cycle count and identify unsupported legacy custom-fuel operations.
- Restore dedicated-server damage-direction synchronization, late-resolved shulker storage bans, furnace fuel rules, prismarine recipe and netherite flammability options; validate invalid regex and floating-point settings and declare restart requirements.
- Correct bamboo biome selection, fossil filtering/fallback and mud generation independent of cherry content; restrict falling-concrete work to its world and prevent repeated conversion/removal.
- Restrict the optional AE2 renderer Mixin to clients, preserve smithing NBT independence, fix its input slot ranges, persist shulker upgrades and apply the intended rabbit biome exclusion.
- Correct lava cauldron bucket withdrawal, rain/water interaction and comparator behavior without changing its registered identity.
- Add a source audit and full configuration-field trace with separately tracked unresolved findings and upstream integration candidates.
- Load dependent configurations in declaration order so the Nether master switch consistently overrides experimental Nether content.

## 2026-10-21 00:43 â€” Stage A stabilization

- Restore coarse-dirt replacement at native terrain-array generation boundaries and configured client book-page sounds; existing chunks are not scanned or regenerated.
- Restore count-aware brewing fuel registration, wildcard/exact exclusions, enumeration and CraftTweaker undo while retaining tag-based eligibility. Synchronize full integer fuel counts using native container progress updates, keep existing fuel NBT keys, and reject invalid IMC registrations.
- Bind native smithing containers to the opened table and eight-block reach. Keep the public two-argument constructor with a bounded opening-position anchor for external callers.
- Adapt dripstone fluid transfer, mud drying, growth, cave generation and single-entity falling columns from upstream #706 without restoring removed libraries or changing local metadata ordering. Preserve old falling-entity reads; add optional column/source-state NBT.
- Isolate incomplete sculk catalyst spreading by disabling its death/XP interception while preserving block and tile registrations and ordinary XP drops.
- Add native lightning-rod rendering, loaded-rod lifecycle indexing, natural strike attraction and eight-tick redstone pulses with persisted deadlines. Existing block-only rods lazily acquire their tile through native lookup. Enable bone-meal azalea growth through the native tree generator.
- Correct structure sapling-pot items, banner color matching, beetroot maturity and unknown item mappings; restrict End-city chest loot processing to the placed chunk. Honor structure/content gates and preserve EndCity/start/piece identifiers.
- Validate biome, potion and enchantment slots against live registry capacities and occupants without silently assigning new IDs; retain compatibility with runtime registry expansion.
- Prevent large generation footprints from loading absent border chunks, use non-loading Nether biome queries, fix monument pillar coordinate checks and inverted delta bounds, and restore nested Nether falling-block state with a finally boundary. Unsafe candidates are skipped before placement; border feature frequency can change, with no retrogen.
- Move elytra velocity flags into native tracker updates, group falling concrete by its world, and bound server packet admission to 128 actions per connection, 4,096 total and 256 drained per tick. Keep connection/world checks and packet discriminators.
- Recheck all 371 public configuration fields, retain ordered master toggles and optional-mod gates, remove an unused NEI class reference from the fuel map, and update the existing audit/configuration trace.

## 2026-10-21 02:43 â€” Stage B native Nether and surface content

- Inventory the maintained 1.7.10 catalog against the Java 1.21.11 ceiling, separating completed paths, progression gaps, experimental mechanics, optional integrations and excluded engine work.
- Add explicit, collision-checked surface biome identities and native climate/decorating paths for Cherry Grove, Mangrove Swamp, Pale Garden, Bamboo Jungle, meadows, groves and mountain variants. Add temperature ocean layers without replacing native coastlines, terrain noise or world height. New-generation switches retain registered biome identities; no retrogen or silent ID changes.
- Complete mangrove propagule tree growth and hanging ages using existing wood, leaf and sapling identities. Bound moss growth, protect cherry tree footprints and connect cherry sapling bee nests. Correct warped nylium placement and add native bone-meal propagation.
- Add piglins, brutes, hoglins, zoglins and striders with native AI, survival interactions, timed conversions, persisted payment/anger state, gold reactions, bartering, breeding, riding and drops. Add Soul Speed with a configured enchantment ID, native movement-attribute lifecycle and soul-sand exemption. Retain the Nether master toggle and experimental provider boundaries.
- Add four bastion template/pool families with bounded deterministic assembly, native saved structure pieces, chunk-clipped placement/loot/mobs and variant loot categories. Preserve existing fortress generation and Nether ore/debris paths.
- Add pale oak families, native growth, pale vegetation, eyeblossoms, resin production/processing/building recipes and a persisted Creaking Heart/Creaking lifecycle. Keep existing wood metadata separate; preserve Stage A's disabled sculk XP interception.
- Add frogs, frogspawn, bucketable tadpoles and froglight acquisition as the mangrove dependency chain. Preserve native bucket placement permissions and event handling.
- Retain the existing dependency/toolchain/Mixin stack, packet IDs, old entity/tile identities and old save formats. New content uses additive identities and NBT. Document legacy adaptations and remaining incomplete mechanics rather than claiming complete modern parity.

- Complete stonecutting, loom patterning and cartography with block-bound native containers, atomic ingredient/output transfers, reusable pattern items, map-copy/scale/lock persistence and NEI views. Preserve existing workstation keys and banner/map identities.
- Add native guardians and persistent elder variants, monument-bound aquatic spawning, charged beams, mining fatigue and wet sponge acquisition. Migrate new monument generation to saved chunk-clipped template pieces without retrogen or forced neighbor loading.
- Add saved dragon-victory gateway progression to the experimental End while preserving native combat/XP/portal behavior and rejecting overlapping crystal respawns. Complete eyeblossom states, bee poisoning, dyes/stew, hanging-moss bone meal and bastion degradation processors.

## 2026-10-21 21:18 — Stage B7 native mob backports and survival dependencies

- Add the missing post-1.7.10 mob families through Java 1.21.11, including aquatic life, illagers, traders/llamas, pandas/cats, allays/wardens, camels/sniffers, armadillos, Breeze/Bogged, copper golems, happy ghasts, nautiluses and undead mounts. Preserve existing local IDs and append new living/projectile/passenger identities with bounded saved state.
- Adapt acquisition to native biomes, villages and structures: bounded water populations, patrols, chunk-contained outposts/allay cages, saved finite raids, dungeon eggs/equipment and naturally summoning underground shriekers. Selected native dungeons provide finite Breeze/Bogged fights; full Trial Chambers, vaults and keys are not included.
- Add buckets, seagrass/turtle eggs, scutes, membranes, crossbows, tridents/enchantments, wind charges, ancient plants, wolf armor, harnesses, nautilus armor and spears. Use native inventory, smithing, merchant and rider paths; retain disabled-prerequisite recipe deferral and server authority.
- Add retained native models, released textures, equipment/variant layers, core state animations and AssetDirector sound events. Preserve existing compatibility boundaries and avoid modern player swimming, waterlogging, cave-engine replacement or world-height changes.
- Complete related husk conversion, fox food/village behavior, piglin crossbows/social combat, hoglin herd retreat, zoglin knockback and tadpole feeding. Index loaded chest lifecycle for bounded copper sorting, and allow adjacent chest transfers without requiring a path into the chest block.
- Release raid-owned mobs back to normal despawning when a raid ends or is abandoned, preventing permanent escaped raider accumulation. Document the full mob inventory, survival adaptations, configuration identities and remaining mechanic/presentation differences in the existing coverage and configuration documents.

## 2026-10-21 22:08 — Persist automatic potion allocations

- Replace fixed potion defaults with persistent namespaced allocation coordinated with Farmer's Delight Legacy 1.7.10. Reserve existing explicit allocations during construction before choosing free live registry slots, retain disabled-effect reservations, and reject collisions or later overwrites. Preserve native potion behavior and entity registrations.
- Persist allocations atomically in config/legacy-potion-ids.properties and verify saved-world snapshots before gameplay. Compare active mappings in Forge's early network handshake before saved potion packets are sent; reject mismatched installations without changing registry state on network threads.
- Preserve existing configured IDs as migration inputs. The former Breath of the Nautilus/Comfort and Dolphin's Grace/Nourishment default collisions caused Farmer's Delight's pre-initialization failure; Log4j then masked the original registration exception with a secondary Potion lookup failure. Document explicit repair for installations blocked on their first combined startup and keep saved allocations stable.

## 2026-10-21 22:33 — Fix production world-loading spawn-stage injection

- Remap the vanilla animal-spawning invocation and select the exact chunk-population method signature in the modern loading-screen Mixin so the spawn-stage marker resolves in both development and production namespaces.
- Keep the marker immediately before world-generation animal spawning without relaxing injection requirements, changing terrain generation or altering saved data.

## 2026-10-22 02:08 — Stabilize modern rendering, aquatic spawning and progression

- Replace placeholder irregular block rendering with retained released 1.21.11 geometry and UVs for shriekers, trial spawners, turtle/sniffer eggs, pitcher crops/plants, dried ghasts and frogspawn. Preserve transparent terrain faces, native cutout rendering and Angelica chunk-worker safety. Restore shared inventory-renderer GL/bounds state and native door rendering.
- Bound natural aquatic admission through a loaded-entity lifecycle index, per-species and shared budgets, local school density and loaded-chunk guards. Skip full native spawn categories early, preserve persistent/bucket/tamed animals and add optional exact-vanilla-squid admission limits. Correct first-spawn nautilus water checks and restrict natural drowned by biome, difficulty, darkness and depth without changing conversions or direct spawning.
- Restore pitcher textures and two-part growth, farmland/bonemeal progression and single-drop harvesting. Prevent ordinary seagrass stacking and support native tall-seagrass growth under the existing identity. Display existing smithing upgrades with ingredient/result slots in NEI without changing their two-input progression; correct charged crossbow icons and held transforms.
- Dispatch copper chest lid sounds from server-owned transitions and use released block, egg, crossbow and mechanical sound events through AssetDirector. Add missing frogspawn hatch and dried-ghast transition playback.
- Add operator aquatic counters and reproducible, bounded seeded biome sampling. Keep climate selection weights and biome identities; prevent the legacy cherry decorator from competing with enabled Cherry Grove generation.
- Replace new outpost generation with released tower geometry, native palette/orientation/loot, pillagers/captains and saved chunk-contained surrounding pieces including allay cages. Preserve old structures and avoid retrogen or forced neighboring chunk loads.

## 2026-10-22 22:25 — Stabilize structures, ghast hydration and crossbow presentation

- Add native saved Ancient City and Trial Chamber assembly from released 1.21.11 templates, weighted pools and processors. Translate complete layouts into the legacy underground height window, reserve native mineshafts/strongholds first, seal surrounding terrain and place only the current chunk. Persist completed chunks to preserve mined blocks and consumed loot; reject footprints containing saved terrain rather than regenerating existing worlds. Add independent generation switches and operator locating.
- Add the required reinforced deepslate, sensors, vaults, keys, loot pots, candles, Echo Shards and placeable Heavy Core. Use released encounter and loot configurations with native finite trial fights, participant scaling, ominous conversion, persisted outstanding mobs/rewards, cooldowns and once-per-player vault rewards. Correct trial visibility so its own casing does not block activation. Explicitly defer unavailable loot instead of substituting unrelated content. Retain experimental Sculk gating and disabled catalyst XP spreading.
- Give cities bounded Deep Dark caverns, sculk floors, walking-vibration sensor relays and indexed shriekers. Preserve natural summoning metadata, add warning decay and Darkness, validate Warden support/collision, and suppress ordinary natural spawning inside saved city bounds.
- Prevent new bastions from intersecting predicted native fortresses or competing bastion reservations. Require exposed Nether fossils and huge fungi to fit their complete loaded footprint, retain intentionally buried fossil behavior, protect underground reservations and stop Nether vine columns at obstacles. Existing saved structures remain unchanged.
- Replace adjacent-water dried-ghast hydration with contained source water, native translucent rendering, bucket interactions and a persisted server-owned twenty-minute loaded-tick timer. Preserve gradual drying and partial progress across unloading/restarts, hatch a real Ghastling, restore contained water on player removal and correct stable child/adult age scaling and snowball growth feedback. Retain the existing Honey Block and recipes.
- Normalize legacy hand transforms before applying released crossbow display transforms. Add distinct player/pillager loading and aiming poses, synchronized pillager charging tied to its actual shot, loading sprite stages and a native illager hand path. Bundle the missing released End flash texture.
- Document configuration, saved-data behavior, height adaptations and deliberate gameplay limitations in WORLD_GENERATION.md and the existing content/coverage references. Preserve Forge 1.7.10, existing registrations, packet identities, optional integrations and the pinned build stack.

## 2026-10-23 03:00 — Reserve underground rooms against neighboring population

- Make native ore/stone veins, springs and underground flowers honor saved city/chamber reservations, including their full spread. Apply the same guard to EFR ore/deepslate blobs, dripstone, glow lichen and cave vines so a neighboring chunk cannot decorate or replace finished rooms. Native surface and Nether generation retain their existing gates.
- Check lake reservations after the native ground search and origin adjustment, using the actual sixteen-by-eight-by-sixteen footprint rather than the original requested height. Suppress spring columns near reserved underground space to prevent immediate fluid flow through not-yet-populated neighboring caves.
- Reuse the existing Soul Fire Mixin for city flames without adding another block identity. With that Mixin disabled, omit city flame markers rather than introducing spreading ordinary fire; preserve global ignition rules.
- Preserve released chest/torch facings during template placement instead of letting incomplete neighboring chunks choose their orientation. Limit city sculk patches to supported terrain and protect tile entities, reinforced deepslate and crafted structure blocks.

## 2026-10-23 15:55 — Rebrand as UAB and replace fragmented configuration

- Rebrand the maintained project as Universal Adaptive Backporter and rewrite player, server, contributor and release documentation with its EFR lineage and distinct backport approach. Preserve technical mod, registry, asset, network and saved-data identities.
- Replace the split configuration owners with one native Forge `config/uab.cfg`, an update-based feature catalog, player-facing descriptions, family controls, dependency resolution and validated per-mob spawn settings. Remove the global experimental override and old warning screen; keep incomplete features explicitly disabled by default.
- Import recognized legacy split and monolithic settings once, preserving explicit false values, customized values and original files with non-overwritten backups. Validate malformed input without allowing Forge to rename or destroy the source. Keep startup settings fixed until restart, with safe client preferences reloadable through the native GUI. Compare common active settings during the existing connection handshake.
- Retain saved registrations independently of new acquisition. Apply availability to recipes, loot, enchantments, generation and new mob creation while preserving chunk-loaded entities, placed blocks and tile entities. Replace spawn-list entries on initialization, preserving original biome differences when no override is requested.
- Make root `version.properties` authoritative for metadata, artifacts, publication and release-tag checks. Produce `uab-<version>+mc1.7.10.jar` and generated update metadata; remove competing version files and upstream hosting project IDs. Add native Forge configuration and migration regression coverage.

## 2026-10-23 22:20 — Complete practical Update Aquatic content

- Add source-water kelp with random-tick and bone-meal growth, supported columns, harvesting and water restoration. Add fast-eaten dried kelp, furnace/smoker processing, reversible nine-item storage and dried-kelp-block fuel for 20 furnace operations.
- Add dry and submerged sea-pickle clusters, one-to-four placement, underwater light levels, bucket interactions, canonical item drops, lime-dye smelting and bone-meal propagation on living coral. Keep contained water local to these blocks without requiring a fluidlogging mod.
- Decorate new eligible Overworld ocean chunks with kelp and warm-ocean sea pickles without retrogen or neighboring chunk generation. Add independent Update Aquatic content/generation settings, stable saved identities, ingredient dictionary entries, released animated textures/models and English names. Keep fish-spawning settings independent.
- Add all five live/dead coral families with blocks, plants, floor/wall fans, native contained-water states, delayed drying death and Silk Touch harvesting. Generate bounded native warm-ocean coral formations.
- Add Heart of the Sea, buried treasure and craftable conduits. Validate native prismarine frames on the server, scale wet-player buffs with the frame and attack wet hostiles at full strength. Use native timed potion effects and a retained static model; preserve independent configuration and saved tile identities.
- Add released shipwreck and warm/cold ocean-ruin layouts with saved chunk-indexed plans, clipped once-only placement, native loot and drowned. Admit only new loaded footprints, retain consumed loot on restart and extend dolphin treasure guidance to real saved ocean sites. Defer unsupported exploration maps and unavailable rewards; leave general waterlogging and swimming-engine work separate.

2026-10-24 02:38 — Woodland mansions, archaeology, pottery and template smithing

- Add saved native woodland-mansion assembly using released rooms, mirrors, roofs, illager/allay encounters and treasure; limit new generation to suitable Roofed Forest terrain and preserve completed chunks across reloads.
- Add suspicious sand/gravel, server-owned brushing and reward/progress persistence; extend new desert wells/pyramids and existing ocean ruins, and generate buried trail ruins from released pools and capped archaeology processors.
- Add all 23 pottery sherds, four independent pot designs, shaped pottery crafting, item/world rendering, one-stack storage, hopper/comparator support and shattering. Migrate old dispenser-backed pots without losing overflow stacks; retain Trial Chamber decorations.
- Add all 18 armor trim patterns and eleven trim materials, including Coast from shipwrecks and Tide from elder guardians. Supply corresponding template loot, exact-substrate duplication recipes, worn overlays, inventory marks and tooltips.
- Migrate the Smithing Table to three inputs with consumed upgrade templates, preserve equipment NBT/damage and existing scripted two-input recipes, and add independent player-facing feature controls. Disabling upgrade templates retains earlier built-in progression.
- Guard disabled Iron Chests upgrade enumeration when NEI is present without Iron Chests, preventing optional-class loading during item-list construction.

2026-10-24 03:22 — Fix release metadata generation with the configuration cache

- Capture the metadata output location and Minecraft version during Gradle configuration, avoiding unsupported script/project access when the task executes or is restored from the configuration cache. Track the Minecraft version as a task input while preserving the update metadata format and release version rules.

2026-10-24 03:50 — Structure locating, aquatic rendering and enabled generation defaults

- Expand `/uab_locate` with operator-only tab completion for eleven structure types, dimension checks, nearest saved-site lookup and bounded generation-candidate searches where supported. Preserve `/efr_locate` and `/locate_endcity`; distinguish recorded sites from candidates and avoid generating terrain during searches.
- Select contained-water drawing with Forge's terrain render pass instead of its entity render pass. Declare the translucent pass for aquatic plants and dried ghasts so kelp, coral, sea pickles and hydrated ghasts retain their contained water surface even when a render section has no ordinary water blocks.
- Restore guardian side/top/bottom geometry with its intended texture coordinates, correct the eye and twelve diagonal spines, and add the articulated three-part tail for both normal and elder textures. Preserve entity identities, elder scale, combat and networking.
- Enable Sculk and Nether biomes by default so fresh configurations include Ancient City and bastion prerequisites. Preserve explicit existing disables, provider compatibility rules and the disabled catalyst XP-spreading path.

2026-10-24 11:14 — Fix external wood fence texture registration

- Give UAB's external wood fences the same delegated icon registration contract as its other plank-backed fences. Biomes O' Plenty and Witchery planks retain ownership of their side/metadata textures; fences read the current source icons after resource reloads instead of passing an unused null texture name to vanilla registration.
- Preserve vanilla fence registration, fence/wall connectivity, rendering and saved block identities. Add focused registration, source-icon delegation and reload regression coverage to the normal verification tasks.

2026-10-24 17:21 — Separate native targets and begin Forge 1.12.2 support

- Move the maintained 1.7.10 sources and build into versions/1.7.10. Share platform-neutral feature metadata, deterministic seeds, resources and Forge configuration logic while retaining native gameplay ownership.
- Add an independent Unimined/Forge 1.12.2 build and initial native materials, barrel/smithing, aquatic entities, tridents, conduits and ocean structures. This remains an incomplete development port, documented separately from maintained 1.7.10 coverage.
- Coordinate independent buildAll/testAll/cleanAll tasks, preserve root version.properties, generate combined update metadata and verify production target metadata and Java 8 bytecode. Retain legacy root Gradle run-task entry points.
- Translate released ocean palettes and native loot pools, defer unavailable prerequisites/rewards and suppress generation observer notifications that could load neighboring chunks. Preserve structure completion, consumed loot and barrel inventory identities.
- Preserve smithing equipment NBT/damage and consume template, equipment and ingot inputs. Keep contained-water behavior local to native Forge hooks; prevent Fortune from multiplying ore blocks when raw drops are disabled.

2026-10-24 17:21 — Correct feature attribution and preserve category preferences

- Move feature attribution into the shared catalog rather than feature-name exceptions in version-classification code. Record component dates for mixed prismarine/red sandstone shapes, Nether bricks, banners, boats, note-block instruments, basalt, tuff, sponge drying, shulker boxes and salmon sizes.
- Correct patch/update attribution for iron nuggets, zombie-villager professions, squid ink, food values, world thumbnails, mushroom Silk Touch, crafting-close behavior and selected sounds. Put custom recipes, alternate gameplay rules and mod integrations under General rather than implying vanilla availability.
- Migrate corrected UAB property locations with an exact, non-overwritten .catalog-v2.bak backup. Preserve explicit values, existing canonical settings, old parent-switch disables and unrelated settings. Keep native prerequisites enabled without generating misleading backport controls.
- Reject conflicting classification aliases and inconsistent component dates, align furnace-crackling aliases as client preferences, retain the legacy FeatureRegistry.TARGET API constant and update translations and distinct-feature counts.

2026-10-24 18:34 — Expand Forge 1.12.2 content, native rendering and connection checks

- Exchange immutable gameplay settings during Forge login and server-list queries. Reject mismatched gameplay settings before world entry while retaining independent client preferences and the legacy client-only server policy. Require MixinBooter 11.18 or later for the Forge handshake hooks.
- Add the shared native configuration screen, safe persistence of pending restart settings, readable update labels and legacy-language adaptation. Preserve technical registry names and existing translations; fill missing content names from the released assets.
- Replace placeholder cubes with released block-state/model geometry, native lantern/chain placement and bounds, separate kelp/water render passes, drowned outer skin, and three-dimensional held/projectile tridents. Supply kelp's source-water level to native fluid queries so nearby flowing water and dropped items cannot crash. Validate the offline asset archive's Minecraft version.
- Give barrels their own native lootable inventory instead of chest tile inheritance, preserve existing inventory/lock/loot NBT, orient the block and synchronize its open state. Clear stale open states after loading without discarding stored items.
- Port branching cherry, shallow-water mangrove roots/propagules and four-sapling pale oak growth. Validate complete tree placement before writing, respect the native growth event and retain observer-safe new-chunk placement.
- Add native single/double slabs, merging, wall connections and building recipes for the supported wood, deepslate, blackstone, tuff, mud, smooth-block, prismarine and red Nether brick families. Retain previously registered stair identities and add polished/chiseled tuff decorations.
- Complete warm/large ruin selection, cold masonry overlays, once-only primary loot/mob markers and beach shipwreck eligibility. Add saved buried treasure and its locate command, native structure reservation checks, exact native fish metadata and pool-level water-breathing potion loot. Consumed loot and existing saved plans remain intact; unavailable exploration maps and rewards remain deferred.

2026-10-24 18:25 — Port native deepslate geology and amethyst acquisition

- Add the eight native deepslate ore variants, ore-specific harvest levels, delegated drops/experience and smelting. Convert vanilla iron/gold harvests to raw ores while preserving Silk Touch, unrelated contributed drops and configured Fortune/base quantities. Restore deepslate's axis, cobbled drop, hardness preferences and building progression.
- Generate configured deepslate layers or ellipsoid clusters and tuff in new chunks, retaining dimension/replacement controls, existing ore identities and saved structure reservations. Confine geology writes to the population chunk.
- Add budding amethyst, all four oriented growth stages, native harvesting/piston rules, amethyst-block and tinted-glass crafting. Generate maintained double-Perlin geode shells with configurable materials, validate complete placements before writing and save unloaded candidates for bounded server-thread completion after their footprint is populated.

2026-10-24 18:44 — Port the native workstation and building-content batch

- Add smokers and blast furnaces with independent native recipe ownership, automatic food/metal selection, explicit overrides/removal, hundred-tick cooking, proportional fuel consumption and native output experience. Preserve inventories, locks, names and full-width fuel timers; expose native sided inventory and item-handler automation.
- Port stonecutters, looms and cartography tables with server-owned recipe selection, atomic output extraction, repeated shift crafting and safe return of transient inputs. Retain native banners and reusable pattern items; copy, scale and lock native map identities with persisted terrain-update locks.
- Add composter levels, delayed maturation, bone-meal output, configured composting percentages and native hopper/item-handler access. Inventory queries do not consume ingredients or extract compost, and failed hopper transfers can restore their output.
- Retain barrel, cooking and composter tile entities when only their block properties change, preventing Forge's default modded-state refresh from discarding inventories or automation state.
- Add missing vanilla stone/slab/stair/wall families and explicit stonecutting conversions. Port modern wood doors, trapdoors, buttons and pressure plates, preserve their general shape controls, register native wood ingredients and keep crimson/warped wood out of furnace fuel.

2026-10-24 19:12 — Port aquatic plants, bubble columns and ocean climates

- Add every live/dead coral block, plant and floor/wall fan, plus dry/submerged sea-pickle clusters. Preserve canonical item drops, Silk Touch, support checks, delayed coral death, bucket transitions, bone-meal spreading and pickle light/dye behavior.
- Consolidate local source-water behavior across aquatic plants, retaining native fluid depth, water after harvesting, separate plant/water render passes and released geometry. Ignore constant fluid-depth properties in model selection instead of baking redundant meshes.
- Add source-water bubble columns above soul sand/magma, separate direction from fluid depth, preserve native liquid rendering and apply server/client movement and air effects. Column updates remain bounded to loaded vertical terrain.
- Register six native ocean-climate biomes and port the maintained seeded climate-cell layer after native coastline/river selection. Preserve native monument eligibility, biome identities, new-chunk-only decoration and warm-ocean acquisition for coral, pickles and tropical fish.
- Deep-copy the source map's saved color data when locking a map so later updates to the original cannot change its locked copy.


2026-10-24 19:46 — Port bamboo, cave plants and native surface biomes

- Add bamboo shoots, native stalk age/leaves/stage states, bounded growth, bone meal, sword harvesting, fuel and jungle decoration. Port bamboo blocks, stripping, planks, mosaics and their native slab/stair/fence/door/redstone families, preserving separate plant and building controls and exact material yields.
- Port moss/carpet spreading, normal/flowering azalea and leaves, native oak-based azalea trees, glow berries, climbing cave vines, harvesting, shearing and saved tip growth limits. Keep berry-state changes from replacing the growing tip tile.
- Add six-face glow lichen with native synchronized attachment masks, precise face selection, shears, bone-meal spread and support removal. Cave decoration stays in new chunks and yields to saved structure bounds.
- Register the ten maintained surface-biome identities through Forge and native climate selection, with their generation controls, legacy terrain profiles, colors, native inhabitants and tree families. Add pale moss, carpet and hanging moss for natural Pale Garden vegetation; retain loaded-footprint validation for tree placement.


2026-10-25 00:04 — Port turtle and dolphin survival content

- Add native dolphins and turtles with the maintained entity identities, native spawning controls, saved state and released geometry. Dolphins retain moisture/air needs, retaliation, item play, swimmer following and guidance to saved treasure/ocean sites/monuments without generating terrain.
- Port seagrass, shears and paired tall growth; retain independent natural generation and water bone meal. Add turtle beach breeding, saved home and pregnancy, return-to-home egg laying, egg count/crack states and incubation, child growth, scutes, repairable turtle helmets and their breathing reserve. Native egg collision enables real trampling callbacks while respecting sneaking, turtles and griefing hooks.
- Register Dolphin's Grace through the native Forge potion registry and released effect icon. A mapped Mixin changes only the player's horizontal water drag after native Depth Strider handling; native vertical movement and swimming controls remain authoritative.
- Map pale moss carpet to the maintained flat geometry instead of feeding unsupported modern side properties to the native model baker.


2026-10-25 00:24 — Port cave aquatics, native buckets and population admission

- Add axolotls with synchronized variants/play-dead recovery, moisture, tropical-fish breeding, inherited/rare variants, hunting, guardian targeting and assisted-player regeneration. Preserve variant, age, health, name and persistence across capture/release. Add glow squid with native squid movement, synchronized damage-darkening, full-bright rendering and conditional glow ink drops. Keep its light state distinct from the native outline-glow effect.
- Replace the custom bucket placement path with native ItemBucket handling so ray tracing, permission checks, Forge bucket events and evaporation remain authoritative. Spawn and restore the bucketed animal only after successful native placement; failed entity joins retain the original bucket.
- Port loaded aquatic lifecycle indexing, shared/species budgets, small-area school allowances and local density. Native world listeners count successful joins and removals; natural spawning alone performs admission. Preserve native monster ownership for guardians, leave other mods' squid subclasses alone, and retain commands, conversions, spawners and bucket releases.
- Respect native NoAI for custom aquatic steering and retain the native breeding, potion and persistence boundaries.


2026-10-25 22:53 — Port Nether biomes, vegetation, inhabitants and equipment to Forge 1.12.2

- Add crimson/warped nylium, roots, sprouts, fungi, huge fungi, shroomlights, hyphae and climbing vines. Preserve exact family prerequisites, loaded-footprint checks, inventory protection, bone-meal spreading and stopped vine tips.
- Adapt the maintained weighted Nether biome layer to native Forge biome identities. Add biome surfaces, forest vegetation, basalt formations, delta pools and released fossil templates while retaining native terrain, caves, fortresses and existing chunk data. Foreign biome providers retain ownership. Restore vertical defaults for generated basalt and muddy roots, and retain fractional biome fog blending with darkness, potion and graphics settings.
- Port piglins/brutes, hoglins/zoglins and striders, native model renderers, bartering, group anger, breeding, conversion and saddle/boost interaction. Preserve equipment and entity state during conversion; keep strider movement server-owned with native rider input and a separate lava spawning path.
- Add charged crossbows with saved arrow ammunition, quick charge, multishot, piercing and tipped/spectral effects. Add treasure-only Soul Speed, native movement modifiers and soul-sand slowdown cancellation. Disabled prerequisites omit acquisition paths instead of substituting ingredients.
- Retain configured aquatic spawn-entry identity between native selection and eligibility queries, allowing actual natural spawning while preserving population admission.
