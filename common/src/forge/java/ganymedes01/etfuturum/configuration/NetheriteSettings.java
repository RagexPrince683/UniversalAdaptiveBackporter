package ganymedes01.etfuturum.configuration;

/** Acquisition switches never remove saved registry identities. The existing master switch still applies. */
public final class NetheriteSettings {
    private NetheriteSettings() { }
    public static boolean isExternalIngot(net.minecraft.item.ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        for (int ore : net.minecraftforge.oredict.OreDictionary.getOreIDs(stack))
            if ("ingotNetherite".equals(net.minecraftforge.oredict.OreDictionary.getOreName(ore))) return true;
        return false;
    }
    public static boolean enabled(String name) {
        String option;
        switch (name) {
            case "ancient_debris": option = "netherite_debris"; break;
            case "netherite_ingot": case "netherite_scrap": option = "netherite_resources"; break;
            case "netherite_block": option = "netherite_storage"; break;
            case "netherite_stairs": case "netherite_barrel": option = "netherite_decorations"; break;
            case "netherite_sword": case "netherite_pickaxe": case "netherite_axe": case "netherite_spade": case "netherite_shovel": case "netherite_hoe": option = "netherite_tools"; break;
            case "netherite_helmet": case "netherite_chestplate": case "netherite_leggings": case "netherite_boots": option = "netherite_armor"; break;
            default: return true;
        }
        return UabConfiguration.get().bool("content", option, true);
    }
}
