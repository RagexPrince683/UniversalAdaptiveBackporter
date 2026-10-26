package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BlockPaleHangingMoss extends BaseFlower implements net.minecraft.block.IGrowable {
	public BlockPaleHangingMoss() { setNames("pale_hanging_moss"); }
	@Override public boolean canBlockStay(World world, int x, int y, int z) {
		Block above = world.getBlock(x, y + 1, z);
		return above == this || above == ModBlocks.PALE_OAK_LEAVES.get() || above.isSideSolid(world, x, y + 1, z, net.minecraftforge.common.util.ForgeDirection.DOWN);
	}
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return world.isAirBlock(x, y, z) && canBlockStay(world, x, y, z); }
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return y > 1 && world.isAirBlock(x, y - 1, z); }
	@Override public boolean func_149852_a(World world, java.util.Random random, int x, int y, int z) { return true; }
	@Override public void func_149853_b(World world, java.util.Random random, int x, int y, int z) {
		if (y > 1 && world.isAirBlock(x, y - 1, z)) world.setBlock(x, y - 1, z, this, 0, 3);
	}
}
