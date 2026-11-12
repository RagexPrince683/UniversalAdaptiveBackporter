package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.compatibility.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.biome.BiomeGenBase;
import static ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.*;

/** Native metadata adapter. Registry lookup is deliberately delayed until each consuming lifecycle. */
public final class ContentCompatibility {
    public static final CompatibilityRegistry REGISTRY = new CompatibilityRegistry(ContentCompatibility::enabled);
    static { BuiltinCompatibility.register(REGISTRY, "1.7.10"); }
    private ContentCompatibility() { }
    public static boolean enabled(String provider) { return !java.util.Arrays.asList("bop", "witchery", "undergroundbiomes").contains(provider) || UabConfiguration.get().bool("modcompat", "provider_" + provider, true); }
    public static List<ItemStack> inputs(String category, CompatibilityRegistry.Use use) {
        List<ItemStack> result = new ArrayList<>();
        for (CompatibilityRegistry.Entry entry : REGISTRY.entries(category, use)) {
            String[] id = entry.identity.split(":", 2);
            Item item = id.length == 2 ? GameRegistry.findItem(id[0], id[1]) : null;
            if (item != null && entry.metadata >= 0) if (result.stream().noneMatch(stack -> stack.getItem() == item && stack.getItemDamage() == entry.metadata)) result.add(new ItemStack(item, 1, entry.metadata));
        }
        return result;
    }
    public static boolean matches(String category, CompatibilityRegistry.Use use, ItemStack stack) {
        return stack != null && REGISTRY.matches(category, use, String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem())),
                stack.getItemDamage(), Collections.emptyMap());
    }
    public static Block firstPlant(String category) {
        for (CompatibilityRegistry.Entry entry : REGISTRY.entries(category, PLANT)) {
            String[] id = entry.identity.split(":", 2);
            if (id.length == 2) { Block block = GameRegistry.findBlock(id[0], id[1]); if (block != null) return block; }
        }
        return null;
    }
    public static boolean block(String category, CompatibilityRegistry.Use use, Block block, int meta) {
        return block != null && REGISTRY.matches(category, use, String.valueOf(Block.blockRegistry.getNameForObject(block)), meta, Collections.emptyMap());
    }
    public static boolean habitat(BiomeGenBase biome) {
        return REGISTRY.matches("panda_habitat", BIOME, biome.getClass().getName(), 0, Collections.emptyMap());
    }
    public static boolean hasNineMaterialRecipe(ItemStack input) {
        for (Object object : net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList()) {
            Object[] ingredients;
            if (object instanceof net.minecraftforge.oredict.ShapedOreRecipe) ingredients = ((net.minecraftforge.oredict.ShapedOreRecipe)object).getInput();
            else if (object instanceof net.minecraftforge.oredict.ShapelessOreRecipe) ingredients = ((net.minecraftforge.oredict.ShapelessOreRecipe)object).getInput().toArray();
            else if (object instanceof net.minecraft.item.crafting.ShapedRecipes) ingredients = ((net.minecraft.item.crafting.ShapedRecipes)object).recipeItems;
            else if (object instanceof net.minecraft.item.crafting.ShapelessRecipes) ingredients = ((net.minecraft.item.crafting.ShapelessRecipes)object).recipeItems.toArray();
            else continue;
            if (ingredients.length != 9) continue;
            boolean matches = true;
            for (Object ingredient : ingredients) {
                if (ingredient instanceof java.util.List) {
                    boolean any = false;
                    for (Object choice : (java.util.List<?>)ingredient) if (choice instanceof ItemStack && net.minecraftforge.oredict.OreDictionary.itemMatches((ItemStack)choice, input, false)) { any = true; break; }
                    if (!any) { matches = false; break; }
                } else if (!(ingredient instanceof ItemStack) || !net.minecraftforge.oredict.OreDictionary.itemMatches((ItemStack)ingredient, input, false)) { matches = false; break; }
            }
            if (matches) return true;
        }
        return false;
    }
    public static void smelting(String category, ItemStack output, float experience) {
        for (ItemStack input : inputs(category, SMELTING)) {
            // Includes existing wildcard registrations. Never take ownership of another mod's recipe.
            if (FurnaceRecipes.smelting().getSmeltingResult(input) != null) continue;
            GameRegistry.addSmelting(input, output.copy(), experience);
            if (ganymedes01.etfuturum.recipes.SmokerRecipes.smelting().getSmeltingResult(input) == null)
                ganymedes01.etfuturum.recipes.SmokerRecipes.smelting().addRecipe(input, output.copy(), experience);
        }
    }
}
