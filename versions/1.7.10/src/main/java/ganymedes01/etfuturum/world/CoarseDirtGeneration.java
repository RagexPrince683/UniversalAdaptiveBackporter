package ganymedes01.etfuturum.world;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public final class CoarseDirtGeneration {
	private CoarseDirtGeneration() {}

	/** Operates only on the terrain array currently being generated, before a chunk exists. */
	public static void replaceColumn(Block[] blocks, byte[] metadata, int x, int z) {
		if (!ConfigWorld.enableCoarseDirtReplacement || !ModBlocks.COARSE_DIRT.isEnabled()) return;
		int height = blocks.length / 256;
		int offset = ((z & 15) * 16 + (x & 15)) * height;
		for (int y = 0; y < height; y++) {
			int index = offset + y;
			if (blocks[index] == Blocks.dirt && metadata[index] == 1) {
				blocks[index] = ModBlocks.COARSE_DIRT.get();
				metadata[index] = 0;
			}
		}
	}
}
