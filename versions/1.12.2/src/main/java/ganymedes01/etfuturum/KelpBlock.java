package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Native contained-water plant; no dependency on a general waterlogging engine. */
public final class KelpBlock extends AquaticPlant implements IGrowable {
    public static final PropertyBool TOP = PropertyBool.create("top");
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, TOP, BlockLiquid.LEVEL); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState(); }
    @Override public int getMetaFromState(IBlockState state) { return 0; }
    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) { return state.withProperty(TOP, world.getBlockState(pos.up()).getBlock() != this); }
    KelpBlock() { super(true); setTickRandomly(true); setDefaultState(blockState.getBaseState().withProperty(TOP, true).withProperty(BlockLiquid.LEVEL, 0)); }
    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) {
        Block below = world.getBlockState(pos.down()).getBlock();
        IBlockState fluid = world.getBlockState(pos);
        boolean source = fluid.getBlock() == this || (fluid.getBlock() == Blocks.WATER || fluid.getBlock() == Blocks.FLOWING_WATER) && fluid.getValue(BlockLiquid.LEVEL) == 0;
        return pos.getY() > 0 && source && (below == this || below != Blocks.MAGMA && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP));
    }
    @Override protected boolean supported(World world, BlockPos pos, IBlockState state) {
        Block below = world.getBlockState(pos.down()).getBlock();
        return pos.getY()>0 && (below==this || below!=Blocks.MAGMA && world.getBlockState(pos.down()).isSideSolid(world,pos.down(),EnumFacing.UP));
    }
    @Override public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean client) {
        BlockPos bottom = pos;
        while (bottom.getY() > 0 && world.getBlockState(bottom.down()).getBlock() == this) bottom = bottom.down();
        return pos.getY() - bottom.getY() < 25 && world.getBlockState(pos.up()).getBlock() == Blocks.WATER && world.getBlockState(pos.up()).getValue(BlockLiquid.LEVEL) == 0;
    }
    @Override public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) { return true; }
    @Override public void grow(World world, Random random, BlockPos pos, IBlockState state) { if (canGrow(world, pos, state, false)) world.setBlockState(pos.up(), getDefaultState(), 3); }
    @Override public void randomTick(World world, BlockPos pos, IBlockState state, Random random) { if (random.nextInt(14) == 0) grow(world, random, pos, state); }
}
