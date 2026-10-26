package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FilledCauldron extends BlockCauldron {
    final boolean lava;
    FilledCauldron(boolean lava){this.lava=lava;setHardness(2);setResistance(2);if(lava)setLightLevel(1);}
    @Override public boolean hasTileEntity(IBlockState state){return !lava;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return lava?null:new PotionCauldronTile();}
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return Items.CAULDRON;}
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return new ItemStack(Items.CAULDRON);}
    @Override public void fillWithRain(World world,BlockPos pos){ }
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(lava&&!world.isRemote&&entity.getEntityBoundingBox().minY<=pos.getY()+.3+state.getValue(LEVEL)*.2){entity.attackEntityFrom(DamageSource.LAVA,4);entity.setFire(15);}}
    static void exchange(EntityPlayer player,EnumHand hand,ItemStack result){if(player.capabilities.isCreativeMode){if(!player.inventory.hasItemStack(result)&&!player.inventory.addItemStackToInventory(result))player.dropItem(result,false);return;}ItemStack held=player.getHeldItem(hand);held.shrink(1);if(held.isEmpty())player.setHeldItem(hand,result);else if(!player.inventory.addItemStackToInventory(result))player.dropItem(result,false);}
    static boolean fill(World world,BlockPos pos,EntityPlayer player,EnumHand hand){ItemStack held=player.getHeldItem(hand);IBlockState old=world.getBlockState(pos);if(old.getBlock()!=Blocks.CAULDRON)return false;
        if(held.getItem()==Items.LAVA_BUCKET&&Content.available("lava_cauldron")){if(!world.isRemote){world.setBlockState(pos,Content.block("lava_cauldron").getDefaultState().withProperty(LEVEL,3),3);if(!player.capabilities.isCreativeMode)player.setHeldItem(hand,new ItemStack(Items.BUCKET));}return true;}
        if(held.getItem()==Items.POTIONITEM&&Content.available("potion_cauldron")&&PotionUtils.getPotionFromItem(held)!=PotionTypes.WATER&&old.getValue(LEVEL)==0){if(!world.isRemote){ItemStack potion=held.copy();potion.setCount(1);world.setBlockState(pos,Content.block("potion_cauldron").getDefaultState().withProperty(LEVEL,1),3);TileEntity tile=world.getTileEntity(pos);if(tile instanceof PotionCauldronTile)((PotionCauldronTile)tile).setPotion(potion);if(!player.capabilities.isCreativeMode)exchange(player,hand,new ItemStack(Items.GLASS_BOTTLE));}return true;}return false;
    }
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(!player.canPlayerEdit(pos,face,held))return false;
        if(lava){if(held.getItem()==Items.BUCKET&&state.getValue(LEVEL)==3){if(!world.isRemote){world.setBlockState(pos,Blocks.CAULDRON.getDefaultState(),3);exchange(player,hand,new ItemStack(Items.LAVA_BUCKET));}return true;}return false;}
        TileEntity value=world.getTileEntity(pos);if(!(value instanceof PotionCauldronTile))return false;PotionCauldronTile tile=(PotionCauldronTile)value;if(tile.potion.isEmpty())return false;int level=state.getValue(LEVEL);
        if(held.getItem()==Items.GLASS_BOTTLE){if(!world.isRemote){ItemStack potion=tile.potion.copy();potion.setCount(1);exchange(player,hand,potion);level(world,pos,state,level-1);}return true;}
        if(held.getItem()==Items.POTIONITEM){boolean same=ItemStack.areItemStackTagsEqual(held,tile.potion);if(same&&level>=3)return false;if(!world.isRemote){if(same)level(world,pos,state,level+1);else world.setBlockState(pos,Blocks.CAULDRON.getDefaultState(),3);if(!player.capabilities.isCreativeMode)exchange(player,hand,new ItemStack(Items.GLASS_BOTTLE));}return true;}
        if(held.getItem()==Items.ARROW){if(!world.isRemote){int maximum=level==1?16:level==2?32:64,count=Math.min(maximum,held.getCount());ItemStack arrows=new ItemStack(Items.TIPPED_ARROW,count);arrows.setTagCompound(tile.potion.getTagCompound()==null?null:tile.potion.getTagCompound().copy());if(!player.capabilities.isCreativeMode)held.shrink(count);if(held.isEmpty())player.setHeldItem(hand,arrows);else if(!player.inventory.addItemStackToInventory(arrows))player.dropItem(arrows,false);level(world,pos,state,count>=maximum?0:level-(count<32?1:2));}return true;}return false;
    }
    private static void level(World world,BlockPos pos,IBlockState state,int level){world.setBlockState(pos,level<=0?Blocks.CAULDRON.getDefaultState():state.withProperty(LEVEL,level),3);world.updateComparatorOutputLevel(pos,state.getBlock());}
}
