package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.tileentities.TileEntityCreakingHeart;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockCreakingHeart extends BlockContainer {
	public BlockCreakingHeart() {
		super(Material.wood);
		setBlockName(Utils.getUnlocalisedName("creaking_heart")); setBlockTextureName("creaking_heart");
		setHardness(10); setResistance(10); setStepSound(soundTypeWood); setCreativeTab(EtFuturum.creativeTabBlocks);
	}
	@Override public int getRenderType() { return 0; }
	@Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityCreakingHeart(); }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return null; }
	@Override protected boolean canSilkHarvest() { return true; }
	@Override public int damageDropped(int meta) { return 0; }
	@Override public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) { return side < 2 ? 0 : side < 4 ? 8 : 4; }
	@Override public int getExpDrop(IBlockAccess world, int meta, int fortune) { return 20 + (world instanceof World ? ((World) world).rand.nextInt(5) : 2); }
	public static boolean isActive(World world, int x, int y, int z) {
		long time = Math.floorMod(world.getWorldTime(), 24000L);
		if (world.provider.hasNoSky || time < 13000 || time >= 23000 || world.getBlock(x, y, z) != ModBlocks.CREAKING_HEART.get()) return false;
		int axis = world.getBlockMetadata(x, y, z) & 12;
		int dx = axis == 4 ? 1 : 0, dz = axis == 8 ? 1 : 0, dy = axis == 0 ? 1 : 0;
		if (axis == 12) return false;
		return alignedLog(world, x + dx, y + dy, z + dz, axis) && alignedLog(world, x - dx, y - dy, z - dz, axis);
	}
	private static boolean alignedLog(World world, int x, int y, int z, int axis) {
		return world.getBlock(x, y, z) == ModBlocks.PALE_OAK_LOG.get() && (world.getBlockMetadata(x, y, z) & 15) == axis;
	}
}
