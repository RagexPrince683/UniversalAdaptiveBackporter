package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraftforge.common.config.Property;

final class RawOreDrops {
    private static final Set<String> EXTRA = new HashSet<>(Arrays.asList(UabConfiguration.get().property("functions", "extraDropRawOres", new String[]{"oreCopper", "oreTin"}, Property.Type.STRING).getStringList()));
    private RawOreDrops() { }
    static void initialize() { }
    static int quantity(String ore, int min, int max, int fortune, Random random) {
        int base = ore != null && EXTRA.contains(ore) ? 2 + random.nextInt(2) : min + random.nextInt(max - min + 1);
        return base * (fortune <= 0 ? 1 : Math.max(1, random.nextInt(fortune + 2)));
    }
}
