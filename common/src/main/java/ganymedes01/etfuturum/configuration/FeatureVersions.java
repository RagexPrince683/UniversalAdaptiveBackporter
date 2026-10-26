package ganymedes01.etfuturum.configuration;

/** Version arithmetic and classification only. Feature attribution lives in options.tsv. */
public final class FeatureVersions {
    public static final String CONTENT_CEILING = "1.21.11";
    public enum Classification { NATIVE, FULL_BACKPORT, ADAPTED, UNSUPPORTED }
    private FeatureVersions() { }

    public static int compare(String left, String right) {
        String[] a = left.split("\\."), b = right.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? Integer.parseInt(a[i]) : 0;
            int y = i < b.length ? Integer.parseInt(b[i]) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }
    private static void version(String value) {
        if (!value.matches("1\\.[0-9]+(?:\\.[0-9]+)?") || compare(value, CONTENT_CEILING) > 0) throw new IllegalArgumentException("Invalid UAB introduction version: " + value);
    }
    static void validate(FeatureRegistry.Option option) {
        if (!option.origin.equals("general")) version(option.origin);
        if (!option.nativeSince.equals("never")) {
            version(option.nativeSince);
            if (option.origin.equals("general") || compare(option.nativeSince, option.origin) < 0) throw new IllegalArgumentException("Native availability precedes introduction: " + option.alias);
        }
        String first = null, last = null;
        for (String introduction : option.components.values()) {
            version(introduction);
            if (option.origin.equals("general") || compare(introduction, option.origin) < 0 || !option.nativeSince.equals("never") && compare(introduction, option.nativeSince) > 0) throw new IllegalArgumentException("Inconsistent family introduction: " + option.alias);
            if (first == null || compare(introduction, first) < 0) first = introduction;
            if (last == null || compare(introduction, last) > 0) last = introduction;
        }
        if (first != null && (!first.equals(option.origin) || !last.equals(option.nativeSince))) throw new IllegalArgumentException("Family dates must cover its first and final introduction: " + option.alias);
    }
    public static String introduction(FeatureRegistry.Option option) { return option.origin.equals("general") ? "custom" : option.origin; }
    public static Classification classify(FeatureRegistry.Option option, String target) {
        if (!option.targets.contains(target)) throw new IllegalArgumentException("Unsupported Minecraft target: " + target);
        if (!option.nativeSince.equals("never") && compare(target, option.nativeSince) >= 0) return Classification.NATIVE;
        if (option.origin.equals("general") || option.experimental) return Classification.ADAPTED;
        for (String origin : option.components.values()) if (compare(target, origin) >= 0) return Classification.ADAPTED;
        return Classification.FULL_BACKPORT;
    }
    public static boolean nativeFeature(FeatureRegistry.Option option, String target) {
        return classify(option, target) == Classification.NATIVE;
    }
}
