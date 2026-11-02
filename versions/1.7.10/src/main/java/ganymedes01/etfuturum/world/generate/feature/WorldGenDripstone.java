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
    private final ganymedes01.etfuturum.configuration.GenerationSettings settings = new ganymedes01.etfuturum.configuration.GenerationSettings();
    private final java.util.Map<World, DoublePerlinNoiseSampler> noiseByWorld = new java.util.WeakHashMap<>();
	@Override
	public boolean generate(World world, Random random, int x, int ignoredY, int z) {
		if (world.provider.dimensionId != 0 || settings.dripstoneChance == 0 || settings.dripstoneMaximum == 0 || random.nextInt(settings.dripstoneChance) != 0 || !world.blockExists(x, 0, z)) return false;
		if (ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new net.minecraft.world.gen.structure.StructureBoundingBox(x, 12, z, x + 15, 50, z + 15))) return false;
		DoublePerlinNoiseSampler noise = noiseByWorld.computeIfAbsent(world, key -> DoublePerlinNoiseSampler.create(new Random(key.getSeed()), -4, 1.0D));
        int placements = 0;
		Block pointed = ModBlocks.POINTED_DRIPSTONE.get();
		for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
			if (random.nextInt(settings.dripstoneColumns) != 0) continue;
            int wx = x + dx, wz = z + dz;
			BiomeGenBase biome = world.getBiomeGenForCoords(wx, wz);
			if (!(biome.rootHeight >= 0.5 || biome.temperature >= 0.5 && biome.temperature <= 1.5 && biome.rainfall >= 0.5)) continue;
			int ceiling = Math.min(world.getActualHeight() - 5, world.getHeightValue(wx, wz) - 16);
			for (int y = 16; y < ceiling; y++) {
				double humidity = (noise.sample(wx * 0.02, y * 0.02, wz * 0.02) + 1) * 0.5;
				if (humidity < 0.65) continue;
				Block base = world.getBlock(wx, y, wz);
				if (base != Blocks.stone && !ganymedes01.etfuturum.compat.CompatUndergroundBiomes.isNaturalStone(base)) continue;
				boolean up = world.isAirBlock(wx, y + 1, wz), down = world.isAirBlock(wx, y - 1, wz);
				if (!up && !down) continue;
				world.setBlock(wx, y, wz, ModBlocks.DRIPSTONE_BLOCK.get(), 0, 2);
				int length = Math.min(settings.dripstoneLength, 1 + (int) ((humidity - 0.65) / 0.05));
				for (int direction = -1; direction <= 1; direction += 2) {
					if (direction == 1 ? !up : !down) continue;
					int placed = 0;
					for (int i = 1; i <= length; i++) {
						int ty = y + direction * i;
						if (!world.isAirBlock(wx, ty, wz)) break;
						if (!world.setBlock(wx, ty, wz, pointed, direction == 1 ? 5 : 0, 2)) break;
						placed++;
					}
					for (int i = placed; i > 0; i--) ((ganymedes01.etfuturum.blocks.BlockPointedDripstone) pointed).updateGeneratedState(world, wx, y + direction * i, wz);
				}
                if (++placements >= settings.dripstoneMaximum) return true;
			}
		}
		return true;
	}
}
