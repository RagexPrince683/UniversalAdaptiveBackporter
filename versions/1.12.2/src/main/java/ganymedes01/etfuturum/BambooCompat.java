package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Registry-only integration; Biomes O' Plenty remains optional. */
public final class BambooCompat {
    private static final ResourceLocation PLANT = new ResourceLocation("biomesoplenty", "bamboo");
    private static final ResourceLocation HABITAT = new ResourceLocation("biomesoplenty", "bamboo_forest");

    private BambooCompat() { }

    public static Block externalPlant() {
        return ForgeRegistries.BLOCKS.containsKey(PLANT) ? ForgeRegistries.BLOCKS.getValue(PLANT) : null;
    }

    public static Item externalItem() {
        Block plant = externalPlant();
        Item item = plant == null ? null : Item.getItemFromBlock(plant);
        return item == Items.AIR ? null : item;
    }

    public static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && (Content.available("bamboo") && stack.getItem() == Content.item("bamboo")
            || externalItem() != null && stack.getItem() == externalItem() && stack.getMetadata() == 0);
    }

    public static boolean isPlant(Block block) {
        return block == Content.block("bamboo") || block == externalPlant();
    }

    public static boolean isPandaHabitat(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE) || HABITAT.equals(biome.getRegistryName());
    }
}
