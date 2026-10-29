package ganymedes01.etfuturum.world.nether.structure;

import com.google.gson.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.world.nether.biome.utils.NetherBiomeManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded, deterministic jigsaw assembly; native MapGenStructure owns chunk placement and saved starts. */
public class MapGenBastion extends MapGenStructure {
	private static final Map<World, MapGenBastion> EXTERNAL = new IdentityHashMap<>();
	private boolean external;
	public static void unload(World world) { EXTERNAL.remove(world); }
	/** Forge population supplies terrain owned by vanilla or an optional Nether mod. */
	public static void populateExternal(World world, net.minecraft.world.chunk.IChunkProvider provider, int cx, int cz) {
		if (world.isRemote || world.provider.dimensionId != -1 || provider instanceof ganymedes01.etfuturum.world.nether.dimension.NetherChunkProvider
				|| !ConfigWorld.enableBastions || !world.getWorldInfo().isMapFeaturesEnabled() || savedChunk(world, cx, cz)) return;
		MapGenBastion generator = EXTERNAL.computeIfAbsent(world, key -> { MapGenBastion gen = new MapGenBastion(); gen.external = true; return gen; });
		generator.func_151539_a(provider, world, cx, cz, null);
		generator.hasStructureAt(cx * 16, 0, cz * 16);
		MapGenStructureData data = (MapGenStructureData) world.perWorldStorage.loadData(MapGenStructureData.class, "EFRBastion");
		StructureBoundingBox clip = new StructureBoundingBox(cx * 16, 0, cz * 16, cx * 16 + 15, 127, cz * 16 + 15);
		Random random = new Random(world.getSeed() ^ cx * 341873128712L ^ cz * 132897987541L);
		for (StructureStart value : generator.structureMap.values()) if (value.getBoundingBox().intersectsWith(clip)) {
			Start start = (Start) value; long key = net.minecraft.world.ChunkCoordIntPair.chunkXZ2Int(cx, cz);
			if (start.externalChunks.add(key)) {
				start.generateStructure(world, random, clip);
				data.func_143043_a(start.func_143021_a(start.func_143019_e(), start.func_143018_f()), start.func_143019_e(), start.func_143018_f());
				data.markDirty();
			}
		}
	}
	private static boolean savedChunk(World world, int x, int z) {
		net.minecraft.world.chunk.IChunkProvider provider = world.getChunkProvider();
		if (!(provider instanceof net.minecraft.world.gen.ChunkProviderServer)) return true;
		Object loader = ((net.minecraft.world.gen.ChunkProviderServer) provider).currentChunkLoader;
		return !(loader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader)
				|| ((net.minecraft.world.chunk.storage.AnvilChunkLoader) loader).chunkExists(world, x, z);
	}
	private static final Map<String, BastionTemplate> TEMPLATES = new HashMap<>();
	private static final Map<String, List<PoolElement>> POOLS = new HashMap<>();
	private static class PoolElement {
		final String name, processors;
		PoolElement(String name, String processors) { this.name = name; this.processors = processors; }
	}
	private static final String[] STARTS = {"bastion/units/air_base", "bastion/hoglin_stable/air_base", "bastion/treasure/big_air_full", "bastion/bridge/starting_pieces/entrance_base"};
	static BastionTemplate template(String name) { return TEMPLATES.computeIfAbsent(name, BastionTemplate::new); }
	@Override public String func_143025_a() { return "EFRBastion"; }
	@Override protected boolean canSpawnStructureAtCoords(int x, int z) {
		if (external && savedChunk(worldObj, x, z)) return false;
		if (!ConfigWorld.enableBastions || !ModBlocks.BLACKSTONE.isEnabled() || !ModBlocks.BASALT.isEnabled() || !ModBlocks.SMOOTH_QUARTZ.isEnabled() || !worldObj.getWorldInfo().isMapFeaturesEnabled()) return false;
		int spacing = ConfigWorld.bastionSpacing;
		int gx = Math.floorDiv(x, spacing), gz = Math.floorDiv(z, spacing);
		Random random = new Random(gx * 341873128712L + gz * 132897987541L + worldObj.getSeed() + 30084232L);
		if (x != gx * spacing + random.nextInt(spacing - 4) || z != gz * spacing + random.nextInt(spacing - 4)) return false;
		if (worldObj.getWorldChunkManager().getBiomeGenAt(x * 16 + 8, z * 16 + 8) == NetherBiomeManager.basaltDeltas) return false;
		int radius = (256 + spacing * 16 - 1) / (spacing * 16) + 1;
		for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
			int nx = gx + dx, nz = gz + dz;
			if (nx > gx || nx == gx && nz >= gz) continue;
			Random other = new Random(nx * 341873128712L + nz * 132897987541L + worldObj.getSeed() + 30084232L);
			int ox = nx * spacing + other.nextInt(spacing - 4), oz = nz * spacing + other.nextInt(spacing - 4);
			if (Math.abs(ox - x) * 16 < 256 && Math.abs(oz - z) * 16 < 256) return false;
		}
		return !ganymedes01.etfuturum.world.structure.NativeStructureReservations.fortress(worldObj, new Start(worldObj, x, z).getBoundingBox());
	}
	@Override protected StructureStart getStructureStart(int x, int z) { return new Start(worldObj, x, z); }
	/** Reuses native placement and fortress-reservation checks without requesting chunks. */
	public static net.minecraft.util.ChunkCoordinates nearestCandidate(World world, int x, int z) {
		net.minecraft.world.chunk.IChunkProvider provider = world.getChunkProvider();
		if (provider instanceof net.minecraft.world.gen.ChunkProviderServer) provider = ((net.minecraft.world.gen.ChunkProviderServer) provider).currentChunkProvider;
		if (!(provider instanceof ganymedes01.etfuturum.world.nether.dimension.NetherChunkProvider)) return null;
		MapGenBastion generator = new MapGenBastion(); generator.worldObj = world;
		int spacing = ConfigWorld.bastionSpacing;
		int gx = Math.floorDiv(x >> 4, spacing), gz = Math.floorDiv(z >> 4, spacing);
		double best = Double.MAX_VALUE; net.minecraft.util.ChunkCoordinates result = null;
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int rx = gx + dx, rz = gz + dz;
			Random random = new Random(rx * 341873128712L + rz * 132897987541L + world.getSeed() + 30084232L);
			int cx = rx * spacing + random.nextInt(spacing - 4), cz = rz * spacing + random.nextInt(spacing - 4);
			if (!generator.canSpawnStructureAtCoords(cx, cz)) continue;
			int px = cx * 16 + 8, pz = cz * 16 + 8;
			double distance = ((double) px - x) * ((double) px - x) + ((double) pz - z) * ((double) pz - z);
			if (distance < best) { best = distance; result = new net.minecraft.util.ChunkCoordinates(px, 33, pz); }
		}
		return result;
	}

	private static List<PoolElement> pool(String id) {
		return POOLS.computeIfAbsent(id, key -> {
			List<PoolElement> names = new ArrayList<>();
			if (key.equals("minecraft:empty")) return names;
			try (InputStream stream = MapGenBastion.class.getResourceAsStream("/data/bastion_pools/" + key.replace("minecraft:bastion/", "") + ".json")) {
				if (stream == null) throw new IllegalStateException("Missing bastion pool " + key);
				JsonArray elements = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("elements");
				for (JsonElement value : elements) {
					JsonObject entry = value.getAsJsonObject(), element = entry.getAsJsonObject("element");
					String name = element.has("location") ? element.get("location").getAsString().replace("minecraft:", "") : "";
					String processors = "minecraft:empty";
					if (element.has("processors")) {
						JsonElement configured = element.get("processors");
						if (configured.isJsonPrimitive()) processors = configured.getAsString();
						else if (!configured.isJsonObject() || configured.getAsJsonObject().getAsJsonArray("processors").size() != 0)
							throw new IllegalStateException("Unsupported inline bastion processors in " + key);
					}
					for (int i = 0; i < entry.get("weight").getAsInt(); i++) names.add(new PoolElement(name, processors));
				}
			} catch (Exception e) { throw new IllegalStateException("Cannot load bastion pool " + key, e); }
			return names;
		});
	}

	public static class Start extends StructureStart {
		private final Set<Long> externalChunks = new HashSet<>();
		@Override public void func_143022_a(NBTTagCompound tag) { super.func_143022_a(tag); net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList(); for (long key : externalChunks) { NBTTagCompound entry = new NBTTagCompound(); entry.setLong("Chunk", key); list.appendTag(entry); } tag.setTag("UABExternalChunks", list); }
		@Override public void func_143017_b(NBTTagCompound tag) { super.func_143017_b(tag); externalChunks.clear(); net.minecraft.nbt.NBTTagList list = tag.getTagList("UABExternalChunks", 10); for (int i = 0; i < list.tagCount(); i++) externalChunks.add(list.getCompoundTagAt(i).getLong("Chunk")); }
		public Start() { }
		public Start(World world, int cx, int cz) {
			super(cx, cz);
			Random random = new Random(world.getSeed() ^ cx * 341873128712L ^ cz * 132897987541L);
			Piece root = new Piece(STARTS[random.nextInt(STARTS.length)], cx * 16 + 8, 33, cz * 16 + 8, random.nextInt(4));
			components.add(root);
			ArrayDeque<Pending> pending = new ArrayDeque<>();
			pending.add(new Pending(root, 0));
			while (!pending.isEmpty() && components.size() < 192) {
				Pending next = pending.removeFirst();
				if (next.depth >= 7) continue;
				BastionTemplate parent = template(next.piece.name);
				List<NBTTagCompound> connectors = new ArrayList<>(parent.connectors);
				Collections.shuffle(connectors, random);
				for (NBTTagCompound connector : connectors) {
					if (components.size() >= 192) break;
					List<PoolElement> choices = new ArrayList<>(pool(connector.getCompoundTag("nbt").getString("pool")));
					Collections.shuffle(choices, random);
					Piece child = attach(next.piece, connector, choices, random, components, root);
					if (child != null) { child.parent = next.piece; components.add(child); pending.addLast(new Pending(child, next.depth + 1)); }
				}
			}
			updateBoundingBox();
		}
	}
	private static class Pending {
		final Piece piece; final int depth;
		Pending(Piece piece, int depth) { this.piece = piece; this.depth = depth; }
	}
	private static Piece attach(Piece parentPiece, NBTTagCompound connector, List<PoolElement> choices, Random random, List<StructureComponent> pieces, Piece root) {
		BastionTemplate parent = template(parentPiece.name);
		BlockPos p = BastionTemplate.position(connector, parentPiece.rotation), facing = parent.front(connector, parentPiece.rotation);
		String target = connector.getCompoundTag("nbt").getString("target");
		for (PoolElement element : choices) {
			String choice = element.name;
			if (choice.isEmpty()) return null;
			BastionTemplate childTemplate = template(choice);
			int initial = random.nextInt(4);
			for (int i = 0; i < 4; i++) {
				int rotation = (initial + i) & 3;
				for (NBTTagCompound childConnector : childTemplate.connectors) {
					if (!target.equals(childConnector.getCompoundTag("nbt").getString("name"))) continue;
					BlockPos cf = childTemplate.front(childConnector, rotation);
					if (cf.getX() != -facing.getX() || cf.getY() != -facing.getY() || cf.getZ() != -facing.getZ()) continue;
					if (connector.getCompoundTag("nbt").getString("joint").equals("aligned")) {
						BlockPos pt = parent.top(connector, parentPiece.rotation), ct = childTemplate.top(childConnector, rotation);
						if (pt.getX() != ct.getX() || pt.getY() != ct.getY() || pt.getZ() != ct.getZ()) continue;
					}
					BlockPos c = BastionTemplate.position(childConnector, rotation);
					Piece result = new Piece(choice, parentPiece.x + p.getX() + facing.getX() - c.getX(), parentPiece.y + p.getY() + facing.getY() - c.getY(), parentPiece.z + p.getZ() + facing.getZ() - c.getZ(), rotation);
					result.processors = element.processors;
					StructureBoundingBox box = result.getBoundingBox();
					if (box.minY < 5 || box.maxY > 122 || Math.abs(result.x - root.x) > 80 || Math.abs(result.z - root.z) > 80) continue;
					boolean collision = false;
					for (StructureComponent existing : pieces) {
						if (!box.intersectsWith(existing.getBoundingBox())) continue;
						boolean ancestor = false;
						for (Piece a = parentPiece; a != null; a = a.parent) if (a == existing && contains(a.getBoundingBox(), box)) { ancestor = true; break; }
						if (!ancestor) { collision = true; break; }
					}
					if (!collision) return result;
				}
			}
		}
		return null;
	}
	private static boolean contains(StructureBoundingBox a, StructureBoundingBox b) {
		return b.minX >= a.minX && b.maxX <= a.maxX && b.minY >= a.minY && b.maxY <= a.maxY && b.minZ >= a.minZ && b.maxZ <= a.maxZ;
	}
	public static class Piece extends StructureComponent {
		String name, processors = "minecraft:bastion_generic_degradation"; int x, y, z, rotation; Piece parent;
		public Piece() { }
		Piece(String name, int x, int y, int z, int rotation) {
			this.name = name; this.x = x; this.y = y; this.z = z; this.rotation = rotation;
			coordBaseMode = -1;
			boundingBox = template(name).box(x, y, z, rotation);
		}
		@Override protected void func_143012_a(NBTTagCompound tag) {
			tag.setString("Template", name); tag.setInteger("X", x); tag.setInteger("Y", y); tag.setInteger("Z", z); tag.setInteger("Rotation", rotation);
			tag.setString("Processors", processors);
		}
		@Override protected void func_143011_b(NBTTagCompound tag) {
			name = tag.getString("Template"); x = tag.getInteger("X"); y = tag.getInteger("Y"); z = tag.getInteger("Z"); rotation = tag.getInteger("Rotation") & 3;
			processors = tag.hasKey("Processors") ? tag.getString("Processors") : "minecraft:bastion_generic_degradation";
		}
		@Override public boolean addComponentParts(World world, Random random, StructureBoundingBox clip) {
			template(name).place(world, random, x, y, z, rotation, processors, clip);
			return true;
		}
	}
}
