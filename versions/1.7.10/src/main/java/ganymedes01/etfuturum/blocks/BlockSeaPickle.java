package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.Random;

/** Wet and dry identities let legacy material queries observe real water only in submerged clusters. */
public final class BlockSeaPickle extends BlockAquaticPlant implements net.minecraft.block.IGrowable {
	@SideOnly(Side.CLIENT) private ReleasedBlockModel[] models;
	public BlockSeaPickle(boolean wet) { super(wet ? "sea_pickle_underwater" : "sea_pickle", wet); setBlockBounds(.375F, 0, .375F, .625F, .375F, .625F); }
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) {
		String[] names = {"sea_pickle", "two_sea_pickles", "three_sea_pickles", "four_sea_pickles"};
		models = new ReleasedBlockModel[4];
		for (int i = 0; i < 4; i++) models[i] = new ReleasedBlockModel(atlas, (submerged ? "" : "dead_") + names[i]);
		blockIcon = models[0].particle;
	}
	@SideOnly(Side.CLIENT) public ReleasedBlockModel getReleasedModel(int meta) { return models[meta & 3]; }
	@Override public int getLightValue(IBlockAccess world, int x, int y, int z) { return submerged ? 6 + (world.getBlockMetadata(x, y, z) & 3) * 3 : 0; }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return ModBlocks.SEA_PICKLE.getItem(); }
	@Override @SideOnly(Side.CLIENT) public Item getItem(World world, int x, int y, int z) { return ModBlocks.SEA_PICKLE.getItem(); }
	@Override public int quantityDropped(int meta, int fortune, Random random) { return (meta & 3) + 1; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) { return y > 0 && world.getBlock(x, y - 1, z).isSideSolid(world, x, y - 1, z, ForgeDirection.UP); }
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return y > 0 && y < 256 && (world.isAirBlock(x, y, z) || sourceWater(world, x, y, z)) && canBlockStay(world, x, y, z); }
	@Override protected Block drainedBlock() { return ModBlocks.SEA_PICKLE.get(); }
	@Override protected Block filledBlock() { return ModBlocks.SEA_PICKLE_UNDERWATER.get(); }
	@Override public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) & 3) == 0 ? AxisAlignedBB.getBoundingBox(x + .375, y, z + .375, x + .625, y + .4375, z + .625) : AxisAlignedBB.getBoundingBox(x + .125, y, z + .125, x + .875, y + .4375, z + .875);
	}
	private boolean livingCoral(World world, int x, int y, int z) { return world.getBlock(x, y - 1, z) instanceof BlockCoralBlock coral && !coral.dead; }
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return ModBlocks.SEA_PICKLE.isEnabled() && submerged && livingCoral(world, x, y, z); }
	@Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
	@Override public void func_149853_b(World world, Random random, int x, int y, int z) {
		if (world.isRemote || !func_149851_a(world, x, y, z, false)) return;
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int nx = x + dx, nz = z + dz;
			if (world.blockExists(nx, y, nz) && sourceWater(world, nx, y, nz) && livingCoral(world, nx, y, nz) && random.nextBoolean()) world.setBlock(nx, y, nz, this, random.nextInt(4), 3);
		}
		world.setBlockMetadataWithNotify(x, y, z, 3, 3); world.func_147451_t(x, y, z);
	}
}
