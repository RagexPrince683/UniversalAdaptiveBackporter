package ganymedes01.etfuturum.compat.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.inventory.ContainerLoom;
import ganymedes01.etfuturum.items.ItemBannerPattern;
import ganymedes01.etfuturum.tileentities.TileEntityBanner.EnumBannerPattern;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.*;

public class LoomHandler extends TemplateRecipeHandler {
	private class PatternRecipe extends CachedRecipe {
		private final List<PositionedStack> inputs = new ArrayList<>();
		private final PositionedStack result;
		PatternRecipe(EnumBannerPattern pattern, int color, ItemStack banner) {
			inputs.add(new PositionedStack(banner, 12, 18));
			inputs.add(new PositionedStack(new ItemStack(Items.dye, 1, color), 34, 18));
			if (pattern.hasCraftingStack()) inputs.add(new PositionedStack(ModItems.BANNER_PATTERN.newItemStack(1, ItemBannerPattern.patterns().indexOf(pattern)), 56, 18));
			result = new PositionedStack(ContainerLoom.applyPattern(banner, 15 - color, pattern), 132, 18);
		}
		@Override public List<PositionedStack> getIngredients() { return inputs; }
		@Override public PositionedStack getResult() { return result; }
		@Override public List<PositionedStack> getOtherStacks() { return Collections.singletonList(new PositionedStack(ModBlocks.LOOM.newItemStack(), 94, 18)); }
	}
	private void add(ItemStack query, boolean usage) {
		if (usage && query != null && query.getItem() != ModBlocks.BANNER.getItem() && query.getItem() != Items.dye && query.getItem() != ModItems.BANNER_PATTERN.get()) return;
		ItemStack banner = ModBlocks.BANNER.newItemStack(1, 15);
		if (query != null && query.getItem() == ModBlocks.BANNER.getItem()) { banner = query.copy(); banner.stackSize = 1; if (!usage) banner.setTagCompound(null); }
		if (ganymedes01.etfuturum.tileentities.TileEntityBanner.getPatterns(banner) >= 6) return;
		for (EnumBannerPattern pattern : EnumBannerPattern.VALUES) if (pattern.hasValidCrafting()) for (int dye = 0; dye < 16; dye++) {
			PatternRecipe recipe = new PatternRecipe(pattern, dye, banner);
			boolean matches = query == null || !usage && query.getItem() == ModBlocks.BANNER.getItem();
			if (usage) for (PositionedStack input : recipe.inputs) if (input.items[0].isItemEqual(query)) matches = true;
			if (matches) arecipes.add(recipe);
		}
	}
	@Override public void loadCraftingRecipes(String id, Object... results) { if (id.equals(getOverlayIdentifier())) add(null, false); else super.loadCraftingRecipes(id, results); }
	@Override public void loadCraftingRecipes(ItemStack result) { if (result.getItem() == ModBlocks.BANNER.getItem()) add(result, false); }
	@Override public void loadUsageRecipes(ItemStack ingredient) { if (ingredient.getItem() == ModBlocks.LOOM.getItem()) add(null, false); else add(ingredient, true); }
	@Override public void drawBackground(int recipe) { }
	@Override public void drawExtras(int recipe) { GuiDraw.drawString(">", 118, 23, 0x404040, false); GuiDraw.drawString(I18n.format("gui.etfuturum.loom.reusable"), 8, 42, 0x404040, false); }
	@Override public String getOverlayIdentifier() { return "etfuturum.loom"; }
	@Override public String getGuiTexture() { return "textures/gui/container/generic_54.png"; }
	@Override public String getRecipeName() { return I18n.format("tile.etfuturum.loom.name"); }
	@Override public int recipiesPerPage() { return 3; }
}
