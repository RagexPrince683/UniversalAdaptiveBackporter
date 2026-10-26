package ganymedes01.etfuturum;

import java.util.Random;
import ganymedes01.etfuturum.entities.EntityTurtle;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class TurtleEgg extends Block {
    public static final PropertyInteger HATCH=PropertyInteger.create("hatch",0,2),EGGS=PropertyInteger.create("eggs",1,4);
    TurtleEgg(){super(Material.DRAGON_EGG);setHardness(.5F);setSoundType(SoundType.STONE);setTickRandomly(true);setDefaultState(blockState.getBaseState().withProperty(HATCH,0).withProperty(EGGS,1));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HATCH,EGGS);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HATCH,Math.min(2,meta&3)).withProperty(EGGS,1+((meta>>2)&3));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(HATCH)|((state.getValue(EGGS)-1)<<2);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(EGGS)==1?new AxisAlignedBB(.1875,0,.1875,.75,.4375,.75):new AxisAlignedBB(.0625,0,.0625,.9375,.4375,.9375);}
    @Override public int quantityDropped(Random random){return 0;}
    @Override public boolean canSilkHarvest(World world,BlockPos pos,IBlockState state,EntityPlayer player){return true;}
    @Override protected ItemStack getSilkTouchDrop(IBlockState state){ItemStack result=Content.stack("turtle_egg");result.setCount(state.getValue(EGGS));return result;}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){
        if(world.isRemote||!MobRegistry.enabled(EntityTurtle.class)||world.getBlockState(pos.down()).getBlock()!=Blocks.SAND)return;
        float angle=world.getCelestialAngle(1);if((angle<.65F||angle>.69F)&&random.nextInt(500)!=0)return;
        int hatch=state.getValue(HATCH);if(hatch<2){world.setBlockState(pos,state.withProperty(HATCH,hatch+1),2);world.playSound(null,pos,SoundEvents.BLOCK_STONE_HIT,SoundCategory.BLOCKS,.7F,1);return;}
        int count=state.getValue(EGGS),spawned=0;for(int i=0;i<count;i++){EntityTurtle turtle=new EntityTurtle(world);turtle.setLocationAndAngles(pos.getX()+.3+i*.13,pos.getY(),pos.getZ()+.3,0,0);turtle.setGrowingAge(-24000);turtle.setHome(pos);if(world.spawnEntity(turtle))spawned++;}
        if(spawned==count)world.setBlockToAir(pos);else if(spawned>0)world.setBlockState(pos,state.withProperty(EGGS,count-spawned),2);
    }
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(held.isEmpty()||held.getItem()!=Item.getItemFromBlock(this)||state.getValue(EGGS)>=4||!player.canPlayerEdit(pos,side,held))return false;if(!world.isRemote){world.setBlockState(pos,state.withProperty(EGGS,state.getValue(EGGS)+1),2);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}
    private void trample(World world,BlockPos pos,Entity entity,int chance){
        if(world.isRemote||!(entity instanceof EntityLivingBase)||entity instanceof EntityTurtle||entity.isSneaking()||world.rand.nextInt(chance)!=0||!(entity instanceof EntityPlayer)&&!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world,entity))return;
        IBlockState state=world.getBlockState(pos);if(state.getBlock()!=this)return;int eggs=state.getValue(EGGS);if(eggs==1)world.setBlockToAir(pos);else world.setBlockState(pos,state.withProperty(EGGS,eggs-1),2);world.playSound(null,pos,SoundEvents.BLOCK_STONE_BREAK,SoundCategory.BLOCKS,.7F,1);
    }
    @Override public void onEntityWalk(World world,BlockPos pos,Entity entity){trample(world,pos,entity,100);}
    @Override public void onFallenUpon(World world,BlockPos pos,Entity entity,float distance){trample(world,pos,entity,3);entity.fall(distance,1);}
}
