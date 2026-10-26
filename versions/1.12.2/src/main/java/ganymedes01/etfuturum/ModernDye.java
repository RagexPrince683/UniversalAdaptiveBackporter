package ganymedes01.etfuturum;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ModernDye extends Item {
    final EnumDyeColor color;
    ModernDye(EnumDyeColor color){this.color=color;}
    public static int color(ItemStack stack){return stack.getItem()==Items.DYE?EnumDyeColor.byDyeDamage(stack.getMetadata()).getMetadata():stack.getItem() instanceof ModernDye?((ModernDye)stack.getItem()).color.getMetadata():-1;}
    ItemStack nativeStack(ItemStack stack){ItemStack result=new ItemStack(Items.DYE,stack.getCount(),color.getDyeDamage());if(stack.hasTagCompound())result.setTagCompound(stack.getTagCompound().copy());return result;}
    @Override public boolean itemInteractionForEntity(ItemStack stack,EntityPlayer player,EntityLivingBase entity,EnumHand hand){ItemStack nativeDye=nativeStack(stack);int before=nativeDye.getCount();boolean result=Items.DYE.itemInteractionForEntity(nativeDye,player,entity,hand);if(result)stack.shrink(before-nativeDye.getCount());return result;}
}
