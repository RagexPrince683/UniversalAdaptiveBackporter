package ganymedes01.etfuturum.world;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Released random-loot tag, flattened once into names; native adapters retain enchantment ownership. */
public final class LootEnchantmentOptions {
    private static final List<String> RANDOM = load();
    private LootEnchantmentOptions() { }
    private static List<String> load() {
        List<String> values = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(LootEnchantmentOptions.class.getResourceAsStream("/data/loot/enchantment_options.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) if (!line.isEmpty()) values.add(line);
        } catch (IOException failure) { throw new IllegalStateException("Cannot load released enchantment options", failure); }
        return Collections.unmodifiableList(values);
    }
    public static List<String> randomLoot() { return RANDOM; }
    public static List<String> expand(String value) {
        if (value.equals("#minecraft:on_random_loot")) return RANDOM;
        if (value.startsWith("#")) throw new IllegalArgumentException("Unsupported loot enchantment tag " + value);
        return Collections.singletonList(value);
    }
}
