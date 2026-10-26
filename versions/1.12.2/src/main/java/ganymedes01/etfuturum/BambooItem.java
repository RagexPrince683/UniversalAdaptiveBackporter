package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class BambooItem extends Item {
    @Override public int getItemBurnTime(ItemStack stack){return 50;}
    @Override public boolean isFull3D(){return true;}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing side,float x,float y,float z){
        ItemStack stack=player.getHeldItem(hand);if(!Content.available("bamboo")||stack.isEmpty())return EnumActionResult.FAIL;
        if(!world.getBlockState(pos).getBlock().isReplaceable(world,pos))pos=pos.offset(side);
        if(pos.getY()>=world.getHeight()-1||!player.canPlayerEdit(pos,side,stack)||!world.isAirBlock(pos))return EnumActionResult.FAIL;
        Block below=world.getBlockState(pos.down()).getBlock();Block target=Content.block(below instanceof BambooBlock||below instanceof BambooShoot?"bamboo":"bamboo_sapling");
        if(!target.canPlaceBlockAt(world,pos)||!world.mayPlace(target,pos,false,side,player))return EnumActionResult.FAIL;
        if(!world.isRemote){IBlockState state=target.getDefaultState();if(below instanceof BambooBlock)state=state.withProperty(BambooBlock.AGE,world.getBlockState(pos.down()).getValue(BambooBlock.AGE));world.setBlockState(pos,state,3);if(below instanceof BambooShoot)world.setBlockState(pos.down(),Content.block("bamboo").getDefaultState(),3);if(!player.capabilities.isCreativeMode)stack.shrink(1);world.playSound(null,pos,net.minecraft.init.SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,1,.8F);}
        return EnumActionResult.SUCCESS;
    }
}
