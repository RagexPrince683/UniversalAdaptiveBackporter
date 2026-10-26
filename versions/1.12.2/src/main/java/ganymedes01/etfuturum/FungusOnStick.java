package ganymedes01.etfuturum;

import ganymedes01.etfuturum.entities.EntityStrider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

final class FungusOnStick extends Item {
    FungusOnStick(){setMaxStackSize(1);setMaxDamage(100);}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){ItemStack stack=player.getHeldItem(hand);if(player.getRidingEntity() instanceof EntityStrider&&((EntityStrider)player.getRidingEntity()).isSaddled()){if(!world.isRemote&&((EntityStrider)player.getRidingEntity()).boost()){if(!player.capabilities.isCreativeMode){stack.damageItem(1,player);if(stack.isEmpty()){ItemStack rod=new ItemStack(Items.FISHING_ROD);if(stack.hasTagCompound())rod.setTagCompound(stack.getTagCompound().copy());return new ActionResult<>(EnumActionResult.SUCCESS,rod);}}}return new ActionResult<>(EnumActionResult.SUCCESS,stack);}return new ActionResult<>(EnumActionResult.PASS,stack);}
}
