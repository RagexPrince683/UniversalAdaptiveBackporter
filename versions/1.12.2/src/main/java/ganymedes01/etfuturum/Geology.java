package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.config.Property;

/** Maintained lower layer and ellipsoid clusters, confined to the chunk being populated. */
final class Geology {
    private final int mode = Features.integer("deepslateGenerationMode", 0, -1, 1);
    private final int height = Features.integer("deepslateMaxY", 22, 0, 255);
    private final int deepslateSize = Features.integer("deepslateClusterSize", 64, 0, 128);
    private final int tuffSize = Features.integer("tuffClusterSize", 32, 0, 64);
    private final boolean stones = UabConfiguration.get().bool("world", "deepslateReplacesStones", true);
    private final boolean dirt = UabConfiguration.get().bool("world", "deepslateReplacesDirt", true);
    private final boolean cobble = UabConfiguration.get().bool("tweaks", "deepslateReplacesCobblestone", false);
    private final boolean whitelist = UabConfiguration.get().bool("world", "deepslateLayerDimensionBlacklistAsWhitelist", false);
    private final int[] dimensions = UabConfiguration.get().property("world", "deepslateLayerDimensionBlacklist", new int[]{-1, 1}, Property.Type.INTEGER).getIntList();
    private final int[] fullHeight = UabConfiguration.get().property("world", "replaceAllStoneWithDeepslateDimensionWhitelist", new int[0], Property.Type.INTEGER).getIntList();
    Geology() { }
    private static boolean contains(int[] values, int value) { for (int candidate : values) if (candidate == value) return true; return false; }
    void generate(World world, Random random, int cx, int cz) {
        boolean flat = world.getWorldInfo().getTerrainType() == WorldType.FLAT;
        if (flat && !world.getWorldInfo().getGeneratorOptions().contains("decoration")) return;
        int top = Math.min(world.getHeight() - 1, contains(fullHeight, world.provider.getDimension()) ? world.getHeight() - 1 : height);
        StructureBoundingBox area = new StructureBoundingBox(cx << 4, 0, cz << 4, (cx << 4) + 15, Math.max(top, height + 12), (cz << 4) + 15);
        if (StructureReservations.vanilla(world, area) || OceanStructures.reserved(world, area) || OceanTreasure.reserved(world, area)) return;
        if (world.provider instanceof WorldProviderSurface && height >= 6) {
            if (Content.available("deepslate") && mode == 1) cluster(world, random, cx, cz, deepslateSize, Content.block("deepslate"));
            if (Content.available("tuff")) cluster(world, random, cx, cz, tuffSize, Content.block("tuff"));
        }
        if (flat || mode != 0 || height <= 0 || !Content.available("deepslate") || contains(dimensions, world.provider.getDimension()) != whitelist) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y <= top; y++) for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            if (top == world.getHeight() - 1 || y < top - 4 || y <= top - random.nextInt(4)) replace(world, pos.setPos((cx << 4) + x, y, (cz << 4) + z), Content.block("deepslate"));
        }
    }
    private void replace(World world, BlockPos pos, Block stone) {
        IBlockState current = world.getBlockState(pos); Block block = current.getBlock();
        if (block.hasTileEntity(current)||ModernStructures.occupied(world,pos)||ganymedes01.etfuturum.api.tags.helpers.BlockTags.hasTag(block,block.getMetaFromState(current),"minecraft:deepslate_ore_replaceables")) return;
        ganymedes01.etfuturum.api.blocksanditems.utils.BlockMetaPair mapped=ganymedes01.etfuturum.api.DeepslateOreRegistry.getOre(block,block.getMetaFromState(current));
        IBlockState replacement=mapped==null?null:mapped.get()==Content.block("deepslate")?stone.getDefaultState():mapped.get().getStateFromMeta(mapped.getMeta());
        if (replacement == null) {
            if (block == Blocks.STONE && (stones || current.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) || UndergroundBiomesCompat.isNaturalStone(block) || block == Blocks.DIRT && dirt) replacement = stone.getDefaultState();
            else if (block == Blocks.COBBLESTONE && cobble && Content.available("cobbled_deepslate")) replacement = Content.block("cobbled_deepslate").getDefaultState();
        }
        if (replacement != null) world.setBlockState(pos, replacement, 18);
    }
    private void cluster(World world, Random random, int cx, int cz, int size, Block stone) {
        if (size <= 0) return;
        // Same interpolated ellipsoids as WorldGenDeepslateLayerBlob; native writes do not populate neighbors.
        double angle = random.nextFloat() * Math.PI;
        double x = (cx << 4) + random.nextInt(16), z = (cz << 4) + random.nextInt(16);
        double y = 6 + random.nextInt(height - 5);
        double x0 = x + Math.sin(angle) * size / 8, x1 = x - Math.sin(angle) * size / 8;
        double z0 = z + Math.cos(angle) * size / 8, z1 = z - Math.cos(angle) * size / 8;
        double y0 = y + random.nextInt(3) - 2, y1 = y + random.nextInt(3) - 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= size; i++) {
            double px = x0 + (x1 - x0) * i / size, py = y0 + (y1 - y0) * i / size, pz = z0 + (z1 - z0) * i / size;
            double radius = ((Math.sin(i * Math.PI / size) + 1) * random.nextDouble() * size / 16 + 1) / 2;
            int minX = Math.max(cx << 4, MathHelper.floor(px - radius)), maxX = Math.min((cx << 4) + 15, MathHelper.floor(px + radius));
            int minZ = Math.max(cz << 4, MathHelper.floor(pz - radius)), maxZ = Math.min((cz << 4) + 15, MathHelper.floor(pz + radius));
            for (int bx = minX; bx <= maxX; bx++) for (int by = Math.max(1, MathHelper.floor(py - radius)); by <= Math.min(world.getHeight() - 1, MathHelper.floor(py + radius)); by++) for (int bz = minZ; bz <= maxZ; bz++) {
                double dx = (bx + .5 - px) / radius, dy = (by + .5 - py) / radius, dz = (bz + .5 - pz) / radius;
                if (dx * dx + dy * dy + dz * dz < 1) replace(world, pos.setPos(bx, by, bz), stone);
            }
        }
    }
}
