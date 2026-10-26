package ganymedes01.etfuturum.recipes;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.api.utils.RecipeHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class OceanRecipes {
	private OceanRecipes() { }
	public static void init() {
		RecipeHelper.addRecipe(ModBlocks.CONDUIT.newItemStack(), "sss", "shs", "sss", 's', ModItems.NAUTILUS_SHELL.get(), 'h', ModItems.HEART_OF_THE_SEA.get());
		RecipeHelper.addSmelting(ModBlocks.KELP.get(), ModItems.DRIED_KELP.newItemStack(), .1F);
		if (ModBlocks.KELP.isEnabled() && ModItems.DRIED_KELP.isEnabled()) SmokerRecipes.smelting().addRecipe(ModBlocks.KELP.newItemStack(), ModItems.DRIED_KELP.newItemStack(), .1F);
		RecipeHelper.addRecipe(ModBlocks.DRIED_KELP_BLOCK.newItemStack(), "kkk", "kkk", "kkk", 'k', ModItems.DRIED_KELP.get());
		RecipeHelper.addShapelessRecipe(ModItems.DRIED_KELP.newItemStack(9), ModBlocks.DRIED_KELP_BLOCK.get());
		RecipeHelper.addSmelting(ModBlocks.SEA_PICKLE.get(), new ItemStack(Items.dye, 1, 10), .1F);
		RecipeHelper.registerOre("cropKelp", ModBlocks.KELP.get());
		RecipeHelper.registerOre("foodDriedKelp", ModItems.DRIED_KELP.get());
		RecipeHelper.registerOre("blockDriedKelp", ModBlocks.DRIED_KELP_BLOCK.get());
		RecipeHelper.registerOre("cropSeaPickle", ModBlocks.SEA_PICKLE.get());
		GameRegistry.registerFuelHandler(stack -> stack != null && stack.getItem() == ModBlocks.DRIED_KELP_BLOCK.getItem() && ModBlocks.DRIED_KELP_BLOCK.isEnabled() ? 4000 : 0);
	}
}
