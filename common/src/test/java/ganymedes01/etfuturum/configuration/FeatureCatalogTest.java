package ganymedes01.etfuturum.configuration;

import java.util.*;

public final class FeatureCatalogTest {
    private static int assertions;
    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(FeatureVersions.compare("1.12.2", "1.9") > 0, "numeric version ordering");
        check(FeatureVersions.compare("1.16", "1.16.0") == 0, "missing patch equals zero");
        check(FeatureVersions.compare("1.21.9", "1.21.11") < 0, "patch ordering");
        check(FeatureRegistry.option("blocksitems", "enableIronNugget").vanillaIntroduction().equals("1.11.1"), "iron nuggets patch introduction");
        check(FeatureRegistry.option("blocksitems", "enableRedNetherBricks").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "mixed introduction family retained");
        check(FeatureRegistry.option("blocksitems", "enableNetherite").classification("1.12.2") == FeatureVersions.Classification.FULL_BACKPORT, "netherite missing in 1.12");
        check(FeatureRegistry.option("sounds", "noteBlockNotes").origin.equals("1.12"), "note instruments begin in 1.12");
        check(FeatureRegistry.option("sounds", "noteBlockNotes").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "1.14 instruments still need a backport");
        check(FeatureRegistry.option("blocksitems", "enableNewBoats").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "chest boats and rafts remain missing");
        check(FeatureRegistry.option("blocksitems", "enableBanners").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "banner-pattern items remain missing");
        check(FeatureRegistry.option("blocksitems", "enablePrismarine").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "native prismarine does not supply later stairs, slabs and walls");
        check(FeatureRegistry.option("blocksitems", "enableRedSandstone").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "red sandstone family retains later smooth shapes and walls");
        check(FeatureRegistry.option("blocksitems", "enableBasalt").nativeSince.equals("1.17"), "smooth basalt postdates Nether Update basalt");
        check(FeatureRegistry.option("blocksitems", "enableTuff").nativeSince.equals("1.21"), "decorative tuff postdates natural tuff");
        check(FeatureRegistry.option("blocksitems", "enableSponge").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "Nether sponge drying still needs a backport");
        check(FeatureRegistry.option("blocksitems", "enableShulkerBoxes").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "undyed shulker boxes still need a backport");
        check(FeatureRegistry.option("functions", "enableSilkMushroom").origin.equals("1.8"), "mushroom cap Silk Touch begins in 1.8");
        check(FeatureRegistry.option("functions", "shulkerBans").origin.equals("general"), "custom storage bans are a General policy");
        check(FeatureRegistry.option("mobs", "salmon").components.get("salmon_sizes").equals("1.21.2"), "salmon size variants have their own introduction");
        check(FeatureRegistry.option("entities", "polarBearRealism").origin.equals("general"), "always-hostile polar bears are a custom rule");
        check(FeatureRegistry.option("tweaks", "spawnAnywhereShulkerColors").origin.equals("general"), "modded-terrain shulker colors are an integration");
        check(FeatureRegistry.option("mixins", "worldSaveThumbnails").origin.equals("1.9"), "world thumbnails introduced in 1.9");
        check(FeatureRegistry.option("entities", "enableSquidInk").origin.equals("1.13"), "ordinary squid ink predates glow squid");
        check(FeatureRegistry.option("functions", "enableRecipeForTotem").category.startsWith("general."), "custom recipes belong to General");
        check(FeatureRegistry.option("modcompat", "baublesMending").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "vanilla Mending does not supply Baubles integration");
        Set<String> aliases = new HashSet<>();
        int features = 0, nativeFeatures = 0;
        Set<String> identities = new HashSet<>();
        for (FeatureRegistry.Option option : FeatureRegistry.options()) {
            check(aliases.add(option.alias), "unique alias " + option.alias);
            check(option.description.length() > 0, "feature description " + option.alias);
            check(option.classification("1.7.10") != FeatureVersions.Classification.NATIVE, "all legacy features missing or adapted");
            check(option.classification("1.12.2") != FeatureVersions.Classification.UNSUPPORTED, "no invented technical limitation");
            if (option.feature) check(option.category.startsWith(option.origin.equals("general") ? "general." : "update_" + option.origin.replace('.', '_') + "."), "feature category matches attribution: " + option.alias);
            if (option.feature && identities.add(option.id)) {
                features++;
                if (FeatureVersions.nativeFeature(option, "1.12.2")) nativeFeatures++;
            }
        }
        check(features == 372, "preserve all 372 current feature identities: " + features);
        System.out.println("UAB shared catalog: " + assertions + " assertions; " + features + " feature switches, " + nativeFeatures + " native to 1.12.2");
    }
}
