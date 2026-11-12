package ganymedes01.etfuturum;

import ganymedes01.etfuturum.compatibility.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.*;
import static ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.*;

/** Native registry/state adapter; no references to optional providers' Java types. */
public final class ContentCompatibility {
    public static final CompatibilityRegistry REGISTRY = new CompatibilityRegistry(ContentCompatibility::enabled);
    static { BuiltinCompatibility.register(REGISTRY, "1.12.2"); }
    private ContentCompatibility() { }
    public static boolean enabled(String provider) { return !java.util.Arrays.asList("bop", "witchery", "undergroundbiomes").contains(provider) || UabConfiguration.get().bool("modcompat", "provider_" + provider, true); }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String,String> properties(IBlockState state) {
        Map<String,String> result = new HashMap<>();
        for (IProperty property : state.getPropertyKeys()) result.put(property.getName(), property.getName(state.getValue(property)));
        return result;
    }
    public static List<ItemStack> inputs(String category, CompatibilityRegistry.Use use) {
        List<ItemStack> result = new ArrayList<>();
        for (CompatibilityRegistry.Entry entry : REGISTRY.entries(category, use)) {
            ResourceLocation id = new ResourceLocation(entry.identity);
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null || item == Items.AIR || entry.metadata < 0) continue;
            if (!entry.properties.isEmpty()) {
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                if (block == null || !properties(block.getStateFromMeta(entry.metadata)).entrySet().containsAll(entry.properties.entrySet())) continue;
            }
            if (result.stream().noneMatch(stack -> stack.getItem() == item && stack.getMetadata() == entry.metadata)) result.add(new ItemStack(item, 1, entry.metadata));
        }
        return result;
    }
    public static boolean matches(String category, CompatibilityRegistry.Use use, ItemStack stack) {
        return !stack.isEmpty() && REGISTRY.matches(category, use, String.valueOf(stack.getItem().getRegistryName()), stack.getMetadata(), Collections.emptyMap());
    }
    public static Block firstPlant(String category) {
        for (CompatibilityRegistry.Entry entry : REGISTRY.entries(category, PLANT)) {
            ResourceLocation id = new ResourceLocation(entry.identity);
            if (ForgeRegistries.BLOCKS.containsKey(id)) return ForgeRegistries.BLOCKS.getValue(id);
        }
        return null;
    }
    public static boolean block(String category, CompatibilityRegistry.Use use, IBlockState state) {
        return REGISTRY.matches(category, use, String.valueOf(state.getBlock().getRegistryName()), state.getBlock().getMetaFromState(state), properties(state));
    }
    public static boolean habitat(Biome biome) { return REGISTRY.matches("panda_habitat", BIOME, String.valueOf(biome.getRegistryName()), 0, Collections.emptyMap()); }
    public static boolean hasNineMaterialRecipe(ItemStack input) {
        for (net.minecraft.item.crafting.IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            if (recipe.getIngredients().size() != 9 || !recipe.canFit(3, 3)) continue;
            boolean matches = true;
            for (net.minecraft.item.crafting.Ingredient ingredient : recipe.getIngredients()) if (!ingredient.apply(input)) { matches = false; break; }
            if (matches) return true;
        }
        return false;
    }
    public static void smelting(String category, ItemStack output, float experience) {
        for (ItemStack input : inputs(category, SMELTING)) if (FurnaceRecipes.instance().getSmeltingResult(input).isEmpty())
            GameRegistry.addSmelting(input, output.copy(), experience);
    }
}
