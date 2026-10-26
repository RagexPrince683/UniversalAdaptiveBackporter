package ganymedes01.etfuturum;

import ganymedes01.etfuturum.configuration.*;

/** Target adapter for the single, shared catalog. Native prerequisites are always satisfied. */
public final class Features {
    private Features() { }
    public static boolean enabled(String alias) {
        int dot = alias.indexOf('.');
        FeatureRegistry.Option option = FeatureRegistry.option(alias.substring(0, dot), alias.substring(dot + 1));
        return UabConfiguration.get().bool(option.source, option.legacyKey, true);
    }
    public static int integer(String key, int fallback, int min, int max) {
        return UabConfiguration.get().integer("world", key, fallback, min, max);
    }
}
