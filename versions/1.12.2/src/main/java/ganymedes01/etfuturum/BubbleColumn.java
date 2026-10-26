package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Native liquid rendering and source depth; direction is stored separately from water level. */
public final class BubbleColumn extends BlockLiquid {
    public static final PropertyBool DRAG=PropertyBool.create("drag");
    BubbleColumn() { super(Material.WATER);setTickRandomly(false);setLightOpacity(Blocks.WATER.getLightOpacity(Blocks.WATER.getDefaultState()));setDefaultState(blockState.getBaseState().withProperty(LEVEL,0).withProperty(DRAG,false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this,LEVEL,DRAG); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(DRAG,(meta&8)!=0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(DRAG)?8:0; }
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state) { if(!world.isRemote)world.scheduleUpdate(pos,this,1); }
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from) { if(!world.isRemote)world.scheduleUpdate(pos,this,1); }
    static void seed(World world,BlockPos pos) {
        if(world.isRemote||!Content.available("bubble_column")||!world.isBlockLoaded(pos)||pos.getY()<1||!AquaticPlant.sourceWater(world,pos))return;
        Block below=world.getBlockState(pos.down()).getBlock();
        if(below==Blocks.SOUL_SAND||below==Blocks.MAGMA)world.setBlockState(pos,Content.block("bubble_column").getDefaultState().withProperty(DRAG,below==Blocks.MAGMA),3);
    }
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random) {
        if(world.isRemote)return;
        IBlockState below=world.getBlockState(pos.down());Block support=below.getBlock();
        boolean valid=support==this||support==Blocks.SOUL_SAND||support==Blocks.MAGMA;
        boolean drag=support==this?below.getValue(DRAG):support==Blocks.MAGMA;
        for(BlockPos next=pos;next.getY()<world.getHeight()&&world.isBlockLoaded(next);next=next.up()) {
            IBlockState current=world.getBlockState(next);
            if(current.getBlock()!=this&&!AquaticPlant.sourceWater(world,next))break;
            IBlockState target=valid?getDefaultState().withProperty(DRAG,drag):Blocks.WATER.getDefaultState();
            if(current!=target)world.setBlockState(next,target,3);
            if(valid)for(EnumFacing side:EnumFacing.HORIZONTALS){BlockPos adjacent=next.offset(side);if(world.isBlockLoaded(adjacent)&&world.isAirBlock(adjacent))world.setBlockState(adjacent,Blocks.FLOWING_WATER.getDefaultState().withProperty(LEVEL,1),3);}
        }
    }
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity) {
        boolean up=!state.getValue(DRAG),surface=world.isAirBlock(pos.up());
        if(surface) { entity.motionY=up?Math.min(1.8,entity.motionY+.1):Math.max(-.9,entity.motionY-.03);if(up)entity.fallDistance=0; }
        else { if(!(entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.isFlying))entity.motionY=up?Math.min(.7,entity.motionY+.6):Math.max(-.3,entity.motionY-.3);entity.fallDistance=0; }
        if(entity instanceof EntityLivingBase)entity.setAir(300);
    }
    @Override public void randomDisplayTick(IBlockState state,World world,BlockPos pos,Random random) {
        world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,pos.getX()+random.nextDouble(),pos.getY()+random.nextDouble(),pos.getZ()+random.nextDouble(),0,state.getValue(DRAG)?-.04:.04,0);
    }
}
