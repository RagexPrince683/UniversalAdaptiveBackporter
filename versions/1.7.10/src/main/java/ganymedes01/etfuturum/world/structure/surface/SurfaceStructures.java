package ganymedes01.etfuturum.world.structure.surface;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.world.structure.mansion.WoodlandMansionPieces;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;

/** Saved starts and per-chunk placement; candidate discovery never requests neighboring chunks. */
public final class SurfaceStructures extends MapGenStructure {
	private static final Map<World, SurfaceStructures> WORLDS = new IdentityHashMap<>();
	private static final Map<String, SurfaceTemplate> TEMPLATES = new HashMap<>();
	private static final ThreadLocal<Boolean> PLACING = new ThreadLocal<>();
	private final Map<Long, Start> candidates = new LinkedHashMap<Long, Start>(64, .75F, true) { @Override protected boolean removeEldestEntry(Map.Entry<Long, Start> entry) { return size() > 128; } };
	private ChunkProviderGenerate terrain;
	private SurfaceStructures(World world) { worldObj = world; range = 8; }
	static SurfaceTemplate template(String name) { return TEMPLATES.computeIfAbsent(name, SurfaceTemplate::new); }
	public static boolean placingTemplate() { return Boolean.TRUE.equals(PLACING.get()); }
	public static void unload(World world) { WORLDS.remove(world); }
	public static void clear() { WORLDS.clear(); }
	public static SurfaceStructures get(World world) { return WORLDS.computeIfAbsent(world, SurfaceStructures::new); }
	@Override public String func_143025_a() { return "UABSurfaceStructures"; }
	private static boolean enabled(int kind) { return ContentAvailability.enabled(kind == 0 ? "woodland_mansions" : "trail_ruins"); }
	private static int grid(int kind) { return kind == 0 ? 80 : 34; }
	public static int[] candidate(long seed, int gx, int gz, int kind) {
		int grid = grid(kind); Random r = new Random(seed + gx * 341873128712L + gz * 132897987541L + 10387319L + kind * 71287L);
		return new int[]{gx * grid + r.nextInt(grid - (kind == 0 ? 20 : 8)), gz * grid + r.nextInt(grid - (kind == 0 ? 20 : 8))};
	}
	/** Uses the generation admission checks without generating or populating chunks. */
	public static net.minecraft.util.ChunkCoordinates nearest(World world, int x, int z, int kind) {
		IChunkProvider provider = world.getChunkProvider();
		if (!enabled(kind) || !world.getWorldInfo().isMapFeaturesEnabled() || !ganymedes01.etfuturum.compat.CompatUndergroundBiomes.hasVanillaTerrain(provider)) return null;
		SurfaceStructures generator = get(world); generator.hasStructureAt(x, 0, z);
		double best = Double.MAX_VALUE; net.minecraft.util.ChunkCoordinates result = null;
		int gx = Math.floorDiv(x >> 4, grid(kind)), gz = Math.floorDiv(z >> 4, grid(kind));
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int[] p = candidate(world.getSeed(), gx + dx, gz + dz, kind);
			Start start = (Start) generator.structureMap.get(ChunkCoordIntPair.chunkXZ2Int(p[0], p[1]));
			if (start == null && generator.canSpawnStructureAtCoords(p[0], p[1])) start = generator.candidates.get(ChunkCoordIntPair.chunkXZ2Int(p[0], p[1]));
			if (start == null || start.kind != kind) continue;
			int px = p[0] * 16 + 8, pz = p[1] * 16 + 8;
			double distance = ((double) px - x) * ((double) px - x) + ((double) pz - z) * ((double) pz - z);
			if (distance < best) { best = distance; result = new net.minecraft.util.ChunkCoordinates(px, start.getBoundingBox().minY, pz); }
		}
		return result;
	}
	public static void populate(World world, IChunkProvider provider, int cx, int cz) {
		if (world.isRemote || world.provider.dimensionId != 0 || !ganymedes01.etfuturum.compat.CompatUndergroundBiomes.hasVanillaTerrain(provider) || !world.getWorldInfo().isMapFeaturesEnabled()) return;
		if (!enabled(0) && !enabled(1)) return;
		SurfaceStructures gen = get(world); gen.func_151539_a(provider, world, cx, cz, null);
		gen.generateStructuresInChunk(world, new Random(world.getSeed() ^ cx * 3129871L ^ cz * 116129781L), cx, cz);
	}
	private int kind(int cx, int cz) {
		for (int kind = 0; kind < 2; kind++) if (enabled(kind)) { int[] p = candidate(worldObj.getSeed(), Math.floorDiv(cx, grid(kind)), Math.floorDiv(cz, grid(kind)), kind); if (p[0] == cx && p[1] == cz && biome(kind, cx * 16 + 8, cz * 16 + 8)) return kind; } return -1;
	}
	private boolean biome(int kind, int x, int z) {
		BiomeGenBase b = worldObj.getWorldChunkManager().getBiomeGenAt(x, z);
		if (kind == 0) return b == BiomeGenBase.roofedForest || b.biomeID == BiomeGenBase.roofedForest.biomeID + 128;
		if (kind == 1) return BiomeDictionary.isBiomeOfType(b, BiomeDictionary.Type.CONIFEROUS) || b == BiomeGenBase.birchForest || b == BiomeGenBase.birchForestHills || b == BiomeGenBase.jungle || b == BiomeGenBase.jungleHills;
		return false;
	}
	@Override protected boolean canSpawnStructureAtCoords(int cx, int cz) {
		int kind = kind(cx, cz); if (kind < 0) return false; long key = ChunkCoordIntPair.chunkXZ2Int(cx, cz);
		if (!candidates.containsKey(key)) { Start start = create(cx, cz, kind); candidates.put(key, start); }
		return candidates.get(key) != null;
	}
	@Override protected StructureStart getStructureStart(int cx, int cz) { return candidates.get(ChunkCoordIntPair.chunkXZ2Int(cx, cz)); }
	private int height(int x, int z) {
		if (terrain == null) terrain = new ChunkProviderGenerate(worldObj, worldObj.getSeed(), false);
		Block[] data = new Block[65536]; terrain.func_147424_a(x >> 4, z >> 4, data);
		for (int y = 250; y > 1; y--) { Block block = data[((x & 15) * 16 + (z & 15)) * 256 + y]; if (block != null && block != Blocks.air && block.getMaterial().isSolid()) return y + 1; } return 1;
	}
	private Start create(int cx, int cz, int kind) {
		Random random = new Random(worldObj.getSeed() ^ cx * 341873128712L ^ cz * 132897987541L ^ kind * 131L);
		int x = cx * 16 + 8, z = cz * 16 + 8, y = height(x, z); if (y < 63) return null;
		List<Piece> pieces = new ArrayList<>(); int rotation = random.nextInt(4);
		if (kind == 0) WoodlandMansionPieces.generate(random, x, y, z, rotation, pieces);
		else if (kind == 1) TrailRuins.generate(random, x, y, z, pieces);
		if (pieces.isEmpty()) return null; Start start = new Start(cx, cz, kind, pieces); StructureBoundingBox box = start.getBoundingBox();
		
		if (kind == 0) {
			int min = 255, max = 0;
			for (int px : new int[]{box.minX, box.maxX}) for (int pz : new int[]{box.minZ, box.maxZ}) { int h = height(px, pz); min = Math.min(min, h); max = Math.max(max, h); }
			if (min < 63 || max - min > 18) return null;
			for (Piece p : pieces) { p.y += min - y; p.foundation = min; p.rebox(); } start.refresh(); box = start.getBoundingBox();
		}
		if (box.minY < 5 || box.maxY >= 250) return null;
		if (ganymedes01.etfuturum.world.structure.NativeStructureReservations.surface(worldObj, box)) return null;
		ChunkProviderServer provider = (ChunkProviderServer) worldObj.getChunkProvider();
		for (int px = box.minX >> 4; px <= box.maxX >> 4; px++) for (int pz = box.minZ >> 4; pz <= box.maxZ >> 4; pz++) {
			if (provider.currentChunkLoader instanceof AnvilChunkLoader && ((AnvilChunkLoader) provider.currentChunkLoader).chunkExists(worldObj, px, pz) || provider.chunkExists(px, pz) && provider.provideChunk(px, pz).isTerrainPopulated) return null;
		}
		return start;
	}
	@Override public boolean generateStructuresInChunk(World world, Random random, int cx, int cz) {
		hasStructureAt(cx * 16, 0, cz * 16); StructureBoundingBox clip = new StructureBoundingBox(cx * 16, 0, cz * 16, cx * 16 + 15, 255, cz * 16 + 15);
		MapGenStructureData data = (MapGenStructureData) world.perWorldStorage.loadData(MapGenStructureData.class, func_143025_a()); boolean placed = false;
		for (StructureStart value : structureMap.values()) { Start start = (Start) value; if (!enabled(start.kind) || !start.getBoundingBox().intersectsWith(clip)) continue;
			start.generateStructure(world, random, clip); data.func_143043_a(start.func_143021_a(start.func_143019_e(), start.func_143018_f()), start.func_143019_e(), start.func_143018_f()); placed = true;
		} if (placed) data.markDirty(); return placed;
	}
	public static class Start extends StructureStart {
		private int kind;
		private final Set<Long> placed = new HashSet<>();
		public Start() { }
		void refresh() { updateBoundingBox(); }
		Start(int cx, int cz, int kind, List<Piece> pieces) { super(cx, cz); this.kind = kind; components.addAll(pieces); updateBoundingBox(); }
		@Override public void generateStructure(World world, Random random, StructureBoundingBox clip) {
			long key = ChunkCoordIntPair.chunkXZ2Int(clip.minX >> 4, clip.minZ >> 4); if (placed.contains(key)) return;
			PLACING.set(true);
			try { for (Object value : components) { Piece piece = (Piece) value; if (piece.getBoundingBox().intersectsWith(clip)) piece.addComponentParts(world, random, clip); } placed.add(key); }
			finally { PLACING.remove(); }
		}
		@Override public void func_143022_a(NBTTagCompound tag) { super.func_143022_a(tag); tag.setInteger("Kind", kind); NBTTagList list = new NBTTagList(); for (long key : placed) { NBTTagCompound e = new NBTTagCompound(); e.setLong("Chunk", key); list.appendTag(e); } tag.setTag("PlacedChunks", list); }
		@Override public void func_143017_b(NBTTagCompound tag) { super.func_143017_b(tag); kind = tag.getInteger("Kind"); placed.clear(); NBTTagList list = tag.getTagList("PlacedChunks", 10); for (int i = 0; i < list.tagCount(); i++) placed.add(list.getCompoundTagAt(i).getLong("Chunk")); }
	}
	public static class Piece extends StructureComponent {
		String name, processors = ""; int x, y, z, rotation, mirror, kind, foundation;
		transient TrailProcessors.Result processed;
		public Piece() { }
		public Piece(String name, int x, int y, int z, int rotation, int mirror, int kind) { this.name = name; this.x = x; this.y = y; this.z = z; this.rotation = rotation; this.mirror = mirror; this.kind = kind; coordBaseMode = -1; rebox(); }
		void rebox() { boundingBox = template(name).box(x, y, z, rotation, mirror); }
		@Override protected void func_143012_a(NBTTagCompound tag) { tag.setString("Template", name); tag.setString("Processors", processors); tag.setInteger("X", x); tag.setInteger("Y", y); tag.setInteger("Z", z); tag.setInteger("Rotation", rotation); tag.setInteger("Mirror", mirror); tag.setInteger("Kind", kind); tag.setInteger("Foundation", foundation); }
		@Override protected void func_143011_b(NBTTagCompound tag) { name = tag.getString("Template"); processors = tag.getString("Processors"); x = tag.getInteger("X"); y = tag.getInteger("Y"); z = tag.getInteger("Z"); rotation = tag.getInteger("Rotation") & 3; mirror = Math.max(0, Math.min(2, tag.getInteger("Mirror"))); kind = tag.getInteger("Kind"); foundation = tag.getInteger("Foundation"); rebox(); }
		@Override public boolean addComponentParts(World world, Random random, StructureBoundingBox clip) { template(name).place(world, this, clip, world.getSeed()); return true; }
	}
}
