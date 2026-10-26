package ganymedes01.etfuturum.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class ItemOminousBottle extends BaseItem {
	public ItemOminousBottle() { super("ominous_bottle"); setMaxStackSize(1); }
	@Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.drink; }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 32; }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { player.setItemInUse(stack, 32); return stack; }
	@Override public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) { if (!world.isRemote) { player.getEntityData().setInteger("EFRBadOmen", 120000); player.getEntityData().setInteger("EFRBadOmenAmplifier", net.minecraft.util.MathHelper.clamp_int(stack.getItemDamage(), 0, 4)); } return player.capabilities.isCreativeMode ? stack : new ItemStack(Items.glass_bottle); }
}
