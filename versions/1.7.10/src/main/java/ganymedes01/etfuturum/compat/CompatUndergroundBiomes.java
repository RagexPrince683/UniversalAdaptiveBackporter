package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderServer;

/** Recognize UBC's geological wrapper without replacing its generation or linking its classes. */
public final class CompatUndergroundBiomes {

	private CompatUndergroundBiomes() { }

	public interface WrappedProvider {
		IChunkProvider uab$terrainProvider();
	}

	public static boolean hasVanillaTerrain(IChunkProvider provider) {
		if (provider instanceof ChunkProviderServer) provider = ((ChunkProviderServer) provider).currentChunkProvider;
		// Only the optional UBC mixin supplies this interface. Do not admit arbitrary terrain generators.
		for (int depth = 0; depth < 16 && provider instanceof WrappedProvider; depth++) {
			IChunkProvider next = ((WrappedProvider) provider).uab$terrainProvider();
			if (next == provider) return false;
			provider = next;
		}
		return provider != null && provider.getClass() == ChunkProviderGenerate.class;
	}

	public static boolean isNaturalStone(Block block) {
		return block != null && (block == Stones.IGNEOUS || block == Stones.METAMORPHIC || block == Stones.SEDIMENTARY);
	}

	// First used during terrain generation, after optional mods have registered their blocks.
	private static final class Stones {
		static final Block IGNEOUS = GameRegistry.findBlock("UndergroundBiomes", "igneousStone");
		static final Block METAMORPHIC = GameRegistry.findBlock("UndergroundBiomes", "metamorphicStone");
		static final Block SEDIMENTARY = GameRegistry.findBlock("UndergroundBiomes", "sedimentaryStone");
	}
}
