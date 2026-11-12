"""Isolated serialization fixtures; no real saves or game installation are mutated."""
import copy
import gzip
import hashlib
import json
from pathlib import Path
import struct
import tempfile
import unittest
import zlib

from nbt import Tag, compound, sequence, string, read, write
from mappings import Mappings, state_key
from import_world import Conversion, palette_indices, region_chunks, source_version, validate_chunk, read_mappings


def packed(values, palette_size):
    bits = max(4, (palette_size-1).bit_length())
    longs = [0]*((len(values)*bits+63)//64)
    for index, entry in enumerate(values):
        word, shift = divmod(index*bits, 64)
        longs[word] |= entry << shift & ((1 << 64)-1)
        if shift+bits > 64:
            longs[word+1] |= entry >> (64-shift)
    return Tag(12, [n if n < 1 << 63 else n-(1 << 64) for n in longs])


def state(name, **props):
    return compound(Name=string("minecraft:"+name), Properties=Tag(10, {k: string(v) for k, v in props.items()}))


def fixture_chunk(extra=None):
    palette = [state("air"), state("stone"), state("oak_stairs", facing="west", half="top", shape="inner_left", waterlogged="false"),
               state("sandstone_slab", type="top", waterlogged="false"), state("kelp", age="12"),
               state("stripped_oak_log", axis="x"), state("chest", facing="north", type="single", waterlogged="false")]
    if extra:
        palette.append(extra)
    blocks = [0]*4096
    for i in range(len(palette)):
        blocks[i] = i
    chunk = compound(xPos=Tag(3, -1), zPos=Tag(3, 0), Status=string("full"),
                     Sections=sequence(10, [compound(Y=Tag(1, 4), Palette=sequence(10, palette), BlockStates=packed(blocks, len(palette)))]),
                     Biomes=Tag(11, [1]*256), Entities=sequence(10), TileEntities=sequence(10),
                     Structures=compound(Starts=compound(), References=compound()))
    return compound(DataVersion=Tag(3, 1631), Level=chunk)


def region(path, root):
    compressed = zlib.compress(write(root))
    sectors = (len(compressed)+5+4095)//4096
    output = bytearray(8192)
    output[31*4:32*4] = ((2 << 8)|sectors).to_bytes(4, "big")
    output.extend(struct.pack(">IB", len(compressed)+1, 2)+compressed)
    output.extend(bytes((-len(output)) % 4096))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(output)


def template(target):
    blocks = {"minecraft:air": 0, "minecraft:stone": 1, "minecraft:oak_stairs": 53, "minecraft:stone_slab": 44,
              "minecraft:double_stone_slab": 43, "minecraft:chest": 54, "minecraft:trapped_chest": 146,
              "minecraft:planks": 5, "etfuturum:kelp": 300, "etfuturum:log_stripped": 301,
              "etfuturum:stripped_oak_log": 301}
    mods = [("etfuturum", "1.1.5"), ("Forge", "10.13.4.1614")] if target == "1.7.10" else [("etfuturum", "1.1.5"), ("minecraft", "1.12.2")]
    fml = compound(ModList=sequence(10, [compound(ModId=string(k), ModVersion=string(v)) for k, v in mods]))
    if target == "1.7.10":
        entries = [("\x01"+k, v) for k, v in blocks.items()] + [("\x02minecraft:diamond", 264)]
        fml.value["ItemData"] = sequence(10, [compound(K=string(k), V=Tag(3, v)) for k, v in entries])
    else:
        def registry(data):
            return compound(ids=sequence(10, [compound(K=string(k), V=Tag(3, v)) for k, v in data.items()]))
        fml.value["Registries"] = Tag(10, {"minecraft:blocks": registry(blocks), "minecraft:items": registry({**blocks, "minecraft:diamond": 264}), "minecraft:biomes": registry({"minecraft:plains": 1})})
    return compound(FML=fml, Data=compound(SpawnX=Tag(3, -1), SpawnY=Tag(3, 65), SpawnZ=Tag(3, 0), LevelName=string("Template")))


class ImportTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="uab-import-test-")
        self.base = Path(self.temp.name)
        self.source, self.template = self.base/"source", self.base/"template"
        self.source.mkdir(); self.template.mkdir()
        self.source_level = compound(Data=compound(DataVersion=Tag(3, 1631), SpawnX=Tag(3, -1), SpawnY=Tag(3, 65), SpawnZ=Tag(3, 0)))
        self.save_source(fixture_chunk())

    def tearDown(self):
        self.temp.cleanup()

    def save_source(self, chunk):
        (self.source/"level.dat").write_bytes(gzip.compress(write(self.source_level), mtime=0))
        region(self.source/"region/r.-1.0.mca", chunk)

    def conversion(self, target="1.7.10", **options):
        (self.template/"level.dat").write_bytes(gzip.compress(write(template(target)), mtime=0))
        return Conversion(self.source, self.template, target, self.base/"output", **options)

    def test_modified_utf8_all_nbt_types(self):
        root = compound(text=string("a\0😀é"), ints=Tag(11, [-1, 1]), longs=Tag(12, [-2**63, 2**63-1]),
                        bytes=Tag(7, b"\x00\xff"), empty=sequence(0), number=Tag(6, 1.25))
        self.assertEqual(read(write(root))[1], root)

    def test_dense_palette_crosses_long_boundaries(self):
        values = [i % 19 for i in range(4096)]
        section = compound(Palette=sequence(10, [state("stone") for _ in range(19)]), BlockStates=packed(values, 19))
        self.assertEqual(list(palette_indices(section)), values)

    def test_malformed_palette_is_rejected(self):
        section = fixture_chunk().value["Level"].value["Sections"].value[1][0]
        section.value["BlockStates"] = Tag(12, [0])
        with self.assertRaises(ValueError): list(palette_indices(section))

    def test_explicit_release_exclusion(self):
        for version in (2975, 3105, 4189):
            with self.subTest(version=version), self.assertRaisesRegex(ValueError, "explicitly excluded"):
                source_version(compound(Data=compound(DataVersion=Tag(3, version))))

    def test_other_unsupported_versions_rejected(self):
        for version in (1343, 1630, 1976, 2865, 2974):
            with self.subTest(version=version), self.assertRaisesRegex(ValueError, "Unsupported source"):
                source_version(compound(Data=compound(DataVersion=Tag(3, version))))

    def test_both_targets_representative_chunk(self):
        for target in ("1.7.10", "1.12.2"):
            with self.subTest(target=target):
                output = self.base/"output"
                if output.exists():
                    import shutil; shutil.rmtree(output)
                before = {p: hashlib.sha256(p.read_bytes()).hexdigest() for p in self.source.rglob("*") if p.is_file()}
                converter = self.conversion(target)
                plan = converter.preview()
                self.assertEqual(plan["blocking"], [])
                # A new instance proves no hidden preview state is required.
                converter = self.conversion(target)
                converter.convert(plan["confirmation"])
                result = next(region_chunks(output/"region/r.-1.0.mca"))[2]
                validate_chunk(result)
                section = result.value["Level"].value["Sections"].value[1][0]
                self.assertEqual(section.value["Blocks"].value[4], 300 & 255)
                self.assertEqual(section.value["Add"].value[2] & 15, 1)
                self.assertEqual(section.value["Data"].value[1] & 15, 5)  # west-facing top stair
                self.assertEqual(section.value["Data"].value[1] >> 4, 9)  # top sandstone slab
                self.assertTrue((output/"level.dat").is_file())
                self.assertEqual(before, {p: hashlib.sha256(p.read_bytes()).hexdigest() for p in before})

    def test_unsupported_never_silently_disappears(self):
        self.save_source(fixture_chunk(state("future_block", valuable="true")))
        converter = self.conversion()
        plan = converter.preview()
        self.assertIn("unsupported blocks", plan["blocking"])
        with self.assertRaisesRegex(ValueError, "blocked"):
            self.conversion().convert(plan["confirmation"])
        self.assertFalse((self.base/"output").exists())

    def test_explicit_fallbacks_report_substitutions(self):
        self.save_source(fixture_chunk(state("future_block")))
        for policy in ("air", "stone"):
            plan = self.conversion(unknown=policy).preview()
            self.assertEqual(plan["blocking"], [])
            self.assertEqual(plan["substitutions"]["minecraft:future_block -> minecraft:"+policy], 1)

    def test_properties_and_third_party_identity(self):
        mapper = self.conversion().mapping
        self.assertIsNone(mapper.resolve("minecraft:stone", {"fake": "true"}))
        for name in ("stone", "water", "kelp", "stripped_oak_log"):
            self.assertIsNone(mapper.resolve("minecraft:" + name, {"waterlogged": "false"}))
        self.assertIsNone(mapper.resolve("other:stone", {}))
        self.assertIsNone(mapper.resolve("minecraft:oak_stairs", {"waterlogged": "true"}))
        mapper.custom["other:stone"] = {"id": "minecraft:stone", "meta": 3}
        self.assertEqual(mapper.resolve("other:stone", {}).metadata, 3)

    def test_height_change_requires_explicit_loss_policy(self):
        plan = self.conversion(offset=256).preview()
        self.assertIn("non-air blocks outside target height", plan["blocking"])
        plan = self.conversion(offset=256, height="discard").preview()
        self.assertEqual(plan["blocking"], [])
        self.assertGreater(plan["losses"]["non-air blocks outside target height"], 0)

    def test_container_and_animal_preservation(self):
        chunk = fixture_chunk(); data = chunk.value["Level"].value
        data["TileEntities"] = sequence(10, [compound(id=string("minecraft:chest"), x=Tag(3, -10), y=Tag(3, 64), z=Tag(3, 0),
                                                      Items=sequence(10, [compound(id=string("minecraft:diamond"), Count=Tag(1, 12), Slot=Tag(1, 0))]))])
        data["Entities"] = sequence(10, [compound(id=string("minecraft:cow"), Pos=sequence(6, [Tag(6, -2.), Tag(6, 65.), Tag(6, .5)]), Health=Tag(5, 7.), Age=Tag(3, -200))])
        self.save_source(chunk)
        for target in ("1.7.10", "1.12.2"):
            converter = self.conversion(target)
            result = converter.chunk(chunk).value["Level"]
            chest = result.value["TileEntities"].value[1][0]
            stack = chest.value["Items"].value[1][0]
            self.assertEqual(stack.value["Count"].value, 12)
            self.assertEqual(stack.value["id"].value, 264 if target == "1.7.10" else "minecraft:diamond")
            cow = result.value["Entities"].value[1][0]
            self.assertEqual(cow.value["Age"].value, -200)
            self.assertEqual(cow.value["Health"].value, 7.)

    def test_custom_animal_attributes_require_review(self):
        for target in ("1.7.10", "1.12.2"):
            chunk=fixture_chunk()
            animal=compound(id=string("minecraft:cow"),Pos=sequence(6,[Tag(6,-2.),Tag(6,65.),Tag(6,.5)]),Health=Tag(5,17.),
                            Attributes=sequence(10,[compound(Name=string("generic.maxHealth"),Base=Tag(6,20.))]))
            chunk.value["Level"].value["Entities"]=sequence(10,[animal]);self.save_source(chunk)
            plan=self.conversion(target).preview()
            self.assertIn("unsupported custom animal attributes minecraft:cow",plan["blocking"])
            self.assertEqual(self.conversion(target,discard=True).preview()["blocking"],[])

    def test_invalid_vanilla_animal_health_rejected(self):
        chunk=fixture_chunk()
        chunk.value["Level"].value["Entities"]=sequence(10,[compound(id=string("minecraft:cow"),Pos=sequence(6,[Tag(6,-2.),Tag(6,65.),Tag(6,.5)]),Health=Tag(5,17.))])
        self.save_source(chunk)
        with self.assertRaisesRegex(ValueError,"Invalid vanilla animal health"):
            self.conversion().preview()

    def test_unsupported_inventory_and_ticks_block(self):
        chunk = fixture_chunk(); data = chunk.value["Level"].value
        data["LiquidTicks"] = sequence(10, [compound(i=string("minecraft:water"))])
        self.save_source(chunk)
        self.assertIn("unsupported LiquidTicks", self.conversion().preview()["blocking"])
        self.assertEqual(self.conversion(discard=True).preview()["blocking"], [])

    def test_confirmation_binds_content_and_policy(self):
        plan = self.conversion().preview()
        with self.assertRaisesRegex(ValueError, "Confirmation"):
            self.conversion(unknown="air").convert(plan["confirmation"])
        (self.source/"region/r.-1.0.mca").write_bytes(b"changed")
        with self.assertRaisesRegex(ValueError, "Confirmation"):
            self.conversion().convert(plan["confirmation"])

    def test_loaded_metadata_cannot_outlive_input_snapshot(self):
        converter = self.conversion()
        self.source_level.value["Data"].value["SpawnX"] = Tag(3, 200)
        self.save_source(fixture_chunk())
        with self.assertRaisesRegex(ValueError, "Input files changed"):
            converter.preview()
        # A plan from a fresh reader must not authorize conversion using stale cached metadata.
        plan = self.conversion().preview()
        with self.assertRaisesRegex(ValueError, "Confirmation|Input files changed"):
            converter.convert(plan["confirmation"])

    def test_no_nested_or_existing_output(self):
        self.conversion()
        for path in (self.source, self.source/"converted", self.base, self.template):
            with self.subTest(path=path), self.assertRaises(ValueError):
                Conversion(self.source, self.template, "1.7.10", path)

    def test_overlapping_region_allocations_rejected(self):
        path = self.source/"region/r.-1.0.mca"
        data = bytearray(path.read_bytes());data[:4] = data[31*4:32*4];path.write_bytes(data)
        with self.assertRaises(ValueError): list(region_chunks(path))


class MappingFileTests(unittest.TestCase):
    def test_conflicting_and_malformed_declarations_fail(self):
        cases = ['{"minecraft:stone":null,"minecraft:stone":{"id":"minecraft:stone"}}',
                 '{"minecraft:stone":{"id":"minecraft:stone","meta":0,"meta":1}}',
                 '[]', '{"minecraft:stone":42}',
                 '{"minecraft:stone":{"id":"minecraft:stone","meta":true}}',
                 '{"minecraft:stone":{"id":"minecraft:stone","meta":16}}']
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory)/"mappings.json"
            for source in cases:
                with self.subTest(source=source):
                    path.write_text(source, encoding="utf-8")
                    with self.assertRaises(ValueError):
                        read_mappings(path)
            path.write_text('{"minecraft:stone":{"id":"minecraft:stone","meta":0},"minecraft:air":null}', encoding="utf-8")
            self.assertEqual(read_mappings(path)["minecraft:stone"]["meta"], 0)


if __name__ == "__main__":
    unittest.main()
