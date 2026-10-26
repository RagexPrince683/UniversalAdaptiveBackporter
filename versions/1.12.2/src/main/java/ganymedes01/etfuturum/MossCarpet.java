package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class MossCarpet extends Block {
    MossCarpet(){super(Material.PLANTS);setHardness(.1F);setSoundType(SoundType.PLANT);}
    @Override public int getMetaFromState(IBlockState state){return 0;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.0625,1);}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return super.canPlaceBlockAt(world,pos)&&!world.isAirBlock(pos.down());}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(!world.isRemote&&world.isAirBlock(pos.down()))world.destroyBlock(pos,true);}
}
