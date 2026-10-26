package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class BambooShoot extends BlockBush implements IGrowable {
    BambooShoot(){setHardness(0);setSoundType(SoundType.PLANT);setTickRandomly(true);}
    @Override protected boolean canSustainBush(IBlockState soil){return BambooBlock.soil(soil);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Content.item("bamboo");}
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack("bamboo");}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.3125,0,.3125,.6875,.75,.6875);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return pos.getY()<world.getHeight()-1&&world.isAirBlock(pos.up());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(world.isRemote||!canGrow(world,pos,state,false))return;IBlockState bamboo=Content.block("bamboo").getDefaultState();world.setBlockState(pos,bamboo,3);world.setBlockState(pos.up(),bamboo.withProperty(BambooBlock.LEAVES,BambooBlock.Leaves.SMALL),3);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){super.updateTick(world,pos,state,random);if(world.getBlockState(pos).getBlock()==this&&random.nextInt(3)==0&&world.getLightFromNeighbors(pos)>=9)grow(world,random,pos,state);}
}
