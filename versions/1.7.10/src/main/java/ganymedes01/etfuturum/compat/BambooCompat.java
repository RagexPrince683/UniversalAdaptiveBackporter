package ganymedes01.etfuturum.compat;

import cpw.mods.fml.common.registry.GameRegistry;
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

	public static Block externalPlant() {
		return GameRegistry.findBlock("BiomesOPlenty", "bamboo");
	}

	public static Item externalItem() {
		Block plant = externalPlant();
		return plant == null ? null : Item.getItemFromBlock(plant);
	}

	public static boolean isFood(ItemStack stack) {
		return stack != null && (ModItems.BAMBOO.isEnabled() && stack.getItem() == ModItems.BAMBOO.get()
				|| externalItem() != null && stack.getItem() == externalItem() && stack.getItemDamage() == 0);
	}

	public static boolean isPlant(Block block) {
		return block == ModBlocks.BAMBOO.get() || block == externalPlant();
	}

	public static boolean isPandaHabitat(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.JUNGLE)
				|| biome.getClass().getName().equals("biomesoplenty.common.biome.overworld.BiomeGenBambooForest");
	}
}
