package ganymedes01.etfuturum.compat.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.items.ItemSmithingTemplate;
import ganymedes01.etfuturum.recipes.ArmorTrims;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.init.Items;
import ganymedes01.etfuturum.recipes.SmithingTableRecipes;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;

/** Views the authoritative smithing registry, including scripts and ore-dictionary alternatives. */
public class SmithingHandler extends TemplateRecipeHandler {
	private class Trim extends CachedRecipe {
		private final List<PositionedStack> ingredients;
		private final PositionedStack output;
		Trim(ItemStack template, ItemStack armor, ItemStack material, ItemStack result) {
			ingredients = Arrays.asList(new PositionedStack(template, 5, 18), new PositionedStack(armor, 45, 18), new PositionedStack(material, 85, 18));
			output = new PositionedStack(result, 139, 18);
		}
		@Override public List<PositionedStack> getIngredients() { return ingredients; }
		@Override public PositionedStack getResult() { return output; }
	}
	private void trims(ItemStack usage, ItemStack requested) {
		if (!ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims")) return;
		List<ItemStack> materials = new ArrayList<>();
		for (Item item : new Item[]{Items.quartz, Items.iron_ingot, Items.redstone, Items.gold_ingot, Items.emerald, Items.diamond, ModItems.NETHERITE_INGOT.get(), ModItems.COPPER_INGOT.get(), ModItems.AMETHYST_SHARD.get(), ModItems.RESIN_BRICK.get()}) { ItemStack stack = new ItemStack(item); if (ArmorTrims.material(stack) != null) materials.add(stack); }
		materials.add(new ItemStack(Items.dye, 1, 4));
		for (ModItems template : ModItems.values()) if (template.isEnabled() && template.get() instanceof ItemSmithingTemplate && !((ItemSmithingTemplate) template.get()).pattern.equals("netherite_upgrade")) {
			for (Object value : Item.itemRegistry) {
				if (!(value instanceof ItemArmor) || !ganymedes01.etfuturum.configuration.ContentAvailability.available(value)) continue;
				ItemStack armor = new ItemStack((Item) value);
				if (requested != null && !armor.isItemEqual(requested)) continue;
				for (ItemStack material : materials) {
					if (usage != null && !template.newItemStack().isItemEqual(usage) && !armor.isItemEqual(usage) && !material.isItemEqual(usage)) continue;
					ItemStack result = ArmorTrims.apply(template.newItemStack(), armor, material);
					if (result != null && (requested == null || ArmorTrims.trim(requested) == null || ItemStack.areItemStackTagsEqual(result, requested))) arecipes.add(new Trim(template.newItemStack(), armor, material, result));
				}
			}
		}
	}
	private class Upgrade extends CachedRecipe {
		private final List<PositionedStack> ingredients;
		private final PositionedStack output;
		Upgrade(SmithingTableRecipes.SmithingTableRecipe recipe) {
			ingredients = new ArrayList<>();
			if (recipe.getTemplate() != null) ingredients.add(new PositionedStack(recipe.getTemplate(), 5, 18));
			ingredients.add(new PositionedStack(recipe.getInputItem(), 45, 18));
			ingredients.add(new PositionedStack(recipe.getMaterial(), 85, 18));
			output = new PositionedStack(recipe.getRecipeOutput().copy(), 139, 18);
		}
		@Override public List<PositionedStack> getIngredients() { return getCycledIngredients(cycleticks / 20, ingredients); }
		@Override public PositionedStack getResult() { return output; }
		@Override public List<PositionedStack> getOtherStacks() { return Collections.singletonList(new PositionedStack(ModBlocks.SMITHING_TABLE.newItemStack(), 66, 45)); }
	}
	private boolean matches(Object input, ItemStack stack) {
		if (input instanceof ItemStack item) return OreDictionary.itemMatches(item, stack, false);
		if (input instanceof List<?> alternatives) for (Object value : alternatives) if (value instanceof ItemStack item && OreDictionary.itemMatches(item, stack, false)) return true;
		return false;
	}
	@Override public void loadCraftingRecipes(String id, Object... results) { if (id.equals(getOverlayIdentifier())) { for (SmithingTableRecipes.SmithingTableRecipe recipe : SmithingTableRecipes.getInstance().getRecipes()) arecipes.add(new Upgrade(recipe)); trims(null, null); } else super.loadCraftingRecipes(id, results); }
	@Override public void loadCraftingRecipes(ItemStack output) { for (SmithingTableRecipes.SmithingTableRecipe recipe : SmithingTableRecipes.getInstance().getRecipes()) if (recipe.getRecipeOutput().isItemEqual(output)) arecipes.add(new Upgrade(recipe)); trims(null, output); }
	@Override public void loadUsageRecipes(ItemStack input) {
		if (input.getItem() == ModBlocks.SMITHING_TABLE.getItem()) { loadCraftingRecipes(getOverlayIdentifier()); return; }
		for (SmithingTableRecipes.SmithingTableRecipe recipe : SmithingTableRecipes.getInstance().getRecipes()) if (matches(recipe.getInputItem(), input) || matches(recipe.getMaterial(), input) || matches(recipe.getTemplate(), input)) arecipes.add(new Upgrade(recipe));
		trims(input, null);
	}
	@Override public void drawBackground(int recipe) {
		org.lwjgl.opengl.GL11.glColor4f(1, 1, 1, 1);
		GuiDraw.changeTexture(getGuiTexture());
		// Reuse the workstation's empty slot frame behind each 16x16 ingredient/result.
		for (int x : new int[]{4, 44, 84, 138}) GuiDraw.drawTexturedModalRect(x, 17, 7, 46, 18, 18);
	}
	@Override public void drawExtras(int recipe) { GuiDraw.drawString("+", 29, 23, 0x404040, false); GuiDraw.drawString(">", 119, 23, 0x404040, false); }
	@Override public String getOverlayIdentifier() { return "etfuturum.smithing"; }
	@Override public String getGuiTexture() { return "textures/gui/container/smithing.png"; }
	@Override public String getRecipeName() { return I18n.format("tile.etfuturum.smithing_table.name"); }
	@Override public int recipiesPerPage() { return 2; }
}
