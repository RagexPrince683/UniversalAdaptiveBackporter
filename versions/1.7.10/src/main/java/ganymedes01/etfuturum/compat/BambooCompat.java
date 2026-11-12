package ganymedes01.etfuturum.compat;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

/** Registry-only integration; Biomes O' Plenty remains optional. */
public final class BambooCompat {
    private BambooCompat() { }
    public static Block externalPlant() { return ContentCompatibility.firstPlant("bamboo"); }
    public static Item externalItem() {
        java.util.List<ItemStack> inputs = ContentCompatibility.inputs("bamboo", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.FOOD);
        return inputs.isEmpty() ? null : inputs.get(0).getItem();
    }
    public static boolean isFood(ItemStack stack) {
        return stack != null && ModItems.BAMBOO.isEnabled() && stack.getItem() == ModItems.BAMBOO.get() || ContentCompatibility.matches("bamboo", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.FOOD, stack);
    }
    public static boolean isPlant(Block block) {
        return block == ModBlocks.BAMBOO.get() || ContentCompatibility.block("bamboo", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.PLANT, block, 0);
    }
    public static boolean isPandaHabitat(BiomeGenBase biome) {
        return BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.JUNGLE) || ContentCompatibility.habitat(biome);
    }
}
