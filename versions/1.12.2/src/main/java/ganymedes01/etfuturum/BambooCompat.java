package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
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
        return !stack.isEmpty() && Content.available("bamboo") && stack.getItem() == Content.item("bamboo") || ContentCompatibility.matches("bamboo", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.FOOD, stack);
    }
    public static boolean isPlant(Block block) {
        return block == Content.block("bamboo") || block != null && ContentCompatibility.block("bamboo", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.PLANT, block.getDefaultState());
    }
    public static boolean isPandaHabitat(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE) || ContentCompatibility.habitat(biome);
    }
}
