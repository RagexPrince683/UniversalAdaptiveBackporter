package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.entities.EntityStrider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWarpedFungusOnAStick extends BaseItem {
	public ItemWarpedFungusOnAStick() { super("warped_fungus_on_a_stick"); setMaxStackSize(1); setMaxDamage(100); }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote && player.ridingEntity instanceof EntityStrider && ((EntityStrider) player.ridingEntity).boost() && !player.capabilities.isCreativeMode) stack.damageItem(1, player);
		if (stack.stackSize == 0) {
			ItemStack rod = new ItemStack(net.minecraft.init.Items.fishing_rod);
			if (stack.hasTagCompound()) rod.setTagCompound((net.minecraft.nbt.NBTTagCompound) stack.getTagCompound().copy());
			return rod;
		}
		return stack;
	}
}
