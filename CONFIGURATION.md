# UAB configuration

## Files and precedence

`config/uab.cfg` is the primary native Forge configuration. There are no separate block, entity, world, sound or experiments files to maintain. The in-game Forge configuration screen uses update and feature names.

For 1.7.10, existing potion allocations remain in `config/legacy-potion-ids.properties`, shared with cooperating legacy mods. This is persistent identity data, not another feature configuration. Do not delete or regenerate it in an existing installation.

The precedence order is: an existing UAB value, a recognized split legacy value under `config/etfuturum/`, a recognized monolithic legacy value (`config/etfuturum/etfuturum.cfg`, then `config/etfuturum.cfg`), and the UAB default. Exact legacy categories are preferred; a monolithic key is matched across categories only when unique. Conflicting aliases for a consolidated boolean preserve a disabled setting.

Migration creates an adjacent `.uab.bak` copy before importing, leaves original files untouched, and records completion in the UAB file. Later starts do not re-import legacy files. Customized generation values, biome/enchantment/potion IDs, dimension lists and the historical turtle weight are retained. Old numeric fossil/geode block selectors are translated to namespaced block names. Unmapped values are reported in the log and remain available in the originals and backups. Invalid scalar/list types, numbers and ranges use a validated default or a bounded value with a warning. Backups are never overwritten.

## Categories and feature families

The shared catalog attributes features from the Bountiful Update through Mounts of Mayhem (1.21.11), plus General. Attribution describes the feature requirement, not whether its target implementation is complete. The 1.12.2 target uses native platform features and adapters for the maintained content; concrete engine limitations remain documented in the content and mob catalogs. Minor updates have distinct categories where needed, including iron nuggets in 1.11.1, 1.16.2, 1.20.5, 1.21.4, 1.21.6, 1.21.7 and 1.21.9. Empty update categories are not displayed. Mixed families remain under their earliest named feature update, with component dates in the shared catalog. For example, prismarine starts in 1.8 but includes 1.13 stairs/slabs and 1.14 walls. Basalt includes 1.17 smooth basalt; tuff includes 1.21 decorations. On 1.12.2, wholly native controls are omitted from freshly generated configuration and vanilla prerequisites cannot be disabled by an old backport switch. Mixed families keep controls for their remaining additions. General fixes can also have verified native availability without inventing a first-release attribution. For example, native zombie/golem combat and embedded-arrow collision retention need no duplicate hook on 1.12.2, while half-block step normalization remains a configurable adaptation.

Corrected category locations migrate automatically. The first such migration preserves the exact original file as `uab.cfg.catalog-v2.bak`; this backup is never overwritten. An explicit value already at the corrected location takes precedence. Otherwise the old value moves, and a feature previously suppressed by its old update's parent switch remains disabled after moving. Unrelated settings are retained.

Each update has an `enabled` switch. Feature families have their own `enabled` switch and useful parameters alongside it. There are no separate controls for each slab, stair, color, waxed state or plant growth stage. New mobs have individual switches. General contains cross-update policies, optional compatibility and custom conveniences; advanced identity settings are kept out of ordinary GUI navigation.

For example, `update_1_16.netherite.enabled` controls the netherite family, with its equipment parameters in the same category. `update_1_21.decorative_copper.enabled` controls chiseled copper, grates, bulbs, doors and trapdoors together. `update_1_13.mobs.cod` contains cod availability and natural spawn parameters.

Disabling an update does not overwrite its children's requested values. Dependent features become unavailable when prerequisites are disabled. End Cities require outer islands and chorus blocks; Ancient Cities require sculk and deepslate; Trial Chambers require copper, tuff and cobbled deepslate; bastions require the Nether provider and blackstone. The Nether content switch also controls dependent Nether blocks, biomes and mobs.

Ordinary ready content, Sculk and Nether biomes are enabled by default, including the prerequisites for Ancient Cities and bastions. Existing explicit disables are preserved during upgrades. The Nether provider remains incompatible with replacement Nether terrain providers such as Netherlicious. Spectator mode, inventory return-on-close and unsupported extra skin layers remain disabled by default. There is no global experimental override. Catalyst XP spreading remains unimplemented even when sculk blocks are enabled.

## Disabling content and existing worlds

Registries, entity identities and tile-entity types remain available so saved objects can load. Availability controls new recipes, loot, natural selection, direct mob creation, biome placement and the relevant generation hooks. Existing blocks, items, entities, placed structures and generated terrain remain; disabling is not a world-cleanup operation. Already running saved block behavior may continue to preserve its state. No live unregistration or automatic retrogen occurs.

Registry identities and native rendering support are retained independently of acquisition controls. A disabled mob cannot be newly created through natural spawning, structures, breeding or conversions, but chunk deserialization still loads already saved mobs. Missing content dependencies suppress the affected new structure instead of substituting unrelated materials.

## Natural spawning

Applicable mobs expose `spawnWeight`, `minGroupSize` and `maxGroupSize` in their feature category. `-1` retains the original value for each biome or mechanism; this preserves intentional biome differences. Weight `0` disables that natural spawn entry. Other weights are relative selection weights, not absolute probabilities. Group sizes must be positive, with maximum at least minimum. Out-of-range values are bounded and invalid combinations are corrected with a warning.

cod, salmon, tropical fish, pufferfish, dolphin, turtle, axolotl, glow squid, drowned, nautilus, zombie nautilus, guardian, piglin, hoglin, strider, cat, panda, llama, armadillo, camel, bogged, parched, camel husk, frog, rabbit, fox, goat, polar bear, husk, stray, shulker have native list-based spawn settings. The existing aquatic population budgets remain separate limits on admission. Changing weights does not change biome eligibility, water, light, depth, dimension or collision requirements. Drowned retain the existing darkness/depth predicate: exposed shallow daytime spawning is rejected; deep or fully dark water can remain eligible.

Special acquisition uses feature switches rather than invented spawn weights: insomnia phantoms, patrols, raids, traders, spawners, structure encounters, conversions, breeding and constructed or hatched mobs. Husk/stray weights affect their optional list-based spawning mode, not the sky-exposed vanilla replacement ratio. Guardian weights apply inside saved monument bounds. Spawn changes apply after restarting; initialization replaces entries instead of appending duplicates.

## Reloads and multiplayer

Content, generation, spawning, recipe, asset and compatibility settings require restarting Minecraft or the dedicated server. The GUI marks this requirement. Saving advanced tooltip, gamemode-switcher, F3-key, transparent-armour, shulker-tooltip-line and subtitle-filter preferences can update their directly read client settings without changing registries. Changes to restart settings remain pending until the next launch.

Both targets compare common active settings during Forge's existing handshake. A mismatch is rejected before gameplay; copy the server's UAB configuration and restart. The 1.12.2 server-list compatibility response carries the same settings fingerprint as login. Client-only preferences are excluded from this comparison and are not generated on dedicated servers. Saving settings in the 1.12.2 configuration screen preserves active restart-bound gameplay until the next launch. The 1.7.10 target also retains its potion-ID checks. The old 1.7.10 field-binding classes remain internal compatibility adapters; they neither own nor save configuration files.

## Feature counts

The catalog contains **372 distinct feature switches**, counting shared legacy aliases once and excluding update parent switches and numeric or presentation parameters. These represent gameplay behaviors, systems, content families and individual mobs, rather than every registered block.

| Origin | Feature switches |
| --- | ---: |
| General / cross-update | 49 |
| 1.8 | 40 |
| 1.9 | 53 |
| 1.10 | 11 |
| 1.11 | 10 |
| 1.11.1 | 1 |
| 1.12 | 8 |
| 1.13 | 42 |
| 1.14 | 40 |
| 1.15 | 5 |
| 1.16 | 31 |
| 1.16.2 | 1 |
| 1.17 | 22 |
| 1.18 | 7 |
| 1.19 | 10 |
| 1.20 | 13 |
| 1.20.5 | 2 |
| 1.21 | 10 |
| 1.21.4 | 1 |
| 1.21.6 | 4 |
| 1.21.7 | 1 |
| 1.21.9 | 5 |
| 1.21.11 | 6 |

Woodland mansions have an Exploration Update generation control. Trails & Tales groups Archaeology, Brushes, Trail ruins, Decorated pots, Armor trims and Netherite upgrade templates separately. Trail ruins require Archaeology/Brushes; trimming and template upgrades require the Smithing Table. Turning off Netherite upgrade templates restores built-in two-input upgrades. Existing scripted two-input recipes remain available with the template slot empty. Acquisition/generation switches retain saved items, decorations, trim data and registered identities.


## Sleeping percentage

On 1.12.2, `update_1_17.sleeping_player_percentage.enabled` adds the saved `playersSleepingPercentage` game rule. `/gamerule playersSleepingPercentage 50` requires half of the current non-spectator players, rounded upward, to be fully asleep. At least one sleeper is required; values above 100 prevent skipping. The default 100 retains the usual all-player threshold. Native daylight-cycle and weather rules still apply.


## Native optional integrations

On 1.12.2, modded raw/deepslate ore paths require the real ore and relevant ingot from the installed mod. Blacklists accept ore-dictionary names or mod IDs. The public raw-ore and deepslate mappings are the same mappings used for harvesting and generation. Missing prerequisites leave those paths unavailable.

Iron Chests compatibility adds the maintained barrel and shulker upgrades. Upgrade-item settings select UAB upgrade items; when those items are disabled, compatible native Iron Chests upgrades can be used directly. Returning a shulker upgrade to its matching Iron Chests item returns a shell when configured. Existing upgraded inventories retain their saved capacity when acquisition or integration settings are disabled, including after removing Iron Chests. Native dyed shulker identities and item NBT remain intact.

Baubles Mending extends native equipment selection and leaves XP cost and repair ratios with the native experience-orb code. Without Baubles, native equipment repair is unchanged.

The optional old husk/stray spawning rules remove their sky requirement in eligible biomes and retain configurable spawn weights. Natural End shulkers remain opt-in; structure shulkers retain their native persistence. Native skin overlay layers, grass item tinting and compatible fence/wall connections do not need legacy replacements on 1.12.2.
