package ganymedes01.etfuturum.world.end.gen;

import net.minecraft.block.Block;

/** Terrain-only prediction: never requests chunks, decorates biomes or writes to a world. */
public interface EndCityTerrain {
    boolean isIslandChunk(int chunkX, int chunkZ);
    void setBlocksInChunk(int chunkX, int chunkZ, Block[] blocks);
    default int minimumCityHeight() { return 60; }
}
