package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.Content;

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
		if (stack==null||stack.isEmpty())return null;
		Item item = stack.getItem();
		if (item == Items.QUARTZ) return "quartz";
		if (item == Items.IRON_INGOT) return "iron";
		if (item == Items.REDSTONE) return "redstone";
		if (item == Items.GOLD_INGOT) return "gold";
		if (item == Items.EMERALD) return "emerald";
		if (item == Items.DIAMOND) return "diamond";
		if (item == Items.DYE && stack.getItemDamage() == 4) return "lapis";
		if (item == Content.item("netherite_ingot") && Content.available("netherite_ingot")) return "netherite";
		if (item == Content.item("copper_ingot") && Content.available("copper_ingot")) return "copper";
		if (item == Content.item("amethyst_shard") && Content.available("amethyst_shard")) return "amethyst";
		if (item == Content.item("resin_brick") && Content.available("resin_brick")) return "resin";
		return null;
	}
	public static NBTTagCompound trim(ItemStack stack) {
		if (stack==null||stack.isEmpty()||!stack.hasTagCompound()) return null;
		NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Trim");
		String pattern = tag.getString("pattern").replace("minecraft:", ""), material = tag.getString("material").replace("minecraft:", "");
		return java.util.Arrays.asList(PATTERNS).contains(pattern) && java.util.Arrays.asList(MATERIALS).contains(material) ? tag : null;
	}
	public static ItemStack apply(ItemStack template, ItemStack base, ItemStack ingredient) {
		if (!ganymedes01.etfuturum.Features.enabled("content.armor_trims") || template.isEmpty() || !(template.getItem() instanceof ItemSmithingTemplate) || base.isEmpty() || !(base.getItem() instanceof ItemArmor)) return ItemStack.EMPTY;
		String pattern = ((ItemSmithingTemplate) template.getItem()).pattern, material = material(ingredient);
		if (!java.util.Arrays.asList(PATTERNS).contains(pattern) || material == null) return ItemStack.EMPTY;
		NBTTagCompound old = trim(base);
		if (old != null && old.getString("pattern").equals("minecraft:" + pattern) && old.getString("material").equals("minecraft:" + material)) return ItemStack.EMPTY;
		ItemStack result = base.copy(); result.setCount(1);
		if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound());
		NBTTagCompound trim = new NBTTagCompound(); trim.setString("pattern", "minecraft:" + pattern); trim.setString("material", "minecraft:" + material);
		result.getTagCompound().setTag("Trim", trim); return result;
	}
}
