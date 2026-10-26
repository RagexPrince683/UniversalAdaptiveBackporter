package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.item.Item;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** Mature propagules plant in mud/clay and shallow source water; hanging ones ripen on leaves. */
public final class MangroveSapling extends ModernSapling {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 4);
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    MangroveSapling() { super("mangrove"); setDefaultState(blockState.getBaseState().withProperty(AGE, 4).withProperty(HANGING, false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, AGE, HANGING); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(AGE, Math.min(4, meta & 7)).withProperty(HANGING, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(AGE) | (state.getValue(HANGING) ? 8 : 0); }
    @Override public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(HANGING)) return world.getBlockState(pos.up()).getBlock() == Content.block("mangrove_leaves");
        Block ground = world.getBlockState(pos.down()).getBlock();
        return ground == Blocks.CLAY || ground == Content.block("mud") || super.canBlockStay(world, pos, state);
    }
    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState old = world.getBlockState(pos);
        return (old.getMaterial() == Material.AIR || old.getBlock().isReplaceable(world, pos) || old.getBlock() instanceof BlockLiquid && old.getMaterial() == Material.WATER && old.getValue(BlockLiquid.LEVEL) == 0) && canBlockStay(world, pos, getDefaultState());
    }
    @Override public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean client) { return !state.getValue(HANGING) || state.getValue(AGE) < 4; }
    @Override public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) { return state.getValue(HANGING) || super.canUseBonemeal(world, random, pos, state); }
    @Override public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!state.getValue(HANGING)) super.grow(world, random, pos, state);
        else if (state.getValue(AGE) < 4) world.setBlockState(pos, state.withProperty(AGE, state.getValue(AGE) + 1), 3);
    }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return state.getValue(HANGING) && state.getValue(AGE) < 4 ? Items.AIR : Content.item("mangrove_sapling"); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) { return new AxisAlignedBB(.4375, state.getValue(HANGING) ? (4 - state.getValue(AGE)) * .125 : 0, .4375, .5625, 1, .5625); }
}
