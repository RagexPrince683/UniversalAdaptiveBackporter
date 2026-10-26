package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class HangingMoss extends Block implements IGrowable {
    public static final PropertyBool TIP=PropertyBool.create("tip");
    HangingMoss(){super(Material.PLANTS);setHardness(0);setSoundType(SoundType.PLANT);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(TIP,true));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,TIP);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState();}
    @Override public int getMetaFromState(IBlockState state){return 0;}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){return state.withProperty(TIP,world.getBlockState(pos.down()).getBlock()!=this);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.0625,.9375,1,.9375);}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    private boolean supported(World world,BlockPos pos){IBlockState above=world.getBlockState(pos.up());return above.getBlock()==this||above.getBlock()==Content.block("pale_oak_leaves")||above.isSideSolid(world,pos.up(),EnumFacing.DOWN);}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return pos.getY()>0&&pos.getY()<world.getHeight()-1&&world.isAirBlock(pos)&&supported(world,pos);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(!world.isRemote&&!supported(world,pos))world.destroyBlock(pos,true);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return pos.getY()>1&&world.isAirBlock(pos.down());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&canGrow(world,pos,state,false))world.setBlockState(pos.down(),getDefaultState(),3);}
}
