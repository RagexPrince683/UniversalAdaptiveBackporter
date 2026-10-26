package ganymedes01.etfuturum.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.api.StrippedLogRegistry;
import ganymedes01.etfuturum.api.utils.RecipeHelper;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import ganymedes01.etfuturum.configuration.configs.ConfigExperiments;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class PaleGardenRecipes {
	private PaleGardenRecipes() { }
	public static void init() {
		if (!ConfigExperiments.enablePaleGarden) return;
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, new ItemStack(Items.dye, 1, 8), ModBlocks.EYEBLOSSOM.newItemStack(1, 0));
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, new ItemStack(Items.dye, 1, 14), ModBlocks.EYEBLOSSOM.newItemStack(1, 1));
		ItemStack planks = ModBlocks.PALE_OAK_PLANKS.newItemStack();
		OreDictionary.registerOre("plankWood", planks);
		OreDictionary.registerOre("logWood", ModBlocks.PALE_OAK_LOG.newItemStack(1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("treeLeaves", ModBlocks.PALE_OAK_LEAVES.newItemStack());
		OreDictionary.registerOre("treeSapling", ModBlocks.PALE_OAK_SAPLING.newItemStack());
		OreDictionary.registerOre("slabWood", ModBlocks.PALE_OAK_SLAB.newItemStack());
		if (ModBlocks.PALE_OAK_FENCE.isEnabled()) OreDictionary.registerOre("fenceWood", ModBlocks.PALE_OAK_FENCE.newItemStack());
		OreDictionary.registerOre("stairWood", ModBlocks.PALE_OAK_STAIRS.newItemStack());
		if (ModBlocks.PALE_OAK_SIGN.isEnabled()) OreDictionary.registerOre("signWood", ModBlocks.PALE_OAK_SIGN.newItemStack());
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_PLANKS.newItemStack(4), ModBlocks.PALE_OAK_LOG.newItemStack(1, OreDictionary.WILDCARD_VALUE));
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_SLAB.newItemStack(6), "ppp", 'p', planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_STAIRS.newItemStack(4), "p  ", "pp ", "ppp", 'p', planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_FENCE.newItemStack(3), "psp", "psp", 'p', planks, 's', "stickWood");
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_FENCE_GATE.newItemStack(), "sps", "sps", 'p', planks, 's', "stickWood");
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_BUTTON.newItemStack(), planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_PRESSURE_PLATE.newItemStack(), "pp", 'p', planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_DOOR.newItemStack(3), "pp", "pp", "pp", 'p', planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_TRAPDOOR.newItemStack(2), "ppp", "ppp", 'p', planks);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_SIGN.newItemStack(3), "ppp", "ppp", " s ", 'p', planks, 's', "stickWood");
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModItems.PALE_OAK_BOAT.newItemStack(), "p p", "ppp", 'p', planks);
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, ModItems.PALE_OAK_CHEST_BOAT.newItemStack(), ModItems.PALE_OAK_BOAT.newItemStack(), Blocks.chest);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_MOSS_CARPET.newItemStack(3), "mm", 'm', ModBlocks.PALE_MOSS.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_BLOCK.newItemStack(), "rrr", "rrr", "rrr", 'r', ModBlocks.RESIN_CLUMP.newItemStack());
		RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_CLUMP.newItemStack(9), ModBlocks.RESIN_BLOCK.newItemStack());
		GameRegistry.addSmelting(ModBlocks.RESIN_CLUMP.newItemStack(), ModItems.RESIN_BRICK.newItemStack(), 0.1F);
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_BRICKS.newItemStack(), "rr", "rr", 'r', ModItems.RESIN_BRICK.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_BRICK_SLAB.newItemStack(6), "rrr", 'r', ModBlocks.RESIN_BRICKS.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_BRICK_STAIRS.newItemStack(4), "r  ", "rr ", "rrr", 'r', ModBlocks.RESIN_BRICKS.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.RESIN_BRICK_WALL.newItemStack(6), "rrr", "rrr", 'r', ModBlocks.RESIN_BRICKS.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.CHISELED_RESIN_BRICKS.newItemStack(), "r", "r", 'r', ModBlocks.RESIN_BRICK_SLAB.newItemStack());
		RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.CREAKING_HEART.newItemStack(), "l", "r", "l", 'l', ModBlocks.PALE_OAK_LOG.newItemStack(), 'r', ModBlocks.RESIN_BLOCK.newItemStack());
		if (ConfigBlocksItems.enableBarkLogs) {
			RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_LOG.newItemStack(3, 1), "ll", "ll", 'l', ModBlocks.PALE_OAK_LOG.newItemStack());
			if (ConfigBlocksItems.enableStrippedLogs) RecipeHelper.addShapedRecipe(RecipeHelper.Priority.HIGH, ModBlocks.PALE_OAK_LOG.newItemStack(3, 3), "ll", "ll", 'l', ModBlocks.PALE_OAK_LOG.newItemStack(1, 2));
		}
		if (ConfigBlocksItems.enableStrippedLogs) {
			StrippedLogRegistry.addLog(ModBlocks.PALE_OAK_LOG.get(), 0, ModBlocks.PALE_OAK_LOG.get(), 2);
			if (ConfigBlocksItems.enableBarkLogs) StrippedLogRegistry.addLog(ModBlocks.PALE_OAK_LOG.get(), 1, ModBlocks.PALE_OAK_LOG.get(), 3);
		}
		GameRegistry.addSmelting(ModBlocks.PALE_OAK_LOG.newItemStack(1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.coal, 1, 1), 0.15F);
	}
}
