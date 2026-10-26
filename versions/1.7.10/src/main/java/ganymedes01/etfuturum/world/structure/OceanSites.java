package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;

/** Saved shipwreck/ruin plans. Only new, loaded, unpopulated footprints are admitted; each chunk is placed once. */
public final class OceanSites extends WorldSavedData {
	private static final String ID = "etfuturum_ocean_sites";
	private static final String[] WRECKS = {"rightsideup_backhalf", "rightsideup_fronthalf", "rightsideup_full", "sideways_backhalf", "sideways_fronthalf", "sideways_full", "upsidedown_backhalf", "upsidedown_fronthalf", "upsidedown_full", "with_mast"};
	private final List<Plan> plans = new ArrayList<>();
	private final Map<Long, List<Plan>> chunks = new HashMap<>();
	public OceanSites() { super(ID); }
	public OceanSites(String id) { super(id); }
	private static OceanSites data(World world) {
		OceanSites value = (OceanSites) world.perWorldStorage.loadData(OceanSites.class, ID);
		if (value == null) { value = new OceanSites(); world.perWorldStorage.setData(ID, value); }
		return value;
	}
	private void index(Plan plan) {
		plans.add(plan);
		for (int cx = plan.x >> 4; cx <= plan.box().maxX >> 4; cx++) for (int cz = plan.z >> 4; cz <= plan.box().maxZ >> 4; cz++) chunks.computeIfAbsent(ChunkCoordIntPair.chunkXZ2Int(cx, cz), key -> new ArrayList<>()).add(plan);
	}
	public static void populate(World world, int chunkX, int chunkZ) {
		if (world.isRemote || world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled()) return;
		OceanSites sites = data(world);
		for (int kind = 0; kind < 2; kind++) {
			if (!ContentAvailability.enabled(kind == 0 ? "shipwrecks" : "ocean_ruins")) continue;
			int grid = kind == 0 ? 24 : 20, gx = Math.floorDiv(chunkX, grid), gz = Math.floorDiv(chunkZ, grid);
			Random random = new Random(world.getSeed() + gx * 341873128712L + gz * 132897987541L + (kind == 0 ? 165745295 : 14357621));
			if (chunkX != gx * grid + random.nextInt(grid - 8) || chunkZ != gz * grid + random.nextInt(grid - 8)) continue;
			int x = chunkX << 4, z = chunkZ << 4; BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);
			if (!BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN) && !(kind == 0 && BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.BEACH))) continue;
			String name;
			if (kind == 0) name = "shipwreck/" + WRECKS[random.nextInt(WRECKS.length)] + (random.nextBoolean() ? "_degraded" : "");
			else { boolean big = random.nextFloat() < .3F; int index = big ? new int[]{1, 2, 3, 8}[random.nextInt(4)] : 1 + random.nextInt(8); boolean warm = biome.temperature > .8F; if (big && warm) index = 4 + random.nextInt(4); name = "underwater_ruin/" + (big ? "big_" : "") + (warm ? "warm_" : "brick_") + index; }
			sites.admit(world, name, x, z, random.nextInt(4), random.nextLong(), chunkX, chunkZ);
		}
		long key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
		List<Plan> pending = sites.chunks.get(key); if (pending == null) return;
		for (Plan plan : pending) if (!plan.completed.contains(key)) {
			OceanTemplate.get(plan.name).place(world, plan.name, plan.x, plan.y, plan.z, plan.rotation, chunkX, chunkZ, plan.seed, plan.name.startsWith("shipwreck/") ? 1 : .9F);
			if (plan.name.contains("brick_")) for (String overlay : new String[]{"mossy_", "cracked_"}) {
				String name = plan.name.replace("brick_", overlay); OceanTemplate.get(name).place(world, name, plan.x, plan.y, plan.z, plan.rotation, chunkX, chunkZ, plan.seed, .35F);
			}
			plan.completed.add(key); sites.markDirty();
		}
	}
	private boolean admit(World world, String name, int x, int z, int rotation, long seed, int currentX, int currentZ) {
		Plan plan = new Plan(); plan.name = name; plan.x = x; plan.z = z; plan.rotation = rotation; plan.seed = seed;
		OceanTemplate template = OceanTemplate.get(name); int maxX = x + template.width(rotation) - 1, maxZ = z + template.depth(rotation) - 1;
		for (int cx = x >> 4; cx <= maxX >> 4; cx++) for (int cz = z >> 4; cz <= maxZ >> 4; cz++) {
			if (!world.getChunkProvider().chunkExists(cx, cz) || chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(cx, cz)) || (cx != currentX || cz != currentZ) && world.getChunkFromChunkCoords(cx, cz).isTerrainPopulated) return false;
		}
		int floor = 255;
		for (int px : new int[]{x + 1, maxX - 1}) for (int pz : new int[]{z + 1, maxZ - 1}) {
			int y = Math.min(75, world.getTopSolidOrLiquidBlock(px, pz) - 1);
			while (y > 20 && (world.getBlock(px, y, pz).getMaterial() == Material.water || !world.getBlock(px, y, pz).getMaterial().isSolid())) y--;
			floor = Math.min(floor, y);
		}
		if (floor < 30 || floor > 68) return false;
		plan.y = floor - (name.startsWith("shipwreck/") ? 0 : 1);
		if (ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, plan.box())) return false;
		index(plan); markDirty(); return true;
	}
	public static ChunkPosition nearestSaved(World world, double x, double z) {
		double best = 128 * 128; ChunkPosition result = null;
		OceanSites saved = data(world); int chunkX = net.minecraft.util.MathHelper.floor_double(x) >> 4, chunkZ = net.minecraft.util.MathHelper.floor_double(z) >> 4;
		for (int cx = -8; cx <= 8; cx++) for (int cz = -8; cz <= 8; cz++) {
			List<Plan> nearby = saved.chunks.get(ChunkCoordIntPair.chunkXZ2Int(chunkX + cx, chunkZ + cz)); if (nearby == null) continue;
			for (Plan plan : nearby) { double dx = plan.x - x, dz = plan.z - z; if (dx * dx + dz * dz < best) { best = dx * dx + dz * dz; result = new ChunkPosition(plan.x, plan.y, plan.z); } }
		}
		return result;
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		plans.clear(); chunks.clear(); NBTTagList list = tag.getTagList("Sites", 10);
		for (int i = 0; i < list.tagCount(); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); Plan plan = new Plan(); plan.name = entry.getString("Template"); plan.x = entry.getInteger("X"); plan.y = entry.getInteger("Y"); plan.z = entry.getInteger("Z"); plan.rotation = entry.getInteger("Rotation") & 3; plan.seed = entry.getLong("Seed"); NBTTagList done = entry.getTagList("Completed", 4); for (int n = 0; n < done.tagCount(); n++) plan.completed.add(((NBTTagLong) done.tagList.get(n)).func_150291_c()); index(plan); }
	}
	@Override public void writeToNBT(NBTTagCompound tag) {
		NBTTagList list = new NBTTagList();
		for (Plan plan : plans) { NBTTagCompound entry = new NBTTagCompound(); entry.setString("Template", plan.name); entry.setInteger("X", plan.x); entry.setInteger("Y", plan.y); entry.setInteger("Z", plan.z); entry.setInteger("Rotation", plan.rotation); entry.setLong("Seed", plan.seed); NBTTagList done = new NBTTagList(); for (long key : plan.completed) done.appendTag(new NBTTagLong(key)); entry.setTag("Completed", done); list.appendTag(entry); } tag.setTag("Sites", list);
	}
	private static final class Plan {
		String name; int x, y, z, rotation; long seed; final Set<Long> completed = new HashSet<>();
		StructureBoundingBox box() { OceanTemplate template = OceanTemplate.get(name); return new StructureBoundingBox(x, y, z, x + template.width(rotation) - 1, y + template.sy - 1, z + template.depth(rotation) - 1); }
	}
}
