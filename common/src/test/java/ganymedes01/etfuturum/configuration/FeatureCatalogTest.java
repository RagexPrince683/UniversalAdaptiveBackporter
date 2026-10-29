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
        check(FeatureRegistry.option("mixins", "stepHeightFix").classification("1.12.2") == FeatureVersions.Classification.ADAPTED, "native collision still retains the custom half-block step normalization");
        check(FeatureRegistry.option("mixins", "stepHeightFix").classification("1.7.10") != FeatureVersions.Classification.NATIVE, "legacy target still needs the step fix");
        check(FeatureRegistry.option("mixins", "arrowFallingFix").classification("1.12.2") == FeatureVersions.Classification.NATIVE, "native embedded arrows already retain collision when metadata changes");
        check(FeatureRegistry.option("mixins", "modernZombieBehavior").classification("1.12.2") == FeatureVersions.Classification.NATIVE, "native zombies already target and attack iron golems");
        check(FeatureRegistry.option("functions", "enableAttackedAtYawFix").side.equals("client"), "damage camera preference stays client-owned");
        check(FeatureRegistry.option("functions", "enablePlayerSkinOverlay").classification("1.12.2") == FeatureVersions.Classification.NATIVE, "native player models own the skin overlay layers");
        check(FeatureRegistry.option("mixins", "fenceWallConnectFix").classification("1.12.2") == FeatureVersions.Classification.NATIVE, "native block face shapes connect compatible modded fences and walls");
        check(FeatureRegistry.option("mixins", "colorGrassBlockItemSides").classification("1.12.2") == FeatureVersions.Classification.NATIVE, "native baked grass models include tinted inventory sides");
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
        FeatureRegistry.Option appearances=FeatureRegistry.option("content","spring_animal_renders");
        check(Boolean.FALSE.equals(appearances.booleanDefault),"Spring animal appearances are opt-in");
        check(appearances.side.equals("client")&&appearances.reload.equals("live"),"animal appearance preference is client-owned and can change safely");
        Set<String> aliases = new HashSet<>();
        check(!FeatureVersions.nativeFeature(FeatureRegistry.option("blocksitems","newBoatSpeed"),"1.12.2"),"custom boat speed is not supplied by native rowing");
        check(!FeatureVersions.nativeFeature(FeatureRegistry.option("blocksitems","newBoatEntityBlacklistAsWhitelist"),"1.12.2"),"custom rider whitelist remains configurable on native boats");
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
        check(features == 397, "preserve the 372 original and twenty-five new feature identities: " + features);
        for (FeatureRegistry.Option option : FeatureRegistry.options()) if (option.feature) {
            String update = option.category.substring(0, option.category.indexOf('.'));
            check(FeatureRegistry.UPDATES.containsKey(update), "every feature has a discoverable update group: " + option.alias);
        }
        check(FeatureVersions.nativeFeature(FeatureRegistry.option("content","shields"),"1.12.2"),"native shields have no replacement toggle");
        check(Boolean.FALSE.equals(FeatureRegistry.option("content","crafters").booleanDefault), "automatic crafters are opt in");
        check(Boolean.FALSE.equals(FeatureRegistry.option("world","useModernStrongholds").booleanDefault), "modern strongholds are opt in");
        check(FeatureVersions.nativeFeature(FeatureRegistry.option("mobs","parrot"),"1.12.2"), "native parrots have no replacement toggle");
        check(FeatureVersions.nativeFeature(FeatureRegistry.option("content","igloos"),"1.12.2"), "native igloos have no replacement toggle");
        check(!FeatureVersions.nativeFeature(FeatureRegistry.option("content","campfires"),"1.12.2"), "campfires are backported on both targets");
        check(FeatureRegistry.option("content","maces").dependencies.containsAll(Arrays.asList("content.heavy_cores","content.wind_charges")), "Mace acquisition retains exact ingredient dependencies");
        check(FeatureRegistry.option("content","netherite_horse_armor").dependencies.contains("blocksitems.enableNetherite"), "horse armor requires its netherite material");
        check(!FeatureVersions.nativeFeature(FeatureRegistry.option("content","netherite_horse_armor"),"1.12.2"), "netherite horse armor is a backport on both targets");
        check(FeatureVersions.nativeFeature(FeatureRegistry.option("enchantspotions","lungeID"),"1.12.2"), "native named Lunge needs no numeric ID control");
        check(FeatureVersions.nativeFeature(FeatureRegistry.option("enchantspotions","densityID"),"1.12.2"), "native named Mace registration needs no legacy numeric ID control");
        System.out.println("UAB shared catalog: " + assertions + " assertions; " + features + " feature switches, " + nativeFeatures + " native to 1.12.2");
    }
}
