package ganymedes01.etfuturum.compatibility;

import java.util.Collections;
import static ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.*;

/** Version-specific identities from the providers' registries, not display-name guesses. */
public final class BuiltinCompatibility {
    private BuiltinCompatibility() { }
    public static void register(CompatibilityRegistry registry, String target) {
        boolean legacy = target.equals("1.7.10");
        if (!legacy && !target.equals("1.12.2")) throw new IllegalArgumentException(target);
        String bop = legacy ? "BiomesOPlenty:" : "biomesoplenty:";
        add(registry, "bop", "bamboo", bop + "bamboo", 0, CRAFTING, FOOD);
        add(registry, "bop", "bamboo", bop + "bamboo", -1, PLANT);
        if (legacy) {
            // Only 8..11 are kelp. 12..15 on the SAME block are coral; never use a wildcard recipe.
            for (int meta = 8; meta <= 11; meta++) add(registry, "bop", "kelp", bop + "coral1", meta, SMELTING, PLANT);
            add(registry, "bop", "panda_habitat", "biomesoplenty.common.biome.overworld.BiomeGenBambooForest", -1, BIOME);
            add(registry, "bop", "panda_habitat", "biomesoplenty.common.biome.overworld.forest.BiomeGenBambooForest", -1, BIOME);
        } else {
            registry.register(new CompatibilityRegistry.Entry("bop", "kelp", bop + "seaweed", 0,
                    Collections.singletonMap("variant", "kelp"), SMELTING, PLANT));
            add(registry, "bop", "panda_habitat", bop + "bamboo_forest", -1, BIOME);
        }
        for (String stone : legacy ? new String[]{"igneousStone", "metamorphicStone", "sedimentaryStone"}
                : new String[]{"igneous_stone", "metamorphic_stone", "sedimentary_stone"})
            add(registry, "undergroundbiomes", "natural_stone", (legacy ? "UndergroundBiomes:" : "undergroundbiomes:") + stone, -1, WORLDGEN, STRUCTURE);
    }
    private static void add(CompatibilityRegistry registry, String provider, String category, String id, int meta, CompatibilityRegistry.Use... uses) {
        registry.register(new CompatibilityRegistry.Entry(provider, category, id, meta, Collections.emptyMap(), uses));
    }
}
