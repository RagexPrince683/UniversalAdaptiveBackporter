package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.items.ItemSmithingTemplate;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;

/** Shared server recipe and client presentation identities. Trims never change armor attributes. */
public final class ArmorTrims {
	public static final String[] PATTERNS = {"sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide", "snout", "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host", "flow", "bolt"};
	public static final String[] MATERIALS = {"quartz", "iron", "netherite", "redstone", "copper", "gold", "emerald", "diamond", "lapis", "amethyst", "resin"};
	private ArmorTrims() { }
	public static String material(ItemStack stack) {
		if (stack == null) return null;
		Item item = stack.getItem();
		if (item == Items.quartz) return "quartz";
		if (item == Items.iron_ingot) return "iron";
		if (item == Items.redstone) return "redstone";
		if (item == Items.gold_ingot) return "gold";
		if (item == Items.emerald) return "emerald";
		if (item == Items.diamond) return "diamond";
		if (item == Items.dye && stack.getItemDamage() == 4) return "lapis";
		if (item == ModItems.NETHERITE_INGOT.get() && ModItems.NETHERITE_INGOT.isEnabled()) return "netherite";
		if (item == ModItems.COPPER_INGOT.get() && ModItems.COPPER_INGOT.isEnabled()) return "copper";
		if (item == ModItems.AMETHYST_SHARD.get() && ModItems.AMETHYST_SHARD.isEnabled()) return "amethyst";
		if (item == ModItems.RESIN_BRICK.get() && ModItems.RESIN_BRICK.isEnabled()) return "resin";
		return null;
	}
	public static NBTTagCompound trim(ItemStack stack) {
		if (stack == null || !stack.hasTagCompound()) return null;
		NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Trim");
		String pattern = tag.getString("pattern").replace("minecraft:", ""), material = tag.getString("material").replace("minecraft:", "");
		return java.util.Arrays.asList(PATTERNS).contains(pattern) && java.util.Arrays.asList(MATERIALS).contains(material) ? tag : null;
	}
	public static ItemStack apply(ItemStack template, ItemStack base, ItemStack ingredient) {
		if (!ContentAvailability.enabled("armor_trims") || template == null || !(template.getItem() instanceof ItemSmithingTemplate) || base == null || !(base.getItem() instanceof ItemArmor)) return null;
		String pattern = ((ItemSmithingTemplate) template.getItem()).pattern, material = material(ingredient);
		if (!java.util.Arrays.asList(PATTERNS).contains(pattern) || material == null) return null;
		NBTTagCompound old = trim(base);
		if (old != null && old.getString("pattern").equals("minecraft:" + pattern) && old.getString("material").equals("minecraft:" + material)) return null;
		ItemStack result = base.copy(); result.stackSize = 1;
		if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound());
		NBTTagCompound trim = new NBTTagCompound(); trim.setString("pattern", "minecraft:" + pattern); trim.setString("material", "minecraft:" + material);
		result.getTagCompound().setTag("Trim", trim); return result;
	}
}
