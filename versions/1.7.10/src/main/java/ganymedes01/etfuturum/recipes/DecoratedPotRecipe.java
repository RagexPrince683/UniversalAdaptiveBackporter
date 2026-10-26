package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.items.ItemPotterySherd;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.*;
import net.minecraft.world.World;

public final class DecoratedPotRecipe implements IRecipe {
	private static final int[] SLOTS = {1, 3, 5, 7};
	@Override public boolean matches(InventoryCrafting grid, World world) {
		if (!ModBlocks.DECORATED_POT.isEnabled() || grid.getSizeInventory() != 9) return false;
		for (int i = 0; i < 9; i++) { ItemStack stack = grid.getStackInSlot(i); boolean side = i == 1 || i == 3 || i == 5 || i == 7;
			if (side ? stack == null || stack.getItem() != Items.brick && (!(stack.getItem() instanceof ItemPotterySherd) || !ganymedes01.etfuturum.configuration.ContentAvailability.available(stack)) : stack != null) return false;
		} return true;
	}
	@Override public ItemStack getCraftingResult(InventoryCrafting grid) {
		ItemStack result = getRecipeOutput(); NBTTagCompound tag = new NBTTagCompound(), block = new NBTTagCompound(); NBTTagList list = new NBTTagList();
		for (int slot : SLOTS) { Item item = grid.getStackInSlot(slot).getItem(); list.appendTag(new NBTTagString(item instanceof ItemPotterySherd ? "etfuturum:" + ((ItemPotterySherd) item).pattern + "_pottery_sherd" : "minecraft:brick")); }
		block.setTag("sherds", list); tag.setTag("BlockEntityTag", block); result.setTagCompound(tag); return result;
	}
	@Override public int getRecipeSize() { return 4; }
	@Override public ItemStack getRecipeOutput() { return ModBlocks.DECORATED_POT.newItemStack(); }
}
