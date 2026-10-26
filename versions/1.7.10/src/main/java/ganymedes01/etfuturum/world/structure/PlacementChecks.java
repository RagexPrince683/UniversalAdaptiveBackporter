package ganymedes01.etfuturum.world.structure;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Non-loading, feature-specific validation for exposed decorations. Buried structures carve instead. */
public final class PlacementChecks {
	private PlacementChecks() { }
	public static boolean clear(World world, int x, int y, int z, int sx, int sy, int sz) {
		if (y < 1 || y + sy >= world.getActualHeight() || !world.checkChunksExist(x, y, z, x + sx - 1, y + sy - 1, z + sz - 1)) return false;
		for (int dx = 0; dx < sx; dx++) for (int dz = 0; dz < sz; dz++) for (int dy = 0; dy < sy; dy++) {
			Block block = world.getBlock(x + dx, y + dy, z + dz);
			if (block.hasTileEntity(world.getBlockMetadata(x + dx, y + dy, z + dz)) || block.getMaterial().isLiquid() || !block.isReplaceable(world, x + dx, y + dy, z + dz) && block != Blocks.air) return false;
		}
		return true;
	}
}
