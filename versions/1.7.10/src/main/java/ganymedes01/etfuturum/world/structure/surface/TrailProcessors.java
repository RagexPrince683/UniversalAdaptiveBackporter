package ganymedes01.etfuturum.world.structure.surface;

import com.google.gson.*;
import ganymedes01.etfuturum.core.utils.structurenbt.BlockStateContainer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released degradation rules and capped archaeology selections, fixed before any chunk is placed. */
final class TrailProcessors {
	private static final Map<String, JsonArray> PROCESSORS = new HashMap<>();
	static final class Result {
		final BlockStateContainer[] states;
		final String[] loot;
		Result(int size) { states = new BlockStateContainer[size]; loot = new String[size]; }
	}
	private static JsonArray processors(String id) {
		return PROCESSORS.computeIfAbsent(id, key -> {
			try (InputStream stream = TrailProcessors.class.getResourceAsStream("/data/archaeology_processors/" + key.replace("minecraft:trail_ruins", "") + ".json")) {
				if (stream == null) throw new IllegalArgumentException("Missing trail ruins processors " + key);
				return new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("processors");
			} catch (IOException e) { throw new IllegalStateException(key, e); }
		});
	}
	static Result prepare(SurfaceTemplate template, SurfaceStructures.Piece piece, long seed) {
		int count = template.blocks.tagCount(); Result result = new Result(count); String[] names = new String[count];
		for (int i = 0; i < count; i++) names[i] = template.names.get(template.blocks.getCompoundTagAt(i).getInteger("state"));
		Random random = new Random(seed ^ piece.x * 3129871L ^ piece.z * 116129781L ^ piece.y);
		for (JsonElement value : processors(piece.processors)) {
			JsonObject processor = value.getAsJsonObject(); String type = processor.get("processor_type").getAsString();
			int limit = type.equals("minecraft:capped") ? processor.get("limit").getAsInt() : count;
			if (type.equals("minecraft:capped")) processor = processor.getAsJsonObject("delegate");
			if (!processor.get("processor_type").getAsString().equals("minecraft:rule")) throw new IllegalArgumentException("Unsupported trail processor " + processor);
			List<Integer> order = new ArrayList<>(); for (int i = 0; i < count; i++) order.add(i);
			if (limit < count) Collections.shuffle(order, random);
			int changed = 0;
			for (int i : order) {
				if (changed >= limit) break;
				for (JsonElement entry : processor.getAsJsonArray("rules")) {
					JsonObject rule = entry.getAsJsonObject(), predicate = rule.getAsJsonObject("input_predicate");
					String predicateType = predicate.get("predicate_type").getAsString();
					boolean match = predicateType.equals("minecraft:tag_match") ? predicate.get("tag").getAsString().equals("minecraft:trail_ruins_replaceable") && names[i].equals("minecraft:gravel") : names[i].equals(predicate.get("block").getAsString());
					if (!match || predicate.has("probability") && random.nextFloat() >= predicate.get("probability").getAsFloat()) continue;
					JsonObject output = rule.getAsJsonObject("output_state"); names[i] = output.get("Name").getAsString();
					Map<String, String> properties = new HashMap<>(); if (output.has("Properties")) for (Map.Entry<String, JsonElement> p : output.getAsJsonObject("Properties").entrySet()) properties.put(p.getKey(), p.getValue().getAsString());
					result.states[i] = SurfaceTemplate.state(names[i], properties);
					if (rule.has("block_entity_modifier")) result.loot[i] = rule.getAsJsonObject("block_entity_modifier").get("loot_table").getAsString();
					changed++; break;
				}
			}
		} return result;
	}
}
