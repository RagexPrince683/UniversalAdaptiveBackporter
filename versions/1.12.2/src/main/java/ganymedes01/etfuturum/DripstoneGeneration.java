package ganymedes01.etfuturum;

import ganymedes01.etfuturum.configuration.GenerationSettings;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/** Sparse cave-surface decoration, confined to the populated chunk. */
final class DripstoneGeneration {
    private final GenerationSettings settings;
    private final java.util.Map<World, NoiseGeneratorSimplex> noiseByWorld = new java.util.WeakHashMap<>();
    DripstoneGeneration(GenerationSettings settings) { this.settings = settings; }
    void generate(World world, Random random, int cx, int cz) {
        if (!Content.available("pointed_dripstone") || settings.dripstoneChance == 0 || settings.dripstoneMaximum == 0 || random.nextInt(settings.dripstoneChance) != 0) return;
        int x = cx << 4, z = cz << 4, placements = 0;
        StructureBoundingBox area = new StructureBoundingBox(x, 1, z, x + 15, world.getHeight() - 1, z + 15);
        if (StructureReservations.vanilla(world, area) || ModernStructures.reserved(world, area) || OceanStructures.reserved(world, area)) return;
        NoiseGeneratorSimplex noise = noiseByWorld.computeIfAbsent(world, key -> new NoiseGeneratorSimplex(new Random(key.getSeed())));
        for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
            if (random.nextInt(settings.dripstoneColumns) != 0) continue;
            BlockPos column = new BlockPos(x + dx, 0, z + dz);
            Biome biome = world.getBiome(column);
            if (!(biome.getBaseHeight() >= .5F || biome.getDefaultTemperature() >= .5F && biome.getDefaultTemperature() <= 1.5F && biome.getRainfall() >= .5F)) continue;
            double humidity = (noise.getValue((x + dx) * .02, (z + dz) * .02) + 1) * .5;
            if (humidity < .65) continue;
            int ceiling = Math.min(world.getHeight() - 5, world.getHeight(column).getY() - 16);
            for (int y = 16; y < ceiling; y++) {
                BlockPos base = new BlockPos(x + dx, y, z + dz);
                Block block = world.getBlockState(base).getBlock();
                if (block != Blocks.STONE && !UndergroundBiomesCompat.isNaturalStone(block) || world.getTileEntity(base) != null) continue;
                boolean up = world.isAirBlock(base.up()), down = world.isAirBlock(base.down());
                if (!up && !down) continue;
                world.setBlockState(base, Content.block("dripstone_block").getDefaultState(), 18);
                int length = Math.min(settings.dripstoneLength, 1 + (int) ((humidity - .65) / .05));
                for (EnumFacing direction : new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN}) {
                    if (direction == EnumFacing.UP ? !up : !down) continue;
                    for (int i = 1; i <= length; i++) {
                        BlockPos target = base.offset(direction, i);
                        if (!world.isAirBlock(target)) break;
                        world.setBlockState(target, Content.block("pointed_dripstone").getDefaultState().withProperty(PointedDripstone.DIRECTION, direction), 18);
                    }
                }
                if (++placements >= settings.dripstoneMaximum) return;
            }
        }
    }
}
