package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class GlowBerries extends ItemFood {
    GlowBerries(){super(2,.1F,false);}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing side,float x,float y,float z){
        if(!Content.available("glow_berries"))return EnumActionResult.FAIL;
        ItemStack held=player.getHeldItem(hand);if(held.isEmpty()||!player.canPlayerEdit(pos.offset(side),side,held))return EnumActionResult.FAIL;
        if(side==EnumFacing.DOWN){
            BlockPos target=pos.down();CaveVines vine=(CaveVines)Content.block("cave_vines");
            if(world.getBlockState(pos).getBlock() instanceof CaveVines&&world.isAirBlock(target)){if(!world.isRemote){CaveVines existing=(CaveVines)world.getBlockState(pos).getBlock();if(existing.tip)existing.extend(world,pos,true);else return EnumActionResult.FAIL;if(!player.capabilities.isCreativeMode)held.shrink(1);}return EnumActionResult.SUCCESS;}
            if(vine.canPlaceBlockAt(world,target)){if(!world.isRemote){world.setBlockState(target,vine.getDefaultState(),3);if(!player.capabilities.isCreativeMode)held.shrink(1);}return EnumActionResult.SUCCESS;}
        }
        return EnumActionResult.PASS;
    }
}
