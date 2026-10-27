package ganymedes01.etfuturum;

import com.google.gson.*;
import net.minecraft.block.state.IBlockState;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Compiles the twelve reference degradation lists once; block randomness is independent of chunk order. */
final class BastionProcessors {
	private static final Map<String, List<Rule>> CACHE = new HashMap<>();
	private static class Rule {
		String input;
		float chance = 1;
		boolean randomMatch, linear;
		float minChance, maxChance;
		int minDist, maxDist;
		IBlockState[] output = new IBlockState[4];
	}
	private static List<Rule> rules(String name) {
		return CACHE.computeIfAbsent(name, key -> {
			List<Rule> rules = new ArrayList<>();
			if (key.isEmpty() || key.equals("minecraft:empty")) return rules;
			try (InputStream stream = BastionProcessors.class.getResourceAsStream("/data/bastion_processors/" + key.replace("minecraft:", "") + ".json")) {
				if (stream == null) throw new IllegalStateException("Missing bastion processors " + key);
				JsonArray processors = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("processors");
				for (JsonElement value : processors) {
					JsonObject processor = value.getAsJsonObject();
					if (!processor.get("processor_type").getAsString().equals("minecraft:rule")) throw new IllegalStateException("Unsupported bastion processor " + processor);
					for (JsonElement entry : processor.getAsJsonArray("rules")) {
						JsonObject data = entry.getAsJsonObject(), predicate = data.getAsJsonObject("input_predicate");
						Rule rule = new Rule();
						String type = predicate.get("predicate_type").getAsString();
						if (!type.equals("minecraft:always_true") && !type.equals("minecraft:block_match") && !type.equals("minecraft:random_block_match")) throw new IllegalStateException("Unsupported bastion rule " + type);
						rule.input = predicate.has("block") ? predicate.get("block").getAsString() : null;
						rule.randomMatch = type.equals("minecraft:random_block_match");
						if (rule.randomMatch) rule.chance = predicate.get("probability").getAsFloat();
						if (!data.getAsJsonObject("location_predicate").get("predicate_type").getAsString().equals("minecraft:always_true")) throw new IllegalStateException("Unsupported bastion location predicate");
						if (data.has("position_predicate")) {
							JsonObject pos = data.getAsJsonObject("position_predicate");
							if (!pos.get("predicate_type").getAsString().equals("minecraft:axis_aligned_linear_pos") || !pos.get("axis").getAsString().equals("y")) throw new IllegalStateException("Unsupported bastion position rule");
							rule.linear = true; rule.minChance = pos.get("min_chance").getAsFloat(); rule.maxChance = pos.get("max_chance").getAsFloat();
							rule.minDist = pos.get("min_dist").getAsInt(); rule.maxDist = pos.get("max_dist").getAsInt();
						}
						JsonObject state = data.getAsJsonObject("output_state");
						for (int rotation = 0; rotation < 4; rotation++) rule.output[rotation] = StructureTemplate.state(state.get("Name").getAsString(), Collections.emptyMap(), rotation);
						rules.add(rule);
					}
				}
			} catch (IOException e) { throw new IllegalStateException("Cannot read bastion processors " + key, e); }
			return rules;
		});
	}
	static IBlockState apply(String processors, String input, IBlockState state, int rotation, Random random, int localY) {
		for (Rule rule : rules(processors)) {
			if (rule.input != null && !rule.input.equals(input) || rule.randomMatch && random.nextFloat() >= rule.chance) continue;
			if (rule.linear) {
				float distance = Math.max(0, Math.min(1, (float) (Math.abs(localY) - rule.minDist) / (rule.maxDist - rule.minDist)));
				if (random.nextFloat() >= rule.minChance + distance * (rule.maxChance - rule.minChance)) continue;
			}
			return rule.output[rotation];
		}
		return state;
	}
}
