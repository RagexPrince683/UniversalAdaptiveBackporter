package ganymedes01.etfuturum.world.end.gen;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import ganymedes01.etfuturum.world.end.dimension.ChunkProviderEFREnd;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.event.world.WorldEvent;

/** Adds UAB city assembly at Forge's once-per-population callback, leaving native terrain intact. */
public final class EndCityGenerationBridge implements IWorldGenerator {
    public static final EndCityGenerationBridge INSTANCE = new EndCityGenerationBridge();
    private final Map<World, MapGenEndCity> worlds = new IdentityHashMap<>();

    public static boolean enabled() {
        return ConfigExperiments.endDimensionProvider && ConfigExperiments.enableEndCities
                && ModBlocks.PURPUR_BLOCK.isEnabled() && ModBlocks.PURPUR_PILLAR.isEnabled()
                && ModBlocks.PURPUR_SLAB.isEnabled() && ModBlocks.PURPUR_STAIRS.isEnabled()
                && ModBlocks.END_BRICKS.isEnabled();
    }

    @Override
    public void generate(Random random, int x, int z, World world, IChunkProvider generator, IChunkProvider provider) {
        if (world.isRemote || world.provider.dimensionId != 1 || !enabled()
                || !world.getWorldInfo().isMapFeaturesEnabled() || generator instanceof ChunkProviderEFREnd
                || !(generator instanceof EndCityTerrain) || savedChunk(world, x, z)) return;
        MapGenEndCity cities = worlds.get(world);
        if (cities == null) {
            cities = new MapGenEndCity((EndCityTerrain) generator) {
                @Override protected boolean canSpawnStructureAtCoords(int cx, int cz) {
                    return super.canSpawnStructureAtCoords(cx, cz) && !savedChunk(worldObj, cx, cz);
                }
                @Override public boolean generateStructuresInChunk(World target, Random rng, int cx, int cz) {
                    // This also protects starts saved by the original UAB provider.
                    for (StructureStart start : structureMap.values()) ((MapGenEndCity.Start) start).protectExistingTerrain();
                    return super.generateStructuresInChunk(target, rng, cx, cz);
                }
            };
            worlds.put(world, cities);
        }
        cities.func_151539_a(generator, world, x, z, null);
        // Use our own deterministic RNG; do not perturb Enderlicious or later generators.
        cities.generateStructuresInChunk(world, new Random(world.getSeed()
                + x * 341873128712L + z * 132897987541L + 10387313L), x, z);
    }

    static boolean savedChunk(World world, int x, int z) {
        IChunkProvider provider = world.getChunkProvider();
        if (!(provider instanceof ChunkProviderServer)) return true;
        Object loader = ((ChunkProviderServer) provider).currentChunkLoader;
        // Unknown storage backends cannot guarantee that a missing loaded chunk is new.
        return !(loader instanceof AnvilChunkLoader) || ((AnvilChunkLoader) loader).chunkExists(world, x, z);
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) { worlds.remove(event.world); }
}
