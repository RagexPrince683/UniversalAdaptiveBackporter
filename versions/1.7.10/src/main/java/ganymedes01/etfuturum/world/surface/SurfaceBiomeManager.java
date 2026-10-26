package ganymedes01.etfuturum.world.surface;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.core.utils.RegistrySafety;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeManager;

/** Explicit identities; never search for free IDs or change an existing save's biome assignments. */
public final class SurfaceBiomeManager {
	private static final java.util.List<BiomeModernSurface> LAND = new java.util.ArrayList<>();
	public static BiomeGenBase cherryGrove, mangroveSwamp, paleGarden, meadow;
	private SurfaceBiomeManager() { }
	public static void init() {
		cherryGrove = add(ConfigWorld.cherryGroveID, "Cherry Grove", BiomeModernSurface.Kind.CHERRY, BiomeManager.BiomeType.COOL);
		mangroveSwamp = add(ConfigWorld.mangroveSwampID, "Mangrove Swamp", BiomeModernSurface.Kind.MANGROVE, BiomeManager.BiomeType.WARM);
		paleGarden = add(ConfigWorld.paleGardenID, "Pale Garden", BiomeModernSurface.Kind.PALE, BiomeManager.BiomeType.WARM);
		meadow = add(ConfigWorld.meadowID, "Meadow", BiomeModernSurface.Kind.MEADOW, BiomeManager.BiomeType.COOL);
		add(ConfigWorld.groveID, "Grove", BiomeModernSurface.Kind.GROVE, BiomeManager.BiomeType.ICY);
		add(ConfigWorld.snowySlopesID, "Snowy Slopes", BiomeModernSurface.Kind.SLOPES, BiomeManager.BiomeType.ICY);
		add(ConfigWorld.jaggedPeaksID, "Jagged Peaks", BiomeModernSurface.Kind.JAGGED, BiomeManager.BiomeType.ICY);
		add(ConfigWorld.frozenPeaksID, "Frozen Peaks", BiomeModernSurface.Kind.FROZEN, BiomeManager.BiomeType.ICY);
		add(ConfigWorld.stonyPeaksID, "Stony Peaks", BiomeModernSurface.Kind.STONY, BiomeManager.BiomeType.WARM);
		add(ConfigWorld.bambooJungleID, "Bamboo Jungle", BiomeModernSurface.Kind.BAMBOO, BiomeManager.BiomeType.WARM);
		OceanBiomeLayer.register();
	}
	private static BiomeGenBase add(int id, String name, BiomeModernSurface.Kind kind, BiomeManager.BiomeType climate) {
		if (id == -1) return null;
		BiomeGenBase biome = new BiomeModernSurface(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), id, name), kind).setBiomeName(name);
		LAND.add((BiomeModernSurface) biome);
		if (ConfigWorld.enableSurfaceBiomes && ConfigWorld.generatesSurfaceBiome(name)) BiomeManager.addBiome(climate, new BiomeManager.BiomeEntry(biome, 3));
		return biome;
	}
	public static void finishSpawns() { for (BiomeModernSurface biome : LAND) biome.finishSpawns(); }
}
