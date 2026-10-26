package ganymedes01.etfuturum.world.structure.underground;

import com.google.gson.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.world.structure.NativeStructureReservations;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.structure.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released jigsaw topology with native saved starts, deterministic reservations and one-chunk placement. */
public final class UndergroundStructures extends MapGenStructure {
	private static final Map<World, UndergroundStructures> WORLDS = new IdentityHashMap<>();
	private static final Map<String, UndergroundTemplate> TEMPLATES = new HashMap<>();
	private static final Map<String, Pool> POOLS = new HashMap<>();
	private static final Set<String> MISSING_TEMPLATES = new HashSet<>();
	private static final ThreadLocal<Boolean> PLACING_TEMPLATE = new ThreadLocal<>();
	public static boolean placingTemplate() { return Boolean.TRUE.equals(PLACING_TEMPLATE.get()); }
	static boolean beginTemplate() { boolean previous = placingTemplate(); PLACING_TEMPLATE.set(true); return previous; }
	static void endTemplate(boolean previous) { if (previous) PLACING_TEMPLATE.set(true); else PLACING_TEMPLATE.remove(); }
	private final Map<Long, Start> candidates = new LinkedHashMap<Long, Start>(64, .75F, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<Long, Start> entry) { return size() > 64; }
	};
	private ChunkProviderGenerate terrain;
	private static final int GRID = 48, RADIUS = 116;
	private UndergroundStructures() { range = 9; }
	public static UndergroundStructures get(World world) { UndergroundStructures value = WORLDS.get(world); if (value == null) { value = new UndergroundStructures(); value.worldObj = world; WORLDS.put(world, value); } return value; }
	public static void unload(World world) { WORLDS.remove(world); }
	public static void clear() { WORLDS.clear(); }
	static UndergroundTemplate template(String id) { return TEMPLATES.computeIfAbsent(id, UndergroundTemplate::new); }
	private static boolean nativeTerrain(World world) {
		IChunkProvider provider = world.getChunkProvider();
		return provider instanceof net.minecraft.world.gen.ChunkProviderServer && ((net.minecraft.world.gen.ChunkProviderServer) provider).currentChunkProvider.getClass() == ChunkProviderGenerate.class;
	}
	public static boolean reserved(World world, StructureBoundingBox box) {
		if (box.maxY < 6 || box.minY > 50) return false;
		if (world.isRemote || world.provider.dimensionId != 0 || !nativeTerrain(world) || !world.getWorldInfo().isMapFeaturesEnabled()) return false;
		UndergroundStructures generator = get(world); generator.hasStructureAt(box.minX, box.minY, box.minZ);
		for (int gx = Math.floorDiv(box.minX >> 4, GRID) - 1; gx <= Math.floorDiv(box.maxX >> 4, GRID) + 1; gx++) for (int gz = Math.floorDiv(box.minZ >> 4, GRID) - 1; gz <= Math.floorDiv(box.maxZ >> 4, GRID) + 1; gz++) {
			int[] p = candidate(world.getSeed(), gx, gz); int x = p[0] * 16 + 8, z = p[1] * 16 + 8;
			if (x + RADIUS + 2 < box.minX || x - RADIUS - 2 > box.maxX || z + RADIUS + 2 < box.minZ || z - RADIUS - 2 > box.maxZ || box.maxY < 6 || box.minY > 50) continue;
			long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(p[0], p[1]);
			if (!generator.structureMap.containsKey(key) && generator.canSpawnStructureAtCoords(p[0], p[1])) generator.func_151538_a(world, p[0], p[1], 0, 0, null);
			StructureStart start = generator.structureMap.get(key); if (start != null && start.getBoundingBox().intersectsWith(box)) return true;
		}
		return false;
	}
	public static boolean oreReserved(World world, int x, int y, int z, int size) {
		int radius = (int) Math.ceil(size * 3 / 16.0) + 2;
		return reserved(world, new StructureBoundingBox(x + 8 - radius, y - radius, z + 8 - radius, x + 8 + radius, y + radius, z + 8 + radius));
	}
	public static boolean containsCity(World world, int x, int y, int z) {
		UndergroundStructures generator = WORLDS.get(world); if (generator == null) return false;
		for (StructureStart value : generator.structureMap.values()) if (((Start) value).city && value.getBoundingBox().isVecInside(x, y, z)) return true;
		return false;
	}
	public static net.minecraft.util.ChunkCoordinates nearest(World world, int x, int z, boolean city) {
		if (!nativeTerrain(world) || !enabled(city)) return null;
		UndergroundStructures generator = get(world); generator.hasStructureAt(x, 0, z);
		double best = Double.MAX_VALUE; net.minecraft.util.ChunkCoordinates result = null;
		int gx = Math.floorDiv(x >> 4, GRID), gz = Math.floorDiv(z >> 4, GRID);
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int[] p = candidate(world.getSeed(), gx + dx, gz + dz); if ((p[2] == 1) != city) continue;
			long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(p[0], p[1]);
			Start start = (Start) generator.structureMap.get(key);
			if (start == null && generator.canSpawnStructureAtCoords(p[0], p[1])) start = generator.candidates.get(key);
			if (start == null) continue;
			int px = p[0] * 16 + 8, pz = p[1] * 16 + 8; double distance = (double) (px - x) * (px - x) + (double) (pz - z) * (pz - z);
			if (distance < best) { best = distance; result = new net.minecraft.util.ChunkCoordinates(px, start.getBoundingBox().minY + 3, pz); }
		}
		return result;
	}
	@Override public String func_143025_a() { return "EFRUnderground"; }
	public static void populate(World world, IChunkProvider provider, int cx, int cz) {
		if (world.isRemote || world.provider.dimensionId != 0 || provider.getClass() != ChunkProviderGenerate.class || !world.getWorldInfo().isMapFeaturesEnabled()) return;
		if (!ConfigWorld.enableAncientCities && !ConfigWorld.enableTrialChambers) return;
		UndergroundStructures generator = get(world);
		generator.func_151539_a(provider, world, cx, cz, null);
		generator.generateStructuresInChunk(world, new Random(world.getSeed() ^ (long) cx * 341873128712L ^ (long) cz * 132897987541L), cx, cz);
	}
	public static int[] candidate(long seed, int gx, int gz) {
		Random r = new Random(seed ^ (long) gx * 341873128712L ^ (long) gz * 132897987541L ^ 94251327L);
		return new int[]{gx * GRID + 20 + r.nextInt(8), gz * GRID + 20 + r.nextInt(8), r.nextInt(3) == 0 ? 1 : 0};
	}
	@Override protected boolean canSpawnStructureAtCoords(int cx, int cz) {
		int[] point = candidate(worldObj.getSeed(), Math.floorDiv(cx, GRID), Math.floorDiv(cz, GRID));
		if (point[0] != cx || point[1] != cz || !enabled(point[2] == 1)) return false;
		if (point[2] == 1 && worldObj.getWorldChunkManager().getBiomeGenAt(cx * 16 + 8, cz * 16 + 8).rootHeight < .4F) return false;
		long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(cx, cz);
		if (!candidates.containsKey(key)) {
			Start start = new Start(worldObj, cx, cz, point[2] == 1);
			if (start.getComponents().isEmpty() || !suitable(start) || NativeStructureReservations.underground(worldObj, start.getBoundingBox())) start = null;
			candidates.put(key, start);
		}
		return candidates.get(key) != null;
	}
	private static boolean enabled(boolean city) {
		if (city) return ConfigWorld.enableAncientCities && ModBlocks.SCULK.isEnabled() && ModBlocks.SCULK_CATALYST.isEnabled() && ModBlocks.SCULK_SENSOR.isEnabled() && ModBlocks.DEEPSLATE.isEnabled() && ModBlocks.DEEPSLATE_BRICKS.isEnabled();
		return ConfigWorld.enableTrialChambers && ModBlocks.COPPER_BLOCK.isEnabled() && ModBlocks.TUFF.isEnabled() && ModBlocks.COBBLED_DEEPSLATE.isEnabled();
	}
	@Override protected StructureStart getStructureStart(int x, int z) { return candidates.get(net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(x, z)); }
	private boolean suitable(Start start) {
		StructureBoundingBox box = start.getBoundingBox();
		if (start.city && worldObj.getWorldChunkManager().getBiomeGenAt((box.minX + box.maxX) / 2, (box.minZ + box.maxZ) / 2).rootHeight < .4F) return false;
		if (terrain == null) terrain = new ChunkProviderGenerate(worldObj, worldObj.getSeed(), false);
		IChunkLoader loader = ((net.minecraft.world.gen.ChunkProviderServer) worldObj.getChunkProvider()).currentChunkLoader;
		Block[] blocks = new Block[65536];
		for (int cx = box.minX >> 4; cx <= box.maxX >> 4; cx++) for (int cz = box.minZ >> 4; cz <= box.maxZ >> 4; cz++) {
			// Refuse any pre-existing saved terrain, even if currently unloaded. This never loads a chunk.
			if (loader instanceof AnvilChunkLoader && ((AnvilChunkLoader) loader).chunkExists(worldObj, cx, cz)) return false;
			Arrays.fill(blocks, null); terrain.func_147424_a(cx, cz, blocks);
			for (int x = Math.max(box.minX, cx * 16); x <= Math.min(box.maxX, cx * 16 + 15); x++) for (int z = Math.max(box.minZ, cz * 16); z <= Math.min(box.maxZ, cz * 16 + 15); z++) {
				int roof = box.maxY + 8;
				Block block = blocks[((x & 15) * 16 + (z & 15)) * 256 + roof];
				if (block == null || !block.getMaterial().isSolid()) return false;
			}
		}
		return true;
	}
	@Override public boolean generateStructuresInChunk(World world, Random random, int cx, int cz) {
		hasStructureAt(cx * 16, 0, cz * 16);
		StructureBoundingBox clip = new StructureBoundingBox(cx * 16, 0, cz * 16, cx * 16 + 15, 255, cz * 16 + 15);
		MapGenStructureData saved = (MapGenStructureData) world.perWorldStorage.loadData(MapGenStructureData.class, func_143025_a()); boolean placed = false;
		for (StructureStart start : structureMap.values()) if (enabled(((Start) start).city) && start.getBoundingBox().intersectsWith(clip)) {
			start.generateStructure(world, random, clip);
			saved.func_143043_a(start.func_143021_a(start.func_143019_e(), start.func_143018_f()), start.func_143019_e(), start.func_143018_f()); placed = true;
		}
		if (placed) saved.markDirty(); return placed;
	}
	private static class Element {
		final String name, processors; Element(String name, String processors) { this.name = name; this.processors = processors; }
	}
	private static class Pool {
		final List<Element> elements = new ArrayList<>(); String fallback = "minecraft:empty";
	}
	private static Pool pool(String id, Map<String, String> aliases) {
		id = aliases.getOrDefault(id, id);
		return POOLS.computeIfAbsent(id, key -> {
			Pool p = new Pool(); if (key.equals("minecraft:empty")) return p;
			try (InputStream in = UndergroundStructures.class.getResourceAsStream("/data/underground_pools/" + key.replace("minecraft:", "") + ".json")) {
				if (in == null) throw new IllegalArgumentException("Missing underground pool " + key);
				JsonObject object = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject(); p.fallback = object.get("fallback").getAsString();
				for (JsonElement value : object.getAsJsonArray("elements")) {
					JsonObject entry = value.getAsJsonObject(), e = entry.getAsJsonObject("element"); String type = e.get("element_type").getAsString();
					String name = type.equals("minecraft:single_pool_element") ? e.get("location").getAsString().replace("minecraft:", "") : type.equals("minecraft:feature_pool_element") ? "@sculk" : "";
					// The released city no_corners pool itself references an absent stairs_5 asset.
					if (!name.isEmpty() && !name.equals("@sculk") && UndergroundStructures.class.getResource("/data/structure/" + name + ".nbt") == null) {
						if (MISSING_TEMPLATES.add(name)) org.apache.logging.log4j.LogManager.getLogger("UAB underground structures").warn("Skipping unavailable released template: {}", name);
						continue;
					}
					for (int i = 0; i < entry.get("weight").getAsInt(); i++) p.elements.add(new Element(name, e.has("processors") && e.get("processors").isJsonPrimitive() ? e.get("processors").getAsString() : "minecraft:empty"));
				}
			} catch (IOException e) { throw new IllegalStateException(key, e); }
			return p;
		});
	}
	public static class Start extends StructureStart {
		private boolean city;
		private final Set<Long> placedChunks = new HashSet<>();
		public Start() { }
		Start(World world, int cx, int cz, boolean city) {
			super(cx, cz); this.city = city;
			Random random = new Random(world.getSeed() ^ (long) cx * 341873128712L ^ (long) cz * 132897987541L);
			Map<String, String> aliases = new HashMap<>();
			String ranged = new String[]{"skeleton", "stray", "poison_skeleton"}[random.nextInt(3)];
			for (String type : new String[]{"ranged", "slow_ranged"}) aliases.put("minecraft:trial_chambers/spawner/contents/" + type, "minecraft:trial_chambers/spawner/" + type + "/" + ranged);
			aliases.put("minecraft:trial_chambers/spawner/contents/melee", "minecraft:trial_chambers/spawner/melee/" + new String[]{"zombie", "husk", "spider"}[random.nextInt(3)]);
			aliases.put("minecraft:trial_chambers/spawner/contents/small_melee", "minecraft:trial_chambers/spawner/small_melee/" + new String[]{"slime", "cave_spider", "silverfish", "baby_zombie"}[random.nextInt(4)]);
			Pool starts = pool(city ? "minecraft:ancient_city/city_center" : "minecraft:trial_chambers/chamber/end", aliases);
			Piece root = new Piece(starts.elements.get(random.nextInt(starts.elements.size())).name, cx * 16 + 8, 24, cz * 16 + 8, random.nextInt(4), city);
			root.processors = starts.elements.get(0).processors;
			components.add(root); ArrayDeque<Pending> queue = new ArrayDeque<>(); queue.add(new Pending(root, 0));
			while (!queue.isEmpty()) {
				Pending next = queue.removeFirst(); Piece parent = next.piece;
				List<NBTTagCompound> connectors = new ArrayList<>(template(parent.name).connectors); Collections.shuffle(connectors, random);
				connectors.sort((a, b) -> Integer.compare(b.getCompoundTag("nbt").getInteger("selection_priority"), a.getCompoundTag("nbt").getInteger("selection_priority")));
				for (NBTTagCompound c : connectors) {
					if (c == parent.incoming) continue;
					String id = c.getCompoundTag("nbt").getString("pool"); if (id.equals("minecraft:empty")) continue;
					Pool p = pool(id, aliases); List<Element> choices = new ArrayList<>();
					if (next.depth < (city ? 7 : 20) && components.size() < 250) { choices.addAll(p.elements); Collections.shuffle(choices, random); }
					Pool fallback = pool(p.fallback, aliases); List<Element> end = new ArrayList<>(fallback.elements); Collections.shuffle(end, random); choices.addAll(end);
					Piece child = components.size() < 256 ? attach(parent, c, choices, components, root, random) : null;
					if (child != null) { child.parent = parent; components.add(child); if (!child.name.equals("@sculk")) queue.add(new Pending(child, next.depth + 1)); }
					else parent.seal(c);
				}
			}
			updateBoundingBox();
			if (boundingBox.maxY - boundingBox.minY > 40 || components.size() < (city ? 8 : 12)) { components.clear(); return; }
			int offset = 8 - boundingBox.minY;
			for (Object value : components) { Piece piece = (Piece) value; piece.y += offset; piece.boundingBox().offset(0, offset, 0); for (int[] cap : piece.caps) cap[1] += offset; }
			updateBoundingBox();
			boundingBox = new StructureBoundingBox(boundingBox.minX - 2, boundingBox.minY - 2, boundingBox.minZ - 2, boundingBox.maxX + 2, boundingBox.maxY + 2, boundingBox.maxZ + 2);
		}
		@Override public void generateStructure(World world, Random random, StructureBoundingBox clip) {
			long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(clip.minX >> 4, clip.minZ >> 4);
			if (placedChunks.contains(key)) return;
			prepare(world, clip);
			for (Object value : components) { Piece piece = (Piece) value; if (!piece.name.equals("@sculk") && piece.getBoundingBox().intersectsWith(clip)) piece.addComponentParts(world, random, clip); }
			for (Object value : components) { Piece piece = (Piece) value; if (piece.name.equals("@sculk") && piece.getBoundingBox().intersectsWith(clip)) piece.addComponentParts(world, random, clip); }
			if (city) surroundingSculk(world, clip);
			placedChunks.add(key);
		}
		private void surroundingSculk(World world, StructureBoundingBox clip) {
			int y = boundingBox.minY + 1;
			for (int x = Math.max(clip.minX, boundingBox.minX + 2); x <= Math.min(clip.maxX, boundingBox.maxX - 2); x++) for (int z = Math.max(clip.minZ, boundingBox.minZ + 2); z <= Math.min(clip.maxZ, boundingBox.maxZ - 2); z++) {
				if (!world.isAirBlock(x, y + 1, z) || !sculkTerrain(world, x, y, z)) continue;
				Random r = new Random(world.getSeed() ^ (long) x * 3129871 ^ (long) z * 116129781L); if (r.nextInt(4) == 0) continue;
				world.setBlock(x, y, z, ModBlocks.SCULK.get(), 0, 2); int kind = r.nextInt(512);
				if (kind < 3) world.setBlock(x, y + 1, z, kind == 0 ? ModBlocks.SCULK_SHRIEKER.get() : kind == 1 ? ModBlocks.SCULK_CATALYST.get() : ModBlocks.SCULK_SENSOR.get(), kind == 0 ? 8 : 0, 2);
			}
		}
		private void prepare(World world, StructureBoundingBox clip) {
			List<StructureBoundingBox> local = new ArrayList<>();
			for (Object value : components) { Piece p = (Piece) value; StructureBoundingBox b = p.getBoundingBox(); if (!p.name.equals("@sculk") && b.maxX + 2 >= clip.minX && b.minX - 2 <= clip.maxX && b.maxZ + 2 >= clip.minZ && b.minZ - 2 <= clip.maxZ) local.add(b); }
			for (int x = Math.max(clip.minX, boundingBox.minX); x <= Math.min(clip.maxX, boundingBox.maxX); x++) for (int z = Math.max(clip.minZ, boundingBox.minZ); z <= Math.min(clip.maxZ, boundingBox.maxZ); z++) for (int y = boundingBox.minY; y <= boundingBox.maxY; y++) {
				boolean interior = false, shell = false;
				if (city) { shell = true; interior = x > boundingBox.minX + 1 && x < boundingBox.maxX - 1 && z > boundingBox.minZ + 1 && z < boundingBox.maxZ - 1 && y > boundingBox.minY + 1 && y < boundingBox.maxY - 1; }
				else for (StructureBoundingBox b : local) {
					if (b.isVecInside(x, y, z)) { interior = true; break; }
					if (x >= b.minX - 2 && x <= b.maxX + 2 && z >= b.minZ - 2 && z <= b.maxZ + 2 && y >= b.minY - 2 && y <= b.maxY + 2) shell = true;
				}
				if (interior || shell) world.setBlock(x, y, z, interior ? Blocks.air : city ? ModBlocks.DEEPSLATE.get() : ModBlocks.TUFF.get(), 0, 2);
			}
		}
		@Override public void func_143022_a(NBTTagCompound tag) { super.func_143022_a(tag); tag.setBoolean("City", city); NBTTagList list = new NBTTagList(); for (long key : placedChunks) { NBTTagCompound entry = new NBTTagCompound(); entry.setLong("Chunk", key); list.appendTag(entry); } tag.setTag("PlacedChunks", list); }
		@Override public void func_143017_b(NBTTagCompound tag) { super.func_143017_b(tag); city = tag.getBoolean("City"); placedChunks.clear(); NBTTagList list = tag.getTagList("PlacedChunks", 10); for (int i = 0; i < list.tagCount(); i++) placedChunks.add(list.getCompoundTagAt(i).getLong("Chunk")); }
	}
	private static class Pending { final Piece piece; final int depth; Pending(Piece p, int d) { piece = p; depth = d; } }
	private static Piece attach(Piece parent, NBTTagCompound connector, List<Element> choices, List<StructureComponent> pieces, Piece root, Random random) {
		UndergroundTemplate pt = template(parent.name); BlockPos p = UndergroundTemplate.position(connector, parent.rotation), facing = pt.direction(connector, parent.rotation, false);
		String target = connector.getCompoundTag("nbt").getString("target");
		for (Element e : choices) {
			if (e.name.isEmpty()) return null;
			if (e.name.equals("@sculk")) return new Piece(e.name, parent.x + p.getX() - 4, parent.y + p.getY() - 1, parent.z + p.getZ() - 4, 0, true);
			UndergroundTemplate t = template(e.name); int first = random.nextInt(4);
			for (int i = 0; i < 4; i++) { int r = (first + i) & 3;
				for (NBTTagCompound c : t.connectors) {
					if (!target.equals(c.getCompoundTag("nbt").getString("name"))) continue;
					BlockPos cf = t.direction(c, r, false); if (cf.getX() != -facing.getX() || cf.getY() != -facing.getY() || cf.getZ() != -facing.getZ()) continue;
					if (connector.getCompoundTag("nbt").getString("joint").equals("aligned")) { BlockPos a = pt.direction(connector, parent.rotation, true), b = t.direction(c, r, true); if (!a.equals(b)) continue; }
					BlockPos cp = UndergroundTemplate.position(c, r); Piece child = new Piece(e.name, parent.x + p.getX() + facing.getX() - cp.getX(), parent.y + p.getY() + facing.getY() - cp.getY(), parent.z + p.getZ() + facing.getZ() - cp.getZ(), r, parent.city);
					StructureBoundingBox box = child.getBoundingBox();
					if (box.minX < root.x - RADIUS || box.maxX > root.x + RADIUS || box.minZ < root.z - RADIUS || box.maxZ > root.z + RADIUS || box.minY < root.y - 8 || box.maxY > root.y + 32) continue;
					boolean collision = false;
					for (StructureComponent existing : pieces) if (!((Piece) existing).name.equals("@sculk") && box.intersectsWith(existing.getBoundingBox())) {
						boolean inside = false; for (Piece ancestor = parent; ancestor != null; ancestor = ancestor.parent) if (ancestor == existing && contains(ancestor.getBoundingBox(), box)) { inside = true; break; }
						if (!inside) { collision = true; break; }
					}
					if (!collision) { child.processors = e.processors; child.incoming = c; return child; }
				}
			}
		}
		return null;
	}
	private static boolean contains(StructureBoundingBox a, StructureBoundingBox b) { return b.minX >= a.minX && b.maxX <= a.maxX && b.minY >= a.minY && b.maxY <= a.maxY && b.minZ >= a.minZ && b.maxZ <= a.maxZ; }
	private static boolean sculkTerrain(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block.hasTileEntity(world.getBlockMetadata(x, y, z))) return false;
		return block == Blocks.stone || block == ModBlocks.DEEPSLATE.get() || block == Blocks.dirt || block == Blocks.grass || block == Blocks.mycelium || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.soul_sand || block == ModBlocks.SOUL_SOIL.get() || block == ModBlocks.TUFF.get() && world.getBlockMetadata(x, y, z) == 0;
	}
	public static class Piece extends StructureComponent {
		private String name, processors = "minecraft:empty"; private int x, y, z, rotation; private boolean city; private Piece parent; private NBTTagCompound incoming;
		private final Set<Long> placed = new HashSet<>(); private final List<int[]> caps = new ArrayList<>();
		public Piece() { }
		Piece(String name, int x, int y, int z, int r, boolean city) { this.name = name; this.x = x; this.y = y; this.z = z; rotation = r; this.city = city; coordBaseMode = -1; boundingBox = name.equals("@sculk") ? new StructureBoundingBox(x, y, z, x + 8, y + 4, z + 8) : template(name).box(x, y, z, r); }
		StructureBoundingBox boundingBox() { return boundingBox; }
		void seal(NBTTagCompound connector) {
			BlockPos p = UndergroundTemplate.position(connector, rotation), f = template(name).direction(connector, rotation, false);
			if (f.getY() != 0 || boundingBox.isVecInside(x + p.getX() + f.getX(), y + p.getY(), z + p.getZ() + f.getZ())) return;
			caps.add(new int[]{x + p.getX(), y + p.getY(), z + p.getZ(), f.getX(), f.getZ()});
		}
		@Override protected void func_143012_a(NBTTagCompound tag) {
			tag.setString("Template", name); tag.setString("Processors", processors); tag.setInteger("X", x); tag.setInteger("Y", y); tag.setInteger("Z", z); tag.setInteger("Rotation", rotation); tag.setBoolean("City", city);
			NBTTagList done = new NBTTagList(), closed = new NBTTagList(); for (long key : placed) { NBTTagCompound c = new NBTTagCompound(); c.setLong("Chunk", key); done.appendTag(c); } for (int[] cap : caps) { NBTTagCompound c = new NBTTagCompound(); c.setIntArray("Cap", cap); closed.appendTag(c); } tag.setTag("Placed", done); tag.setTag("Caps", closed);
		}
		@Override protected void func_143011_b(NBTTagCompound tag) {
			name = tag.getString("Template"); processors = tag.getString("Processors"); x = tag.getInteger("X"); y = tag.getInteger("Y"); z = tag.getInteger("Z"); rotation = tag.getInteger("Rotation") & 3; city = tag.getBoolean("City"); placed.clear(); caps.clear();
			NBTTagList done = tag.getTagList("Placed", 10), closed = tag.getTagList("Caps", 10); for (int i = 0; i < done.tagCount(); i++) placed.add(done.getCompoundTagAt(i).getLong("Chunk")); for (int i = 0; i < closed.tagCount(); i++) { int[] cap = closed.getCompoundTagAt(i).getIntArray("Cap"); if (cap.length == 5) caps.add(cap); }
		}
		@Override public boolean addComponentParts(World world, Random random, StructureBoundingBox clip) {
			long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(clip.minX >> 4, clip.minZ >> 4); if (placed.contains(key)) return true;
			if (name.equals("@sculk")) { sculk(world, clip); placed.add(key); return true; }
			Block shell = city ? ModBlocks.DEEPSLATE.get() : ModBlocks.TUFF.get();
			template(name).place(world, x, y, z, rotation, clip, world.getSeed(), processors);
			for (int[] cap : caps) for (int w = -1; w <= 1; w++) for (int h = 0; h < 3; h++) { int wx = cap[0] + cap[4] * w, wy = cap[1] + h, wz = cap[2] + cap[3] * w; if (clip.isVecInside(wx, wy, wz) && boundingBox.isVecInside(wx, wy, wz)) world.setBlock(wx, wy, wz, shell, 0, 2); }
			placed.add(key); return true;
		}
		private void sculk(World world, StructureBoundingBox clip) {
			for (int wx = Math.max(clip.minX, x); wx <= Math.min(clip.maxX, x + 8); wx++) for (int wz = Math.max(clip.minZ, z); wz <= Math.min(clip.maxZ, z + 8); wz++) {
				Random r = new Random(world.getSeed() ^ (long) wx * 3129871 ^ (long) wz * 116129781L);
				if (r.nextInt(4) == 0) continue;
				for (int wy = y + 3; wy >= y; wy--) if (world.isAirBlock(wx, wy + 1, wz) && sculkTerrain(world, wx, wy, wz)) {
					world.setBlock(wx, wy, wz, ModBlocks.SCULK.get(), 0, 2);
					int kind = r.nextInt(40); if (kind < 3) world.setBlock(wx, wy + 1, wz, kind == 0 ? ModBlocks.SCULK_SHRIEKER.get() : kind == 1 ? ModBlocks.SCULK_CATALYST.get() : ModBlocks.SCULK_SENSOR.get(), kind == 0 ? 8 : 0, 2);
					break;
				}
			}
		}
	}
}
