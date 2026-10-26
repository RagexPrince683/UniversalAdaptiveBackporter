package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockAquaticPlant;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import java.util.Random;

/** New-chunk, chunk-contained ocean decoration. No neighboring chunk requests or retrogen. */
public final class OceanPlants {
	private OceanPlants() { }
	public static void generate(World world, Random random, int chunkX, int chunkZ) {
		if (world.provider.dimensionId != 0) return;
		if (net.minecraftforge.common.BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords((chunkX << 4) + 8, (chunkZ << 4) + 8), net.minecraftforge.common.BiomeDictionary.Type.OCEAN)) CoralReefs.generate(world, random, chunkX, chunkZ);
		if (!ContentAvailability.enabled("generate_ocean_plants")) return;
		for (int attempt = 0; attempt < 24; attempt++) {
			int x = (chunkX << 4) + random.nextInt(16), z = (chunkZ << 4) + random.nextInt(16);
			if (!world.blockExists(x, 63, z)) continue;
			BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
			if (!BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN)) continue;
			boolean warm = biome.temperature > .8F;
			if (warm ? !ModBlocks.SEA_PICKLE.isEnabled() : !ModBlocks.KELP.isEnabled() || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SNOWY)) continue;
			// The first solid seabed under the open ocean, never a cave or a structure interior.
			int y = Math.min(62, world.getHeightValue(x, z) - 1);
			while (y > 1 && BlockAquaticPlant.sourceWater(world, x, y, z)) y--;
			y++;
			if (y >= 62 || ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new net.minecraft.world.gen.structure.StructureBoundingBox(x, y - 1, z, x, 63, z))) continue;
			Block plant = warm ? ModBlocks.SEA_PICKLE_UNDERWATER.get() : ModBlocks.KELP.get();
			if (!plant.canPlaceBlockAt(world, x, y, z)) continue;
			if (warm) {
				if (random.nextInt(6) == 0) world.setBlock(x, y, z, plant, random.nextInt(4), 2);
			} else {
				int height = 1 + random.nextInt(10);
				for (int i = 0; i < height && y + i < 62 && BlockAquaticPlant.sourceWater(world, x, y + i, z); i++) world.setBlock(x, y + i, z, plant, 0, 2);
			}
		}
	}
}
