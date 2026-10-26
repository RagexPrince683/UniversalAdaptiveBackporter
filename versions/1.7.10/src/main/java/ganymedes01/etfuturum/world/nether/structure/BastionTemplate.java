package ganymedes01.etfuturum.world.nether.structure;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.*;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.ChestGenHooks;

import java.io.InputStream;
import java.util.*;

/** Immutable reference data; placement and all side effects obey the populated chunk's clip. */
final class BastionTemplate {
	private final NBTTagCompound data;
	private final List<Map<String, String>> properties = new ArrayList<>();
	private final List<String> names = new ArrayList<>();
	final List<NBTTagCompound> connectors = new ArrayList<>();
	final int sx, sy, sz;
	private BlockStateContainer[][] preparedPalette;
	private final Map<String, BlockStateContainer[]> finalStates = new HashMap<>();

	BastionTemplate(String name) {
		try (InputStream stream = getClass().getResourceAsStream("/data/structure/" + name + ".nbt")) {
			if (stream == null) throw new IllegalArgumentException("Missing bastion template: " + name);
			data = CompressedStreamTools.readCompressed(stream);
		} catch (Exception e) { throw new IllegalStateException("Cannot load bastion template " + name, e); }
		BlockPos size = NBTStructure.getPosFromTagList(data.getTagList("size", 3));
		sx = size.getX(); sy = size.getY(); sz = size.getZ();
		NBTTagList palette = data.getTagList("palette", 10);
		for (int i = 0; i < palette.tagCount(); i++) {
			NBTTagCompound entry = palette.getCompoundTagAt(i);
			names.add(entry.getString("Name"));
			properties.add(NBTStructure.getProperties(entry));
		}
		NBTTagList blocks = data.getTagList("blocks", 10);
		for (int i = 0; i < blocks.tagCount(); i++) {
			NBTTagCompound entry = blocks.getCompoundTagAt(i);
			if (names.get(entry.getInteger("state")).equals("minecraft:jigsaw")) connectors.add(entry);
		}
		preparedPalette = new BlockStateContainer[4][names.size()];
		for (int rotation = 0; rotation < 4; rotation++) for (int i = 0; i < names.size(); i++) preparedPalette[rotation][i] = state(names.get(i), properties.get(i), rotation);
		for (NBTTagCompound connector : connectors) {
			String finalState = connector.getCompoundTag("nbt").getString("final_state");
			if (finalState.isEmpty()) finalState = "minecraft:air";
			if (!finalStates.containsKey(finalState)) {
				BlockStateContainer[] states = new BlockStateContainer[4];
				for (int rotation = 0; rotation < 4; rotation++) states[rotation] = state(finalState, Collections.emptyMap(), rotation);
				finalStates.put(finalState, states);
			}
		}
	}

	static BlockPos rotate(int x, int y, int z, int rotation) {
		switch (rotation & 3) {
			case 1: return new BlockPos(-z, y, x);
			case 2: return new BlockPos(-x, y, -z);
			case 3: return new BlockPos(z, y, -x);
			default: return new BlockPos(x, y, z);
		}
	}
	static BlockPos position(NBTTagCompound entry, int rotation) {
		BlockPos pos = NBTStructure.getPosFromTagList(entry.getTagList("pos", 3));
		return rotate(pos.getX(), pos.getY(), pos.getZ(), rotation);
	}
	BlockPos front(NBTTagCompound connector, int rotation) {
		String facing = properties.get(connector.getInteger("state")).get("orientation").split("_")[0];
		return direction(facing, rotation);
	}
	BlockPos top(NBTTagCompound connector, int rotation) { return direction(properties.get(connector.getInteger("state")).get("orientation").split("_")[1], rotation); }
	private BlockPos direction(String facing, int rotation) {
		switch (facing) {
			case "up": return new BlockPos(0, 1, 0);
			case "down": return new BlockPos(0, -1, 0);
			case "north": return rotate(0, 0, -1, rotation);
			case "south": return rotate(0, 0, 1, rotation);
			case "east": return rotate(1, 0, 0, rotation);
			default: return rotate(-1, 0, 0, rotation);
		}
	}
	StructureBoundingBox box(int x, int y, int z, int rotation) {
		BlockPos a = rotate(sx - 1, sy - 1, sz - 1, rotation);
		return new StructureBoundingBox(x + Math.min(0, a.getX()), y, z + Math.min(0, a.getZ()), x + Math.max(0, a.getX()), y + sy - 1, z + Math.max(0, a.getZ()));
	}

	static BlockStateContainer state(String name, Map<String, String> props, int rotation) {
		int bracket = name.indexOf('[');
		if (bracket >= 0) {
			if (!name.endsWith("]")) throw new IllegalStateException("Invalid bastion final state " + name);
			Map<String, String> parsed = new HashMap<>(props);
			for (String property : name.substring(bracket + 1, name.length() - 1).split(",")) {
				String[] pair = property.split("=", 2);
				if (pair.length != 2) throw new IllegalStateException("Invalid bastion state property " + property);
				parsed.put(pair[0], pair[1]);
			}
			props = parsed; name = name.substring(0, bracket);
		}
		if (name.startsWith("minecraft:jigsaw")) return new BlockStateContainer(Blocks.air);
		Block block;
		int subtype = 0;
		switch (name.replace("minecraft:", "")) {
			case "air": block = Blocks.air; break;
			case "chest": block = Blocks.chest; break;
			case "fire": block = Blocks.fire; break;
			case "glowstone": block = Blocks.glowstone; break;
			case "gold_block": block = Blocks.gold_block; break;
			case "lava": block = Blocks.lava; break;
			case "nether_wart": block = Blocks.nether_wart; break;
			case "netherrack": block = Blocks.netherrack; break;
			case "quartz_block": block = Blocks.quartz_block; break;
			case "soul_sand": block = Blocks.soul_sand; break;
			case "spawner": block = Blocks.mob_spawner; break;
			case "basalt": block = ModBlocks.BASALT.get(); break;
			case "polished_basalt": block = ModBlocks.BASALT.get(); subtype = 1; break;
			case "blackstone": block = ModBlocks.BLACKSTONE.get(); break;
			case "polished_blackstone": block = ModBlocks.BLACKSTONE.get(); subtype = 1; break;
			case "polished_blackstone_bricks": block = ModBlocks.BLACKSTONE.get(); subtype = 2; break;
			case "cracked_polished_blackstone_bricks": block = ModBlocks.BLACKSTONE.get(); subtype = 3; break;
			case "chiseled_polished_blackstone": block = ModBlocks.BLACKSTONE.get(); subtype = 4; break;
			case "blackstone_slab": block = "double".equals(props.get("type")) ? ModBlocks.DOUBLE_BLACKSTONE_SLAB.get() : ModBlocks.BLACKSTONE_SLAB.get(); break;
			case "blackstone_stairs": block = ModBlocks.BLACKSTONE_STAIRS.get(); break;
			case "polished_blackstone_brick_stairs": block = ModBlocks.POLISHED_BLACKSTONE_BRICK_STAIRS.get(); break;
			case "blackstone_wall": block = ModBlocks.BLACKSTONE_WALL.get(); break;
			case "gilded_blackstone": block = ModBlocks.GILDED_BLACKSTONE.isEnabled() ? ModBlocks.GILDED_BLACKSTONE.get() : ModBlocks.BLACKSTONE.get(); break;
			case "smooth_quartz": block = ModBlocks.SMOOTH_QUARTZ.isEnabled() ? ModBlocks.SMOOTH_QUARTZ.get() : Blocks.quartz_block; break;
			case "smooth_quartz_slab": block = "double".equals(props.get("type")) ? ModBlocks.DOUBLE_SMOOTH_QUARTZ_SLAB.get() : ModBlocks.SMOOTH_QUARTZ_SLAB.get(); break;
			case "lantern": block = ModBlocks.LANTERN.isEnabled() ? ModBlocks.LANTERN.get() : Blocks.air; break;
			case "iron_chain": block = ModBlocks.CHAIN.isEnabled() ? ModBlocks.CHAIN.get() : Blocks.air; name = "minecraft:chain"; break;
			case "magma_block": block = ModBlocks.MAGMA.isEnabled() ? ModBlocks.MAGMA.get() : Blocks.netherrack; break;
			default: throw new IllegalStateException("Unsupported bastion block " + name);
		}
		if (block == null) throw new IllegalStateException("Unsupported bastion block " + name);
		Map<String, String> rotated = new HashMap<>(props);
		if (rotation == 1 || rotation == 3) {
			for (Map.Entry<String, String> property : rotated.entrySet()) {
				if (property.getValue().equals("north")) property.setValue("south");
				else if (property.getValue().equals("south")) property.setValue("north");
			}
		}
		ForgeDirection direction = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.WEST, ForgeDirection.SOUTH, ForgeDirection.EAST}[rotation];
		int meta = EFRBlockStateConverter.INSTANCE.getMetaFromState(name, rotated, direction) + subtype;
		if (name.equals("minecraft:chain")) {
			String axis = props.get("axis");
			meta = "x".equals(axis) ? ((rotation & 1) == 0 ? 1 : 2) : "z".equals(axis) ? ((rotation & 1) == 0 ? 2 : 1) : 0;
		}
		if (name.equals("minecraft:lantern")) meta = Boolean.parseBoolean(props.get("hanging")) ? 1 : 0;
		return new BlockStateContainer(block, meta);
	}

	void place(World world, Random random, int x, int y, int z, int rotation, String processors, StructureBoundingBox clip) {
		Random degradation = new Random(0);
		NBTTagList blocks = data.getTagList("blocks", 10);
		for (int i = 0; i < blocks.tagCount(); i++) {
			NBTTagCompound entry = blocks.getCompoundTagAt(i);
			BlockPos pos = position(entry, rotation);
			int wx = x + pos.getX(), wy = y + pos.getY(), wz = z + pos.getZ();
			if (!clip.isVecInside(wx, wy, wz)) continue;
			int index = entry.getInteger("state");
			String name = names.get(index);
			NBTTagCompound nbt = entry.getCompoundTag("nbt");
			BlockStateContainer state = preparedPalette[rotation][index];
			if (name.equals("minecraft:jigsaw")) {
				name = nbt.getString("final_state");
				if (name.isEmpty()) name = "minecraft:air";
				state = finalStates.get(name)[rotation];
				if (name.indexOf('[') >= 0) name = name.substring(0, name.indexOf('['));
			}
			long seed = (long) wx * 3129871 ^ (long) wz * 116129781L ^ wy;
			degradation.setSeed((seed * seed * 42317861L + seed * 11L) >> 16 ^ world.getSeed());
			state = BastionProcessors.apply(processors, name, state, rotation, degradation, pos.getY());
			world.setBlock(wx, wy, wz, state.getBlock(), state.getMeta(), 2);
			if (state.getBlock() == Blocks.chest && nbt.hasKey("LootTable")) {
				TileEntity tile = world.getTileEntity(wx, wy, wz);
				if (tile instanceof IInventory) {
					ChestGenHooks loot = BastionLoot.get(nbt.getString("LootTable"));
					WeightedRandomChestContent.generateChestContents(random, loot.getItems(random), (IInventory) tile, loot.getCount(random));
					ganymedes01.etfuturum.world.structure.ExplorationLoot.bastion((IInventory) tile, nbt.getString("LootTable"), random);
				}
			} else if (state.getBlock() == Blocks.mob_spawner) {
				TileEntity tile = world.getTileEntity(wx, wy, wz);
				if (tile instanceof TileEntityMobSpawner) ((TileEntityMobSpawner) tile).func_145881_a().setEntityName("LavaSlime");
			}
		}
		if (!ConfigEntities.enableNetherMobs) return;
		NBTTagList entities = data.getTagList("entities", 10);
		for (int i = 0; i < entities.tagCount(); i++) {
			NBTTagCompound entry = entities.getCompoundTagAt(i);
			NBTTagList pos = entry.getTagList("pos", 6);
			BlockPos p = rotate((int) Math.floor(pos.func_150309_d(0)), (int) Math.floor(pos.func_150309_d(1)), (int) Math.floor(pos.func_150309_d(2)), rotation);
			if (!clip.isVecInside(x + p.getX(), y + p.getY(), z + p.getZ())) continue;
			String id = entry.getCompoundTag("nbt").getString("id");
			EntityLiving mob;
			if (id.equals("minecraft:piglin_brute")) mob = new EntityPiglinBrute(world);
			else if (id.equals("minecraft:piglin")) mob = new EntityPiglin(world);
			else if (id.equals("minecraft:hoglin")) mob = new EntityHoglin(world);
			else continue;
			mob.setLocationAndAngles(x + p.getX() + 0.5, y + p.getY(), z + p.getZ() + 0.5, rotation * 90, 0);
			mob.func_110163_bv();
			world.spawnEntityInWorld(mob);
		}
	}
}
