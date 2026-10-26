package ganymedes01.etfuturum.configuration;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraft.entity.*;
import net.minecraft.world.biome.Biome;
import ganymedes01.etfuturum.core.utils.Logger;
import java.util.*;

/** Startup-only spawn parameters. Biome selection and entity eligibility remain native. */
public final class SpawnSettings {
	private static final Map<String, int[]> SETTINGS = new HashMap<>();
	private SpawnSettings() { }
	public static void initialize() {
		SETTINGS.clear();
		for (FeatureRegistry.Option option : FeatureRegistry.options()) {
			if (!option.source.equals("spawns") || !ganymedes01.etfuturum.MobRegistry.implemented(option.legacyKey.split("\\.")[0])) continue;
			String[] key = option.legacyKey.split("\\.");
			int index = key[1].equals("spawnWeight") ? 0 : key[1].equals("minGroupSize") ? 1 : 2;
			int[] values = SETTINGS.computeIfAbsent(className(key[0]), ignored -> new int[]{-1, -1, -1});
			values[index] = UabConfiguration.get().integer("spawns", option.legacyKey, -1, -1, index == 0 ? 10000 : 64);
			if (index != 0 && values[index] == 0) {
				values[index] = 1;
				UabConfiguration.get().nativeConfig().get(option.category, option.field, -1).set(1);
				Logger.warn("[UAB] " + option.name + " group size must be at least one; using one.");
			}
		}
		for (Map.Entry<String, int[]> entry : SETTINGS.entrySet()) {
			int[] value = entry.getValue();
			if (value[1] > 0 && value[2] > 0 && value[2] < value[1]) {
				value[2] = value[1];
				Logger.warn("[UAB] Maximum group size was smaller than minimum for " + entry.getKey() + "; using " + value[1]);
				for (FeatureRegistry.Option option : FeatureRegistry.options()) if (option.source.equals("spawns") && option.field.equals("maxGroupSize") && className(option.legacyKey.split("\\.")[0]).equals(entry.getKey()))
					UabConfiguration.get().nativeConfig().get(option.category, option.field, -1).set(value[2]);
			}
		}
	}
	private static String className(String id) {
		if (id.equals("pufferfish")) return "EntityPufferfish";
		StringBuilder name = new StringBuilder("Entity");
		for (String word : id.split("_")) name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		return name.toString();
	}
	public static Biome.SpawnListEntry entry(Class<? extends EntityLiving> type, int weight, int min, int max) {
		int[] configured = SETTINGS.get(type.getSimpleName());
		if (!ganymedes01.etfuturum.MobRegistry.enabled(type)) return null;
		if (configured != null) {
			if (configured[0] >= 0) weight = configured[0];
			if (configured[1] > 0) min = configured[1];
			if (configured[2] > 0) max = configured[2];
		}
		return weight == 0 ? null : new Biome.SpawnListEntry(type, weight, min, Math.max(min, max));
	}
	public static List<Biome.SpawnListEntry> list(Biome.SpawnListEntry... entries) {
		List<Biome.SpawnListEntry> result = new ArrayList<>();
		for (Biome.SpawnListEntry original : entries) add(result, original.entityClass, original.itemWeight, original.minGroupCount, original.maxGroupCount);
		return Collections.unmodifiableList(result);
	}
	public static void add(List<Biome.SpawnListEntry> list, Class<? extends EntityLiving> type, int weight, int min, int max) {
		Biome.SpawnListEntry entry = entry(type, weight, min, max);
		if (entry != null) list.add(entry);
	}
	public static void addSpawn(Class<? extends EntityLiving> type, int weight, int min, int max, EnumCreatureType category, Biome... biomes) {
		Biome.SpawnListEntry entry = entry(type, weight, min, max);
		for (Biome biome : new LinkedHashSet<>(Arrays.asList(biomes))) {
			if (biome == null) continue;
			EntityRegistry.removeSpawn(type, category, biome);
			if (entry != null) EntityRegistry.addSpawn(type, entry.itemWeight, entry.minGroupCount, entry.maxGroupCount, category, biome);
		}
	}
}
