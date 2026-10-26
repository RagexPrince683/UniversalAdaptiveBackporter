package ganymedes01.etfuturum.configuration.configs;

import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.lib.Reference;

import java.io.File;
import java.util.List;

public class ConfigExperiments extends ConfigBase {

	private static final String catExperiments = "EXPERIMENTAL FEATURES -- TREAD CAREFULLY";
	public static boolean enableSculk;
	public static boolean enableCrimsonBlocks;
	public static boolean enableWarpedBlocks;
	public static boolean enableMangroveBlocks;
	public static boolean enableMossAzalea;
	public static boolean enableDripstone;
	public static boolean enableLightningRod;
	public static boolean enablePaleGarden;

	public static boolean netherDimensionProvider;
	public static boolean endDimensionProvider;
	public static boolean enableEndCities;

	public ConfigExperiments() {
		super("experiments");
	}

	@Override
	protected void syncConfigOptions() {
		enablePaleGarden = getBoolean("enablePaleGarden", catExperiments, true, "Enables pale oak, pale vegetation, resin and the Creaking Heart dependency chain. Biome IDs and natural generation are configured separately in world.cfg.");
		enableCrimsonBlocks = getBoolean("enableCrimsonBlocks", catExperiments, true, "Enables the crimson nylium, wood, and plants. This must be on for the crimson forest biome to generate unless Netherlicious is installed.\nThe nether wart block is still a separate toggle, both this and the wart toggle must be turned off to disable the nether wart block, because crimson trees need the wart blocks.");
		enableWarpedBlocks = getBoolean("enableWarpedBlocks", catExperiments, true, "Enables the warped nylium, wood, and plants. This must be on for the warped forest biome to generate unless Netherlicious is installed. Requires newNether to be enabled without Netherlicious.");
		enableMangroveBlocks = getBoolean("enableMangroveBlocks", catExperiments, true, "Enables mangrove wood and all of its wood subtypes, and the roots (+ muddy versions).");
		enableSculk = getBoolean("enableSculk", catExperiments, true, "Enables sculk-related blocks. Catalyst spreading/bloom remains incomplete and does not intercept mob XP.");
		enableDripstone = getBoolean("enableDripstone", catExperiments, true, "Enables experimental dripstone: cave generation in new Overworld chunks, water growth, mud drying, cauldron filling and falling columns. Local saved metadata is preserved.");
		enableMossAzalea = getBoolean("enableMossAzalea", catExperiments, true, "Enables moss and azalea. Bone meal can grow an oak-trunk tree with azalea and flowering azalea leaves.");
		enableLightningRod = getBoolean("enableLightningRod", catExperiments, true, "Enables experimental lightning rods. Exposed loaded rods attract natural strikes within 128 blocks and emit an eight-tick redstone pulse.");

		netherDimensionProvider = getBoolean("netherDimensionProvider", catExperiments, true, "Enables the Nether dimension provider override needed for supplying custom biomes. This is partially ignored if Netherlicious is installed. Netherlicious has compat to generate Et Futurum Requiem biomes with Netherlicious blocks.\nThis is so you can have vanilla-style biomes in Netherlicious while Requiem is installed. Turning this off or setting each individual biome ID to -1 will prevent my version of Nether biomes from generating. Don't forget to turn off my Nether blocks in blocksitems.cfg since my biomes will generate with Netherlicious blocks if available. [not implemented yet]");
		endDimensionProvider = getBoolean("endDimensionProvider", catExperiments, true, "Enables outer End islands and a saved gateway after each of the first twenty dragon victories. Pending gateways wait for their small footprint to load. The native legacy dragon fight and four-crystal respawn are retained; modern dragon combat phases are not introduced.");
		enableEndCities = getBoolean("enableEndCities", catExperiments, true, "Enables End City structure generation on outer End islands. Requires the endDimensionProvider experimental feature to be enabled.");
	}

	public static List<String> getEnabledElements() {
		return ganymedes01.etfuturum.configuration.UabConfiguration.get().enabledExperiments();
	}

	public static String buildLoadedExperimentsList(boolean color) {
		StringBuilder elements = new StringBuilder();
		for (int i = 0; i < getEnabledElements().size(); i++) {
			if (color) {
				elements.append("\u00a7e");
			}
			elements.append(getEnabledElements().get(i));
			if (color) {
				elements.append("\u00a7r");
			}
			if (i != getEnabledElements().size() - 1) {
				elements.append(", ");
			}
		}
		return elements.toString();
	}
}
