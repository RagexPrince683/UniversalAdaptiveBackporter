package ganymedes01.etfuturum.world.structure.underground;

import com.google.gson.*;
import ganymedes01.etfuturum.core.utils.structurenbt.BlockStateContainer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released processor lists; all random decisions derive from block coordinates, independent of population order. */
final class UndergroundProcessors {
	private static final Set<String> ROTTABLE = new HashSet<>(Arrays.asList("deepslate", "deepslate_bricks", "deepslate_tiles", "deepslate_brick_slab", "deepslate_tile_slab", "deepslate_brick_stairs", "deepslate_tile_wall", "deepslate_brick_wall", "cobbled_deepslate", "cracked_deepslate_bricks", "cracked_deepslate_tiles", "gray_wool"));
	private static final Map<String, JsonArray> CACHE = new HashMap<>();
	private static JsonArray list(String name) {
		return CACHE.computeIfAbsent(name, id -> {
			if (id.isEmpty() || id.equals("minecraft:empty")) return new JsonArray();
			try (InputStream in = UndergroundProcessors.class.getResourceAsStream("/data/underground_processors/" + id.replace("minecraft:", "") + ".json")) {
				if (in == null) throw new IllegalArgumentException("Missing underground processors " + id);
				return new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("processors");
			} catch (IOException e) { throw new IllegalStateException(id, e); }
		});
	}
	static BlockStateContainer apply(String id, String name, BlockStateContainer original, int rotation, Random random) {
		BlockStateContainer result = original;
		for (JsonElement entry : list(id)) {
			JsonObject processor = entry.getAsJsonObject(); String type = processor.get("processor_type").getAsString();
			if (type.equals("minecraft:block_rot")) {
				// Exact released ancient_city_replaceable tag.
				if (ROTTABLE.contains(name.replace("minecraft:", "")) && random.nextFloat() > processor.get("integrity").getAsFloat()) return null;
			} else if (type.equals("minecraft:rule")) {
				for (JsonElement value : processor.getAsJsonArray("rules")) {
					JsonObject rule = value.getAsJsonObject(), predicate = rule.getAsJsonObject("input_predicate");
					if (!name.equals(predicate.get("block").getAsString()) || predicate.has("probability") && random.nextFloat() >= predicate.get("probability").getAsFloat()) continue;
					JsonObject state = rule.getAsJsonObject("output_state"); Map<String, String> props = new HashMap<>();
					if (state.has("Properties")) for (Map.Entry<String, JsonElement> p : state.getAsJsonObject("Properties").entrySet()) props.put(p.getKey(), p.getValue().getAsString());
					result = UndergroundTemplate.state(state.get("Name").getAsString(), props, rotation); break;
				}
			} else if (!type.equals("minecraft:protected_blocks")) throw new IllegalArgumentException("Unsupported underground processor " + type);
		}
		return result;
	}
}
