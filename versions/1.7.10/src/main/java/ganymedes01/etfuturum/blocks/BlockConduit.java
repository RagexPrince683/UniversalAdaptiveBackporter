package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import java.util.Random;

public final class BlockConduit extends BlockAquaticPlant {
	@SideOnly(Side.CLIENT) private ReleasedBlockModel model;
	public BlockConduit(boolean wet) { super(wet ? "conduit_underwater" : "conduit", wet); setHardness(3); setResistance(3); setBlockBounds(.3125F, .3125F, .3125F, .6875F, .6875F, .6875F); }
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) { model = new ReleasedBlockModel(atlas, "conduit"); blockIcon = model.particle; }
	@SideOnly(Side.CLIENT) public ReleasedBlockModel getReleasedModel() { return model; }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityConduit(); }
	@Override public int getLightValue() { return 15; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) { return true; }
	@Override public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return net.minecraft.util.AxisAlignedBB.getBoundingBox(x + .3125, y + .3125, z + .3125, x + .6875, y + .6875, z + .6875); }
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return y >= 0 && y < 256 && (world.isAirBlock(x, y, z) || sourceWater(world, x, y, z)); }
	@Override protected Block drainedBlock() { return ModBlocks.CONDUIT.get(); }
	@Override protected Block filledBlock() { return ModBlocks.CONDUIT_UNDERWATER.get(); }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return ModBlocks.CONDUIT.getItem(); }
	@Override @SideOnly(Side.CLIENT) public Item getItem(World world, int x, int y, int z) { return ModBlocks.CONDUIT.getItem(); }
	@Override protected ItemStack createStackedBlock(int meta) { return ModBlocks.CONDUIT.newItemStack(); }
}
