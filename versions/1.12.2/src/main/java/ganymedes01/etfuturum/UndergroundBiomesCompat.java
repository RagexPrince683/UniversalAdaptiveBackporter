package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Natural UBC geology only; crafted stone and ore variants keep their own behavior. */
final class UndergroundBiomesCompat {
    private UndergroundBiomesCompat() { }

    static boolean isNaturalStone(Block block) {
        return block != null && (block == Stones.IGNEOUS || block == Stones.METAMORPHIC || block == Stones.SEDIMENTARY);
    }

    // Resolve after block registration, when generation first asks about optional geology.
    private static final class Stones {
        static final Block IGNEOUS = find("igneous_stone");
        static final Block METAMORPHIC = find("metamorphic_stone");
        static final Block SEDIMENTARY = find("sedimentary_stone");

        private static Block find(String name) {
            ResourceLocation id = new ResourceLocation("undergroundbiomes", name);
            return ForgeRegistries.BLOCKS.containsKey(id) ? ForgeRegistries.BLOCKS.getValue(id) : null;
        }
    }
}
