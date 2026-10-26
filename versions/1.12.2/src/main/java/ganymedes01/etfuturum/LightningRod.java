package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class LightningRod extends Block {
    public static final PropertyDirection FACING=PropertyDirection.create("facing");public static final PropertyBool POWERED=PropertyBool.create("powered");
    LightningRod(){super(Material.IRON);setHardness(3);setResistance(6);setHarvestLevel("pickaxe",1);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.UP).withProperty(POWERED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,POWERED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(meta&7)).withProperty(POWERED,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getIndex()|(state.getValue(POWERED)?8:0);}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase player){return getDefaultState().withProperty(FACING,face);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){switch(state.getValue(FACING).getAxis()){case X:return new AxisAlignedBB(0,.375,.375,1,.625,.625);case Z:return new AxisAlignedBB(.375,.375,0,.625,.625,1);default:return new AxisAlignedBB(.375,0,.375,.625,1,.625);}}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new LightningRodTile();}
    @Override public boolean canProvidePower(IBlockState state){return true;}
    @Override public int getWeakPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing face){return state.getValue(POWERED)?15:0;}
    @Override public int getStrongPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing face){return face==state.getValue(FACING)?getWeakPower(state,world,pos,face):0;}
    public void strike(World world,BlockPos pos){if(world.isRemote)return;TileEntity tile=world.getTileEntity(pos);if(tile instanceof LightningRodTile)((LightningRodTile)tile).power();IBlockState state=world.getBlockState(pos);world.setBlockState(pos,state.withProperty(POWERED,true),3);notify(world,pos,state);world.scheduleUpdate(pos,this,8);}
    private void notify(World world,BlockPos pos,IBlockState state){world.notifyNeighborsOfStateChange(pos,this,false);world.notifyNeighborsOfStateChange(pos.offset(state.getValue(FACING).getOpposite()),this,false);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(world.isRemote||!state.getValue(POWERED))return;TileEntity tile=world.getTileEntity(pos);int remaining=tile instanceof LightningRodTile?((LightningRodTile)tile).remaining():0;if(remaining>0){world.scheduleUpdate(pos,this,remaining);return;}world.setBlockState(pos,state.withProperty(POWERED,false),3);notify(world,pos,state);}
}
