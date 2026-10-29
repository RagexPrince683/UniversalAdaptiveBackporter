package ganymedes01.etfuturum.configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import ganymedes01.etfuturum.entities.EntityCod;

/** Native Forge configuration regression tests; no replacement parser or extra test framework. */
public final class ConfigurationTest {
	private static int assertions;
	private static void check(boolean value, String message) {
		assertions++;
		if (!value) throw new AssertionError(message);
	}
	private static File folder(String name) throws IOException { return Files.createTempDirectory("uab-" + name + "-").toFile(); }
	private static void set(Configuration config, String source, String key, String value) {
		FeatureRegistry.Option option = FeatureRegistry.option(source, key);
		config.get(option.category, option.field, value).set(value);
	}
	private static UabConfiguration open(File folder, boolean client) { UabConfiguration.open(folder, client); return UabConfiguration.get(); }
	public static void main(String[] ignored) throws Exception {
		java.lang.reflect.Field home = cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
		home.setAccessible(true); home.set(null, new File(System.getProperty("java.io.tmpdir")));
		File clean = folder("clean");
		UabConfiguration config = open(clean, true);
		check(config.bool("blocksitems", "enableCopper", true), "clean default");
		check(config.bool("experiments", "enableSculk", false), "sculk enabled by default for Ancient Cities");
		check(config.bool("experiments", "netherDimensionProvider", false), "Nether biomes enabled by default for bastions");
		check(config.bool("experiments", "enableDripstone", true), "ordinary content has no experiments opt-in");
		config.finish();
		check(new File(clean, "uab.cfg").isFile(), "single native config generated");
		config = open(clean, true);
		check(config.bool("blocksitems", "enableCopper", true), "existing UAB config loads");
		config.finish();

		File upgrade = folder("split-upgrade");
		File oldPath = new File(upgrade, "etfuturum/blocksitems.cfg");
		Configuration old = new Configuration(oldPath);
		FeatureRegistry.Option copper = FeatureRegistry.option("blocksitems", "enableCopper");
		old.get(copper.legacyCategory, copper.legacyKey, true).set(false);
		old.get("obsolete", "unknownFeature", 17).set(42);
		old.save();
		byte[] original = Files.readAllBytes(oldPath.toPath());
		Configuration oldWorld = new Configuration(new File(upgrade, "etfuturum/world.cfg"));
		oldWorld.get("generation", "copperClusterSize", 8).set(27);
		oldWorld.get("generation", "fossilBoneBlock", 0).set(2);
		oldWorld.save();
		Configuration oldEntities = new Configuration(new File(upgrade, "etfuturum/entities.cfg"));
		oldEntities.get("passive", "turtleSpawnWeight", 5).set(19); oldEntities.save();
		config = open(upgrade, true);
		check(!config.bool("blocksitems", "enableCopper", true), "legacy explicit false preserved");
		check(config.integer("world", "copperClusterSize", 8, 0, 64) == 27, "custom generation value preserved");
		check(config.property("world", "fossilBlockID", "etfuturum:bone", Property.Type.STRING).getString().equals("uptodate:bone_block"), "old numeric block selector translated");
		ContentAvailability.initialize(); SpawnSettings.initialize();
		FeatureRegistry.Option turtle = FeatureRegistry.option("spawns", "turtle.spawnWeight");
		check(config.nativeConfig().get(turtle.category, turtle.field, -1).getInt() == 19, "legacy turtle weight migrated to one canonical field");
		config.finish();
		check(Arrays.equals(original, Files.readAllBytes(oldPath.toPath())), "original legacy file untouched");
		File backup = new File(oldPath.getPath() + ".uab.bak");
		check(Arrays.equals(original, Files.readAllBytes(backup.toPath())), "backup matches original bytes");
		check(config.diagnostics().stream().anyMatch(s -> s.contains("unknownFeature")), "unmapped values reported");
		old.get(copper.legacyCategory, copper.legacyKey, true).set(true); old.save();
		config = open(upgrade, true);
		check(!config.bool("blocksitems", "enableCopper", true), "migration does not repeat over UAB values");
		check(Arrays.equals(original, Files.readAllBytes(backup.toPath())), "backup never overwritten");
		config.finish();

		File precedence = folder("precedence");
		Configuration current = new Configuration(new File(precedence, "uab.cfg")); set(current, "blocksitems", "enableCopper", "true"); current.save();
		old = new Configuration(new File(precedence, "etfuturum/blocksitems.cfg")); old.get(copper.legacyCategory, copper.legacyKey, true).set(false); old.save();
		config = open(precedence, true);
		check(config.bool("blocksitems", "enableCopper", true), "existing UAB values take precedence"); config.finish();

		File monolithic = folder("monolithic");
		oldPath = new File(monolithic, "etfuturum.cfg"); old = new Configuration(oldPath); old.get("historic blocks", "enableCopper", true).set(false); old.save();
		config = open(monolithic, true); check(!config.bool("blocksitems", "enableCopper", true), "unique monolithic key imported"); config.finish(); check(oldPath.exists(), "monolithic file retained");

		File disabled = folder("disabled");
		current = new Configuration(new File(disabled, "uab.cfg")); current.get("update_1_16", "enabled", true).set(false); set(current, "blocksitems", "enableCopper", "false"); current.save();
		config = open(disabled, true);
		check(!config.bool("blocksitems", "enableNetherite", true), "parent update disables features");
		check(!config.bool("blocksitems", "enableCopper", true), "individual feature disabled");
		check(!config.bool("world", "enableTrialChambers", true), "missing dependency disables generation");
		check(config.nativeConfig().get("update_1_16.netherite", "enabled", true).getBoolean(), "parent does not erase remembered child preference");
		config.finish();

		File invalid = folder("invalid"); current = new Configuration(new File(invalid, "uab.cfg"));
		set(current, "world", "copperClusterSize", "broken"); set(current, "world", "bastionSpacing", "-100"); set(current, "blocksitems", "netheriteSpeed", "NaN");
		set(current, "spawns", "cod.spawnWeight", "50001"); set(current, "spawns", "cod.minGroupSize", "7"); set(current, "spawns", "cod.maxGroupSize", "2"); current.save();
		config = open(invalid, true);
		check(config.integer("world", "copperClusterSize", 8, 0, 64) == 8, "malformed number uses default");
		check(config.integer("world", "bastionSpacing", 27, 8, 128) == 8, "range validated");
		check(config.decimal("blocksitems", "netheriteSpeed", 9, .1F, Float.MAX_VALUE) == 9, "non-finite number rejected");
		ContentAvailability.initialize(); SpawnSettings.initialize();
		net.minecraft.world.biome.BiomeGenBase.SpawnListEntry entry = SpawnSettings.entry(EntityCod.class, 15, 3, 6);
		check(entry.itemWeight == 10000 && entry.minGroupCount == 7 && entry.maxGroupCount == 7, "spawn bounds and group relationship enforced");
		config.finish();
		String signature = config.handshake();
		set(config.nativeConfig(), "world", "copperClusterSize", "32"); config.nativeConfig().save(); config.reloadClient();
		check(config.integer("world", "copperClusterSize", 8, 0, 64) == 8, "restart settings frozen during reload");
		check(signature.equals(config.handshake()), "reload retains active handshake state");

		for (String id : new String[]{"broken", "-2", "32768", "65535"}) {
			File identity = folder("potion-identity");
			current = new Configuration(new File(identity, "uab.cfg"));
			set(current, "enchantspotions", "darknessID", id); current.save();
			config = open(identity, true);
			boolean rejected = false;
			java.lang.reflect.Method readId = ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions.class.getDeclaredMethod("potionId", String.class);
			readId.setAccessible(true);
			try { readId.invoke(null, "darknessID"); }
			catch (java.lang.reflect.InvocationTargetException expected) { rejected = expected.getCause() instanceof IllegalArgumentException; }
			check(rejected, "invalid saved potion ID fails explicitly: " + id);
			FeatureRegistry.Option option = FeatureRegistry.option("enchantspotions", "darknessID");
			check(id.equals(config.nativeConfig().getCategory(option.category).get(option.field).getString()), "invalid identity is not rewritten");
		}

		File server = folder("server"); config = open(server, false); config.bool("functions", "enableExtraF3HTooltips", true); config.bool("blocksitems", "enableCopper", true); config.finish();
		check(!config.nativeConfig().hasCategory(FeatureRegistry.option("functions", "enableExtraF3HTooltips").category), "dedicated config excludes client preferences");

        File live = folder("client-reload"); config = open(live, true);
        check(config.bool("functions", "enableExtraF3HTooltips", true), "live preference default"); config.finish();
        set(config.nativeConfig(), "functions", "enableExtraF3HTooltips", "false"); config.reloadClient();
        check(!config.bool("functions", "enableExtraF3HTooltips", true), "safe client preference applies without registry reload"); config.finish();

        File zero = folder("zero-weight"); current = new Configuration(new File(zero, "uab.cfg"));
        set(current, "spawns", "cod.spawnWeight", "0"); current.save(); config = open(zero, true); ContentAvailability.initialize(); SpawnSettings.initialize();
        check(SpawnSettings.entry(EntityCod.class, 15, 3, 6) == null, "zero weight removes natural entry"); config.finish();

        File species = folder("disabled-species"); current = new Configuration(new File(species, "uab.cfg"));
        set(current, "mobs", "cod", "false"); current.save(); config = open(species, true); ContentAvailability.initialize(); SpawnSettings.initialize();
        check(SpawnSettings.entry(EntityCod.class, 15, 3, 6) == null, "individual species toggle removes its entry"); config.finish();

        File malformed = folder("malformed-syntax"); File malformedFile = new File(malformed, "etfuturum.cfg");
        byte[] malformedBytes = "category {\n B:enableCopper=false\n @unparseable\n".getBytes(StandardCharsets.UTF_8); Files.write(malformedFile.toPath(), malformedBytes);
        boolean rejected = false;
        try { open(malformed, true); } catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "malformed syntax fails clearly instead of erasing unknown preferences");
        check(Arrays.equals(malformedBytes, Files.readAllBytes(malformedFile.toPath())), "malformed original is never renamed or rewritten");
        check(Arrays.equals(malformedBytes, Files.readAllBytes(new File(malformedFile.getPath() + ".uab.bak").toPath())), "malformed original is backed up before parsing");
		for (FeatureRegistry.Option option : FeatureRegistry.options()) {
			check(option.targets.contains("1.7.10") && option.targets.contains("1.12.2"), "catalog classifies both supported targets");
			check(!option.description.toLowerCase(Locale.ROOT).contains("mixin"), "player descriptions exclude implementation mechanisms");
		}
		System.out.println("UAB configuration tests passed: " + assertions + " assertions");
	}
}
