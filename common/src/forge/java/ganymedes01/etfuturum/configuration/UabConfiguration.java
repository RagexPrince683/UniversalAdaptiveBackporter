package ganymedes01.etfuturum.configuration;

import ganymedes01.etfuturum.core.utils.Logger;
import net.minecraftforge.common.config.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Single configuration owner, loaded before feature selection. Registry settings freeze at startup. */
public final class UabConfiguration {
	private static UabConfiguration instance;
	private final Configuration config;
	private final File targetFile, workingFile;
	private final Map<String, Property> properties = new HashMap<>();
	private final Map<String, Property> startupProperties = new HashMap<>();
	private final boolean client;
	private final String target;
	private final Map<String, Object> values = new HashMap<>();
	private final Map<String, Configuration> legacy = new LinkedHashMap<>();
	private final Set<String> imported = new HashSet<>();
	private final boolean importing;
	private boolean frozen;
	private final List<String> diagnostics = new ArrayList<>();

	private UabConfiguration(File directory, boolean client, String target) {
		this.target = target;
		this.client = client;
		targetFile = new File(directory, "uab.cfg");
		workingFile = stagedCopy(targetFile);
		config = readNative(workingFile, targetFile);
		migrateCatalogLocations();
		importing = !config.get("general.migration", "legacyImported", false).getBoolean(false);
		if (importing) {
			LinkedHashSet<String> sources = new LinkedHashSet<>();
			for (FeatureRegistry.Option option : FeatureRegistry.options()) if (!option.source.equals("content") && !option.source.equals("mobs")) sources.add(option.source);
			for (String source : sources) readLegacy(source, new File(directory, "etfuturum/" + source + ".cfg"));
			readLegacy("single-directory", new File(directory, "etfuturum/etfuturum.cfg"));
			readLegacy("single-root", new File(directory, "etfuturum.cfg"));
		}
	}
	public static void open(File directory) { open(directory, true); }
	public static void open(File directory, boolean client) { open(directory, client, "1.7.10"); }
	public static void open(File directory, boolean client, String target) {
		if (!FeatureRegistry.TARGETS.contains(target)) throw new IllegalArgumentException("Unsupported Minecraft target: " + target);
		instance = new UabConfiguration(directory, client, target);
	}
	public static UabConfiguration get() {
		if (instance == null) throw new IllegalStateException("UAB configuration has not been opened");
		return instance;
	}
	public Configuration nativeConfig() { return config; }
	public List<String> diagnostics() { return Collections.unmodifiableList(diagnostics); }
	private void migrateCatalogLocations() {
		boolean backedUp = false;
		for (FeatureRegistry.Option option : FeatureRegistry.options()) {
			if (option.previousLocation.isEmpty()) continue;
			int dot = option.previousLocation.lastIndexOf('.');
			String category = option.previousLocation.substring(0, dot), field = option.previousLocation.substring(dot + 1);
			if (!config.hasKey(category, field)) continue;
			if (!backedUp && targetFile.isFile()) {
				try {
					Path backup = Paths.get(targetFile.getPath() + ".catalog-v2.bak");
					if (!Files.exists(backup)) Files.copy(targetFile.toPath(), backup);
				} catch (IOException failure) { throw new IllegalStateException("Cannot safely back up UAB configuration before category migration", failure); }
				backedUp = true;
			}
			Property old = config.getCategory(category).get(field);
			if (!config.hasKey(option.category, option.field)) {
				Property moved = old.isList() ? new Property(option.field, old.getStringList(), old.getType()) : new Property(option.field, old.getString(), old.getType());
				String update = category.substring(0, category.indexOf('.'));
				if (option.feature && !update.equals("general") && config.hasKey(update, "enabled") && !config.getCategory(update).get("enabled").getBoolean(true)) moved.set(false);
				config.getCategory(option.category).put(option.field, moved);
			}
			ConfigCategory previous = config.getCategory(category); previous.remove(field);
			if (previous.isEmpty() && previous.getChildren().isEmpty()) config.removeCategory(previous);
		}
	}
	private void warn(String message) { diagnostics.add(message); Logger.warn("[UAB] " + message); }
	private void readLegacy(String source, File file) {
		if (!file.isFile()) return;
		try {
			Path backup = Paths.get(file.getPath() + ".uab.bak");
			if (!Files.exists(backup)) Files.copy(file.toPath(), backup);
			File staging = stagedCopy(file);
			Configuration old = readNative(staging, file); legacy.put(source, old);
		} catch (IOException | RuntimeException e) {
			throw new IllegalStateException("UAB could not safely read and back up " + file + "; original files were retained", e);
		}
	}
	private static File stagedCopy(File source) {
		try {
			File staging = Files.createTempFile("uab-config-", ".cfg").toFile();
			staging.deleteOnExit();
			if (source.isFile()) Files.copy(source.toPath(), staging.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return staging;
		} catch (IOException e) { throw new IllegalStateException("UAB could not stage configuration " + source, e); }
	}
	private static Configuration readNative(File staging, File original) {
		Configuration result = new Configuration(staging);
		// Forge's constructor recovers malformed syntax by renaming its input. Only a disposable copy is passed to it.
		File[] errors = staging.getParentFile().listFiles((directory, name) -> name.startsWith(staging.getName() + "_") && name.endsWith(".errored"));
		if (errors != null && errors.length > 0) throw new IllegalStateException("Malformed configuration syntax in " + original + "; the original file was retained. Correct it before restarting UAB.");
		return result;
	}
	private void savePrimary() {
		try {
			Files.newOutputStream(workingFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING).close();
			config.save();
			if (Files.size(workingFile.toPath()) == 0) throw new IOException("Forge did not write the configuration");
			Files.createDirectories(targetFile.toPath().getParent());
			Path pending = Files.createTempFile(targetFile.toPath().getParent(), ".uab-", ".tmp");
			try {
				Files.copy(workingFile.toPath(), pending, StandardCopyOption.REPLACE_EXISTING);
				try { Files.move(pending, targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
				catch (AtomicMoveNotSupportedException unsupported) { Files.move(pending, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
			} finally { Files.deleteIfExists(pending); }
		} catch (IOException e) { throw new IllegalStateException("UAB could not safely save " + targetFile, e); }
	}
	private Property legacy(FeatureRegistry.Option option) {
		if (option.source.equals("content") && option.legacyKey.equals("seagrass")) return legacy(FeatureRegistry.option("entities", "enableAquaticSpawning"));
		if (option.source.equals("content") && option.legacyKey.equals("bamboo_plants")) return legacy(FeatureRegistry.option("blocksitems", "enableBambooBlocks"));
		Configuration split = legacy.get(option.source);
		if (split != null && split.hasKey(option.legacyCategory, option.legacyKey)) return consume(option.source, split, option.legacyCategory, option.legacyKey);
		for (String source : new String[]{"single-directory", "single-root"}) {
			Configuration old = legacy.get(source);
			if (old == null) continue;
			if (old.hasKey(option.legacyCategory, option.legacyKey)) return consume(source, old, option.legacyCategory, option.legacyKey);
			String found = null;
			for (String category : old.getCategoryNames()) if (old.hasKey(category, option.legacyKey)) {
				if (found != null) { found = null; break; }
				found = category;
			}
			if (found != null) return consume(source, old, found, option.legacyKey);
		}
		return null;
	}
	private Property consume(String source, Configuration old, String category, String key) {
		imported.add(source + ":" + category + ":" + key);
		return old.getCategory(category).get(key);
	}
	private Property create(FeatureRegistry.Option option, Object fallback, Property.Type type) {
		option.bindDefault(fallback);
		if (FeatureVersions.nativeFeature(option, target) || !client && option.side.equals("client")) return detached(option, fallback, type);
		Property cached = properties.get(option.id);
		if (cached != null) return cached;
		boolean exists = config.hasKey(option.category, option.field);
		Property property;
		if (fallback instanceof int[]) property = config.get(option.category, option.field, (int[]) fallback);
		else if (fallback instanceof String[]) property = config.get(option.category, option.field, (String[]) fallback);
		else property = config.get(option.category, option.field, String.valueOf(fallback), option.description, type);
		if (importing) {
			Property old = legacy(option);
			for (FeatureRegistry.Option alias : FeatureRegistry.options()) if (alias != option && alias.id.equals(option.id)) {
				Property other = legacy(alias);
				if (old == null || type == Property.Type.BOOLEAN && other != null && other.getString().equalsIgnoreCase("false")) old = other;
			}
			if (!exists && old != null) {
				if (property.isList() != old.isList()) warn("Ignoring invalid list/scalar value for " + option.name);
				else if (old.isList()) property.set(old.getStringList());
				else property.set(old.getString());
			} else if (!exists && old == null) importOldBlockSelector(option, property);
		}
		String comment = option.description + (option.experimental && !option.description.toLowerCase(Locale.ROOT).contains("experimental") ? " Experimental: incomplete support; enable with care." : "") + "\n" + option.restartBehavior() + ".";
		if (!option.dependencies.isEmpty()) {
			StringJoiner names = new StringJoiner(", ");
			for (String dependency : option.dependencies) { int dot = dependency.indexOf('.'); names.add(FeatureRegistry.option(dependency.substring(0, dot), dependency.substring(dot + 1)).name); }
			comment += "\nRequires: " + names + ".";
		}
		ForgePropertyAdapter.setComment(property, comment);
		property.setRequiresMcRestart(!option.reload.equals("live"));
		property.setLanguageKey("uab.config." + option.id);
		String family = option.name;
		for (FeatureRegistry.Option candidate : FeatureRegistry.options()) if (candidate.feature && candidate.category.equals(option.category)) { family = candidate.name; break; }
		config.getCategory(option.category).setComment(family);
		config.getCategory(option.category).setLanguageKey("uab.config." + option.category);
		String update = option.category.substring(0, option.category.indexOf('.'));
		config.getCategory(update).setComment(FeatureRegistry.UPDATES.get(update));
		config.getCategory(update).setLanguageKey("uab.config." + update);
		if (!update.equals("general")) config.get(update, "enabled", true, "Enable this update's features. Individual switches are remembered when this is off. Restart required.").setRequiresMcRestart(true);
		properties.put(option.id, property);
		return property;
	}
	private void importOldBlockSelector(FeatureRegistry.Option option, Property target) {
		if (!option.source.equals("world")) return;
		String oldKey = option.legacyKey.equals("fossilBlockID") ? "fossilBoneBlock" : option.legacyKey.equals("amethystOuterBlockID") ? "amethystOuterBlock" : null;
		if (oldKey == null) return;
		Configuration old = legacy.get("world");
		if (old == null || !old.hasKey("generation", oldKey)) return;
		Property selector = consume("world", old, "generation", oldKey);
		try {
			int index = Integer.parseInt(selector.getString());
			String[] ids = oldKey.equals("fossilBoneBlock") ? new String[]{"etfuturum:bone", "netherlicious:BoneBlock", "uptodate:bone_block"} : new String[]{"etfuturum:smooth_basalt", "etfuturum:tuff", "netherlicious:BasaltBricks:6"};
			if (index >= 0 && index < ids.length) target.set(ids[index]); else warn("Invalid old block selector for " + option.name + "; using the default");
		} catch (NumberFormatException e) { warn("Invalid old block selector for " + option.name + "; using the default"); }
	}
	private static Property detached(FeatureRegistry.Option option, Object value, Property.Type type) {
		if (value instanceof String[]) return new Property(option.field, (String[]) value, type);
		if (value instanceof int[]) {
			int[] ints = (int[]) value; String[] strings = new String[ints.length];
			for (int i = 0; i < ints.length; i++) strings[i] = String.valueOf(ints[i]);
			return new Property(option.field, strings, type);
		}
		return new Property(option.field, String.valueOf(value), type);
	}
	public Property property(String source, String key, Object fallback, Property.Type type) {
		FeatureRegistry.Option option = FeatureRegistry.option(source, key);
		if (frozen && !option.reload.equals("live") && startupProperties.containsKey(option.id)) return startupProperties.get(option.id);
		Property property = create(option, fallback, type);
		if (type == Property.Type.INTEGER && property.isList() && !property.isIntList()) {
			warn("Invalid integer list for " + option.name + "; using the default list");
			property.set(detached(option, fallback, type).getStringList());
		}
		return property;
	}
	private boolean cached(FeatureRegistry.Option option) { return frozen && !option.reload.equals("live") && values.containsKey(option.id); }
	public boolean bool(String source, String key, boolean fallback) {
		FeatureRegistry.Option option = FeatureRegistry.option(source, key);
		if (FeatureVersions.nativeFeature(option, target)) return true;
		if (option.booleanDefault != null) fallback = option.booleanDefault;
		if (cached(option)) return (Boolean) values.get(option.id);
		Property property = create(option, fallback, Property.Type.BOOLEAN);
		String raw = property.getString();
		if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) { warn("Invalid boolean for " + option.name + "; using " + fallback); property.set(fallback); }
		boolean value = property.getBoolean(fallback);
		if (option.feature) {
			String update = option.category.substring(0, option.category.indexOf('.'));
			if (!update.equals("general")) value &= config.get(update, "enabled", true).getBoolean(true);
			for (String dependency : option.dependencies) {
				int dot = dependency.indexOf('.');
				FeatureRegistry.Option required = FeatureRegistry.option(dependency.substring(0, dot), dependency.substring(dot + 1));
				Object def = required.defaultValue();
				value &= bool(required.source, required.legacyKey, def instanceof Boolean ? (Boolean) def : true);
			}
		}
		values.put(option.id, value);
		return value;
	}
	public int integer(String source, String key, int fallback, int min, int max) {
		FeatureRegistry.Option option = FeatureRegistry.option(source, key);
		if (cached(option)) return (Integer) values.get(option.id);
		Property property = create(option, fallback, Property.Type.INTEGER);
		int value = fallback;
		try { value = Integer.parseInt(property.getString().trim()); } catch (NumberFormatException e) { warn("Invalid integer for " + option.name + "; using " + fallback); }
		int bounded = Math.max(min, Math.min(max, value));
		if (bounded != value) warn("Out-of-range value for " + option.name + "; clamped to " + bounded);
		property.set(bounded); property.setMinValue(min); property.setMaxValue(max);
		values.put(option.id, bounded);
		return bounded;
	}
	public float decimal(String source, String key, float fallback, float min, float max) {
		FeatureRegistry.Option option = FeatureRegistry.option(source, key);
		if (cached(option)) return (Float) values.get(option.id);
		Property property = create(option, fallback, Property.Type.DOUBLE);
		float value = fallback;
		try { value = Float.parseFloat(property.getString().trim()); } catch (NumberFormatException e) { warn("Invalid number for " + option.name + "; using " + fallback); }
		if (!Float.isFinite(value)) { warn("Non-finite number for " + option.name + "; using " + fallback); value = fallback; }
		float bounded = Math.max(min, Math.min(max, value));
		if (bounded != value) warn("Out-of-range value for " + option.name + "; clamped to " + bounded);
		property.set((double) bounded); property.setMinValue(min); property.setMaxValue(max);
		values.put(option.id, bounded);
		return bounded;
	}
	public void finish() {
		if (!frozen) for (FeatureRegistry.Option option : FeatureRegistry.options()) {
			Property property = properties.get(option.id);
			if (property != null) startupProperties.put(option.id, detached(option, property.isList() ? property.getStringList().clone() : property.getString(), property.getType()));
		}
		if (importing && !frozen) {
			for (Map.Entry<String, Configuration> file : legacy.entrySet()) for (String category : file.getValue().getCategoryNames()) for (String key : file.getValue().getCategory(category).keySet())
				if (!imported.contains(file.getKey() + ":" + category + ":" + key)) warn("Legacy setting retained in backup but not imported: " + file.getKey() + "/" + category + "/" + key);
			config.get("general.migration", "legacyImported", false).set(true);
			if (!legacy.isEmpty()) Logger.info("[UAB] Imported legacy configuration. Existing UAB settings took precedence; originals and .uab.bak backups were retained.");
		}
		config.getCategory("general.migration").setShowInGui(false);
		if (config.hasCategory("general.advanced")) config.getCategory("general.advanced").setShowInGui(false);
		savePrimary();
		frozen = true;
	}
	public void reloadClient() { properties.clear(); }
	public String handshake() {
		try {
			java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			TreeMap<String, String> state = new TreeMap<>();
			for (FeatureRegistry.Option option : FeatureRegistry.options()) {
				if (option.side.equals("client")) continue;
				Property property = startupProperties.get(option.id);
				Object value = values.get(option.id);
				if (property != null) state.put(option.id, value != null ? value.toString() : property.isList() ? Arrays.toString(property.getStringList()) : property.getString());
			}
			for (Map.Entry<String, String> entry : state.entrySet()) digest.update((entry.getKey() + "=" + entry.getValue() + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder();
			for (byte value : digest.digest()) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
			return result.toString();
		} catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
	}
	public boolean matches(Map<String, String> remote) {
		boolean matches = handshake().equals(remote.get("uab:configuration"));
		if (!matches) Logger.error("[UAB] Client and server gameplay settings differ. Copy config/uab.cfg from the server and restart Minecraft; client-only preferences may differ.");
		return matches;
	}
	public List<String> enabledExperiments() {
		List<String> result = new ArrayList<>();
		for (FeatureRegistry.Option option : FeatureRegistry.options()) if (option.experimental && Boolean.TRUE.equals(values.get(option.id))) result.add(option.name);
		return result;
	}
}
