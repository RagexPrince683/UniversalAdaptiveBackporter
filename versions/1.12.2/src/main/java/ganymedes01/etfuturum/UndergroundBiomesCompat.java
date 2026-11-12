package ganymedes01.etfuturum;
import net.minecraft.block.Block;
/** Explicit natural geology provider; crafted stones and ores are excluded. */
final class UndergroundBiomesCompat {
    private UndergroundBiomesCompat() { }
    static boolean isNaturalStone(Block block) {
        return block != null && ContentCompatibility.block("natural_stone", ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.WORLDGEN, block.getDefaultState());
    }
}
