package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class SeaPickle extends AquaticPlant implements IGrowable {
    public static final PropertyInteger PICKLES=PropertyInteger.create("pickles",1,4);
    SeaPickle(boolean wet) { super(wet);setDefaultState(getDefaultState().withProperty(PICKLES,1)); }
    @Override protected BlockStateContainer createBlockState() { return states(PICKLES); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(PICKLES,(meta&3)+1); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(PICKLES)-1; }
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos) { return submerged?3+3*state.getValue(PICKLES):0; }
    @Override public int quantityDropped(IBlockState state,int fortune,Random random) { return state.getValue(PICKLES); }
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune) { return Content.item("sea_pickle"); }
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state) { return Content.stack("sea_pickle"); }
    @Override protected boolean supported(World world,BlockPos pos,IBlockState state) { return pos.getY()>0&&world.getBlockState(pos.down()).isSideSolid(world,pos.down(),EnumFacing.UP); }
    @Override protected Block drained() { return Content.block("sea_pickle"); }
    @Override protected Block filled() { return Content.block("sea_pickle_underwater"); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) { double margin=state.getValue(PICKLES)==1?.375:.125;return new AxisAlignedBB(margin,0,margin,1-margin,.4375,1-margin); }
    private boolean coral(World world,BlockPos pos) { Block below=world.getBlockState(pos.down()).getBlock();return below instanceof CoralBlock&&!((CoralBlock)below).dead; }
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client) { return Content.available("sea_pickle")&&submerged&&coral(world,pos); }
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state) { return true; }
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state) {
        if(world.isRemote||!canGrow(world,pos,state,false))return;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){BlockPos target=pos.add(dx,0,dz);if(world.isBlockLoaded(target)&&sourceWater(world,target)&&coral(world,target)&&random.nextBoolean())world.setBlockState(target,getDefaultState().withProperty(PICKLES,1+random.nextInt(4)),3);}
        world.setBlockState(pos,state.withProperty(PICKLES,4),3);
    }
}
