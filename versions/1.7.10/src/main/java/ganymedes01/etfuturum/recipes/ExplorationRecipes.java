package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.api.utils.RecipeHelper;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ExplorationRecipes {
    private ExplorationRecipes() { }
    public static void init() {
        GameRegistry.addRecipe(new DecoratedPotRecipe());
		copy("minecraft:bolt_armor_trim_smithing_template", "minecraft:copper_block");
		copy("minecraft:bolt_armor_trim_smithing_template", "minecraft:waxed_copper_block");
		copy("minecraft:coast_armor_trim_smithing_template", "minecraft:cobblestone");
		copy("minecraft:dune_armor_trim_smithing_template", "minecraft:sandstone");
		copy("minecraft:eye_armor_trim_smithing_template", "minecraft:end_stone");
		copy("minecraft:flow_armor_trim_smithing_template", "minecraft:breeze_rod");
		copy("minecraft:host_armor_trim_smithing_template", "minecraft:terracotta");
		copy("minecraft:netherite_upgrade_smithing_template", "minecraft:netherrack");
		copy("minecraft:raiser_armor_trim_smithing_template", "minecraft:terracotta");
		copy("minecraft:rib_armor_trim_smithing_template", "minecraft:netherrack");
		copy("minecraft:sentry_armor_trim_smithing_template", "minecraft:cobblestone");
		copy("minecraft:shaper_armor_trim_smithing_template", "minecraft:terracotta");
		copy("minecraft:silence_armor_trim_smithing_template", "minecraft:cobbled_deepslate");
		copy("minecraft:snout_armor_trim_smithing_template", "minecraft:blackstone");
		copy("minecraft:spire_armor_trim_smithing_template", "minecraft:purpur_block");
		copy("minecraft:tide_armor_trim_smithing_template", "minecraft:prismarine");
		copy("minecraft:vex_armor_trim_smithing_template", "minecraft:cobblestone");
		copy("minecraft:ward_armor_trim_smithing_template", "minecraft:cobbled_deepslate");
		copy("minecraft:wayfinder_armor_trim_smithing_template", "minecraft:terracotta");
		copy("minecraft:wild_armor_trim_smithing_template", "minecraft:mossy_cobblestone");
    }
    private static void copy(String id, String substrate) {
        ItemStack template = UndergroundLoot.item(id), block = UndergroundLoot.item(substrate);
        if (template == null || block == null) return;
        ItemStack output = template.copy(); output.stackSize = 2;
        RecipeHelper.addRecipe(output, "dtd", "dbd", "ddd", 'd', Items.diamond, 't', template, 'b', block);
    }
}
