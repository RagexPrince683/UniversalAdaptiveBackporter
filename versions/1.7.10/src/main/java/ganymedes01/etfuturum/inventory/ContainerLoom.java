package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.blocks.itemblocks.ItemBlockBanner;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.lib.EnumColor;
import ganymedes01.etfuturum.items.ItemBannerPattern;
import ganymedes01.etfuturum.tileentities.TileEntityBanner;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import java.util.*;

public class ContainerLoom extends ContainerSelectableWorkstation {
	private List<EnumBannerPattern> patterns = Collections.emptyList();
	public ContainerLoom(InventoryPlayer inventory, World world, int x, int y, int z) {
		super(inventory, world, x, y, z, ModBlocks.LOOM.get(), 3);
	}
	public static int dye(ItemStack stack) {
		if (stack != null) for (String ore : Utils.getOreNames(stack)) for (EnumColor color : EnumColor.VALUES)
			if (ore.equals(color.getOreName())) return color.getDamage();
		return -1;
	}
	@Override protected boolean accepts(int slot, ItemStack stack) {
		return slot == 0 ? stack.getItem() == ModBlocks.BANNER.getItem() : slot == 1 ? dye(stack) >= 0
				: stack.getItem() == ModItems.BANNER_PATTERN.get() && ItemBannerPattern.pattern(stack) != null;
	}
	@Override protected void refresh() {
		patterns = new ArrayList<>();
		ItemStack banner = inputs.getStackInSlot(0), color = inputs.getStackInSlot(1), template = inputs.getStackInSlot(2);
		if (banner != null && accepts(0, banner) && TileEntityBanner.getPatterns(banner) < 6 && dye(color) >= 0) {
			if (template != null) {
				EnumBannerPattern pattern = ItemBannerPattern.pattern(template);
				if (pattern != null && accepts(2, template)) patterns.add(pattern);
			} else for (EnumBannerPattern pattern : EnumBannerPattern.VALUES)
				if (pattern.hasValidCrafting() && !pattern.hasCraftingStack()) patterns.add(pattern);
		}
		if (selected >= patterns.size()) selected = 0;
		ItemStack result = null;
		if (!patterns.isEmpty()) result = applyPattern(banner, dye(color), patterns.get(selected));
		output.setInventorySlotContents(0, result);
	}
	public static ItemStack applyPattern(ItemStack banner, int color, EnumBannerPattern pattern) {
		ItemStack result = banner.copy(); result.stackSize = 1;
		NBTTagCompound tag = ItemBlockBanner.getSubTag(result, "BlockEntityTag", true);
		NBTTagList layers = tag.getTagList("Patterns", 10);
		NBTTagCompound layer = new NBTTagCompound();
		layer.setString("Pattern", pattern.getPatternID());
		layer.setInteger("Color", color);
		layers.appendTag(layer); tag.setTag("Patterns", layers);
		return result;
	}
	@Override public List<String> choices() {
		List<String> names = new ArrayList<>();
		for (EnumBannerPattern pattern : patterns) names.add("gui.etfuturum.loom." + pattern.getPatternName());
		return names;
	}
	@Override protected ItemStack craft(EntityPlayer player) {
		ItemStack result = output.getStackInSlot(0).copy();
		inputs.decrStackSize(0, 1); inputs.decrStackSize(1, 1);
		return result;
	}
	@Override public String title() { return "tile.etfuturum.loom.name"; }
}
