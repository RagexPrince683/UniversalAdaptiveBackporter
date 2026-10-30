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


2026-10-26 06:55 — Port functional content, archaeology and remaining compact creature families to Forge 1.12.2

- Add flowers and native dye crafting, suspicious stew, candles, targets, creative light blocks, decorative copper, lightning rods, lava/potion cauldrons, dripstone, brushable blocks and decorated pots. Preserve native redstone, potion/item NBT, tool permissions, saved inventory identities and observer updates; keep hidden archaeology loot out of client updates. Consolidate copper waxing, scraping and oxidation across native shapes and doors.
- Add the maintained released loot tables, pottery designs, ancient crops and sniffer eggs. Retain source acquisition weights in native dungeon/structure loot, require actual enabled materials, and preserve brush progress, plant halves, hatching progress and saved item metadata.
- Port cats, pandas, wandering traders, trader llamas, allays, sniffers, armadillos, brown mooshrooms, bogged, parched, pillagers, ravagers and breezes, including their maintained interactions, AI, genetics, equipment, saved state and native renderers. Add wolf armor and wind-charge interactions. Village raids use persisted world state and native 1.12.2 evokers/vindicators.
- Add sculk sensors/shriekers, the Warden, Darkness and Swift Sneak. Preserve wool occlusion, player warning cooldowns, native redstone and lifecycle indexing; catalyst spreading remains intentionally absent as in the maintained implementation.
- Complete Creaking Hearts, bound Creakings, resin and eyeblossoms; natural pale trees can contain hearts. Add copper chests, bounded loaded-chest sorting, copper golem oxidation/statues and revival. Chest and statue rendering retains the maintained geometry and metadata.
- Port frog pregnancy, frogspawn, bucketed tadpole maturation and froglights. Add camels/camel husks, happy ghasts, dried-ghast hydration and nautilus variants. Use native multiple-passenger lists and persistence instead of auxiliary seat entities, retain server ownership of movement and native rider input, and preserve harness colors, body armor, dash cooldowns and flight homes. Breath of the Nautilus pauses oxygen consumption without refilling air.
- Register modern sounds on both sides and acquire the released audio into a checked content-addressed client cache during startup. Keep network/file acquisition out of playback and rendering. Adapt native entity/equipment layers, item metadata models and contained-water rendering to the destination engine.


2026-10-26 08:15 — Port bees, foxes, goats and cosmetic smithing to Forge 1.12.2

- Preserve the maintained goat ramming/jumping AI, screaming variants, horn ownership and eight instruments. Port fox hunting, sleeping, trusted players, berry gathering, food effects, breeding and carried-item rendering; use native rabbit/polar-bear/fish targets and native item ownership.
- Add native bee flight/pollination, crop growth, stinging and saved three-occupant hives/nests. Retain honey comparators, smoke tags, Silk Touch bee/honey data and native placement. Release saved occupants only after successful entity admission, and update their home when a hive is relocated. Eligible native and modern tree growth can populate nests without replacing distant terrain.
- Port the public block/item/biome tag helpers, metadata references and deprecated bee-plant adapter to a canonical inheritance index with coherent reverse-query invalidation. Discover modded flowers/crops after registration and preserve the bee growth extension point.
- Replace hardcoded smithing output selection with the maintained recipe API, preserving two-input addon recipes and full equipment NBT. Add all eighteen armor-trim patterns and eleven materials, native armor-layer/inventory overlays, tooltips and seven-diamond template copying with exact enabled substrates. Add independent template rolls to the corresponding native treasure/elder-guardian tables without replacing their existing pools.
- Preserve allay/sniffer item ownership when entity admission rejects a delivery. Add the maintained honey-drip appearance and held-item light-block markers/selection, and adapt filled hive and tooting horn models to native item overrides.


2026-10-26 15:34 — Port saved structures, finite encounters and record playback to Forge 1.12.2

- Add native saved bastion, trail-ruin, Ancient City and Trial Chamber assemblies with released palettes/processors, deterministic native terrain admission and native-structure reservations. Persist completed population chunks so carving, loot and bound mobs are not repeated; refuse existing saved terrain and never request neighbor chunks during template placement. Preserve native woodland mansions and fortresses.
- Add maintained outpost towers and saved surrounding allay cages, tents, logs and targets, with native pillager ownership, banners and loot. Extend the locate command to eligible new structure families without generating their terrain.
- Port finite Trial Spawner configurations, normal/ominous encounters, outstanding-mob persistence, reward queues and cooldowns. Add per-player vault history, matching keys and saved ejection queues. Keep server encounters authoritative while the native client supplies block models and spawner previews.
- Correct Trial Spawner floor selection on both targets: raised chamber/dungeon spawners now search nearby supported heights instead of requiring the surrounding floor to be level with the spawner block. Preserve collision, liquid, loaded-chunk, finite-wave and accepted-spawn checks.
- Add all nine maintained music discs, fragment crafting, native jukebox playback/comparator values, treasure pools and the existing returned-fireball/chicken-jockey acquisition rules. Adapt flattened structural states, colored beds, skulls, potted plants and released client geometry to native 1.12.2 state and tile data. Prevent ores, delayed geodes and later vegetation from overwriting reserved structures.


2026-10-26 16:34 — Port native vehicles, wood signs and portable storage controls

- Add the maintained boat materials, chest boats and bamboo rafts using native 1.12.2 paddle packets, movement validation and passenger lists. Preserve vehicle/material/name/inventory data across native portal transfers; keep chest-boat inventory ownership through retirement of the old entity. Add material-correct recipes and dispenser placement, retaining historical item identities and avoiding duplicate creative entries for native boats.
- Retain configurable boat speed, land glide, second seats, rider ID/class filters, optional underwater buoyancy, fall-breaking distance and free view rotation at native owners. Keep client steering hooks separate from dedicated-server code. Correct the catalog so custom boat controls are not removed as vanilla-supplied settings on 1.12.2.
- Add eleven native wood-sign families with standing/wall placement, text components, saved data and native sign-editing packets. Send the placed block before opening its editor so the client selects the correct tile renderer and wood texture. Preserve native permission/ownership checks and the maintained registry identities.
- Add undyed shulker boxes through native opening, inventory, collision, loot and item handlers. Preserve the original recipe identity when changing shell-and-chest crafting to the undyed output, and keep dyeing/washing inventory data intact. Apply maintained storage bans to native GUI slots, hoppers and item capabilities; port native shulker dye/water interactions and the configurable client tooltip limit.
- Add maintained boat/raft, chest, sign and portable-box rendering. Reuse native inventory screens and container packets, including inventory-key opening from a ridden chest boat. Existing native dyed boxes and ordinary boats retain their registry identities.


2026-10-26 20:38 — Port client lighting, ambience, menus and native plant rendering

- Add native float-based lightmap calculation with independently configurable warm block light, hue-preserving gamma, night-vision color floors and End ambient color. Retain Forge provider color hooks and native calculation when the additions are disabled or the provider is incompatible.
- Add deterministic End sky flashes, matching light/fog changes, delayed positional sound and the public client flash API. Preserve custom sky renderers and restore renderer state. Add maintained Nether ambience, biome music/moods, fading loops, spores and ash, plus the additional cave sounds.
- Add shape-bounded block-breaking particles while preserving block-provided effects, maintained sound distances, the five later note-block instruments, native book page-turning and successful bonemeal sounds. Apply the maintained block-sound adaptations at native sound-type boundaries.
- Add the four-mode F3/F4 selector through native server commands, the maintained texture/language override pack, and loading displays driven by native progress and received client chunks. Retain other Forge loading-screen handlers.
- Correct standing wood-sign editor transforms. Accept maintained flowers, roots, fungi, saplings and azaleas through native flower-pot interactions and saved tile data; render their additional plant while retaining native pot geometry, drops and permissions.


2026-10-26 20:38 — Port native movement, sleeping and compatibility hooks

- Add configurable item buoyancy, momentum preservation, netherite/debris fire resistance and native liquid-current adaptations. Retain existing third-party immunity, current item ownership, historical fireproof-item identity and custom flammability settings. Scale actual native current contributions, including their inlined axis constants.
- Add maintained horse flotation, jump-assisted climbing and projectile block callbacks at native movement owners. Allow configured ascending Elytra takeoff through native client requests and server equipment/state validation. Preserve native behavior when these additions are disabled.
- Add the saved sleeping-player percentage rule using current per-world player counts, spectator exclusion, fully asleep checks and native night/weather handling. Keep threshold rounding, a one-sleeper minimum and values above one hundred disabling night skipping.
- Synchronize server-owned damage direction through the maintained packet identity, applying only finite values to the current client connection on its game thread. Classify the camera preference as client-owned. Add raw-item ore tags and Nether sponge drying, preserving native absorption and recipe identities.
- Retain the fake tile inventory API at the native hopper boundary, squid ink/blindness controls and half-block step-height normalization. Classify verified native zombie/golem combat and embedded-arrow collision retention independently of their General grouping; keep the additional step normalization configurable.


2026-10-26 22:04 — Complete native optional ore, storage and legacy settings adapters

- Add canonical public raw-ore and metadata-indexed deepslate mappings. Resolve real optional ore/ingot prerequisites and blacklist entries, preserve maintained metal slots and special ore identities, and forward native harvest levels, drops, experience and smelting. Keep missing materials unavailable and retain foreign copper support when UAB copper is disabled.
- Add optional Iron Chests barrel and shulker upgrades using native inventories, item handlers, server containers and saved item data. Preserve their different tier-number contracts and all maintained upgrade metadata, names, locks, loot, colors and contents. Retain capacity independently of acquisition settings or optional-mod presence; add maintained tier textures, crystal previews, wide inventory layouts and optional returned shells.
- Extend native Mending selection to equipped Baubles while retaining native XP costs, repair ratios and Baubles synchronization. Keep the integration optional.
- Add maintained rose/old-gravel and prismarine recipes, the historical daylight-detector identity and native End-portal pick item. Apply the red-sandstone family prerequisite to its additional shapes.
- Add alternate husk/stray spawning and optional natural End shulkers, preserving configured spawn parameters and native structure persistence. Classify verified native skin layers, grass inventory tinting and fence/wall connections without replacing their native owners.

- Supply native loot-table entry resources for every maintained structure reward table, so saved native containers reach the released-loot adapter through Forge loading. Keep optional ore color registration in client initialization after native item colors exist.

- Synchronize storage tiers in native chunk and tile updates, with bounded crystal previews, while native container packets own the open inventory. Send the authoritative block and tier before opening larger windows, including immediately after an upgrade. Forward optional ore harvest-tool policies exactly, including native null-tool and negative-level rules.

2026-10-26 23:13 — Guard native raw-ore harvests during fluid destruction

- Skip empty harvests and blocks without an item form before querying Forge 1.12.2's OreDictionary. Water destroying these blocks, including during immediate spring-generation updates, no longer aborts the integrated server with an invalid-stack exception.
- Resolve raw-ore mappings only when a nonempty matching block drop can be converted. Preserve other drops, silk touch, fortune, metadata, copied tags and optional ore blacklists; ignore empty or null drop entries without replacing them. Keep vanilla fluid propagation, notifications, scheduling and solidification unchanged.

2026-10-27 16:12 — Restore Enderlicious cities and safe extended potion identities on 1.7.10

- Retain Enderlicious's native End provider and add saved End City population through Forge, including the existing attached towers and ships. Predict native terrain without loading chunks or advancing decoration randomness; adapt the support-height threshold to its lower terrain band while preserving the seeded placement grid and central-island exclusion. Honor existing content/generation gates, protect saved terrain and persist completed city population sections.
- Preserve End City start validity across save/reload, including older starts, and let both city locator commands use the supported native terrain adapter.
- Expand the native potion registry to at least 128 slots without shrinking external expansions or losing entries. Require the enabled EndlessIDs potion module for explicit IDs above 127, bounded by the live registry and UAB's existing 32767 configuration limit. Keep automatic allocations in the native range, reject malformed/unsupported IDs and collisions explicitly, and preserve existing saved allocation identities without automatic remapping.

2026-10-27 20:30 — Complete missing honey survival paths and repair native content integration

- Add dispenser shearing/bottling on both targets, the missing 1.12.2 honey crafting/unpacking recipes, sugar recipes on both targets and glass-container returns. Restore legacy honey nutrition and the native drinking animation/duration. Keep legacy hive harvesting server-owned, save honey/occupant changes and retain bees when release is cancelled. Keep fire evacuation with the hive tick owner.
- Render 1.7.10 lightning rods with released geometry, explicit UVs, powered textures and six orientations. Replace 1.12.2 aquatic plants' static water cube with native fluid rendering through a local read-only view, retaining native tint, surface heights, flow and overlay behavior.
- Add the missing 1.7.10 single/tall seagrass water pass. Normalize adjacent submerged vegetation and locally water-filled blocks while rendering contained water, preserving native fluid heights and biome tint without changing the world.
- Add 1.7.10 parrots, a distinct Elder Guardian egg, snowy-biome igloos with basement curing supplies and saved loot, and opt-in world-locked 128-stronghold rings using native portal generation. Preserve the corresponding native 1.12.2 systems and existing entity identities.
- Add native campfires/soul campfires with persistent four-slot cooking, lighting/extinguishing and bee calming on both targets. Add 1.12.2 honey piston adhesion, slime separation and entity carrying through server-safe piston progress. Restore honey's half-strength jump, avoid duplicate walking slowdown on 1.12.2 and correct the legacy honey collision base.
- Preserve HEE's End provider and Netherlicious biome surfaces. Add clipped saved bastion population for externally owned legacy Nether terrain, retaining native terrain/provider ownership and existing bastion identities. Record remaining content and integration gaps in the implementation matrix.

2026-10-27 23:36 — Add Mace survival combat and exact enchantment rewards

- Add the Heavy Core/Breeze Rod Mace recipe, 500-durability weapons, Breeze Rod repairs, native attack attributes and smash damage on both targets. Route full smash damage into native hit and armor processing, retain native critical decisions, and apply impact knockback and saved fall context after successful hits. Wind Burst uses a non-destructive, occlusion-aware impulse and permitted wind-reactive interactions.
- Register Density, Breach and Wind Burst with stable legacy IDs and native 1.12.2 registry identities. Preserve native/Forge special-armor processing while reducing armor effectiveness for Breach, and restrict Mace enchanting to its supported enchantments. Keep Wind Burst out of ordinary enchanting/trade book lists, including the native 1.12.2 librarian chooser; enforce damage-enchantment exclusivity in both legacy anvil directions.
- Resolve released random-loot enchantment tags and explicit option lists rather than ignoring them. Convert book rewards into real enchanted books, restore explicit legacy Swift Sneak rewards and support fixed ominous-vault Wind Burst rewards without substituting unsupported enchantments.

2026-10-28 00:49 — Resolve native sound samples before ambience replacement

- Fix a 1.12.2 client crash when cave, Nether music or note-block replacements read an unresolved native sound's volume and pitch. Resolve the original sample at the Forge sound-event boundary before preserving its playback properties.

2026-10-28 06:59 — Complete adaptive workstations, decoration and update families

- Complete campfire food display and client synchronization, hay-bale signal smoke, Frost Walker protection, burning-arrow ignition and bucket draining while retaining native cooking and bee calming.
- Add native grindstone repair/disenchant containers, bells, spyglasses, hanging signs and chiseled bookshelves to both targets. Reuse existing workstation extraction and sign ownership/networking; keep native village and profession systems authoritative.
- Add saved deterministic ruined portal sites in Overworld and Nether terrain, with repairable broken frames, chest rewards and saved-site discovery. Keep each footprint within its owning chunk and preserve external terrain providers.
- Add Copper Age equipment, nuggets, torches, weathering fixtures and twelve wood shelves. Reuse copper oxidation/wax/scrape/lightning behavior, preserve rod orientation and power through identity changes, correct legacy neighbor-stage lookup, and prevent client-side wax inventory mutations. Copper horse armor uses native slots, persistence and tracking; its crafting recipe provides an adaptive survival route.
- Add Spring to Life vegetation, fallen logs, climate animal coats and sheep colors, wolf coats/voices and colored eggs. Retain native animal identities and AI, persist variants and synchronize tracking snapshots on the client thread; preserve older saved coats and breeding/hatching inheritance.
- Define Adapted coverage separately from unfinished functionality. Native movement/presentation, compatible village ownership, loot/rates and placement/spawn adaptations are deliberate backport choices, not missing features.

2026-10-28 08:36 — Preserve copper state and complete adaptive sculk integration

- Preserve copper-chest inventories and active container ownership across oxidation, waxing and scraping; add the missing weather/wax identities under the existing chest setting.
- Preserve lightning-rod pulse deadlines through native identity changes, translate retained model face references for the 1.12.2 model loader, and apply the released dry-foliage palette to leaf litter.
- Bind legacy animal appearance snapshots to server identities without relying on UUIDs omitted by vanilla mob spawn packets. Keep native 1.12.2 UUID checks and use server-safe wolf sound registry names.
- Add calibrated sensor filtering, comparator frequencies, persisted pulses and loaded-chunk vibration routing. Feed native actions into that routing without treating ordinary block placement as an earlier container interaction.
- Add bounded, saved catalyst spreading charge from native mob XP. Retain excess XP, avoid duplicate collectible orbs and preserve the legacy catalyst entry point.
- Adapt cow and pig texture sheets to native model dimensions, preserving shared model UVs used by other renderers.

2026-10-28 09:26 — Add persistent lodestone and recovery navigation

- Activate lodestones and bound compasses on both native targets with the latest iron recipe, survival binding/retargeting, preserved custom tags, dimension tracking and loaded-only target validation. Ordinary vanilla compasses retain their own behavior.
- Add recovery-compass crafting, holder-specific last-death targets and native death/respawn persistence without overwriting unrelated saved player data. Add the missing 1.12.2 echo shard to existing Ancient City loot resolution.
- Use native icon/item-property needle frames and inventory NBT synchronization. Adapt copper horse armor to native 128-pixel UV coverage with the released copper palette.

2026-10-28 09:43 — Keep native animal appearances by default

- Make Spring cow, pig, chicken and wolf texture overrides a separate client preference, disabled by default. Preserve appearances and models supplied by vanilla or other mods while keeping Spring gameplay and saved variants independent.
- Allow explicit client opt-in without changing server settings, and tolerate externally replaced texture methods. Native models and their shared UV data remain untouched.

2026-10-28 10:06 — Add functional native lecterns

- Add craftable lecterns on both targets with saved book contents and pages, server-owned reading controls, comparator output, short page-turn pulses and downward hopper extraction.
- Render books with the native book model and use an adapted read-only screen with long-page scrolling. Taking a book requires inventory space; native village professions and other mods retain ownership of their existing systems.
- Replace decorative bookshelf substitutions in released structures with functional lecterns and translate released template books into native book data.
- Preserve lectern and shelf inventories and open-container tile identities on both targets when page pulses, book occupancy or redstone power change block state or metadata.

2026-10-28 10:35 — Add rooted dirt and hanging-root survival paths

- Add rooted soil and ceiling roots on both targets, with bone-meal growth, hoe conversion, shears collection and hanging-root composting.
- Supply rooted dirt beneath successfully grown azaleas and through additive wandering-trader offers. Preserve tree-growth protection hooks, failed-growth soil restoration and existing cave-biome ownership.

2026-10-28 10:51 — Keep shoulder parrots out of the first-person camera

- Hide only the local player's perched parrots while viewing through that player's first-person camera on 1.7.10. Keep their tracked entities, shoulder state and visibility to other players and third-person cameras intact.

2026-10-28 11:00 — Add legacy directional shields

- Add craftable 1.7.10 shields with a native one-hand pose, directional blocking, raising delay, durability, plank repairs and player-wide axe disable timers synchronized through item data.
- Keep the old sword damage reduction separate from shield protection and render shields as native 3D items. Preserve vanilla 1.12.2 shields without adding replacement registrations or controls.

2026-10-28 14:05 — Add spear-specific Lunge enchantments

- Register Lunge I–III on both targets with enchanting, book/anvil and released-loot integration. Primary actions request a server-owned horizontal impulse with hunger, durability and movement checks.
- Preserve native attack rules and existing spear thrusts; mining and right-click interactions do not activate the ability. Keep its movement limit separate from attack cooldowns, and honor copper/netherite material prerequisites for legacy spear availability.

2026-10-28 14:15 — Add netherite horse armor through native equipment

- Add netherite horse armor on both targets, using native horse inventory, persistence, synchronization and armor calculations. Preserve existing copper armor identities and native model geometry.
- Upgrade diamond horse armor through the existing netherite smithing and template settings, retaining base-item data. Keep disabled-feature registrations available for saved equipment.

2026-10-28 14:32 — Match native right-hand shield poses

- Use native right-hand shield display transforms for idle and raised first-person views. Remove inherited flat-item scale and rotation from the held model so raised third-person shields cover the front correctly.
- Turn only the shield-bearing right forearm into its blocking pose, preserving other items and keeping handedness, inventory and combat rules unchanged.

2026-10-28 15:05 — Add native automatic crafters

- Add nine-slot crafters on both targets with disabled slots, comparator output, four-tick rising-edge crafts, hopper access and six output directions. Keep each target’s native recipe and inventory APIs authoritative.
- Preserve committed results and recipe containers in saved output state, deliver through adjacent inventories before ejection, and retry refused output without recreating the portion already accepted. Synchronize disabled slots through native container messages and keep render assets on client paths.
- Normalize zero-count legacy inventory stacks before recipe snapshots so empty-stack callers cannot stall automatic crafting.

2026-10-28 16:25 — Add weighted bundles and preserved color recipes

- Add bundles and all sixteen colors on both targets with weighted storage, nesting limits, native inventory right-click interactions, last-in-first-out retrieval, capacity bars and contents tooltips.
- Preserve stored item data through dyeing and saves, respect slot permissions and crafting-result ownership, and retain items when a world drop is refused. Keep unrelated inventory clicks on their native paths.

2026-10-28 16:45 — Add native scaffolding support and climbing

- Add scaffolding on both targets with six-block bridge support, vertical stacking, native falling entities, one-way platforms and jump/sneak climbing. Use local scheduled updates and preserve native placement permissions and world saves.
- Keep legacy bamboo-item acquisition under its existing bamboo-plants setting, independent of bamboo wood, so scaffolding retains its actual survival prerequisite.

2026-10-28 16:47 — Leave automatic crafters disabled by default

- Default the crafter acquisition setting to false on both targets. Preserve explicit existing settings, saved blocks and inventories.

2026-10-28 18:59 — Restore native ruined-portal chest loot

- Supply the missing 1.12.2 native loot entry so Forge invokes the maintained ruined-portal reward adapter instead of caching an empty table.
- Check every released table for its required native entry during artifact verification, preventing newly added loot tables from silently producing empty treasure.

2026-10-28 19:09 — Add powder-snow survival and freezing interactions

- Add powder snow, buckets and snowy cauldrons on both targets, including native bucket interactions and dispenser placement/collection. Reuse sampled native snowfall positions for cauldron filling without terrain replacement or world scans.
- Add sinking, leather-equipment protection, leather-boot support/climbing, persisted freezing/thawing and skeleton-to-stray conversion. Preserve native entity data and refuse conversion while another riding or leash relationship owns the entity.

2026-10-28 19:44 — Add scoped respawn-anchor integration

- Add glowstone charging, dispenser charging, light/comparator output, survival crafting and saved anchor bindings on both targets. Charged anchors used outside Nether-like providers use native explosions.
- Resolve safe exits through the existing worlds and scope the provider exception to a validated native death respawn. Preserve native spawn-point changes and End-return handling, and consume a charge after a successful anchor respawn.
- Preserve saved bindings when native respawn restores an existing bed during fallback; treat later player-selected bed and spawn-point changes as authoritative.

2026-10-28 19:51 — Expose the missing update groups in configuration screens

- Include Bundles of Bravery and Spring to Life in both native configuration screens so their existing settings remain discoverable. Validate update-group coverage when checking the shared catalog.

2026-10-28 23:45 — Fix respawn-anchor startup with Mixin injection counting

- Allow the unused development or production naming variant of each respawn-anchor hook to match zero calls on both targets, including when Mixin injection counting is enabled. Preserve the group requirement that a provider hook and stored-spawn hook must each match.
- Prevent the resulting player-manager loading failure when starting an integrated or dedicated server without changing anchor bindings, respawn rules or charge consumption.

2026-10-28 23:48 — Fix decorated-pot item rendering on 1.12.2

- Render decorated-pot items through Minecraft's registered tile renderer so texture binding uses the initialized dispatcher. Prevent inventory rendering crashes while retaining the same model and item designs.

2026-10-29 00:17 — Keep elytra cameras separate from movement and repair rocket interactions

- Calculate legacy elytra camera height inside camera rendering instead of changing the player's physical position offset, preventing illegal-stance disconnects and keeping packet and collision coordinates authoritative.
- Reject invalid takeoff requests on both targets. Stop legacy boosts when their rider dies, mounts or changes worlds, accept entity ID zero, and support air/block firework use while respecting interaction denial and refused entity spawns. Expose native flight-duration 1–3 rockets in the 1.12.2 UAB tab alongside the existing legacy entries.
- Align native client prediction with server rocket ownership so a block click cannot fall through into a second air-use packet and consume a second rocket.
- Preserve legacy spear ray origins when native block tracing mutates its input vector, keeping thrust visibility and Lunge mining checks consistent.

2026-10-29 00:17 — Make optional mixin hooks compatible with injection counting

- Give explicitly optional hooks zero individual debug expectations on both targets, including alternate mapped names and rendering compatibility fallbacks. Preserve required hook groups and their minimum match checks for combat, fluids, inventories, world generation and other maintained features.

2026-10-29 02:50 — Preserve vanilla artwork and separate Happy Ghast harness faces

- Remove the accidental 1.7.10 vanilla stone texture replacement and the optional grass, sponge, dark-oak end and door-item texture overrides on both targets. Retain the old texture setting for configuration compatibility without allowing it to replace vanilla artwork; keep language replacements and added variants independent.
- Separate the 1.7.10 Happy Ghast harness shell from the body to prevent coplanar depth flicker. Preserve its UVs, goggles, render-pass behavior, mounting and passenger positions.

2026-10-29 14:34 — Remove nullable shared state from legacy step collisions

- Read the original movement bounding box directly from Minecraft's local snapshot instead of a separately captured shared reference, preventing a null dereference in the 1.7.10 alternate step-collision hook.
- Anchor the hook at the step-down height read so earlier collision offsets do not determine its injection point. Preserve the dual-approach stepping algorithm and existing configuration.

2026-10-29 20:32 — Repair legacy Underground Biomes generation and modded wood textures

- Correct the world coordinates passed to modded stone during 1.7.10 deepslate conversion. This prevents Underground Biomes Constructs from loading distant chunks for ore replacement, causing extreme world creation delays and generation stalls.
- Recognize Underground Biomes' vanilla-terrain wrapper for modern structure placement, reservations and location searches. Allow its natural geological stones to support dripstone and ancient-city sculk without replacing its generator or disabling UAB features.
- Extend the related natural-stone support to the independent 1.12.2 implementation so its deepslate layers, tuff clusters and ancient-city sculk also work with Underground Biomes geology. The 1.12.2 conversion coordinates already use the correct native positions.
- Restore inherited Biomes O' Plenty and Witchery stripped-log, boat and chest-boat artwork. Register distinct stripped faces, map compatibility texture domains and species spellings correctly, and refresh boat textures when synchronized data changes. Preserve existing block/item registrations, metadata and saved boat identities.

2026-10-29 20:49 — Restore legacy modded door and trapdoor artwork

- Restore the inherited Biomes O' Plenty and Witchery door block faces, door inventory icons and trapdoor textures for 1.7.10. Resolve their resource paths using the artwork's species names rather than the registry prefixes, preserving all existing registrations and metadata.
