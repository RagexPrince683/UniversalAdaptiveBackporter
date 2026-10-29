package ganymedes01.etfuturum.mixins.late.enderlicious;

import ganymedes01.etfuturum.world.end.gen.EndCityTerrain;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/** Enderlicious 1.1.2's raw density pass does not consume its population RNG. */
@Pseudo
@Mixin(targets = "DelirusCrux.Enderlicious.Dimension.EndChunkProvider", remap = false)
public abstract class MixinEndChunkProvider implements EndCityTerrain {
    @Shadow private World endWorld;
    @Shadow public abstract void generateEndTerrain(int x, int z, Block[] blocks, BiomeGenBase[] biomes);

    @Override
    public boolean isIslandChunk(int x, int z) {
        // Keep UAB's exclusion of the central 1024-block region. The native density
        // pass, rather than UAB's unrelated island noise, supplies island suitability.
        return (long) x * x + (long) z * z > 4096L;
    }

    @Override
    public void setBlocksInChunk(int x, int z, Block[] blocks) {
        BiomeGenBase[] biomes = endWorld.getWorldChunkManager().loadBlockGeneratorData(null, x * 16, z * 16, 16, 16);
        generateEndTerrain(x, z, blocks, biomes);
    }

    @Override
    public int minimumCityHeight() {
        // Enderlicious starts its upper density taper at Y=56 (UAB: Y=64).
        // Retain the four-corner island test relative to that native terrain band.
        return 52;
    }
}
