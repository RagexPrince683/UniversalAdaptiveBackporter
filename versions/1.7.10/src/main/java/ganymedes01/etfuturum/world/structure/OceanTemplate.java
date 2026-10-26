package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.structurenbt.*;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.io.*;
import java.util.*;

/** Cached released ocean geometry with explicit legacy palette conversion and chunk-clipped placement. */
final class OceanTemplate {
	private static final Map<String, OceanTemplate> CACHE = new HashMap<>();
	static OceanTemplate get(String id) { return CACHE.computeIfAbsent(id, OceanTemplate::new); }
	final int sx, sy, sz;
	private final NBTTagList entries;
	private final String[] names;
	private final BlockStateContainer[][] states;
	private OceanTemplate(String id) {
		NBTTagCompound data;
		try (InputStream in = OceanTemplate.class.getResourceAsStream("/data/structure/" + id + ".nbt")) {
			if (in == null) throw new IllegalArgumentException("Missing ocean template " + id);
			data = CompressedStreamTools.readCompressed(in);
		} catch (IOException e) { throw new IllegalStateException(id, e); }
		NBTTagList size = data.getTagList("size", 3); sx = ((NBTTagInt) size.tagList.get(0)).func_150287_d(); sy = ((NBTTagInt) size.tagList.get(1)).func_150287_d(); sz = ((NBTTagInt) size.tagList.get(2)).func_150287_d();
		NBTTagList palette = data.hasKey("palette") ? data.getTagList("palette", 10) : (NBTTagList) data.getTagList("palettes", 9).tagList.get(0);
		entries = data.getTagList("blocks", 10); names = new String[palette.tagCount()]; states = new BlockStateContainer[4][names.length];
		ForgeDirection[] directions = {ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST};
		for (int i = 0; i < names.length; i++) {
			NBTTagCompound entry = palette.getCompoundTagAt(i); String name = entry.getString("Name"); names[i] = name;
			Map<String, String> props = NBTStructure.getProperties(entry);
			for (int r = 0; r < 4; r++) {
				Block block = name.equals("minecraft:magma_block") ? ModBlocks.MAGMA.get() : name.equals("minecraft:polished_granite") || name.equals("minecraft:polished_diorite") ? ModBlocks.STONE.get() : EFRBlockStateConverter.INSTANCE.getBlockFromNamespace(name, props);
				if (name.equals("minecraft:prismarine")) block = ModBlocks.PRISMARINE_BLOCK.get();
				if (name.equals("minecraft:spruce_fence")) block = ModBlocks.FENCE_SPRUCE.get();
				if (name.equals("minecraft:structure_block") || name.equals("minecraft:air")) block = Blocks.air;
				if (block == null || block == Blocks.stone && !name.equals("minecraft:stone")) throw new IllegalArgumentException("Unmapped ocean block " + name);
				int meta = name.equals("minecraft:polished_granite") ? 2 : name.equals("minecraft:polished_diorite") ? 4 : EFRBlockStateConverter.INSTANCE.getMetaFromStateWithSubtypeAdditions(name, props, directions[r]);
				states[r][i] = new BlockStateContainer(block, meta & 15);
			}
		}
	}
	int width(int rotation) { return (rotation & 1) == 0 ? sx : sz; }
	int depth(int rotation) { return (rotation & 1) == 0 ? sz : sx; }
	private int[] point(NBTTagCompound entry, int rotation) {
		NBTTagList pos = entry.getTagList("pos", 3); int x = ((NBTTagInt) pos.tagList.get(0)).func_150287_d(), y = ((NBTTagInt) pos.tagList.get(1)).func_150287_d(), z = ((NBTTagInt) pos.tagList.get(2)).func_150287_d();
		switch (rotation & 3) { case 1: return new int[]{sz - 1 - z, y, x}; case 2: return new int[]{sx - 1 - x, y, sz - 1 - z}; case 3: return new int[]{z, y, sx - 1 - x}; default: return new int[]{x, y, z}; }
	}
	void place(World world, String id, int x, int y, int z, int rotation, int chunkX, int chunkZ, long seed, float integrity) {
		List<int[]> chests = new ArrayList<>();
		for (int i = 0; i < entries.tagCount(); i++) {
			NBTTagCompound entry = entries.getCompoundTagAt(i); int[] p = point(entry, rotation); int wx = x + p[0], wy = y + p[1], wz = z + p[2];
			if (wx >> 4 != chunkX || wz >> 4 != chunkZ) continue;
			int state = entry.getInteger("state"); if (names[state].equals("minecraft:structure_block")) continue;
			BlockStateContainer converted = states[rotation][state]; Block block = converted.getBlock();
			if (block == Blocks.air || integrity < .5F && block == Blocks.chest || !ganymedes01.etfuturum.configuration.ContentAvailability.available(block) || world.getTileEntity(wx, wy, wz) != null || world.getBlock(wx, wy, wz) == Blocks.bedrock) continue;
			Random random = new Random(seed ^ (long) wx * 3129871 ^ (long) wz * 116129781L ^ wy);
			if (random.nextFloat() > integrity) continue;
			if (id.startsWith("underwater_ruin/") && integrity >= .5F && (block == Blocks.sand || block == Blocks.gravel) && ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology") && random.nextFloat() < .15F) {
				boolean warm = id.contains("warm_");
				world.setBlock(wx, wy, wz, warm ? ModBlocks.SUSPICIOUS_SAND.get() : ModBlocks.SUSPICIOUS_GRAVEL.get(), 0, 2);
				if (world.getTileEntity(wx, wy, wz) instanceof ganymedes01.etfuturum.tileentities.TileEntityBrushable brushable) brushable.configure("minecraft:archaeology/ocean_ruin_" + (warm ? "warm" : "cold"), random.nextLong());
			} else world.setBlock(wx, wy, wz, block, converted.getMeta(), 2);
			if (block == Blocks.chest) chests.add(new int[]{wx, wy, wz, converted.getMeta()});
		}
		for (int[] chest : chests) world.setBlockMetadataWithNotify(chest[0], chest[1], chest[2], chest[3], 2);
		if (integrity < .5F) return; // Cold-ruin overlays decorate the primary piece; they do not repeat its loot or mobs.
		for (int i = 0; i < entries.tagCount(); i++) {
			NBTTagCompound entry = entries.getCompoundTagAt(i); if (!names[entry.getInteger("state")].equals("minecraft:structure_block")) continue;
			int[] p = point(entry, rotation); int wx = x + p[0], wy = y + p[1], wz = z + p[2]; if (wx >> 4 != chunkX || wz >> 4 != chunkZ) continue;
			String marker = entry.getCompoundTag("nbt").getString("metadata"); Random random = new Random(seed ^ wx * 31L ^ wz * 719L ^ wy);
			boolean ruinChest = id.startsWith("underwater_ruin/") && marker.equals("chest");
			if (ruinChest && world.getTileEntity(wx, wy, wz) == null) world.setBlock(wx, wy, wz, Blocks.chest, new int[]{2, 5, 3, 4}[rotation & 3], 2);
			if (marker.endsWith("chest") && world.getTileEntity(wx, ruinChest ? wy : wy - 1, wz) instanceof IInventory inventory) {
				String table = id.startsWith("shipwreck/") ? marker.equals("map_chest") ? "shipwreck_map" : marker.equals("treasure_chest") ? "shipwreck_treasure" : "shipwreck_supply" : id.contains("big_") ? "underwater_ruin_big" : "underwater_ruin_small";
				UndergroundLoot.fill(inventory, "minecraft:chests/" + table, random);
			} else if (marker.equals("drowned") && world.difficultySetting != net.minecraft.world.EnumDifficulty.PEACEFUL && ganymedes01.etfuturum.configuration.ContentAvailability.mobEnabled(ganymedes01.etfuturum.entities.EntityDrowned.class)) {
				ganymedes01.etfuturum.entities.EntityDrowned drowned = new ganymedes01.etfuturum.entities.EntityDrowned(world); drowned.setLocationAndAngles(wx + .5, wy, wz + .5, 0, 0); world.spawnEntityInWorld(drowned);
			}
		}
	}
}
