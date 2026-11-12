package ganymedes01.etfuturum.compatibility;

import java.util.*;
import static ganymedes01.etfuturum.compatibility.CompatibilityRegistry.Use.*;

public final class CompatibilityTest {
    private static int assertions;
    private static void check(boolean value, String message) { assertions++; if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Set<String> disabled = new HashSet<>();
        for (String target : new String[]{"1.7.10", "1.12.2"}) {
            CompatibilityRegistry registry = new CompatibilityRegistry(provider -> !disabled.contains(provider));
            BuiltinCompatibility.register(registry, target);
            BuiltinCompatibility.register(registry, target);
            boolean old = target.equals("1.7.10");
            String bamboo = old ? "BiomesOPlenty:bamboo" : "biomesoplenty:bamboo";
            check(registry.entries("bamboo", CRAFTING).size() == 1, "repeated provider bootstrap is idempotent");
            check(registry.matches("bamboo", CRAFTING, bamboo, 0, Collections.emptyMap()), "supported crafting item");
            check(!registry.matches("bamboo", CRAFTING, bamboo, 1, Collections.emptyMap()), "item metadata is exact");
            check(!registry.matches("bamboo", CRAFTING, "other:bamboo", 0, Collections.emptyMap()), "names do not imply equivalence");
            check(registry.matches("bamboo", PLANT, bamboo, 15, Collections.emptyMap()), "growth age is not item metadata");
            for (int meta=0; meta<16; meta++) check(registry.matches("kelp", SMELTING, old?"BiomesOPlenty:coral1":"biomesoplenty:seaweed", meta,
                    old?Collections.emptyMap():Collections.singletonMap("variant", "kelp")) == (old?meta>=8&&meta<=11:meta==0), "kelp variants exclude coral: " + meta);
            check(!registry.matches("kelp", SMELTING, "biomesoplenty:seaweed", 0, Collections.singletonMap("variant", "coral")), "state properties are authoritative");
            disabled.add("bop");
            check(registry.entries("bamboo", FOOD).isEmpty(), "provider disable removes material substitutions");
            check(registry.entries("panda_habitat", BIOME).isEmpty(), "provider disable removes biome aliases");
            check(registry.entries("natural_stone", WORLDGEN).size() == 3, "providers are independent");
            disabled.clear();
            registry.registerProvider("addon", () -> false);
            registry.register(new CompatibilityRegistry.Entry("addon", "fixture", "test:block", 2, Collections.emptyMap(), STRUCTURE, LOOT, ADVANCEMENT));
            check(registry.entries("fixture", STRUCTURE).isEmpty(), "addon-owned configuration");
            registry.registerProvider("addon", () -> true);
            check(registry.matches("fixture", LOOT, "test:block", 2, Collections.emptyMap()), "loot extension point");
            check(registry.matches("fixture", ADVANCEMENT, "test:block", 2, Collections.emptyMap()), "advancement extension point");
            List<WoodFamilies.Family> woods = WoodFamilies.forTarget(target);
            check(woods.size() == (old ? 20 : 16), "complete native wood matrix");
            for (WoodFamilies.Family wood : woods) if (wood.planks != null) check(wood.plankMeta >= 0 && wood.plankMeta <= 15, "valid plank variant");
            check(woods.get(0).texture.equals("sacredoak"), "preserve inherited sacred-oak asset spelling");
            if (!old) check(woods.get(2).texture.equals("dark"), "modern umbran uses inherited dark artwork");
        }
        System.out.println("Compatibility declarations: " + assertions + " assertions across both native targets");
    }
}
