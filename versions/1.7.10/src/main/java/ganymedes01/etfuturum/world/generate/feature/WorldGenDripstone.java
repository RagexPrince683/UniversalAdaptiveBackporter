package ganymedes01.etfuturum.world.generate.feature;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.helpers.DoublePerlinNoiseSampler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;

/** PR #706's humid/mountain cave placement, using the fork's noise and metadata implementation. */
public class WorldGenDripstone extends WorldGenerator {
	@Override
	public boolean generate(World world, Random random, int x, int ignoredY, int z) {
		if (world.provider.dimensionId != 0 || !world.checkChunksExist(x - 1, 0, z - 1, x + 16, 255, z + 16)) return false;
		if (ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new net.minecraft.world.gen.structure.StructureBoundingBox(x, 12, z, x + 15, 50, z + 15))) return false;
		DoublePerlinNoiseSampler noise = DoublePerlinNoiseSampler.create(new Random(world.getSeed()), -4, 1.0D);
		Block pointed = ModBlocks.POINTED_DRIPSTONE.get();
		for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
			int wx = x + dx, wz = z + dz;
			BiomeGenBase biome = world.getBiomeGenForCoords(wx, wz);
			if (!(biome.rootHeight >= 0.5 || biome.temperature >= 0.5 && biome.temperature <= 1.5 && biome.rainfall >= 0.5)) continue;
			int ceiling = Math.min(world.getActualHeight() - 5, world.getHeightValue(wx, wz) - 16);
			for (int y = 16; y < ceiling; y++) {
				double humidity = (noise.sample(wx * 0.02, y * 0.02, wz * 0.02) + 1) * 0.5;
				if (humidity < 0.65 || world.getBlock(wx, y, wz) != Blocks.stone) continue;
				boolean up = world.isAirBlock(wx, y + 1, wz), down = world.isAirBlock(wx, y - 1, wz);
				if (!up && !down) continue;
				world.setBlock(wx, y, wz, ModBlocks.DRIPSTONE_BLOCK.get(), 0, 2);
				int length = Math.min(4, 1 + (int) ((humidity - 0.65) / 0.05));
				for (int direction = -1; direction <= 1; direction += 2) {
					if (direction == 1 ? !up : !down) continue;
					for (int i = 1; i <= length; i++) {
						int ty = y + direction * i;
						if (!world.isAirBlock(wx, ty, wz)) break;
						world.setBlock(wx, ty, wz, pointed, direction == 1 ? 5 : 0, 3);
					}
				}
			}
		}
		return true;
	}
}
