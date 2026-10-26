package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.event.terraingen.TerrainGen;

/** Native roots, sprouts and fungi use the maintained soil contract and fungus growth. */
public final class NetherPlant extends BlockBush implements IGrowable {
    final boolean crimson,fungus;
    NetherPlant(boolean crimson,boolean fungus){this.crimson=crimson;this.fungus=fungus;setHardness(0);setSoundType(SoundType.PLANT);}
    @Override public EnumPlantType getPlantType(IBlockAccess world,BlockPos pos){return EnumPlantType.Nether;}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){IBlockState soil=world.getBlockState(pos.down());Block block=soil.getBlock();return block==Blocks.MYCELIUM||block instanceof NyliumBlock||block==Content.BLOCKS.get("soul_soil")||block.canSustainPlant(soil,world,pos.down(),EnumFacing.UP,this)||block.canSustainPlant(soil,world,pos.down(),EnumFacing.UP,Blocks.TALLGRASS);}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.1,0,.1,.9,.8,.9);}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return !fungus;}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return world.isAirBlock(pos)&&canBlockStay(world,pos,getDefaultState());}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){Block soil=world.getBlockState(pos.down()).getBlock();return fungus&&soil instanceof NyliumBlock&&((NyliumBlock)soil).crimson==crimson;}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return random.nextFloat()<.4F;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&canGrow(world,pos,state,false)&&TerrainGen.saplingGrowTree(world,random,pos))HugeFungus.generate(world,random,pos,crimson,true);}
}
