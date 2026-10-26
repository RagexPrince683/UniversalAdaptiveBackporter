package ganymedes01.etfuturum.compat.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.*;

/** Map identity and world data are finalized by the server; these display the three processing operations. */
public class CartographyHandler extends TemplateRecipeHandler {
	private class MapRecipe extends CachedRecipe {
		private final List<PositionedStack> inputs;
		private final PositionedStack result;
		private final String operation;
		MapRecipe(int operation) {
			this.operation = new String[]{"copy", "scale", "lock"}[operation];
			ItemStack ingredient = operation == 0 ? new ItemStack(Items.map) : operation == 1 ? new ItemStack(Items.paper) : new ItemStack(Blocks.glass_pane);
			inputs = Arrays.asList(new PositionedStack(new ItemStack(Items.filled_map), 24, 18), new PositionedStack(ingredient, 48, 18));
			result = new PositionedStack(new ItemStack(Items.filled_map, operation == 0 ? 2 : 1), 128, 18);
		}
		@Override public List<PositionedStack> getIngredients() { return inputs; }
		@Override public PositionedStack getResult() { return result; }
		@Override public List<PositionedStack> getOtherStacks() { return Collections.singletonList(new PositionedStack(ModBlocks.CARTOGRAPHY_TABLE.newItemStack(), 90, 18)); }
	}
	@Override public void loadCraftingRecipes(String id, Object... results) {
		if (id.equals(getOverlayIdentifier())) for (int i = 0; i < 3; i++) arecipes.add(new MapRecipe(i)); else super.loadCraftingRecipes(id, results);
	}
	@Override public void loadCraftingRecipes(ItemStack result) { if (result.getItem() == Items.filled_map) loadCraftingRecipes(getOverlayIdentifier()); }
	@Override public void loadUsageRecipes(ItemStack ingredient) {
		if (ingredient.getItem() == ModBlocks.CARTOGRAPHY_TABLE.getItem() || ingredient.getItem() == Items.filled_map) loadCraftingRecipes(getOverlayIdentifier());
		else for (int i = 0; i < 3; i++) { MapRecipe recipe = new MapRecipe(i); if (ingredient.isItemEqual(recipe.inputs.get(1).items[0])) arecipes.add(recipe); }
	}
	@Override public void drawBackground(int recipe) { }
	@Override public void drawExtras(int recipe) { GuiDraw.drawString(I18n.format("gui.etfuturum.cartography." + ((MapRecipe) arecipes.get(recipe)).operation), 24, 42, 0x404040, false); }
	@Override public String getOverlayIdentifier() { return "etfuturum.cartography"; }
	@Override public String getGuiTexture() { return "textures/gui/container/generic_54.png"; }
	@Override public String getRecipeName() { return I18n.format("tile.etfuturum.cartography_table.name"); }
	@Override public int recipiesPerPage() { return 3; }
}
