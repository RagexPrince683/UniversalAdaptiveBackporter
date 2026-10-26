package ganymedes01.etfuturum.configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Semantic feature identities, import aliases and native Forge property locations. */
public final class FeatureRegistry {
	/** Legacy API default; use TARGETS and an explicit target for multiversion classification. */
	@Deprecated public static final String TARGET = "1.7.10";
	public static final Set<String> TARGETS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("1.7.10", "1.12.2")));
	public static final Map<String, String> UPDATES = new LinkedHashMap<>();
	private static final Map<String, Option> OPTIONS = new LinkedHashMap<>();
	static {
		String[] versions = {"1.8", "1.9", "1.10", "1.11", "1.11.1", "1.12", "1.13", "1.14", "1.15", "1.16", "1.16.2", "1.17", "1.18", "1.19", "1.20", "1.20.5", "1.21", "1.21.4", "1.21.6", "1.21.7", "1.21.9", "1.21.11"};
		String[] names = {"Bountiful Update", "Combat Update", "Frostburn Update", "Exploration Update", "Exploration Update - Iron Nuggets", "World of Color Update", "Update Aquatic", "Village & Pillage", "Buzzy Bees", "Nether Update", "Piglin Brutes", "Caves & Cliffs Part I", "Caves & Cliffs Part II", "The Wild Update", "Trails & Tales", "Armored Paws", "Tricky Trials", "The Garden Awakens", "Chase the Skies", "Lava Chicken", "The Copper Age", "Mounts of Mayhem"};
		for (int i = 0; i < versions.length; i++) UPDATES.put("update_" + versions[i].replace('.', '_'), "Minecraft " + versions[i] + " - " + names[i]);
		UPDATES.put("general", "General");
		try (InputStream stream = FeatureRegistry.class.getResourceAsStream("/uab/options.tsv")) {
			if (stream == null) throw new IOException("Missing UAB feature catalog");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty() || line.startsWith("#")) continue;
					String[] fields = line.split("\\|", -1);
					if (fields.length != 17) throw new IOException("Invalid UAB catalog row: " + line);
					Option option = new Option(fields);
					if (OPTIONS.put(option.alias, option) != null) throw new IOException("Duplicate UAB alias: " + option.alias);
				}
			}
		} catch (IOException e) { throw new ExceptionInInitializerError(e); }
		Map<String, Option> locations = new HashMap<>();
		for (Option option : OPTIONS.values()) {
			Option previous = locations.put(option.id, option);
			if (previous != null && (!previous.origin.equals(option.origin) || !previous.nativeSince.equals(option.nativeSince)
				|| !previous.components.equals(option.components) || !previous.side.equals(option.side))) throw new ExceptionInInitializerError("Conflicting UAB classification aliases: " + previous.alias + " and " + option.alias);
			for (String dependency : option.dependencies) if (!OPTIONS.containsKey(dependency)) throw new ExceptionInInitializerError("Unknown UAB dependency: " + dependency);
		}
	}
	private FeatureRegistry() { }
	public static Collection<Option> options() { return Collections.unmodifiableCollection(OPTIONS.values()); }
	public static Option option(String source, String key) {
		Option option = OPTIONS.get(source + "." + key);
		if (option == null) throw new IllegalArgumentException("Uncatalogued UAB setting: " + source + "." + key);
		return option;
	}
	public static final class Option {
		public final String source, legacyCategory, legacyKey, origin, category, field, side, reload, name, description, id, alias;
		public final boolean experimental, feature;
		public final Boolean booleanDefault;
		public final List<String> dependencies;
		public final Map<String, String> components;
		public final String nativeSince, previousLocation;
		/** Targets understood by the catalog, rather than a promise of implementation coverage. */
		public final Set<String> targets = TARGETS;
		private Object defaultValue;
		Option(String[] f) {
			source = f[0]; legacyCategory = f[1].toLowerCase(Locale.ROOT); legacyKey = f[2]; origin = f[3]; category = f[4]; field = f[5];
			side = f[6]; reload = f[7]; experimental = f[8].equals("experimental");
			dependencies = f[9].isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(f[9].split(",")));
			name = f[10]; description = f[11]; feature = f[12].equals("feature");
			id = category + "." + field; alias = source + "." + legacyKey;
			booleanDefault = f[13].isEmpty() ? null : Boolean.valueOf(f[13]);
			defaultValue = booleanDefault;
			Map<String, String> parts = new LinkedHashMap<>();
			if (!f[14].isEmpty()) for (String component : f[14].split(";")) {
				String[] pair = component.split("@", -1);
				if (pair.length != 2 || parts.put(pair[0], pair[1]) != null) throw new IllegalArgumentException("Invalid feature component: " + component);
			}
			components = Collections.unmodifiableMap(parts); nativeSince = f[15]; previousLocation = f[16];
			FeatureVersions.validate(this);
		}
		void bindDefault(Object value) { if (defaultValue == null) defaultValue = value; }
		public Object defaultValue() { return defaultValue; }
		public List<String> fields() {
			List<String> result = new ArrayList<>();
			for (Option option : OPTIONS.values()) if (option.category.equals(category) && !result.contains(option.field)) result.add(option.field);
			return Collections.unmodifiableList(result);
		}
		public String restartBehavior() { return reload.equals("live") ? "Client settings can be reloaded" : "Restart Minecraft or the server"; }
		public String compatibilityRestrictions() { return description; }
		public FeatureVersions.Classification classification(String target) { return FeatureVersions.classify(this, target); }
		public String vanillaIntroduction() { return FeatureVersions.introduction(this); }
	}
}
