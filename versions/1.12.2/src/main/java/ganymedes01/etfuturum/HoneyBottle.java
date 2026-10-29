package ganymedes01.etfuturum;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.World;

final class HoneyBottle extends ItemFood {
    HoneyBottle() { super(6, .1F, false); setMaxStackSize(16); setAlwaysEdible(); setContainerItem(Items.GLASS_BOTTLE); }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.DRINK; }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return 40; }
    @Override public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (!world.isRemote) entity.removePotionEffect(MobEffects.POISON);
        ItemStack remaining = super.onItemUseFinish(stack, world, entity);
        if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) {
            if (remaining.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (entity instanceof EntityPlayer && !((EntityPlayer) entity).inventory.addItemStackToInventory(bottle)) entity.entityDropItem(bottle, 0);
        }
        return remaining;
    }
}
