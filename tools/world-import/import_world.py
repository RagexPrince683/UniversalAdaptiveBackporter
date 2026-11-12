#!/usr/bin/env python3
"""Experimental offline Java 1.13.2 -> UAB Anvil import. Never rewrites a source world."""
import argparse
import copy
import gzip
import hashlib
import json
import math
import os
from pathlib import Path
import re
import shutil
import struct
import tempfile
import zlib

from nbt import Tag, compound, read, sequence, string, write, MAX_BYTES
from mappings import Block, Mappings, state_key

SUPPORTED_SOURCE = {1631: "1.13.2"}
# Explicit product boundary: 1.18.2 and every later release are excluded.
EXCLUDED_DATA_VERSION = 2975


def read_mappings(path):
    def unique_pairs(pairs):
        result = {}
        for key, item in pairs:
            if key in result:
                raise ValueError("Duplicate mapping key: " + key)
            result[key] = item
        return result
    mappings = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=unique_pairs)
    if not isinstance(mappings, dict):
        raise ValueError("Mappings must be a JSON object")
    for state, target in mappings.items():
        if target is not None and (not isinstance(target, dict) or set(target) - {"id", "meta"}
                or not isinstance(target.get("id"), str) or type(target.get("meta", 0)) is not int
                or not 0 <= target.get("meta", 0) <= 15):
            raise ValueError("Invalid mapping for " + state + ": expected id and metadata 0-15, or null")
    return mappings


def value(tag, name, fallback=None):
    return tag.value[name].value if name in tag.value else fallback


def inflate(data, compression):
    if compression not in (1, 2):
        raise ValueError("Unsupported region compression (external streams are not supported)")
    decoder = zlib.decompressobj(31 if compression == 1 else zlib.MAX_WBITS)
    raw = decoder.decompress(data, MAX_BYTES + 1)
    if len(raw) > MAX_BYTES or not decoder.eof or decoder.unused_data:
        raise ValueError("Invalid or oversized compressed NBT")
    return raw


def level(path):
    return read(inflate((path / "level.dat").read_bytes(), 1))[1]


def source_version(root):
    version = value(root.value["Data"], "DataVersion", -1)
    if version >= EXCLUDED_DATA_VERSION:
        raise ValueError("Minecraft 1.18.2+ worlds are explicitly excluded from this importer")
    if version not in SUPPORTED_SOURCE:
        raise ValueError("Unsupported source DataVersion %s; initial support is Java 1.13.2 (1631) only" % version)
    return version


def target_registries(root, target):
    fml = root.value.get("FML")
    if fml is None:
        raise ValueError("Target template must be a saved Forge/UAB world")
    mods = {value(t, "ModId").lower(): value(t, "ModVersion") for t in value(fml, "ModList", (0, []))[1]}
    if "etfuturum" not in mods:
        raise ValueError("Target template does not contain a UAB registry snapshot")
    if target == "1.7.10":
        if not mods.get("forge", "").startswith("10.13.4.") or "ItemData" not in fml.value:
            raise ValueError("Template is not a Forge 1.7.10 world")
        entries = value(fml, "ItemData")[1]
        blocks = {value(t, "K")[1:]: value(t, "V") for t in entries if value(t, "K").startswith("\x01")}
        items = {value(t, "K")[1:]: value(t, "V") for t in entries if value(t, "K").startswith("\x02")}
        items.update({k: v for k, v in blocks.items() if k not in items})
        biomes = set(range(256))  # Source vanilla biome numbers are validated separately below.
    else:
        if mods.get("minecraft") != "1.12.2":
            raise ValueError("Template is not a Forge 1.12.2 world")
        registries = fml.value["Registries"].value
        def registry(name):
            return {value(t, "K"): value(t, "V") for t in value(registries[name], "ids")[1]}
        blocks, items, biome_ids = registry("minecraft:blocks"), registry("minecraft:items"), registry("minecraft:biomes")
        biomes = set(biome_ids.values())
    if blocks.get("minecraft:air") != 0 or blocks.get("minecraft:stone") != 1:
        raise ValueError("Unexpected target vanilla block registry")
    return blocks, items, biomes, mods


def region_chunks(path):
    match = re.fullmatch(r"r\.(-?\d+)\.(-?\d+)\.mca", path.name)
    if match is None:
        raise ValueError("Invalid region filename: " + path.name)
    rx, rz = map(int, match.groups())
    data = path.read_bytes()
    if len(data) < 8192 or len(data) % 4096:
        raise ValueError("Invalid region length: " + str(path))
    occupied = {0, 1}
    for slot in range(1024):
        location = int.from_bytes(data[slot*4:slot*4+4], "big")
        offset, sectors = location >> 8, location & 255
        if not offset and not sectors:
            continue
        if offset < 2 or sectors == 0 or (offset + sectors) * 4096 > len(data):
            raise ValueError("Region chunk outside allocation")
        allocation = set(range(offset, offset + sectors))
        if occupied.intersection(allocation):
            raise ValueError("Overlapping region chunk allocations")
        occupied.update(allocation)
        start = offset * 4096
        length = int.from_bytes(data[start:start+4], "big")
        if length < 2 or length + 4 > sectors * 4096:
            raise ValueError("Invalid compressed chunk length")
        root = read(inflate(data[start+5:start+4+length], data[start+4]))[1]
        chunk = root.value.get("Level")
        if chunk is None or value(chunk, "xPos") != rx*32+slot%32 or value(chunk, "zPos") != rz*32+slot//32:
            raise ValueError("Chunk coordinates do not match region slot")
        yield slot, int.from_bytes(data[4096+slot*4:4100+slot*4], "big"), root


def palette_indices(section):
    entries = value(section, "Palette", (0, []))[1]
    if not entries or len(entries) > 4096:
        raise ValueError("Invalid section palette")
    bits = max(4, (len(entries)-1).bit_length())
    packed = value(section, "BlockStates", [])
    if len(packed) != (4096 * bits + 63) // 64:
        raise ValueError("Invalid 1.13.2 BlockStates length")
    longs = [n & ((1 << 64)-1) for n in packed]
    for index in range(4096):
        bit = index * bits
        word, shift = divmod(bit, 64)
        decoded = longs[word] >> shift
        if shift + bits > 64:
            decoded |= longs[word+1] << (64-shift)
        decoded &= (1 << bits)-1
        if decoded >= len(entries):
            raise ValueError("Section references a missing palette entry")
        yield decoded


def nibble(array, index, value):
    array[index//2] |= (value & 15) << ((index & 1)*4)


class Conversion:
    def __init__(self, source, template, target, output, custom=None, unknown="halt", discard=False, offset=0, height="halt"):
        self.source, self.template, self.output = map(lambda p: Path(p).resolve(), (source, template, output))
        self.target, self.unknown, self.discard, self.offset, self.height = target, unknown, discard, offset, height
        if target not in ("1.7.10", "1.12.2") or offset % 16:
            raise ValueError("Choose a supported target and a height offset divisible by 16")
        for input_path in (self.source, self.template):
            if self.output == input_path or self.output.is_relative_to(input_path) or input_path.is_relative_to(self.output):
                raise ValueError("Output and input directories must be separate, without nesting")
        if self.output.exists():
            raise ValueError("Output already exists; choose a new directory")
        self.custom = copy.deepcopy(custom or {})
        # Bind cached level metadata to the same input snapshot used by preview/publication.
        self.input_fingerprint = self.fingerprint()
        self.source_level, self.template_level = level(self.source), level(self.template)
        self.version = source_version(self.source_level)
        self.blocks, self.items, self.biomes, self.mods = target_registries(self.template_level, target)
        self.mapping = Mappings(target, self.blocks, self.custom)
        self.report = {"experimental": True, "source_version": SUPPORTED_SOURCE[self.version], "target": target,
                       "source": str(self.source), "registry_template": str(self.template), "output": str(self.output),
                       "backup": "Source and registry template are read-only and remain untouched; output is a new world directory.",
                       "target_mods": self.mods, "chunks": 0, "mapped_blocks": 0, "unsupported_blocks": {}, "substitutions": {},
                       "losses": {}, "blocking": [], "height_offset": offset, "height_policy": height,
                       "unknown_policy": unknown, "discard_unsupported_data": discard,
                       "warnings": ["Experimental terrain import; complete gameplay acceptance is not established.",
                                    "Source player data, advancements, statistics, maps, raids and world-generation metadata are not imported.",
                                    "Target template world settings and generation rules are retained; new terrain follows the target generator.",
                                    "Lighting is marked for recalculation; stair connections and kelp ages are recomputed by the target."]}
        self.initial_report = copy.deepcopy(self.report)
        if self.input_fingerprint != self.fingerprint():
            raise ValueError("Input files changed while reading world metadata; stop Minecraft and retry")

    def loss(self, name, count=1, allowed=None):
        self.report["losses"][name] = self.report["losses"].get(name, 0) + count
        if count and not (self.discard if allowed is None else allowed):
            if name not in self.report["blocking"]:
                self.report["blocking"].append(name)

    def map_palette(self, entry):
        name = value(entry, "Name")
        props = {key: tag.value for key, tag in entry.value.get("Properties", compound()).value.items()}
        if not isinstance(name, str) or any(not isinstance(v, str) for v in props.values()):
            raise ValueError("Invalid block state")
        return state_key(name, props), self.mapping.resolve(name, props)

    def chunk(self, root):
        if value(root, "DataVersion") != self.version:
            raise ValueError("Mixed/unsupported chunk DataVersion")
        source = root.value["Level"]
        if value(source, "Status") not in ("full", "postprocessed", "minecraft:full"):
            raise ValueError("Incomplete/proto chunk is not supported")
        result = compound(xPos=Tag(3, value(source, "xPos")), zPos=Tag(3, value(source, "zPos")),
                          LastUpdate=Tag(4, 0), InhabitedTime=Tag(4, value(source, "InhabitedTime", 0)),
                          TerrainPopulated=Tag(1, 1), LightPopulated=Tag(1, 0))
        sections, seen_y, heightmap = [], set(), [0] * 256
        for section in value(source, "Sections", (0, []))[1]:
            y = value(section, "Y")
            # 1.13.2 has 0..15 storage sections; newer height schemas are never admitted here.
            if not isinstance(y, int) or not 0 <= y <= 15 or y in seen_y:
                raise ValueError("Invalid/duplicate source section height")
            seen_y.add(y)
            mapped_palette = [self.map_palette(e) for e in value(section, "Palette", (0, []))[1]]
            indices = list(palette_indices(section))
            target_y = y + self.offset//16
            if not 0 <= target_y <= 15:
                count = sum(mapped_palette[i][0] != "minecraft:air" for i in indices)
                self.loss("non-air blocks outside target height", count, self.height == "discard")
                continue
            ids, metadata, high = bytearray(4096), bytearray(2048), bytearray(2048)
            for index, palette_id in enumerate(indices):
                key, block = mapped_palette[palette_id]
                if block is None:
                    missing = self.report["unsupported_blocks"]
                    missing[key] = missing.get(key, 0) + 1
                    if self.unknown == "halt":
                        if "unsupported blocks" not in self.report["blocking"]:
                            self.report["blocking"].append("unsupported blocks")
                        block = Block("minecraft:air")  # Preview only; blocked plans can never be published.
                    else:
                        block = Block("minecraft:" + self.unknown)
                        replacements = self.report["substitutions"]
                        replacements[key + " -> " + block.identity] = replacements.get(key + " -> " + block.identity, 0) + 1
                else:
                    self.report["mapped_blocks"] += 1
                block_id = self.blocks[block.identity]
                ids[index] = block_id & 255
                nibble(high, index, block_id >> 8)
                nibble(metadata, index, block.metadata)
                if block_id:
                    column = (index >> 4 & 15)*16 + (index & 15)
                    heightmap[column] = max(heightmap[column], target_y*16 + (index >> 8) + 1)
            converted = compound(Y=Tag(1, target_y), Blocks=Tag(7, bytes(ids)), Data=Tag(7, bytes(metadata)),
                                 BlockLight=Tag(7, bytes(2048)), SkyLight=Tag(7, bytes(2048)))
            if any(high):
                converted.value["Add"] = Tag(7, bytes(high))
            sections.append(converted)
        result.value["Sections"] = sequence(10, sorted(sections, key=lambda s: value(s, "Y")))
        result.value["HeightMap"] = Tag(11, heightmap)
        biome_data = value(source, "Biomes")
        if not isinstance(biome_data, list) or len(biome_data) != 256:
            raise ValueError("Only 1.13.2 two-dimensional biome storage is supported")
        converted_biomes = []
        for biome in biome_data:
            # 1.13 oceans need target-specific identities, not vanilla 1.12 numeric assumptions.
            if biome in (44, 45, 46, 47, 48, 49, 50):
                fallback = {44: 0, 45: 0, 46: 0, 47: 24, 48: 24, 49: 24, 50: 10}[biome]
                self.loss("modern ocean biome substituted with legacy ocean", 1)
                biome = fallback
            if biome not in set(range(0, 40)) | {127, 129, 130, 131, 132, 133, 134, 140, 149, 151, 155, 156, 157, 158, 160, 161, 162, 163, 164, 165, 166, 167} or biome not in self.biomes:
                self.loss("unsupported biome replaced with plains", 1)
                biome = 1
            converted_biomes.append(biome)
        result.value["Biomes"] = Tag(7, bytes(converted_biomes))
        entities = []
        for entity in value(source, "Entities", (0, []))[1]:
            converted = self.entity(entity)
            if converted is not None:
                entities.append(converted)
        result.value["Entities"] = sequence(10, entities)
        tiles = []
        for tile in value(source, "TileEntities", (0, []))[1]:
            converted = self.tile(tile)
            if converted is not None:
                tiles.append(converted)
        result.value["TileEntities"] = sequence(10, tiles)
        for tile in tiles:
            x, y, z = (value(tile, coordinate) for coordinate in ("x", "y", "z"))
            if x//16 != value(source, "xPos") or z//16 != value(source, "zPos"):
                raise ValueError("Block entity lies outside its chunk")
            section = next((s for s in sections if value(s, "Y") == y//16), None)
            index = (y % 16)*256+(z % 16)*16+(x % 16)
            if section is None:
                raise ValueError("Container has no target section")
            identifier = value(section, "Blocks")[index]
            if "Add" in section.value:
                identifier |= (value(section, "Add")[index//2] >> ((index & 1)*4) & 15) << 8
            if identifier not in {self.blocks.get("minecraft:chest"), self.blocks.get("minecraft:trapped_chest")}:
                raise ValueError("Container does not match the converted block")
        for ticks in ("TileTicks", "LiquidTicks", "ToBeTicked", "LiquidsToBeTicked", "PostProcessing"):
            values = value(source, ticks, (0, []))[1]
            count = sum(len(v.value[1]) if v.kind == 9 else 1 for v in values)
            self.loss("unsupported " + ticks, count)
        structures = source.value.get("Structures", compound()).value
        count = sum(len(tag.value) for tag in structures.values() if tag.kind == 10)
        self.loss("structure starts/references", count)
        return compound(Level=result, **({"DataVersion": Tag(3, 1343)} if self.target == "1.12.2" else {}))

    def entity(self, source):
        ids = {"minecraft:cow": "Cow", "minecraft:pig": "Pig", "minecraft:chicken": "Chicken", "minecraft:sheep": "Sheep"}
        identifier = value(source, "id")
        allowed = {"id", "Pos", "Motion", "Rotation", "FallDistance", "Fire", "Air", "OnGround", "Dimension", "Invulnerable", "PortalCooldown", "UUIDMost", "UUIDLeast", "Health", "HurtTime", "DeathTime", "AbsorptionAmount", "Age", "ForcedAge", "InLove", "LoveCauseMost", "LoveCauseLeast", "Color", "Sheared", "Saddle", "EggLayTime", "CanPickUpLoot", "PersistenceRequired", "Silent", "NoAI", "Leashed", "FallFlying", "HandItems", "ArmorItems", "HandDropChances", "ArmorDropChances", "Attributes"}
        if identifier not in ids or any(key not in allowed for key in source.value) or value(source, "Leashed", 0):
            self.loss("unsupported entity " + str(identifier))
            return None
        if any(any(t.value for t in value(source, key, (0, []))[1]) for key in ("HandItems", "ArmorItems")):
            self.loss("equipped animal " + identifier)
            return None
        health_limit = {"minecraft:cow": 10, "minecraft:pig": 10, "minecraft:chicken": 4, "minecraft:sheep": 8}[identifier]
        defaults = {"generic.maxHealth": health_limit, "generic.followRange": 16, "generic.knockbackResistance": 0,
                    "generic.movementSpeed": {"minecraft:cow": .2, "minecraft:pig": .25, "minecraft:chicken": .25, "minecraft:sheep": .23}[identifier],
                    "generic.armor": 0, "generic.armorToughness": 0}
        for attribute in value(source, "Attributes", (0, []))[1]:
            name, base = value(attribute, "Name"), value(attribute, "Base")
            modifiers = value(attribute, "Modifiers", (0, []))[1]
            if name not in defaults or not isinstance(base, (int, float)) or not math.isfinite(base) or abs(base-defaults[name]) > .000001 or modifiers:
                self.loss("unsupported custom animal attributes " + identifier)
                return None
        if self.target == "1.7.10" and (value(source, "NoAI", 0) or value(source, "Silent", 0) or value(source, "ForcedAge", 0)):
            self.loss("unsupported legacy animal control state " + identifier)
            return None
        health = value(source, "Health", health_limit)
        if not isinstance(health, (int, float)) or not math.isfinite(health) or not 0 <= health <= health_limit:
            raise ValueError("Invalid vanilla animal health; custom attributes require a separate migration")
        result = copy.deepcopy(source)
        result.value["id"] = string(ids[identifier] if self.target == "1.7.10" else identifier)
        for key in ("Attributes", "FallFlying", "HandItems", "ArmorItems", "HandDropChances", "ArmorDropChances"):
            # Only verified vanilla defaults reach this point; custom values are never silently reset.
            if key == "Attributes" and key in result.value:
                self.loss("animal attributes rebuilt by target", allowed=True)
            result.value.pop(key, None)
        pos = result.value["Pos"].value[1]
        if len(pos) != 3 or any(not math.isfinite(p.value) for p in pos):
            raise ValueError("Invalid entity position")
        pos[1] = Tag(6, pos[1].value + self.offset)
        if not math.isfinite(pos[1].value) or not 0 <= pos[1].value < 256:
            self.loss("entity outside target height", allowed=self.height == "discard")
            return None
        return result

    def tile(self, source):
        identifier = value(source, "id")
        # A conservative initial preservation path: vanilla chests with explicitly supported item stacks.
        allowed = {"id", "x", "y", "z", "Items", "Lock", "CustomName", "LootTable", "LootTableSeed"}
        if identifier not in ("minecraft:chest", "minecraft:trapped_chest") or any(k not in allowed for k in source.value) or value(source, "LootTable") or value(source, "CustomName") or self.target == "1.7.10" and value(source, "Lock"):
            self.loss("unsupported block entity " + str(identifier))
            return None
        result = copy.deepcopy(source)
        result.value["id"] = string("Chest" if self.target == "1.7.10" else "minecraft:chest")
        result.value["y"] = Tag(3, value(source, "y") + self.offset)
        if not 0 <= value(result, "y") < 256:
            self.loss("container outside target height", allowed=self.height == "discard")
            return None
        items = []
        slots = set()
        for stack in value(source, "Items", (0, []))[1]:
            name = value(stack, "id")
            # Item NBT needs its own versioned migration; reject enchanted/custom stacks in this first POC.
            if any(k not in {"id", "Count", "Slot"} for k in stack.value) or name not in self.items:
                self.loss("unsupported container item " + str(name))
                continue
            if not 0 < value(stack, "Count", 0) <= 64 or not 0 <= value(stack, "Slot", -1) < 27:
                raise ValueError("Invalid chest inventory stack")
            if value(stack, "Slot") in slots:
                raise ValueError("Duplicate chest inventory slot")
            slots.add(value(stack, "Slot"))
            item = copy.deepcopy(stack)
            item.value["id"] = Tag(2, self.items[name]) if self.target == "1.7.10" else string(name)
            item.value["Damage"] = Tag(2, 0)
            items.append(item)
        result.value["Items"] = sequence(10, items)
        return result

    def fingerprint(self):
        digest = hashlib.sha256()
        for base in (self.source, self.template):
            files = sorted(p for p in base.rglob("*") if p.is_file()) if base == self.source else [base/"level.dat"]
            for path in files:
                if path.is_symlink():
                    raise ValueError("Symbolic links are not supported")
                digest.update(str(path).encode())
                with path.open("rb") as stream:
                    for data in iter(lambda: stream.read(1024*1024), b""):
                        digest.update(data)
        digest.update(json.dumps([self.target, str(self.output), self.unknown, self.discard, self.offset, self.height, self.custom], sort_keys=True).encode())
        return digest.hexdigest()

    def scan(self, destination=None):
        self.report = copy.deepcopy(self.initial_report)
        for directory in self.source.iterdir():
            if directory.is_dir() and (directory.name.startswith("DIM") or directory.name in ("dimensions", "entities", "poi")):
                self.loss("unsupported dimension/storage directory " + directory.name, sum(p.stat().st_size for p in directory.rglob("*") if p.is_file()))
        files = sorted((self.source/"region").glob("*.mca"))
        if not files:
            raise ValueError("Source has no Overworld region chunks")
        self.report["source_region_bytes"] = sum(p.stat().st_size for p in files)
        self.report["estimated_output_upper_bound_bytes"] = 8192*len(files)
        for path in files:
            output = bytearray(8192)
            for slot, timestamp, source in region_chunks(path):
                converted = self.chunk(source)
                # Validate the serialized output before it can become a loadable world.
                raw = write(converted)
                read(raw)
                validate_chunk(converted)
                compressed = zlib.compress(raw)
                sectors = (len(compressed)+5+4095)//4096
                if sectors > 255:
                    raise ValueError("Converted chunk exceeds legacy region allocation")
                self.report["estimated_output_upper_bound_bytes"] += sectors*4096
                if destination is not None:
                    offset = len(output)//4096
                    output[slot*4:slot*4+4] = ((offset << 8)|sectors).to_bytes(4, "big")
                    output[4096+slot*4:4100+slot*4] = timestamp.to_bytes(4, "big")
                    output.extend(struct.pack(">IB", len(compressed)+1, 2) + compressed)
                    output.extend(bytes((-len(output)) % 4096))
                self.report["chunks"] += 1
            if destination is not None:
                (destination/"region"/path.name).write_bytes(output)
        self.report["coverage"] = {"mapped_blocks": self.report["mapped_blocks"], "unmapped_blocks": sum(self.report["unsupported_blocks"].values())}
        self.report["required_optional_mods"] = sorted({t["id"].split(":")[0] for t in self.custom.values() if t and t["id"].split(":")[0] not in ("minecraft", "etfuturum")})
        return self.report

    def preview(self):
        before = self.fingerprint()
        if before != self.input_fingerprint:
            raise ValueError("Input files changed since world metadata was read; create a fresh preview")
        self.scan()
        if before != self.fingerprint():
            raise ValueError("Input files changed during preview; stop Minecraft and retry")
        self.report["confirmation"] = before
        return self.report

    def convert(self, confirmation):
        before = self.fingerprint()
        if before != confirmation or before != self.input_fingerprint:
            raise ValueError("Confirmation does not match this source/template/policy preview")
        if not self.output.parent.is_dir():
            raise ValueError("Output parent directory must already exist")
        staging = Path(tempfile.mkdtemp(prefix=".uab-import-", dir=self.output.parent))
        try:
            (staging/"region").mkdir()
            self.scan(staging)
            if self.report["blocking"]:
                raise ValueError("Conversion blocked: " + ", ".join(self.report["blocking"]))
            if before != self.fingerprint():
                raise ValueError("Input files changed during conversion; output will not be published")
            target = copy.deepcopy(self.template_level)
            data = target.value["Data"].value
            for key in ("Player", "DimensionData", "DragonFight", "ScheduledEvents"):
                data.pop(key, None)
            # Start at the imported source spawn. Original target terrain/player data is never copied.
            source = self.source_level.value["Data"]
            for key in ("SpawnX", "SpawnZ"):
                if key in source.value: data[key] = copy.deepcopy(source.value[key])
            data["SpawnY"] = Tag(3, max(1, min(255, value(source, "SpawnY", 64)+self.offset)))
            data["LevelName"] = string("UAB import - " + self.source.name)
            self.report["confirmation"] = before
            self.report["complete"] = True
            (staging/"conversion-report.json").write_text(json.dumps(self.report, indent=2, sort_keys=True), encoding="utf-8")
            # Written last: failed or interrupted staging directories cannot be loaded as worlds.
            (staging/"level.dat").write_bytes(gzip.compress(write(target)))
            if self.output.exists():
                raise ValueError("Output was created by another process")
            os.rename(staging, self.output)
            return self.report
        except BaseException:
            shutil.rmtree(staging)
            raise


def validate_chunk(root):
    chunk = root.value["Level"]
    for section in value(chunk, "Sections")[1]:
        if not 0 <= value(section, "Y") <= 15:
            raise ValueError("Invalid target section")
        for name, length in (("Blocks", 4096), ("Data", 2048), ("Add", 2048), ("SkyLight", 2048), ("BlockLight", 2048)):
            if name in section.value and len(value(section, name)) != length:
                raise ValueError("Invalid target " + name)
    if len(value(chunk, "Biomes")) != 256 or len(value(chunk, "HeightMap")) != 256:
        raise ValueError("Invalid target chunk columns")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True, type=Path)
    parser.add_argument("--template", required=True, type=Path, help="Stopped, saved target UAB world; supplies actual Forge registry IDs")
    parser.add_argument("--target", required=True, choices=("1.7.10", "1.12.2"))
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--mappings", type=Path, help="Explicit source-state -> target identity/metadata JSON declarations")
    parser.add_argument("--unknown", choices=("halt", "air", "stone"), default="halt")
    parser.add_argument("--discard-unsupported-data", action="store_true")
    parser.add_argument("--height-offset", type=int, default=0)
    parser.add_argument("--height-policy", choices=("halt", "discard"), default="halt")
    parser.add_argument("--confirm", help="Exact confirmation hash from the reviewed preview")
    args = parser.parse_args()
    try:
        custom = read_mappings(args.mappings) if args.mappings else None
        conversion = Conversion(args.source, args.template, args.target, args.output, custom, args.unknown,
                                args.discard_unsupported_data, args.height_offset, args.height_policy)
        report = conversion.convert(args.confirm) if args.confirm else conversion.preview()
        print(json.dumps(report, indent=2, sort_keys=True))
        return 2 if report["blocking"] else 0
    except (ValueError, OSError, KeyError, TypeError, struct.error, zlib.error) as error:
        print(json.dumps({"error": str(error), "output_published": False}, indent=2))
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
