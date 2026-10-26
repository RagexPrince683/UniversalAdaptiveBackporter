package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.EntityGuardian;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;
import java.util.*;

/** Saved starts and clipped placement replace all-or-nothing loaded-footprint rejection. */
public class MapGenOceanMonument extends MapGenStructure {
	private static final Map<World, MapGenOceanMonument> GENERATORS = new IdentityHashMap<>();
	public MapGenOceanMonument() { range = 4; }
	public static MapGenOceanMonument get(World world) {
		MapGenOceanMonument generator = GENERATORS.get(world);
		if (generator == null) { generator = new MapGenOceanMonument(); generator.worldObj = world; GENERATORS.put(world, generator); }
		return generator;
	}
	public static void unload(World world) { GENERATORS.remove(world); }
	public static void clear() { GENERATORS.clear(); }
	public static net.minecraft.world.ChunkPosition nearestSaved(World world, double x, double z) {
		MapGenOceanMonument generator = get(world);
		generator.hasStructureAt((int) x, 0, (int) z);
		double nearest = 128 * 128;
		net.minecraft.world.ChunkPosition result = null;
		int chunkX = net.minecraft.util.MathHelper.floor_double(x) >> 4, chunkZ = net.minecraft.util.MathHelper.floor_double(z) >> 4;
		for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
			StructureStart start = generator.structureMap.get(net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(chunkX + dx, chunkZ + dz));
			if (start == null) continue;
			StructureBoundingBox box = start.getBoundingBox();
			int cx = (box.minX + box.maxX) / 2, cz = (box.minZ + box.maxZ) / 2;
			double distance = (cx - x) * (cx - x) + (cz - z) * (cz - z);
			if (distance < nearest) { nearest = distance; result = new net.minecraft.world.ChunkPosition(cx, 58, cz); }
		}
		return result;
	}
	public static boolean contains(World world, int x, int y, int z) {
		MapGenOceanMonument generator = get(world);
		if (!generator.hasStructureAt(x, y, z)) return false;
		StructureStart start = generator.func_143028_c(x, y, z);
		return start != null && !start.getComponents().isEmpty() && y >= ((Piece) start.getComponents().getFirst()).baseY;
	}
	@Override public boolean generateStructuresInChunk(World world, Random random, int cx, int cz) {
		// Unlike vanilla's +8 population window, this clips to the one chunk guaranteed to exist.
		hasStructureAt(cx * 16, 0, cz * 16); // initialize native saved starts
		StructureBoundingBox clip = new StructureBoundingBox(cx * 16, 0, cz * 16, cx * 16 + 15, world.getActualHeight() - 1, cz * 16 + 15);
		MapGenStructureData saved = (MapGenStructureData) world.perWorldStorage.loadData(MapGenStructureData.class, func_143025_a());
		boolean placed = false;
		for (StructureStart start : structureMap.values()) {
			if (!start.getBoundingBox().intersectsWith(clip)) continue;
			start.generateStructure(world, random, clip);
			saved.func_143043_a(start.func_143021_a(start.func_143019_e(), start.func_143018_f()), start.func_143019_e(), start.func_143018_f());
			placed = true;
		}
		if (placed) saved.markDirty();
		return placed;
	}
	@Override public String func_143025_a() { return "EFROceanMonument"; }
	@Override protected boolean canSpawnStructureAtCoords(int x, int z) {
		return worldObj.getWorldInfo().isMapFeaturesEnabled() && OceanMonument.canSpawnAt(worldObj, x, z);
	}
	@Override protected StructureStart getStructureStart(int x, int z) { return new Start(x, z); }
	public static class Start extends StructureStart {
		public Start() { }
		public Start(int x, int z) { super(x, z); components.add(new Piece(x * 16 + 8, 39, z * 16 + 8)); updateBoundingBox(); }
	}
	public static class Piece extends StructureComponent {
		private int baseY;
		private int elders;
		private final Set<Long> placedChunks = new HashSet<>();
		private final int[][] elderPositions = {{12, 5, 14}, {45, 5, 14}, {28, 17, 28}};
		public Piece() { }
		public Piece(int x, int y, int z) { coordBaseMode = -1; baseY = y; boundingBox = new StructureBoundingBox(x, 0, z, x + 57, y + 21, z + 57); }
		@Override protected void func_143012_a(NBTTagCompound tag) {
			tag.setInteger("BaseY", baseY); tag.setInteger("Elders", elders);
			net.minecraft.nbt.NBTTagList chunks = new net.minecraft.nbt.NBTTagList();
			for (long key : placedChunks) { NBTTagCompound chunk = new NBTTagCompound(); chunk.setLong("Chunk", key); chunks.appendTag(chunk); }
			tag.setTag("PlacedChunks", chunks);
		}
		@Override protected void func_143011_b(NBTTagCompound tag) {
			baseY = tag.getInteger("BaseY"); elders = tag.getInteger("Elders");
			placedChunks.clear(); net.minecraft.nbt.NBTTagList chunks = tag.getTagList("PlacedChunks", 10);
			for (int i = 0; i < chunks.tagCount(); i++) placedChunks.add(chunks.getCompoundTagAt(i).getLong("Chunk"));
		}
		@Override public boolean addComponentParts(World world, Random random, StructureBoundingBox clip) {
			int minX = Math.max(clip.minX, boundingBox.minX), minZ = Math.max(clip.minZ, boundingBox.minZ);
			int maxX = Math.min(clip.maxX, boundingBox.maxX), maxZ = Math.min(clip.maxZ, boundingBox.maxZ);
			if (!world.checkChunksExist(minX, 0, minZ, maxX, world.getActualHeight() - 1, maxZ)) return true;
			long chunk = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(clip.minX >> 4, clip.minZ >> 4);
			if (placedChunks.contains(chunk)) return true;
			OceanMonument.buildTemple(world, boundingBox.minX, baseY, boundingBox.minZ, clip);
			if (ConfigEntities.enableGuardians) for (int i = 0; i < elderPositions.length; i++) {
				int x = boundingBox.minX + elderPositions[i][0], y = baseY + elderPositions[i][1], z = boundingBox.minZ + elderPositions[i][2];
				if ((elders & 1 << i) != 0 || !clip.isVecInside(x, y, z)) continue;
				EntityGuardian guardian = new EntityGuardian(world); guardian.setElder(true); guardian.setLocationAndAngles(x + .5, y, z + .5, 0, 0);
				if (world.spawnEntityInWorld(guardian)) elders |= 1 << i;
			}
			placedChunks.add(chunk);
			return true;
		}
	}
}
