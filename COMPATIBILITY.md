# Content compatibility providers

UAB keeps third-party integrations optional. The content-equivalence registry describes identities, metadata/state constraints, and the operations for which an equivalence is valid. It never compares translated item names and does not replace either mod's world generator.

## Player and pack configuration

| Setting in `config/uab.cfg` | Behavior |
| --- | --- |
| `update_1_13.kelp.enabled` | UAB kelp plant acquisition and growth. Existing plant identities remain loadable. |
| `update_1_13.dried_kelp.enabled` | Dried kelp food and processing, independently of the plant. Defaults to enabled. |
| `update_1_13.dried_kelp_block.enabled` | Dried kelp storage blocks, compression/decompression and fuel. Defaults to enabled; crafting also requires the food item. |
| `update_1_14.bamboo_plants.enabled`, shown as Bamboo plants | UAB bamboo plants. Bamboo construction blocks and pandas retain their separate switches. |
| `general.compatibility.bop.enabled` | BOP material recipes, panda food/plant/biome aliases, and UAB wood-derivative acquisition. |
| `general.compatibility.witchery.enabled` | Witchery wood-derivative acquisition on 1.7.10. |
| `general.compatibility.undergroundbiomes.enabled` | Recognition of natural Underground Biomes stones by UAB geological/structure consumers. |

Provider settings default to enabled and require a restart. They govern this content framework; specialized pre-existing integrations, such as deepslate ore families, retain their own settings. Disabling a provider retains registered wood identities for saved worlds. It hides/disables their acquisition and skips their recipes/stripping registrations. Removing the supplying mod itself still requires the usual Forge missing-content precautions.

To use BOP kelp exclusively, disable UAB **Kelp plants** and leave **Dried kelp food**, **Dried kelp block**, and the BOP provider enabled. UAB's ocean decoration respects the plant switch and does not force BOP generation on or off. The former kelp setting now governs plants only; existing explicit disables of that setting do not automatically disable the new dried-content switches.

## Native support matrix

| Area | Minecraft 1.7.10 | Minecraft 1.12.2 |
| --- | --- | --- |
| BOP kelp processing | `BiomesOPlenty:coral1` metadata **8–11** only; the normal dropped/creative item is 11. Coral at 12–15 is excluded. | `biomesoplenty:seaweed`, metadata 0, with `variant=kelp`. Position and fluid-level properties do not introduce extra item variants. |
| BOP bamboo construction | Nine `BiomesOPlenty:bamboo` items, metadata 0, to a UAB bamboo block. | Nine `biomesoplenty:bamboo` items, metadata 0, to a UAB bamboo block. |
| Pandas | BOP bamboo food/following and stalks for breeding; jungle or recognized BOP Bamboo Forest class. | BOP bamboo food/following and stalks for breeding; jungle or BOP `bamboo_forest` biome identity. |
| BOP wood derivatives | Existing 15 plank families and two log-only families; standing/wall signs, logs, boats and other existing derivatives retain their identities. | Add the 16 BOP plank-family standing/wall signs through UAB's existing native sign system. Existing BOP doors, fences and other native derivatives are not duplicated. |
| Witchery wood | Existing rowan, alder and hawthorn families, including signs. | No maintained native Witchery release is assumed. No guessed port registry names are registered. |
| Underground Biomes | Explicit igneous, metamorphic and sedimentary natural-stone blocks; existing generator-wrapper recognition remains separate. | Explicit native registry/state lookup for the same geological categories. |

Panda spawn entries continue through the existing bounded spawn lists, availability gates, passive-spawning settings, configured weights and native animal spawn predicates. No independent spawn loop is added.

Furnace integration checks existing recipes, including wildcard inputs, before adding a conversion. It preserves another mod's existing output. The 1.7.10 smoker also obtains supported food processing through its existing furnace adapter. Bamboo integration checks for an existing nine-material recipe before supplying one. Unavailable inputs/outputs are skipped. Registrations are not overwritten.

The existing BOP/Witchery sign artwork is shared between targets without changing its texture namespace or species spelling. Modern BOP umbran uses the inherited `dark` artwork. Species without shipped sign artwork use the vanilla sign appearance; sign texture selection is cached and invalidated on resource reload. This fallback preserves editable standing/wall signs rather than displaying missing textures.

## Extending the registry

The platform-neutral API is `ganymedes01.etfuturum.compatibility.CompatibilityRegistry`. An entry declares:

- A provider and semantic category, such as `bamboo`, `kelp`, `natural_stone` or `panda_habitat`.
- An exact registry identity, metadata (`-1` explicitly permits all metadata), and optional named block-state properties.
- Its allowed uses: crafting, smelting, food, placed plants, biome aliases, world generation, structural replacements, loot or advancement-equivalent adapters.

`BuiltinCompatibility` supplies version-specific BOP and geological declarations. `WoodFamilies` supplies source-material descriptors to the existing derivative/sign systems. Native `ContentCompatibility` adapters resolve actual registered content at the consuming lifecycle. No optional provider's Java classes are linked.

Optional addons can register their own provider configuration gate with `REGISTRY.registerProvider(id, BooleanSupplier)` and then register explicit entries during initialization, before recipes or generation. They can add categories/consumers without changing UAB's core declarations. Loot, advancement and structural uses are extension points: declaring one does not automatically invent loot tables, achievements or world replacements. Consumers must explicitly request the permitted operation and implement its native lifecycle.

Registry lookups preserve 1.7.10 metadata and use native registry identities and property constraints on 1.12.2. Item metadata is distinct from a placed bamboo stalk's growth age. A material declaration does not globally add an OreDictionary tag or make every variant interchangeable.

## Existing integration boundaries

The inspected existing integrations include `ExternalContent` registry references, BOP ore families, the external wood descriptors and texture adapters, Witchery wood content, Underground Biomes' optional wrapper mixin, ore/stripping registries, and the specialized equipment/storage/scripting adapters. Their mod APIs and configuration remain intact. The new registry is authoritative for bamboo material/plant/biome equivalence, BOP kelp inputs and natural geological-stone recognition. It does not replace unrelated equipment or world-provider implementations.

The experimental offline world importer has a separate storage pipeline and explicit state-mapping registry; see [its feasibility and usage notes](tools/world-import/README.md). It never participates in normal chunk loading.

## Continuation verification — November 11, 2026

The final aggregate build packages both targets and verifies Java 8 bytecode and target metadata. Native runtime observations used Corretto 8 with BOP 2.1.0.2308 on 1.7.10 and 7.0.1.2445 on 1.12.2, in disposable installations. These are bounded acceptance results, not universal modpack or world-conversion qualification.

- Both integrated servers passed grouped kelp, bamboo, panda habitat/food, sign registration and aquatic-passenger checks (43 assertions on 1.7.10; 50 on 1.12.2). The same Drowned remained attached through 400 directly invoked production callbacks and subsequent ordinary world ticks. Player interaction, yaw/forward/pitch steering and voluntary dismount passed separately on both targets. Client riding links were observed on both versions.
- Final BOP-enabled dedicated-server callbacks verified that feeding enters panda breeding mode with eight BOP stalks, but not below the threshold, and that existing kelp furnace outputs survive compatibility re-registration. The legacy native biome lists passed habitat/group checks across 165 registered biomes; native 1.12.2 Forge spawn events excluded plains, included Bamboo Forest and avoided duplicate panda entries. These are callback/list checks, not statistical natural-spawn or complete breeding gameplay acceptance.
- The legacy integrated mount-removal observation was resolved as test setup: the native stack showed distance-based despawning when the player was far from the pool. Difficulty, animal spawning, loaded chunks and water were checked explicitly. No forced-remount mechanism was added.
- With BOP installed and its provider disabled after a full process restart, all 15 legacy and 16 newer standing/wall sign families retained their blocks and four text lines. Acquisition and recipes were disabled. Both saved mount/passenger UUID pairs and the jockey-initialization guard persisted. A further 1.12.2 startup kept BOP disabled with the geology provider independently enabled.
- Additional registry checks retained the legacy BOP boats/logs/derivatives with acquisition disabled. The newer target retained BOP ownership of its 16 door and 32 fence/gate blocks without adding corresponding UAB duplicates.
- Native 1.12.2 cow checks passed for three coats, adults/calves, complete sheets, mapped model selection and original-model restoration, including after a real resource reload. Native framebuffers were inspected. A disposable resource pack supplied previously missing sign artwork and replaced a cow texture; adding/removing it correctly invalidated cached texture selection. Riding flags and preservation of pig/chicken/wolf models passed. The earlier 1.7.10 cow checks remain applicable because that implementation was unchanged.
- The final 1.12.2 client excludes tile-rendered sign blocks from baked model loading, correcting the missing-model errors observed during these checks. Sign item models remain separate.
- Fresh final-importer fixtures mapped 8,192 blocks per target without unsupported states. Both actual dedicated servers loaded them and retained the supported blocks, metadata, 12 chest diamonds and cow health 7 after save, shutdown and process restart. Representative plains-biome, open-sky light 15 and opaque-interior sky light 0 checks passed. Broad transparent-block lighting and real-world biome coverage remain unqualified.
- All 19 separate Python importer regression tests passed, including duplicate/malformed mapping declarations, unknown properties and cached-metadata/input-snapshot consistency.

Live Witchery/Underground Biomes installations and Angelica were unavailable in this verification environment. Their declaration/configuration checks and existing adapters do not establish live compatibility. Long survival sessions, broad modpack combinations, performance benchmarks and arbitrary resource packs/worlds remain outside this bounded verification. Loot and advancement declarations remain extension points, not complete gameplay integrations.

The 1.12.2 logs also contain vanilla stone-slab model-loading errors already present in the earlier handoff client logs. Those separate rendering errors were recorded without expanding this sign/framework/importer task into another renderer change. Their visible impact remains unqualified, so this continuation is not a blanket release-readiness assertion.
