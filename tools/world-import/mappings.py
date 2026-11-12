"""Conservative, versioned mapping registry. Unrecognized properties never become defaults."""
from dataclasses import dataclass


@dataclass(frozen=True)
class Block:
    identity: str
    metadata: int = 0


STATIC = set("air stone cobblestone bedrock dirt coarse_dirt podzol grass_block sand red_sand gravel glass obsidian netherrack soul_sand glowstone end_stone clay bricks stone_bricks mossy_stone_bricks cracked_stone_bricks chiseled_stone_bricks iron_block gold_block diamond_block emerald_block lapis_block coal_block redstone_block iron_ore gold_ore diamond_ore emerald_ore lapis_ore coal_ore redstone_ore sponge wet_sponge sea_lantern prismarine prismarine_bricks dark_prismarine dried_kelp_block blue_ice packed_ice ice snow_block slime_block barrier end_stone_bricks magma_block bone_block nether_wart_block red_nether_bricks purpur_block smooth_stone smooth_sandstone smooth_red_sandstone cut_sandstone cut_red_sandstone sandstone chiseled_sandstone red_sandstone chiseled_red_sandstone terracotta bookshelf crafting_table enchanting_table farmland grass_path mycelium mossy_cobblestone dead_bush cobweb lily_pad melon pumpkin carved_pumpkin jack_o_lantern hay_block tnt spawner".split())
COLORS = "white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black".split()
WOODS = "oak spruce birch jungle acacia dark_oak".split()


def state_key(name, properties):
    return name + ("[" + ",".join(k + "=" + v for k, v in sorted(properties.items())) + "]" if properties else "")


class Mappings:
    def __init__(self, target, blocks, custom=None):
        self.target, self.blocks, self.custom = target, blocks, custom or {}

    def resolve(self, name, properties):
        key = state_key(name, properties)
        if key in self.custom:
            value = self.custom[key]
            if value is None:
                return None  # Explicitly unsupported, subject to the selected fallback policy.
            block = Block(value["id"], value.get("meta", 0))
            return self.validate(block)
        if not name.startswith("minecraft:"):
            return None  # Optional mods require explicit declarations, even when names match.
        path, props = name[10:], dict(properties)
        if (path.endswith(("_stairs", "_slab")) or path in ("chest", "trapped_chest")) and props.pop("waterlogged", "false") != "false":
            return None
        meta, identity = 0, None
        if path in ("water", "lava"):
            level = props.pop("level", "0")
            if not level.isdigit() or int(level) > 15:
                return None
            identity, meta = "minecraft:" + path, int(level)
        elif path in ("kelp", "kelp_plant"):
            age = props.pop("age", "0")
            if not age.isdigit() or int(age) > 25:
                return None
            identity = "etfuturum:kelp"  # Column ends/age are recomputed by the legacy plant engine.
        elif path in ("chest", "trapped_chest"):
            facing, chest_type = props.pop("facing", "north"), props.pop("type", "single")
            if facing not in ("north", "south", "west", "east") or chest_type not in ("single", "left", "right"):
                return None
            identity, meta = "minecraft:" + path, {"north": 2, "south": 3, "west": 4, "east": 5}[facing]
        elif path.endswith("_planks") and path[:-7] in WOODS:
            identity, meta = "minecraft:planks", WOODS.index(path[:-7])
        elif path.endswith(("_log", "_wood")):
            stripped = path.startswith("stripped_")
            wood = path.removeprefix("stripped_").removesuffix("_log").removesuffix("_wood")
            axis = props.pop("axis", "y")
            if wood not in WOODS or axis not in ("x", "y", "z"):
                return None
            species = WOODS.index(wood)
            if stripped:
                identity = "etfuturum:" + path if self.target == "1.12.2" else "etfuturum:" + ("wood" if path.endswith("_wood") else "log") + ("2" if species >= 4 else "") + "_stripped"
                meta = (0 if self.target == "1.12.2" else species % 4) + {"y": 0, "x": 4, "z": 8}[axis]
                if self.target == "1.7.10" and path.endswith("_wood"):
                    meta = species % 4
            elif path.endswith("_wood"):
                identity, meta = "minecraft:log" + ("2" if species >= 4 else ""), species % 4 + 12
            else:
                identity, meta = "minecraft:log" + ("2" if species >= 4 else ""), species % 4 + {"y": 0, "x": 4, "z": 8}[axis]
        elif path.endswith("_stairs"):
            facing, half, shape = props.pop("facing", "north"), props.pop("half", "bottom"), props.pop("shape", "straight")
            if facing not in ("east", "west", "south", "north") or half not in ("bottom", "top") or shape not in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                return None
            meta = {"east": 0, "west": 1, "south": 2, "north": 3}[facing] + (4 if half == "top" else 0)
            aliases = {"stone_stairs": "stone_stairs", "cobblestone_stairs": "stone_stairs", "stone_brick_stairs": "stone_brick_stairs", "brick_stairs": "brick_stairs", "nether_brick_stairs": "nether_brick_stairs"}
            base = aliases.get(path, path)
            identity = "minecraft:" + base if "minecraft:" + base in self.blocks else "etfuturum:" + path
            if self.target == "1.7.10":
                identity = {"etfuturum:prismarine_brick_stairs": "etfuturum:prismarine_stairs_brick", "etfuturum:dark_prismarine_stairs": "etfuturum:prismarine_stairs_dark"}.get(identity, identity)
        elif path.endswith("_slab"):
            half = props.pop("type", "bottom")
            if half not in ("bottom", "top", "double"):
                return None
            slab = path[:-5]
            old = ["smooth_stone", "sandstone", "petrified_oak", "cobblestone", "brick", "stone_brick", "nether_brick", "quartz"]
            if slab in old:
                identity, meta = "minecraft:" + ("double_" if half == "double" else "") + "stone_slab", old.index(slab)
            elif slab in WOODS:
                identity, meta = "minecraft:" + ("double_" if half == "double" else "") + "wooden_slab", WOODS.index(slab)
            elif self.target == "1.7.10" and slab in ("prismarine", "prismarine_brick", "dark_prismarine"):
                identity, meta = "etfuturum:" + ("double_" if half == "double" else "") + "prismarine_slab", ["prismarine", "prismarine_brick", "dark_prismarine"].index(slab)
            elif slab == "red_sandstone" and self.target == "1.12.2":
                identity = "minecraft:" + ("double_" if half == "double" else "") + "stone_slab2"
            else:
                identity = "etfuturum:" + ((slab + "_double_slab") if half == "double" and self.target == "1.12.2" else ("double_" if half == "double" else "") + path)
            if half == "top":
                meta += 8
        else:
            for suffix, legacy in (("wool", "wool"), ("carpet", "carpet"), ("stained_glass", "stained_glass"), ("stained_glass_pane", "stained_glass_pane"), ("terracotta", "stained_hardened_clay"), ("concrete", "concrete"), ("concrete_powder", "concrete_powder")):
                if path.endswith("_" + suffix) and path[:-(len(suffix)+1)] in COLORS:
                    color = path[:-(len(suffix)+1)]
                    identity, meta = "minecraft:" + legacy, COLORS.index(color)
                    if identity not in self.blocks and self.target == "1.7.10":
                        identity = "etfuturum:" + legacy
                    break
            if identity is None and path in STATIC:
                aliases = {"grass_block": ("grass", 0), "coarse_dirt": ("dirt", 1), "podzol": ("dirt", 2), "red_sand": ("sand", 1), "bricks": ("brick_block", 0), "stone_bricks": ("stonebrick", 0), "mossy_stone_bricks": ("stonebrick", 1), "cracked_stone_bricks": ("stonebrick", 2), "chiseled_stone_bricks": ("stonebrick", 3), "terracotta": ("hardened_clay", 0), "melon": ("melon_block", 0), "carved_pumpkin": ("pumpkin", 0), "jack_o_lantern": ("lit_pumpkin", 0), "snow_block": ("snow", 0), "slime_block": ("slime", 0), "cobweb": ("web", 0), "dead_bush": ("deadbush", 0), "lily_pad": ("waterlily", 0), "enchanting_table": ("enchanting_table", 0), "magma_block": ("magma", 0), "red_nether_bricks": ("red_nether_brick", 0), "end_stone_bricks": ("end_bricks", 0)}
                old, meta = aliases.get(path, (path, 0))
                identity = "minecraft:" + old
                if path in ("prismarine", "prismarine_bricks", "dark_prismarine"):
                    identity = "minecraft:prismarine" if self.target == "1.12.2" else "etfuturum:prismarine_block"
                    meta = ["prismarine", "prismarine_bricks", "dark_prismarine"].index(path)
                elif path == "wet_sponge":
                    identity, meta = "minecraft:sponge", 1
                    if self.target == "1.7.10": identity, meta = "etfuturum:sponge", 1
                elif identity not in self.blocks:
                    identity = "etfuturum:" + path
        if props or identity is None:
            return None
        return self.validate(Block(identity, meta))

    def validate(self, block):
        if block.identity not in self.blocks or not isinstance(block.metadata, int) or not 0 <= block.metadata <= 15:
            return None
        if not 0 <= self.blocks[block.identity] <= 4095:
            return None
        return block
