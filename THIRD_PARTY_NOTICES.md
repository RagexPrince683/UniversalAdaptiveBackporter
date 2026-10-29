# Third-party notices

## Minecraft reference content

The additional pale oak, resin, frog, guardian, banner-pattern and Nether entity textures, bastion structure NBT, bastion template pools and degradation processors are reference content from Mojang's official Minecraft Java Edition 1.21.11 client archive. These assets remain Mojang/Microsoft content and are not relicensed by this project's code license. The project already contains Minecraft asset adaptations and uses MCLib AssetDirector for additional Mojang resources. No post-1.21.11 reference assets are included in this addition.

## HogUtils-derived components

The Et Futurum tag containers, block/item metadata utilities, recipe helper,
fast random generator, dummy world, generation-state tracking, registry iteration,
and multi-block sound interface include code adapted from
[HogUtils](https://github.com/Roadhog360/HogUtils), specifically revision
`5782dbbe51c301512a08d820f122326984b19b7b`.

HogUtils is distributed under the following license:

> MIT License
>
> Copyright (c) [year] [fullname]
>
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.

The xoshiro256** implementation retains its upstream public-domain notice in
`FastRandom.java`.

## Stage B7 mob reference assets

The modern mob textures and retained box/pivot model definitions are adapted from Mojang’s official Java Edition 1.21.11 client, using its official mappings as a behavioral reference. These Minecraft assets and adapted vanilla code retain Mojang’s rights; the project license does not relicense them. Sounds are resolved from Mojang’s asset service through the existing MCLib AssetDirector integration rather than bundled as audio files.

The released irregular-block element/UV definitions under `assets/etfuturum/models/block`, tall-seagrass textures, kelp/sea-pickle/coral models and textures, dried-kelp/Heart of the Sea/conduit sprites, shipwreck and underwater-ruin templates and ocean loot tables, and five pillager-outpost templates under `data/structure/pillager_outpost` are also adapted from that same official 1.21.11 client. Model parent inheritance is resolved in the supplied definitions for the native 1.7.10 tessellator. Outpost and ocean block states are translated at placement to existing legacy identities; modern entity NBT is not imported.

The underground structure pass additionally adapts the released 1.21.11 Ancient City and Trial Chamber templates, pools, processors, loot tables and trial-spawner configurations. Added small-block retained models/textures and the bundled End flash texture come from that same client. These reference assets retain Mojang's rights and are not relicensed by the project license.

The mansion room-grid algorithm and its 73 room/building templates, the 84 trail-ruin templates and their jigsaw pools/processors, archaeology and mansion loot tables, pottery patterns/base textures, sherd/template/Relic sprites and armor trim sheets/palettes are adapted from Mojang's released Java Edition client and official mapped code. Palette-mapped trim sheets are prepared for the native legacy biped renderer. These assets and adapted vanilla code retain Mojang's rights; LGPL licensing of the project does not relicense them.

The added lightning-rod/campfire element and UV definitions, campfire sprites/animation metadata, five parrot textures, igloo templates and igloo chest loot are adapted from Mojang's official Java Edition 1.21.11 client. The legacy parrot model geometry and pose logic also reference the native 1.12.2 model. Minecraft assets and adapted reference code retain Mojang/Microsoft rights; this project's code license does not relicense them.

The Mace sprite and flattened random-loot enchantment tag also derive from the official 1.21.11 client. Mace combat constants, released enchantment effects and recipe/reward definitions are adapted at each native runtime boundary.

The grindstone, bell, chiseled bookshelf, hanging-sign, shelf, Copper Age equipment and fixture, Spring to Life plant and animal textures/models, spyglass scope, and ruined-portal loot references also derive from that official 1.21.11 client. Native rendering and gameplay adapters retain the supported legacy engines. These reference assets retain Mojang/Microsoft rights.

Cow and pig variant texture sheets retain their released upper 32 rows for the native legacy models. The unused modern geometry region is omitted so shared vanilla model data remains unchanged. Dry-foliage colors use the released colormap sampled with native biome climates.

Copper horse armor retains the official 1.12.2 gold-armor coverage and native 128-pixel UV layout. Its colors are translated through corresponding official 1.21.11 gold/copper horse-equipment palette entries; native geometry is retained. Both reference assets retain Mojang/Microsoft rights.

Lodestone faces and compass/recovery-compass needle sprites are adapted from the official 1.21.11 client. Compass bearing follows the native legacy frame convention; Minecraft reference assets retain Mojang/Microsoft rights.

Netherite horse armor uses the released 1.21.11 item sprite. Its native horse-body texture retains the same legacy coverage as copper armor, with colors translated through corresponding official copper/netherite horse-equipment texels. These assets retain Mojang/Microsoft rights.

Crafter model and block textures derive from the official 1.21.11 client; native inventory screens, recipe execution, redstone and rendering adapters retain the supported legacy engines. Reference assets retain Mojang/Microsoft rights.

Bundle and colored-bundle sprites derive from the official 1.21.11 client. Inventory interaction and storage adapters retain native legacy interfaces. Reference assets retain Mojang/Microsoft rights.

Scaffolding elements and textures derive from the official Minecraft 1.21.11 client. Explicit UVs adapt the released models to the existing legacy tessellator. Reference assets retain Mojang/Microsoft rights.

Powder snow, bucket and filled-cauldron models/textures derive from the official Minecraft 1.21.11 client. Cauldron element inheritance and explicit UVs are adapted for native legacy rendering. Reference assets retain Mojang/Microsoft rights.

Respawn-anchor block faces and animation metadata derive from the official Minecraft 1.21.11 client. These reference assets retain Mojang/Microsoft rights.
