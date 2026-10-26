package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.Random;

public final class BlockKelp extends BlockAquaticPlant implements IGrowable {
	@SideOnly(Side.CLIENT) private ReleasedBlockModel tip, stem;
	public BlockKelp() { super("kelp", true); setTickRandomly(true); }
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) {
		tip = new ReleasedBlockModel(atlas, "kelp"); stem = new ReleasedBlockModel(atlas, "kelp_plant"); blockIcon = tip.particle;
	}
	@SideOnly(Side.CLIENT) public ReleasedBlockModel getReleasedModel(boolean top) { return top ? tip : stem; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) {
		Block below = world.getBlock(x, y - 1, z);
		return y > 0 && (below == this || below != ModBlocks.MAGMA.get() && below.isSideSolid(world, x, y - 1, z, ForgeDirection.UP));
	}
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return y > 0 && y < 255 && sourceWater(world, x, y, z) && canBlockStay(world, x, y, z); }
	private int top(World world, int x, int y, int z) { while (y < 255 && world.getBlock(x, y + 1, z) == this) y++; return y; }
	private boolean canGrow(World world, int x, int y, int z) {
		if (!ModBlocks.KELP.isEnabled() || y >= 255 || !sourceWater(world, x, y + 1, z)) return false;
		int bottom = y; while (bottom > 0 && world.getBlock(x, bottom - 1, z) == this) bottom--;
		return y - bottom + 1 < 26;
	}
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote && world.getBlock(x, y + 1, z) != this && random.nextFloat() < .14F && canGrow(world, x, y, z)) world.setBlock(x, y + 1, z, this, 0, 3);
	}
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return canGrow(world, x, top(world, x, y, z), z); }
	@Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
	@Override public void func_149853_b(World world, Random random, int x, int y, int z) {
		y = top(world, x, y, z);
		if (!world.isRemote && canGrow(world, x, y, z)) world.setBlock(x, y + 1, z, this, 0, 3);
	}
}
