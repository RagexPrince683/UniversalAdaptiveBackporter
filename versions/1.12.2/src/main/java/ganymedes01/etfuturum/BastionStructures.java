package ganymedes01.etfuturum;

import com.google.gson.*;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded, deterministic jigsaw assembly; native MapGenStructure owns chunk placement and saved starts. */
public class BastionStructures extends NativeStructureGenerator {
	private static final Map<String, List<PoolElement>> POOLS = new HashMap<>();
	private static class PoolElement {
		final String name, processors;
		PoolElement(String name, String processors) { this.name = name; this.processors = processors; }
	}
	BastionStructures(){range=12;}
    private static final String[] STARTS = {"bastion/units/air_base", "bastion/hoglin_stable/air_base", "bastion/treasure/big_air_full", "bastion/bridge/starting_pieces/entrance_base"};
	static StructureTemplate template(String name) { return StructureTemplate.get(name); }
	@Override public String getStructureName() { return "EFRBastion"; }
	@Override protected boolean canSpawnStructureAtCoords(int x, int z) {
		if (!Features.enabled("world.enableBastions") || !Content.available("blackstone") || !Content.available("basalt") || !Content.available("smooth_quartz") || !world.getWorldInfo().isMapFeaturesEnabled()) return false;
		int spacing = BastionStructures.spacing;
		int gx = Math.floorDiv(x, spacing), gz = Math.floorDiv(z, spacing);
		Random random = new Random(gx * 341873128712L + gz * 132897987541L + world.getSeed() + 30084232L);
		if (x != gx * spacing + random.nextInt(spacing - 4) || z != gz * spacing + random.nextInt(spacing - 4)) return false;
		if (NetherBiomes.kind(world.getBiomeProvider().getBiome(new BlockPos(x * 16 + 8, 64, z * 16 + 8))) == NetherBiomes.Kind.BASALT) return false;
		int radius = (256 + spacing * 16 - 1) / (spacing * 16) + 1;
		for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
			int nx = gx + dx, nz = gz + dz;
			if (nx > gx || nx == gx && nz >= gz) continue;
			Random other = new Random(nx * 341873128712L + nz * 132897987541L + world.getSeed() + 30084232L);
			int ox = nx * spacing + other.nextInt(spacing - 4), oz = nz * spacing + other.nextInt(spacing - 4);
			if (Math.abs(ox - x) * 16 < 256 && Math.abs(oz - z) * 16 < 256) return false;
		}
		return admit(new Start(world, x, z));
	}
	@Override protected StructureStart getStructureStart(int x, int z) { return admitted.remove(net.minecraft.util.math.ChunkPos.asLong(x,z)); }
	BlockPos nearest(World world,BlockPos origin){this.world=world;initializeStructureData(world);BlockPos result=super.getNearestStructurePos(world,origin,false);double distance=result==null?Double.MAX_VALUE:result.distanceSq(origin);int gx=Math.floorDiv(origin.getX()>>4,spacing),gz=Math.floorDiv(origin.getZ()>>4,spacing);for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){int rx=gx+dx,rz=gz+dz;Random random=new Random(rx*341873128712L+rz*132897987541L+world.getSeed()+30084232L);int cx=rx*spacing+random.nextInt(spacing-4),cz=rz*spacing+random.nextInt(spacing-4);if(!canSpawnStructureAtCoords(cx,cz))continue;BlockPos at=new BlockPos(cx*16+8,33,cz*16+8);double d=at.distanceSq(origin);if(d<distance){distance=d;result=at;}admitted.remove(net.minecraft.util.math.ChunkPos.asLong(cx,cz));}return result;}
    static int spacing;
    private final Map<Long,Start> admitted = new HashMap<>();
    private boolean admit(Start start) { if(start.getComponents().isEmpty() || !StructureTerrain.fresh(world,start.getBoundingBox()) || NativeStructureReservations.fortress(world,start.getBoundingBox()))return false;admitted.put(net.minecraft.util.math.ChunkPos.asLong(start.getChunkPosX(),start.getChunkPosZ()),start);return true; }
    @Override protected boolean enabled(StructureStart start){return Features.enabled("world.enableBastions")&&Features.enabled("experiments.netherDimensionProvider")&&Content.available("blackstone")&&Content.available("basalt")&&Content.available("smooth_quartz");}

	private static List<PoolElement> pool(String id) {
		return POOLS.computeIfAbsent(id, key -> {
			List<PoolElement> names = new ArrayList<>();
			if (key.equals("minecraft:empty")) return names;
			try (InputStream stream = BastionStructures.class.getResourceAsStream("/data/bastion_pools/" + key.replace("minecraft:bastion/", "") + ".json")) {
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
        private final Set<Long> placedChunks=new HashSet<>();
        @Override public void generateStructure(World world,Random random,StructureBoundingBox clip){long key=net.minecraft.util.math.ChunkPos.asLong(clip.minX>>4,clip.minZ>>4);if(placedChunks.contains(key))return;super.generateStructure(world,random,clip);placedChunks.add(key);}
        @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList list=new NBTTagList();for(long key:placedChunks){NBTTagCompound entry=new NBTTagCompound();entry.setLong("Chunk",key);list.appendTag(entry);}tag.setTag("PlacedChunks",list);}
        @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);placedChunks.clear();NBTTagList list=tag.getTagList("PlacedChunks",10);for(int i=0;i<list.tagCount();i++)placedChunks.add(list.getCompoundTagAt(i).getLong("Chunk"));}
		public Start() { }
		public Start(World world, int cx, int cz) {
			super(cx, cz);
			Random random = new Random(world.getSeed() ^ cx * 341873128712L ^ cz * 132897987541L);
			Piece root = new Piece(STARTS[random.nextInt(STARTS.length)], cx * 16 + 8, 33, cz * 16 + 8, random.nextInt(4));
			if(!template(root.name).available())return; components.add(root);
			ArrayDeque<Pending> pending = new ArrayDeque<>();
			pending.add(new Pending(root, 0));
			while (!pending.isEmpty() && components.size() < 192) {
				Pending next = pending.removeFirst();
				if (next.depth >= 7) continue;
				StructureTemplate parent = template(next.piece.name);
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
		StructureTemplate parent = template(parentPiece.name);
		BlockPos p = StructureTemplate.position(connector, parentPiece.rotation), facing = parent.front(connector, parentPiece.rotation);
		String target = connector.getCompoundTag("nbt").getString("target");
		for (PoolElement element : choices) {
			String choice = element.name;
			if (choice.isEmpty()) return null;
			StructureTemplate childTemplate = template(choice);if(!childTemplate.available())continue;
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
					BlockPos c = StructureTemplate.position(childConnector, rotation);
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
			setCoordBaseMode(null);
			boundingBox = template(name).box(x, y, z, rotation);
		}
		@Override protected void writeStructureToNBT(NBTTagCompound tag) {
			tag.setString("Template", name); tag.setInteger("X", x); tag.setInteger("Y", y); tag.setInteger("Z", z); tag.setInteger("Rotation", rotation);
			tag.setString("Processors", processors);
		}
		@Override protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
			name = tag.getString("Template"); x = tag.getInteger("X"); y = tag.getInteger("Y"); z = tag.getInteger("Z"); rotation = tag.getInteger("Rotation") & 3;
			processors = tag.hasKey("Processors") ? tag.getString("Processors") : "minecraft:bastion_generic_degradation";
		}
		@Override public boolean addComponentParts(World world, Random random, StructureBoundingBox clip) {
			template(name).place(world, x, y, z, rotation, clip, world.getSeed(), processors);
			return true;
		}
	}
}
