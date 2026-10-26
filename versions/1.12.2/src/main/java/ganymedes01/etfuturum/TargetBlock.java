package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class TargetBlock extends Block {
    public static final PropertyInteger POWER=PropertyInteger.create("power",0,15);
    TargetBlock(){super(Material.GRASS);setHardness(.5F);setDefaultState(blockState.getBaseState().withProperty(POWER,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,POWER);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(POWER,meta&15);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(POWER);}
    @Override public int damageDropped(IBlockState state){return 0;}
    public void hit(World world,BlockPos pos,RayTraceResult hit,Entity projectile){if(world.isRemote||world.getBlockState(pos).getBlock()!=this||world.getBlockState(pos).getValue(POWER)>0)return;double x=Math.abs(hit.hitVec.x-Math.floor(hit.hitVec.x)-.5),y=Math.abs(hit.hitVec.y-Math.floor(hit.hitVec.y)-.5),z=Math.abs(hit.hitVec.z-Math.floor(hit.hitVec.z)-.5);double miss=hit.sideHit.getAxis()==EnumFacing.Axis.Y?Math.max(x,z):hit.sideHit.getAxis()==EnumFacing.Axis.X?Math.max(y,z):Math.max(x,y);world.setBlockState(pos,getDefaultState().withProperty(POWER,Math.max(1,MathHelper.ceil(15*MathHelper.clamp((.5-miss)/.5,0,1)))),3);world.notifyNeighborsOfStateChange(pos,this,false);world.scheduleUpdate(pos,this,projectile instanceof EntityArrow?20:8);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){world.setBlockState(pos,state.withProperty(POWER,0),3);world.notifyNeighborsOfStateChange(pos,this,false);}
    @Override public boolean canProvidePower(IBlockState state){return true;}
    @Override public int getWeakPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return state.getValue(POWER);}
    @Override public int getStrongPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return state.getValue(POWER);}
    @Override public boolean shouldCheckWeakPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
}
