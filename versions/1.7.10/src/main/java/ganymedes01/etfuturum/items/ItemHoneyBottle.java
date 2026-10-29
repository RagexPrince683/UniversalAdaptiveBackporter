package ganymedes01.etfuturum.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class ItemHoneyBottle extends BaseItem {
	public ItemHoneyBottle() {
		setNames("honey_bottle");
		setContainerItem(Items.glass_bottle);
		setMaxStackSize(16);
	}

	@Override
	public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_) {
		if (!p_77654_3_.capabilities.isCreativeMode) {
			--p_77654_1_.stackSize;
		}

		if (!p_77654_2_.isRemote) {
			p_77654_3_.getFoodStats().addStats(6, 0.1F);
			p_77654_3_.removePotionEffect(Potion.poison.id);
		}

		if (!p_77654_3_.capabilities.isCreativeMode) {
			if (p_77654_1_.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}

			ItemStack bottle = new ItemStack(Items.glass_bottle);
			if (!p_77654_3_.inventory.addItemStackToInventory(bottle)) p_77654_3_.dropPlayerItemWithRandomChoice(bottle, false);
		}

		return p_77654_1_;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
		player.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
		return itemStackIn;
	}
}
