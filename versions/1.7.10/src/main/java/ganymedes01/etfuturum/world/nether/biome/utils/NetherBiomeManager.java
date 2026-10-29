package ganymedes01.etfuturum.world.nether.biome.utils;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.RegistrySafety;
import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.world.nether.biome.BiomeBasaltDeltas;
import ganymedes01.etfuturum.world.nether.biome.BiomeCrimsonForest;
import ganymedes01.etfuturum.world.nether.biome.BiomeSoulSandValley;
import ganymedes01.etfuturum.world.nether.biome.BiomeWarpedForest;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

public class NetherBiomeManager {

	public static boolean netherliciousCompat;

	public static BiomeGenBase crimsonForest;
	public static BiomeGenBase warpedForest;
	public static BiomeGenBase soulSandValley;
	public static BiomeGenBase basaltDeltas;

	public static void init() {
		if (ConfigWorld.crimsonForestID != -1) {
			crimsonForest = new BiomeCrimsonForest(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), ConfigWorld.crimsonForestID, "Crimson Forest")).setBiomeName("Crimson Forest");
		}
		if (ConfigWorld.warpedForestID != -1) {
			warpedForest = new BiomeWarpedForest(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), ConfigWorld.warpedForestID, "Warped Forest")).setBiomeName("Warped Forest");
		}
		if (ConfigWorld.soulSandValleyID != -1) {
			soulSandValley = new BiomeSoulSandValley(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), ConfigWorld.soulSandValleyID, "Soul Sand Valley")).setBiomeName("Soul Sand Valley");
		}
		if (ConfigWorld.basaltDeltasID != -1) {
			basaltDeltas = new BiomeBasaltDeltas(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), ConfigWorld.basaltDeltasID, "Basalt Deltas")).setBiomeName("Basalt Deltas");
		}

		if (!ganymedes01.etfuturum.compat.ModsList.NETHERLICIOUS.isLoaded()) {
			BiomeGenBase.hell.topBlock = Blocks.netherrack;
			BiomeGenBase.hell.fillerBlock = Blocks.netherrack;
	//		BiomeGenBase.hell.biomeName = "Nether Wastes"; //Breaks some mods that stupidly check for the biome name "hell" instead of the fucking ID or tags...
			BiomeGenBase.hell.field_76754_C = 0; // fillerBlockMetadata
			//Sets filler block meta to 0. Vanilla sets it to a ridiculous value for some reason.
			//This value seems to be unused in vanilla as well.
		}
	}

	public static int provideCrimsonForestID() {
		return crimsonForest != null ? crimsonForest.biomeID : -1;
	}

	public static int provideWarpedForestID() {
		return warpedForest != null ? warpedForest.biomeID : -1;
	}

	public static int provideSoulSandValleyID() {
		return soulSandValley != null ? soulSandValley.biomeID : -1;
	}

	public static int provideBasaltDeltasID() {
		return basaltDeltas != null ? basaltDeltas.biomeID : -1;
	}

	public static int provideCrimsonForestWeight() {
		return crimsonForest != null && ganymedes01.etfuturum.configuration.ContentAvailability.enabled("generate_crimson_forest") ? ConfigWorld.crimsonForestWeight : -1;
	}

	public static int provideWarpedForestWeight() {
		return warpedForest != null && ganymedes01.etfuturum.configuration.ContentAvailability.enabled("generate_warped_forest") ? ConfigWorld.warpedForestWeight : -1;
	}

	public static int provideSoulSandValleyWeight() {
		return soulSandValley != null && ganymedes01.etfuturum.configuration.ContentAvailability.enabled("generate_soul_sand_valley") ? ConfigWorld.soulSandValleyWeight : -1;
	}

	public static int provideBasaltDeltasWeight() {
		return basaltDeltas != null && ganymedes01.etfuturum.configuration.ContentAvailability.enabled("generate_basalt_deltas") ? ConfigWorld.basaltDeltasWeight : -1;
	}
}
