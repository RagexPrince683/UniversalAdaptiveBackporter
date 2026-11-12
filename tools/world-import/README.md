# Experimental offline world importer

This proof of concept converts selected Java **1.13.2** Overworld chunks into a **new** Forge/UAB 1.7.10 or 1.12.2 world directory. It uses Python 3.9+ and the standard library. It is independent of UAB's normal world-loading and generation code.

**Minecraft 1.18.2 and all later worlds are explicitly excluded.** Every other unimplemented source format, including snapshots and mixed-version chunks, is also rejected. Initial support is exactly DataVersion **1631**, not a claim that every 1.13.2 world can be fully converted.

## Feasibility assessment

An offline pipeline is viable for a bounded set of palettes and NBT data. Legacy chunk-load ID replacement is unsuitable: it receives the wrong storage schema, lacks source-version ownership and cannot safely account for discarded data. UAB's existing structure-state readers inform the mapping work, but those placement-oriented readers intentionally tolerate properties and substitutions that are too permissive for importing a valuable save. They are not used to silently accept unknown world states.

| Storage boundary | Initial implementation and policy |
| --- | --- |
| World metadata | Detect `level.dat` DataVersion. Read target Forge registry snapshots from a separate saved target world. Retain the template's native loader metadata, settings and generator; use the source spawn with the selected height offset. |
| Region files | Validate sector bounds, overlapping allocations, chunk coordinates, compression and NBT before conversion. Write new Anvil files. External region streams are unsupported. |
| Sections | Decode 1.13.2 dense `Palette`/`BlockStates` arrays, including entries crossing long boundaries. Write native 4096-byte `Blocks`, nibble `Data`, and `Add` arrays where IDs exceed 255. |
| Block identities/properties | Explicit vanilla/UAB mappings, orientation/axis metadata, slab halves and double slabs, wood species and color metadata. Unknown properties fail mapping. Waterlogged states require an explicit custom mapping or an explicit fallback policy. Stair connections and kelp ages are recomputed by the target engine. |
| Biomes | Initial two-dimensional, 256-column vanilla biome IDs only. New ocean categories require an explicitly accepted legacy-ocean substitution; unsupported IDs require an explicitly accepted plains substitution. |
| Height | Initial source sections must be 0–15. An explicit offset, in multiples of 16, can move them. Non-air terrain outside 0–255 blocks conversion unless `--height-policy discard` is selected; losses are counted. Negative-height/new-height schemas are not admitted. |
| Lighting/height map | Write target column heights and mark lighting unpopulated for native recalculation. Old light arrays and modern heightmaps are not copied. Lighting acceptance remains a runtime qualification boundary, especially for transparent/complex blocks. |
| Entities | Conservative vanilla cow, pig, chicken and sheep preservation, including positions, age, health and UUID tags. Unsupported entities/equipment/relationships/custom fields block conversion unless their loss is selected. Verified vanilla-default animal attributes are rebuilt by the target and reported. Custom bases/modifiers require explicit loss acceptance; invalid health above vanilla maximum is rejected rather than silently clamped. |
| Containers | Vanilla chest/trapped-chest coordinates and simple same-identity item stacks. Preserve counts/slots; emit numeric item IDs on 1.7.10 and namespaced IDs on 1.12.2. Custom names, loot-table containers, item NBT and unmapped items require review/loss acceptance. Validate that container NBT matches its converted block. |
| Structures and ticks | Nonempty structure starts/references and scheduled block/fluid ticks are unsupported and block conversion unless explicitly discarded. No claim of redstone or multi-block-structure parity. |
| Dimensions and auxiliary storage | Populated non-Overworld/dimension/entity/POI directories block conversion unless discarded. Player data, advancements, statistics, maps, raids and generation metadata are not imported; the preview warns about this. Source files remain untouched. |

Modern deepslate, tuff, copper oxidation, later wood families and newer dimension schemas are outside the selected source version. Their inclusion in UAB is not evidence that their world formats are supported. Later source adapters must remain below the explicit 1.18.2 exclusion and require their own fixtures and validation.

The initial mapping set is deliberately incomplete. It is useful for representative terrain and small reviewed builds, with full-state custom declarations for additional tested blocks. Unsupported states never automatically become air. The `waterlogged` property is recognized only on supported stairs, slabs and chests; even `waterlogged=false` on unrelated blocks is rejected.

Format references: the [Glowstone 1.13 serialization design](https://docs.glowstone.net/en/latest/Development/Design_Documents/1.13_Update.html) describes the palette transition. Target serialization and registry layouts were inspected in the actual cached Forge/Minecraft implementations and saved registry snapshots. The initial fixtures exercise dense 1.13.2 encoding rather than the later padded-long format.

## Preview, review, then convert

1. Stop the source world and a separate target UAB instance. Keep your normal backups.
2. Create/save a disposable target world with the intended UAB version, optional mods and configuration. This is the **registry template**; its existing terrain is not copied. The converter verifies its Forge target and UAB registry entry.
3. Choose a new output directory outside both input directories. It must not already exist; its parent directory must exist.
4. Run a preview:

```powershell
python tools/world-import/import_world.py --source "D:/Copies/Source1132" --template "D:/Tests/UAB1710Template" --target 1.7.10 --output "D:/Imports/Converted1132"
```

The JSON preview shows the detected/target versions, registry template and mods, mapped/unmapped block counts, missing states, substitutions, data losses, height policy, estimated region output size, output/backup information and blocking conditions. Size is an estimate for the converted regions, not a prediction of subsequent target-world growth.

The default policies halt on unsupported blocks and data. To make a reviewed lossy plan, rerun with the policies you intend to accept:

```powershell
python tools/world-import/import_world.py --source "D:/Copies/Source1132" --template "D:/Tests/UAB1710Template" --target 1.7.10 --output "D:/Imports/Converted1132" --unknown stone --discard-unsupported-data
```

`--unknown air` is also available, but must be explicitly selected. `--height-offset 16` moves terrain up one section; `--height-policy discard` explicitly permits non-air losses beyond legacy height. Neither option admits a newer unsupported storage format.

5. Review a preview with an empty `blocking` list. Copy its exact `confirmation` hash into a second invocation with **the same arguments**, adding `--confirm HASH`.

The hash binds the source files, template `level.dat`, target, output path, mappings and policies. Changed input invalidates approval. Cached world metadata is bound to the input snapshot taken before it was read; changes require a fresh converter and preview. The converter checks for input changes again before publication. It creates a sibling staging directory, validates converted chunks, writes a conversion report, writes `level.dat` last and publishes by directory rename. An error does not publish a loadable world. It never modifies the source or template. Do not run Minecraft against either input during preview/conversion.

6. Read `conversion-report.json` in the new output and test a copy in the matching target instance. New chunks use the template's legacy generator and can form terrain seams. The source remains the authoritative original.

## Explicit mappings and optional providers

`--mappings PATH.json` adds exact full-state declarations. The target identity must exist in the template snapshot; metadata must fit 0–15. Registry IDs are resolved from the snapshot rather than guessed or hardcoded. Optional-mod content requires an explicit declaration, even if its translated name matches another block.

```json
{
  "minecraft:some_block[facing=north,waterlogged=false]": {"id": "etfuturum:tested_equivalent", "meta": 2},
  "example:tested_material": {"id": "biomesoplenty:tested_target", "meta": 0},
  "example:explicitly_unsupported": null
}
```

Property keys in a state key must be sorted alphabetically. These are syntax examples, not shipped equivalences. A `null` mapping follows the selected unsupported-block policy. The report lists required optional target namespaces. Removing those mods later is outside the conversion contract.

Mapping JSON must be an object. Duplicate keys, including duplicate `id` or `meta` fields, are rejected rather than silently replacing an earlier declaration. Non-null mappings accept only a string `id` and optional integer `meta` from 0 through 15; booleans are not metadata.

## Regression fixtures

```powershell
python -B -m unittest discover -s tools/world-import -p test_import.py -v
```

Fixtures are generated in disposable directories by `test_import.py`. They cover both target schemas, long-boundary palette packing, metadata/extended IDs, slab/stair/wood properties, unsupported-state policies, modified UTF-8, simple chest inventories and animals, height loss, region validation, source immutability, confirmation invalidation and explicit 1.18.2+ rejection. They do not establish broad real-world compatibility or complete gameplay acceptance.
