package ganymedes01.etfuturum.compat.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.recipes.StonecuttingRecipes;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import java.util.*;

public class StonecuttingHandler extends TemplateRecipeHandler {
	private class CuttingRecipe extends CachedRecipe {
		private final PositionedStack input, output;
		CuttingRecipe(StonecuttingRecipes.Recipe recipe) {
			input = new PositionedStack(recipe.input.copy(), 36, 18);
			output = new PositionedStack(recipe.output.copy(), 112, 18);
		}
		@Override public List<PositionedStack> getIngredients() { return Collections.singletonList(input); }
		@Override public PositionedStack getResult() { return output; }
		@Override public List<PositionedStack> getOtherStacks() { return Collections.singletonList(new PositionedStack(ModBlocks.STONECUTTER.newItemStack(), 74, 18)); }
	}
	@Override public void loadCraftingRecipes(String id, Object... results) {
		if (id.equals(getOverlayIdentifier())) for (StonecuttingRecipes.Recipe recipe : StonecuttingRecipes.all()) arecipes.add(new CuttingRecipe(recipe));
		else super.loadCraftingRecipes(id, results);
	}
	@Override public void loadCraftingRecipes(ItemStack result) {
		for (StonecuttingRecipes.Recipe recipe : StonecuttingRecipes.all()) if (result.isItemEqual(recipe.output)) arecipes.add(new CuttingRecipe(recipe));
	}
	@Override public void loadUsageRecipes(ItemStack ingredient) {
		if (ingredient.getItem() == ModBlocks.STONECUTTER.getItem()) loadCraftingRecipes(getOverlayIdentifier());
		else for (StonecuttingRecipes.Recipe recipe : StonecuttingRecipes.all()) if (ingredient.isItemEqual(recipe.input)) arecipes.add(new CuttingRecipe(recipe));
	}
	@Override public void drawBackground(int recipe) { }
	@Override public void drawExtras(int recipe) { GuiDraw.drawString(">", 96, 23, 0x404040, false); }
	@Override public String getOverlayIdentifier() { return "etfuturum.stonecutting"; }
	@Override public String getGuiTexture() { return "textures/gui/container/generic_54.png"; }
	@Override public String getRecipeName() { return I18n.format("tile.etfuturum.stonecutter.name"); }
	@Override public int recipiesPerPage() { return 3; }
}
