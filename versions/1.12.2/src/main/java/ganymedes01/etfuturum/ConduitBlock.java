package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

final class ConduitBlock extends Block {
    ConduitBlock() { super(Material.IRON); setHardness(3); setLightLevel(1); }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return new ConduitTile(); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) { return new net.minecraft.util.math.AxisAlignedBB(.3125, .3125, .3125, .6875, .6875, .6875); }
}
