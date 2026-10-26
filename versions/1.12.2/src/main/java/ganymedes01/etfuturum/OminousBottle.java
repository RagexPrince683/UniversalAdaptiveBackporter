package ganymedes01.etfuturum;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

final class OminousBottle extends Item {
    OminousBottle(){setMaxStackSize(1);setHasSubtypes(true);}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.DRINK;}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 32;}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){player.setActiveHand(hand);return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));}
    @Override public ItemStack onItemUseFinish(ItemStack stack,World world,EntityLivingBase living){if(!world.isRemote){living.getEntityData().setInteger("EFRBadOmen",120000);living.getEntityData().setInteger("EFRBadOmenAmplifier",MathHelper.clamp(stack.getMetadata(),0,4));}return living instanceof EntityPlayer&&((EntityPlayer)living).capabilities.isCreativeMode?stack:new ItemStack(Items.GLASS_BOTTLE);}
}
