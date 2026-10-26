package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class LanternBlock extends Block {
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    LanternBlock() { super(Material.IRON); setHardness(3.5F); setResistance(3.5F); setHarvestLevel("pickaxe", 0); setLightOpacity(0); setSoundType(SoundType.METAL); setDefaultState(blockState.getBaseState().withProperty(HANGING, false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, HANGING); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(HANGING, (meta & 1) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(HANGING) ? 1 : 0; }
    private boolean supported(World world, BlockPos pos, boolean hanging) {
        BlockPos anchor = hanging ? pos.up() : pos.down();
        IBlockState support = world.getBlockState(anchor); Block block = support.getBlock();
        if (support.isSideSolid(world, anchor, hanging ? EnumFacing.DOWN : EnumFacing.UP)) return true;
        if (block instanceof ChainBlock && support.getValue(BlockRotatedPillar.AXIS) == EnumFacing.Axis.Y) return true;
        if (!hanging) return block.canPlaceTorchOnTop(support, world, anchor);
        return block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockSlab && !((BlockSlab) block).isDouble() && support.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM || block instanceof BlockStairs && support.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM;
    }
    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) { return super.canPlaceBlockAt(world, pos) && (supported(world, pos, false) || supported(world, pos, true)); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer, EnumHand hand) { return getDefaultState().withProperty(HANGING, side == EnumFacing.DOWN && supported(world, pos, true) || !supported(world, pos, false)); }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos from) {
        if (!world.isRemote && !supported(world, pos, state.getValue(HANGING))) { dropBlockAsItem(world, pos, state, 0); world.setBlockToAir(pos); }
    }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return new AxisAlignedBB(.3125, state.getValue(HANGING) ? .0625 : 0, .3125, .6875, state.getValue(HANGING) ? .5 : .4375, .6875); }
}
