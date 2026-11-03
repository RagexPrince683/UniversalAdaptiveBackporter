package ganymedes01.etfuturum.configuration;

/** Player equipment acquisition is independent of copper building and decorative content. */
public final class CopperSettings {
    private CopperSettings() { }
    public static boolean enabled(String name) {
        switch (name) {
            case "copper_sword": case "copper_pickaxe": case "copper_axe": case "copper_shovel": case "copper_hoe":
                return UabConfiguration.get().bool("content", "copper_tools", true);
            case "copper_helmet": case "copper_chestplate": case "copper_leggings": case "copper_boots":
                return UabConfiguration.get().bool("content", "copper_armor", true);
            default: return true;
        }
    }
}
