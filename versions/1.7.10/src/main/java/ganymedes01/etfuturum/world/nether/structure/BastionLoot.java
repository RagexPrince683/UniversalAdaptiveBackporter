package ganymedes01.etfuturum.world.nether.structure;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.api.utils.RecipeHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public final class BastionLoot {
	private BastionLoot() { }
	public static ChestGenHooks get(String modernName) {
		return ChestGenHooks.getInfo("etfuturum:" + modernName.replace("minecraft:chests/", ""));
	}
	public static void init() {
		for (String variant : new String[]{"bridge", "hoglin_stable", "other", "treasure"}) {
			ChestGenHooks table = get("minecraft:chests/bastion_" + variant);
			table.setMin(3); table.setMax(6);
			add(table, new ItemStack(Items.gold_ingot), 2, 8, 10);
			add(table, new ItemStack(Items.iron_ingot), 4, 9, 10);
			add(table, new ItemStack(Items.gold_nugget), 5, 27, 10);
			add(table, new ItemStack(Items.string), 4, 6, 5);
			add(table, new ItemStack(Items.leather), 1, 3, 5);
			add(table, new ItemStack(Items.arrow), 5, 17, 10);
			add(table, new ItemStack(Blocks.obsidian), 4, 6, 5);
			add(table, ModBlocks.CRYING_OBSIDIAN.newItemStack(), 1, 5, 5);
			add(table, ModBlocks.GILDED_BLACKSTONE.newItemStack(), 1, 5, 5);
			add(table, ModItems.NETHERITE_SCRAP.newItemStack(), 1, 1, 2);
			add(table, ModItems.PIGSTEP_RECORD.newItemStack(), 1, 1, 1);
			if (ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions.enableSoulSpeed) table.addItem(new WeightedRandomChestContent(new ItemStack(Items.book), 1, 1, 5) {
				@Override protected ItemStack[] generateChestContent(java.util.Random random, net.minecraft.inventory.IInventory inventory) {
					return new ItemStack[]{Items.enchanted_book.getEnchantedItemStack(new net.minecraft.enchantment.EnchantmentData(ganymedes01.etfuturum.ModEnchantments.soulSpeed, 1 + random.nextInt(3)))};
				}
			});
			if (variant.equals("treasure")) {
				add(table, ModItems.NETHERITE_INGOT.newItemStack(), 1, 1, 2);
				add(table, ModBlocks.ANCIENT_DEBRIS.newItemStack(), 1, 2, 5);
				add(table, new ItemStack(Items.diamond), 2, 6, 5);
				add(table, new ItemStack(Items.diamond_chestplate), 1, 1, 3);
				add(table, new ItemStack(Items.diamond_sword), 1, 1, 3);
			} else if (variant.equals("hoglin_stable")) {
				add(table, new ItemStack(Items.saddle), 1, 1, 5);
				add(table, new ItemStack(Items.golden_carrot), 8, 17, 10);
				add(table, new ItemStack(Items.cooked_porkchop), 2, 5, 10);
			}
		}
	}
	private static void add(ChestGenHooks table, ItemStack stack, int min, int max, int weight) {
		if (RecipeHelper.validateItems(stack)) table.addItem(new WeightedRandomChestContent(stack, min, max, weight));
	}
}
