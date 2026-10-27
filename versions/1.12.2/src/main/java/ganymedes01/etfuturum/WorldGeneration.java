package ganymedes01.etfuturum;

import java.util.Random;
import ganymedes01.etfuturum.world.GenerationSeeds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

/** New chunks only; flag 18 suppresses observer callbacks which otherwise load neighboring chunks. */
public final class WorldGeneration implements IWorldGenerator {
    private final Geology geology = new Geology();
    private final Geodes geodes = new Geodes();
    private final PlantGeneration plants = new PlantGeneration();
    private final int copper = Features.integer("copperClusterSize", 8, 0, 64);
    private final int debris = Features.integer("debrisMax", 3, 0, 64);
    private final int smallDebris = Features.integer("smallDebrisMax", 2, 0, 64);
    private final int cherryChance = Features.integer("cherryTreeRarity", 72, 0, 127);
    private final boolean exposedDebris = ganymedes01.etfuturum.configuration.UabConfiguration.get().bool("world", "enableAirDebris", false);
    public WorldGeneration() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(geodes);
        // Bind startup controls before the configuration is frozen and saved.
        Features.enabled("content.shipwrecks");
        Features.enabled("content.ocean_ruins");
        Features.enabled("content.buried_treasure");
        Features.enabled("content.generate_ocean_plants");Features.enabled("content.generate_coral_reefs");
    }

    @Override public void generate(Random ignored, int cx, int cz, World world, IChunkGenerator generator, IChunkProvider provider) {
        if (world.isRemote) return;
        ModernStructures.populate(world,cx,cz);
        Random random = GenerationSeeds.chunk(world.getSeed(), cx, cz, 0x554142L);
        if (world.provider.getDimension() == 0) {
            if (Content.available("copper_ore")) veins(world, random, cx, cz, "copper_ore", copper, 8, 4, 80, Blocks.STONE, false);
            BlockPos center = new BlockPos((cx << 4) + 8, 0, (cz << 4) + 8);
            Biome biome = world.getBiome(center);
            if (Content.available("sweet_berries") && BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) {
                for (int i = 0; i < 3; i++) {
                    BlockPos p = world.getHeight(center.add(random.nextInt(12) - 6, 0, random.nextInt(12) - 6));
                    if (Content.block("sweet_berry_bush").canPlaceBlockAt(world, p)) world.setBlockState(p, Content.block("sweet_berry_bush").getStateFromMeta(3), 18);
                }
            }
            if (Content.available("cherry_sapling") && cherryChance > 0 && BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN) && random.nextInt(cherryChance) == 0) ModernSapling.generate(world, random, world.getHeight(center), "cherry");
            OceanPlants.generate(world,random,cx,cz);
            OceanTreasure.generate(world, cx, cz);
            OceanStructures.populate(world, cx, cz);
            Seagrass.generate(world,random,cx,cz);
            plants.generate(world,random,cx,cz);
            SurfaceBiomes.decorate(world,random,cx,cz);
        } else if (world.provider.getDimension() == -1) {
            if (Content.available("ancient_debris")) {
                veins(world, random, cx, cz, "ancient_debris", debris, 1, 8, 22, Blocks.NETHERRACK, !exposedDebris);
                veins(world, random, cx, cz, "ancient_debris", smallDebris, 1, 8, 119, Blocks.NETHERRACK, !exposedDebris);
            }
            if (Content.available("nether_gold_ore")) veins(world, random, cx, cz, "nether_gold_ore", 10, 10, 10, 117, Blocks.NETHERRACK, false);
            NetherVegetation.generate(world, GenerationSeeds.chunk(world.getSeed(), cx, cz, 0x4E455448L), cx, cz);
        }
        geology.generate(world, GenerationSeeds.chunk(world.getSeed(), cx, cz, 0x44454550L), cx, cz);
        geodes.generate(world, GenerationSeeds.chunk(world.getSeed(), cx, cz, 0x47454F44L), cx, cz);
    }
    private static void veins(World world, Random random, int cx, int cz, String name, int size, int count, int minY, int maxY, Block replace, boolean buried) {
        for (int n = 0; n < count; n++) {
            BlockPos p = new BlockPos((cx << 4) + random.nextInt(16), minY + random.nextInt(maxY - minY + 1), (cz << 4) + random.nextInt(16));
            for (int i = 0; i < size; i++) {
                if ((p.getX() >> 4) == cx && (p.getZ() >> 4) == cz && p.getY() > 0 && p.getY() < world.getHeight() - 1 && world.getBlockState(p).getBlock() == replace) {
                    boolean valid = true;
                    if (buried) for (EnumFacing direction : EnumFacing.values()) {
                        BlockPos neighbor = p.offset(direction);
                        if (!world.isBlockLoaded(neighbor) || world.isAirBlock(neighbor)) { valid = false; break; }
                    }
                    if (valid&&!ModernStructures.occupied(world,p)) world.setBlockState(p, Content.block(name).getDefaultState(), 18);
                }
                p = p.offset(EnumFacing.values()[random.nextInt(6)]);
            }
        }
    }
}
