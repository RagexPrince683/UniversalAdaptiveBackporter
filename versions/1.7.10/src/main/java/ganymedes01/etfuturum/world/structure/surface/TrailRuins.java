package ganymedes01.etfuturum.world.structure.surface;

import com.google.gson.*;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded released jigsaw topology. Roads, houses and the tower share one saved assembly. */
final class TrailRuins {
	private static final Map<String, Pool> POOLS = new HashMap<>();
	private static final class Element { final String name, processors; Element(String n, String p) { name = n; processors = p; } }
	private static final class Pool { final List<Element> elements = new ArrayList<>(); String fallback; }
	private static Pool pool(String id) {
		return POOLS.computeIfAbsent(id, key -> {
			Pool pool = new Pool(); pool.fallback = "minecraft:empty"; if (key.equals("minecraft:empty")) return pool;
			try (InputStream stream = TrailRuins.class.getResourceAsStream("/data/archaeology_pools/" + key.replace("minecraft:trail_ruins/", "") + ".json")) {
				if (stream == null) throw new IllegalArgumentException("Missing trail ruins pool " + key);
				JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject(); pool.fallback = json.get("fallback").getAsString();
				for (JsonElement value : json.getAsJsonArray("elements")) { JsonObject e = value.getAsJsonObject(), item = e.getAsJsonObject("element"); String name = item.has("location") ? item.get("location").getAsString().replace("minecraft:", "") : "";
					for (int i = 0; i < e.get("weight").getAsInt(); i++) pool.elements.add(new Element(name, item.has("processors") && item.get("processors").isJsonPrimitive() ? item.get("processors").getAsString() : ""));
				}
			} catch (IOException e) { throw new IllegalStateException(key, e); } return pool;
		});
	}
	static void generate(Random random, int x, int y, int z, List<SurfaceStructures.Piece> result) {
		Pool pool = pool("minecraft:trail_ruins/tower"); Element first = pool.elements.get(random.nextInt(pool.elements.size()));
		SurfaceStructures.Piece root = new SurfaceStructures.Piece(first.name, x, y - 15, z, random.nextInt(4), 0, 1); root.processors = first.processors;
		result.add(root); ArrayDeque<Pending> queue = new ArrayDeque<>(); queue.add(new Pending(root, 0, null));
		while (!queue.isEmpty()) {
			Pending pending = queue.removeFirst(); SurfaceStructures.Piece parent = pending.piece;
			List<NBTTagCompound> connectors = new ArrayList<>(SurfaceStructures.template(parent.name).connectors); Collections.shuffle(connectors, random);
			for (NBTTagCompound connector : connectors) {
				if (connector == pending.incoming) continue;
				Pool choices = pool(connector.getCompoundTag("nbt").getString("pool")); List<Element> elements = new ArrayList<>();
				if (pending.depth < 7 && result.size() < 120) { elements.addAll(choices.elements); Collections.shuffle(elements, random); }
				List<Element> fallback = new ArrayList<>(pool(choices.fallback).elements); Collections.shuffle(fallback, random); elements.addAll(fallback);
				Pending child = attach(parent, connector, elements, result, root, random, pending.depth + 1);
				if (child != null) { result.add(child.piece); queue.add(child); }
			}
		}
	}
	private static Pending attach(SurfaceStructures.Piece parent, NBTTagCompound connector, List<Element> choices, List<SurfaceStructures.Piece> pieces, SurfaceStructures.Piece root, Random random, int depth) {
		SurfaceTemplate t = SurfaceStructures.template(parent.name); BlockPos pos = SurfaceTemplate.position(connector, parent.rotation, 0), dir = t.direction(connector, parent.rotation, false);
		String target = connector.getCompoundTag("nbt").getString("target");
		for (Element e : choices) {
			if (e.name.isEmpty()) return null; SurfaceTemplate next = SurfaceStructures.template(e.name); int start = random.nextInt(4);
			for (int i = 0; i < 4; i++) { int r = (start + i) & 3;
				for (NBTTagCompound c : next.connectors) {
					if (!target.equals(c.getCompoundTag("nbt").getString("name"))) continue;
					BlockPos other = next.direction(c, r, false); if (other.getX() != -dir.getX() || other.getY() != -dir.getY() || other.getZ() != -dir.getZ()) continue;
					if (connector.getCompoundTag("nbt").getString("joint").equals("aligned") && !t.direction(connector, parent.rotation, true).equals(next.direction(c, r, true))) continue;
					BlockPos local = SurfaceTemplate.position(c, r, 0);
					SurfaceStructures.Piece child = new SurfaceStructures.Piece(e.name, parent.x + pos.getX() + dir.getX() - local.getX(), parent.y + pos.getY() + dir.getY() - local.getY(), parent.z + pos.getZ() + dir.getZ() - local.getZ(), r, 0, 1); child.processors = e.processors;
					StructureBoundingBox box = child.getBoundingBox(); if (box.minX < root.x - 80 || box.maxX > root.x + 80 || box.minZ < root.z - 80 || box.maxZ > root.z + 80 || box.minY < 8 || box.maxY > root.y + 48) continue;
					boolean collision = false; for (SurfaceStructures.Piece existing : pieces) if (box.intersectsWith(existing.getBoundingBox())) { collision = true; break; }
					if (!collision) return new Pending(child, depth, c);
				}
			}
		} return null;
	}
	private static final class Pending { final SurfaceStructures.Piece piece; final int depth; final NBTTagCompound incoming; Pending(SurfaceStructures.Piece p, int d, NBTTagCompound c) { piece = p; depth = d; incoming = c; } }
}
