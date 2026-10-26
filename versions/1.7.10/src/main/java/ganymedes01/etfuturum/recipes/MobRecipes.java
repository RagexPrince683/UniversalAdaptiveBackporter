package ganymedes01.etfuturum.recipes;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraftforge.oredict.ShapedOreRecipe;
public final class MobRecipes {
	private MobRecipes() { }
	public static void init() {
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.CROSSBOW.newItemStack(), "sis", "tht", " s ", 's', Items.stick, 'i', Items.iron_ingot, 't', Items.string, 'h', Blocks.tripwire_hook);
		if (ModItems.COPPER_INGOT.isEnabled()) ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.BRUSH.newItemStack(), "f", "c", "s", 'f', Items.feather, 'c', ModItems.COPPER_INGOT.get(), 's', Items.stick);
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.WOLF_ARMOR.newItemStack(), "s  ", "sss", "s s", 's', ModItems.ARMADILLO_SCUTE.get());
		ganymedes01.etfuturum.api.utils.RecipeHelper.addShapelessRecipe(ModItems.WIND_CHARGE.newItemStack(4), ModItems.BREEZE_ROD.get());
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModBlocks.DRIED_GHAST.newItemStack(), "ggg", "gsg", "ggg", 'g', Items.ghast_tear, 's', Blocks.soul_sand);
		for (int color = 0; color < 16; color++) ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.HARNESS.newItemStack(1, color), "lll", "gwg", 'l', Items.leather, 'g', Blocks.glass, 'w', new ItemStack(Blocks.wool, 1, color));
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(new ShapedOreRecipe(ModItems.WOODEN_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', "plankWood", 's', Items.stick));
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.STONE_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', Blocks.cobblestone, 's', Items.stick);
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.IRON_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', Items.iron_ingot, 's', Items.stick);
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.GOLDEN_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', Items.gold_ingot, 's', Items.stick);
		ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.DIAMOND_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', Items.diamond, 's', Items.stick);
		if (ModItems.COPPER_INGOT.isEnabled()) { ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModItems.COPPER_SPEAR.newItemStack(), "  m", " s ", "s  ", 'm', ModItems.COPPER_INGOT.get(), 's', Items.stick); ganymedes01.etfuturum.api.utils.RecipeHelper.addRecipe(ModBlocks.COPPER_CHEST.newItemStack(), "ccc", "cxc", "ccc", 'c', ModItems.COPPER_INGOT.get(), 'x', Blocks.chest); }
		// Nautilus armor remains structure loot, as in the released game; disabled prerequisites never get substitutes.
		ganymedes01.etfuturum.api.utils.RecipeHelper.addShapelessRecipe(new ItemStack(Items.dye, 1, 14), ModBlocks.TORCHFLOWER.get());
		ganymedes01.etfuturum.api.utils.RecipeHelper.addShapelessRecipe(new ItemStack(Items.dye, 2, 6), ModBlocks.PITCHER_PLANT.get());
	}
}
