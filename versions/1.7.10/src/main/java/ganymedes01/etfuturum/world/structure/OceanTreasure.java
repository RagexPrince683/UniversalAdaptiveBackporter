package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;

/** Chunk-local buried treasure with saved locations and once-only placement/loot. */
public final class OceanTreasure extends WorldSavedData {
	private static final String ID = "etfuturum_ocean_treasure";
	private final Map<Long, int[]> positions = new HashMap<>();
	public OceanTreasure() { super(ID); }
	public OceanTreasure(String id) { super(id); }
	private static OceanTreasure data(World world) {
		OceanTreasure value = (OceanTreasure) world.perWorldStorage.loadData(OceanTreasure.class, ID);
		if (value == null) { value = new OceanTreasure(); world.perWorldStorage.setData(ID, value); }
		return value;
	}
	public static void generate(World world, int chunkX, int chunkZ) {
		if (world.isRemote || world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled() || !ContentAvailability.enabled("buried_treasure")) return;
		Random random = new Random(world.getSeed() + chunkX * 341873128712L + chunkZ * 132897987541L + 10387320);
		if (random.nextInt(100) != 0) return;
		int x = (chunkX << 4) + 9, z = (chunkZ << 4) + 9;
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if (!BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.BEACH)) return;
		place(world, x, z, random);
	}
	public static boolean place(World world, int x, int z, Random random) {
		if (world.isRemote || !world.blockExists(x, 63, z)) return false;
		long key = ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4);
		OceanTreasure value = data(world); if (value.positions.containsKey(key)) return false;
		for (int y = Math.min(70, world.getTopSolidOrLiquidBlock(x, z) - 2); y >= 40; y--) {
			Block block = world.getBlock(x, y, z);
			if ((block != Blocks.sand && block != Blocks.gravel) || world.getTileEntity(x, y, z) != null) continue;
			if (ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new StructureBoundingBox(x, y, z, x, y, z))) return false;
			world.setBlock(x, y, z, Blocks.chest, 2, 2);
			TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
			if (chest == null) return false;
			UndergroundLoot.fill(chest, "minecraft:chests/buried_treasure", random);
			value.positions.put(key, new int[]{x, y, z}); value.markDirty(); return true;
		}
		return false;
	}
	public static ChunkPosition nearestSaved(World world, double x, double z) {
		double best = 128 * 128; int[] found = null;
		OceanTreasure saved = data(world); int chunkX = net.minecraft.util.MathHelper.floor_double(x) >> 4, chunkZ = net.minecraft.util.MathHelper.floor_double(z) >> 4;
		for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
			int[] point = saved.positions.get(ChunkCoordIntPair.chunkXZ2Int(chunkX + dx, chunkZ + dz)); if (point == null) continue;
			double distance = (point[0] - x) * (point[0] - x) + (point[2] - z) * (point[2] - z);
			if (distance < best) { best = distance; found = point; }
		}
		return found == null ? null : new ChunkPosition(found[0], found[1], found[2]);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		positions.clear(); NBTTagList list = tag.getTagList("Locations", 10);
		for (int i = 0; i < list.tagCount(); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); int[] point = entry.getIntArray("Position"); if (point.length == 3) positions.put(ChunkCoordIntPair.chunkXZ2Int(point[0] >> 4, point[2] >> 4), point); }
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		NBTTagList list = new NBTTagList(); for (int[] point : positions.values()) { NBTTagCompound entry = new NBTTagCompound(); entry.setIntArray("Position", point); list.appendTag(entry); } tag.setTag("Locations", list);
	}
}
