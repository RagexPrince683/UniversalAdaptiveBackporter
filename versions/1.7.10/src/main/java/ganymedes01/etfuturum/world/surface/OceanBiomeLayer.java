package ganymedes01.etfuturum.world.surface;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.core.utils.RegistrySafety;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

/** Temperature-only substitution after native rivers/coastlines. Both query resolutions share one layer. */
public final class OceanBiomeLayer extends GenLayer {
	private static final BiomeGenBase[] SHALLOW = new BiomeGenBase[5], DEEP = new BiomeGenBase[5];
	public static boolean isOcean(BiomeGenBase biome) {
		for (BiomeGenBase value : SHALLOW) if (value != null && value == biome) return true;
		return isDeepOcean(biome);
	}
	public static boolean isDeepOcean(BiomeGenBase biome) {
		for (BiomeGenBase value : DEEP) if (value != null && value == biome) return true;
		return false;
	}
	private OceanBiomeLayer(GenLayer parent) { super(712034L); this.parent = parent; }
	public static void register() {
		SHALLOW[0] = ocean(ConfigWorld.warmOceanID, "Warm Ocean", 0.9F, false);
		SHALLOW[1] = ocean(ConfigWorld.lukewarmOceanID, "Lukewarm Ocean", 0.6F, false);
		SHALLOW[2] = BiomeGenBase.ocean;
		SHALLOW[3] = ocean(ConfigWorld.coldOceanID, "Cold Ocean", 0.2F, false);
		SHALLOW[4] = BiomeGenBase.frozenOcean;
		DEEP[0] = DEEP[1] = ocean(ConfigWorld.deepLukewarmOceanID, "Deep Lukewarm Ocean", 0.6F, true);
		DEEP[2] = BiomeGenBase.deepOcean;
		DEEP[3] = ocean(ConfigWorld.deepColdOceanID, "Deep Cold Ocean", 0.2F, true);
		DEEP[4] = ocean(ConfigWorld.deepFrozenOceanID, "Deep Frozen Ocean", 0, true);
		if (ConfigWorld.enableSurfaceBiomes) MinecraftForge.TERRAIN_GEN_BUS.register(new Events());
	}
	private static BiomeGenBase ocean(int id, String name, float temperature, boolean deep) {
		if (id == -1) return null;
		BiomeGenBase biome = new Ocean(RegistrySafety.requireVacant(BiomeGenBase.getBiomeGenArray(), id, name), temperature, deep).setBiomeName(name);
		BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER);
		BiomeManager.oceanBiomes.add(biome);
		return biome;
	}
	@Override public int[] getInts(int x, int z, int width, int height) {
		int[] source = parent.getInts(x, z, width, height), result = IntCache.getIntCache(width * height);
		for (int dz = 0; dz < height; dz++) for (int dx = 0; dx < width; dx++) {
			int index = dx + dz * width, old = source[index];
			result[index] = old;
			if (old != BiomeGenBase.ocean.biomeID && old != BiomeGenBase.deepOcean.biomeID) continue;
			initChunkSeed(Math.floorDiv(x + dx, 64), Math.floorDiv(z + dz, 64));
			BiomeGenBase biome = (old == BiomeGenBase.deepOcean.biomeID ? DEEP : SHALLOW)[nextInt(5)];
			if (biome != null && (biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean || biome == BiomeGenBase.frozenOcean || ConfigWorld.generatesSurfaceBiome(biome.biomeName))) result[index] = biome.biomeID;
		}
		return result;
	}
	private static class Ocean extends BiomeGenBase {
		Ocean(int id, float temperature, boolean deep) {
			super(id); setTemperatureRainfall(temperature, 0.5F); setHeight(new Height(deep ? -1.8F : -1.0F, 0.1F));
			topBlock = temperature >= 0.6F ? Blocks.sand : Blocks.gravel; fillerBlock = topBlock; spawnableCreatureList.clear();
			if (temperature < 0.15F) setEnableSnow();
		}
	}
	public static class Events {
		@SubscribeEvent public void layers(WorldTypeEvent.InitBiomeGens event) {
			if (event.worldType != WorldType.DEFAULT && event.worldType != WorldType.LARGE_BIOMES && event.worldType != WorldType.AMPLIFIED) return;
			if (event.newBiomeGens.length < 2) return;
			GenLayer ocean = new OceanBiomeLayer(event.newBiomeGens[0]);
			GenLayer voronoi = new GenLayerVoronoiZoom(10, ocean);
			ocean.initWorldGenSeed(event.seed); voronoi.initWorldGenSeed(event.seed);
			event.newBiomeGens[0] = ocean; event.newBiomeGens[1] = voronoi;
			if (event.newBiomeGens.length > 2) event.newBiomeGens[2] = ocean;
		}
	}
}
