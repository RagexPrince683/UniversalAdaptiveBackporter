package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.*;

@Mod.EventBusSubscriber(modid = UAB.ID)
public final class Crafting {
    private Crafting() { }
    private static boolean enabled(ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        return !id.getNamespace().equals(UAB.ID) || Content.available(id.getPath());
    }
    static void shaped(RegistryEvent.Register<IRecipe> event, String id, ItemStack output, Object... recipe) {
        if (!enabled(output)) return;
        for (Object ingredient : recipe) {
            if (ingredient instanceof ItemStack && !enabled((ItemStack) ingredient)) return;
            if (ingredient instanceof Item && !enabled(new ItemStack((Item) ingredient))) return;
            if (ingredient instanceof Block && !enabled(new ItemStack((Block) ingredient))) return;
        }
        event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation(UAB.ID, id), output, recipe).setRegistryName(UAB.ID, id));
    }
    private static ItemStack count(String name, int count) { ItemStack stack = Content.stack(name); stack.setCount(count); return stack; }
    private static void compression(RegistryEvent.Register<IRecipe> event, String item, String block) {
        shaped(event, block, Content.stack(block), "III", "III", "III", 'I', Content.item(item));
        if (Content.available(item) && Content.available(block)) event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID, item), count(item, 9), Content.item(block)).setRegistryName(UAB.ID, item + "_from_block"));
    }
    @SubscribeEvent public static void recipes(RegistryEvent.Register<IRecipe> event) {
        ModernRecipes.register(event);
        compression(event, "copper_ingot", "copper_block"); compression(event, "netherite_ingot", "netherite_block");
        for (String ore : new String[]{"copper", "iron", "gold"}) compression(event, "raw_" + ore, "raw_" + ore + "_block");
        compression(event, "dried_kelp", "dried_kelp_block");
        shaped(event,"bamboo_block",Content.stack("bamboo_block"),"BBB","BBB","BBB",'B',Content.item("bamboo"));
        shaped(event,"bamboo_mosaic",Content.stack("bamboo_mosaic"),"S","S",'S',Content.block("bamboo_slab"));
        shaped(event,"moss_carpet",count("moss_carpet",3),"MM",'M',Content.block("moss_block"));
        shaped(event,"turtle_helmet",Content.stack("turtle_helmet"),"SSS","S S",'S',Content.item("turtle_scute"));
        shaped(event,"crossbow",Content.stack("crossbow"),"SIS","THT"," S ",'S',Items.STICK,'I',Items.IRON_INGOT,'T',Items.STRING,'H',Blocks.TRIPWIRE_HOOK);
        if(Content.available("warped_fungus_on_a_stick")&&Content.available("warped_fungus"))event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,"warped_fungus_on_a_stick"),Content.stack("warped_fungus_on_a_stick"),Items.FISHING_ROD,Content.block("warped_fungus")).setRegistryName(UAB.ID,"warped_fungus_on_a_stick"));
        shaped(event,"sticks_from_bamboo",new ItemStack(Items.STICK),"B","B",'B',Content.item("bamboo"));
        // The vanilla recipe is shapeless with four scrap and four gold; use its exact quantities.
        if (Content.available("netherite_ingot")) event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID, "netherite"), Content.stack("netherite_ingot"),
            Content.item("netherite_scrap"), Content.item("netherite_scrap"), Content.item("netherite_scrap"), Content.item("netherite_scrap"),
            Items.GOLD_INGOT, Items.GOLD_INGOT, Items.GOLD_INGOT, Items.GOLD_INGOT).setRegistryName(UAB.ID, "netherite_ingot_shapeless"));
        shaped(event, "barrel", Content.stack("barrel"), "PSP", "P P", "PSP", 'P', "plankWood", 'S', "slabWood");
        shaped(event, "smoker", Content.stack("smoker"), " L ", "LFL", " L ", 'L', "logWood", 'F', Blocks.FURNACE);
        shaped(event, "blast_furnace", Content.stack("blast_furnace"), "III", "IFI", "SSS", 'I', Items.IRON_INGOT, 'F', Blocks.FURNACE, 'S', Content.block("smooth_stone"));
        shaped(event, "stonecutter", Content.stack("stonecutter"), " I ", "SSS", 'I', Items.IRON_INGOT, 'S', new ItemStack(Blocks.STONE));
        shaped(event, "loom", Content.stack("loom"), "SS", "PP", 'S', Items.STRING, 'P', "plankWood");
        shaped(event, "cartography_table", Content.stack("cartography_table"), "AA", "PP", "PP", 'A', Items.PAPER, 'P', "plankWood");
        shaped(event, "composter", Content.stack("composter"), "S S", "S S", "SSS", 'S', "slabWood");
        for (Map.Entry<String, Item> entry : Content.ITEMS.entrySet()) if (entry.getValue() instanceof BannerPatternItem && Content.available(entry.getKey())) {
            net.minecraft.tileentity.BannerPattern pattern = ((BannerPatternItem) entry.getValue()).pattern;
            event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID, entry.getKey()), Content.stack(entry.getKey()), Items.PAPER, pattern.getPatternItem().copy()).setRegistryName(UAB.ID, entry.getKey()));
        }
        shaped(event, "smithing_table", Content.stack("smithing_table"), "II", "PP", "PP", 'I', Items.IRON_INGOT, 'P', "plankWood");
        shaped(event, "fletching_table", Content.stack("fletching_table"), "FF", "PP", "PP", 'F', Items.FLINT, 'P', "plankWood");
        shaped(event, "lantern", Content.stack("lantern"), "NNN", "NTN", "NNN", 'N', Items.IRON_NUGGET, 'T', Blocks.TORCH);
        shaped(event, "chain", Content.stack("chain"), "N", "I", "N", 'N', Items.IRON_NUGGET, 'I', Items.IRON_INGOT);
        shaped(event, "conduit", Content.stack("conduit"), "SSS", "SHS", "SSS", 'S', Content.item("nautilus_shell"), 'H', Content.item("heart_of_the_sea"));
        shaped(event, "quartz_bricks", count("quartz_bricks", 4), "QQ", "QQ", 'Q', Blocks.QUARTZ_BLOCK);
        shaped(event, "packed_mud", Content.stack("packed_mud"), "MW", 'M', Content.block("mud"), 'W', Items.WHEAT);
        shaped(event, "mud_bricks", count("mud_bricks", 4), "MM", "MM", 'M', Content.block("packed_mud"));
        shaped(event, "amethyst_block", Content.stack("amethyst_block"), "AA", "AA", 'A', Content.item("amethyst_shard"));
        shaped(event, "tinted_glass", count("tinted_glass", 2), " A ", "AGA", " A ", 'A', Content.item("amethyst_shard"), 'G', Blocks.GLASS);
        for (Map.Entry<String, net.minecraft.block.state.IBlockState> shape : Content.SHAPE_BASES.entrySet()) {
            String name = shape.getKey(); ItemStack base = new ItemStack(shape.getValue().getBlock(), 1, shape.getValue().getBlock().getMetaFromState(shape.getValue()));
            if (name.endsWith("_slab")) shaped(event, name, count(name, 6), "BBB", 'B', base);
            else if (name.endsWith("_wall")) shaped(event, name, count(name, 6), "BBB", "BBB", 'B', base);
            else shaped(event, name, count(name, 4), "B  ", "BB ", "BBB", 'B', base);
        }
        shaped(event, "polished_tuff", count("polished_tuff", 4), "TT", "TT", 'T', Content.block("tuff"));
        shaped(event, "polished_deepslate", count("polished_deepslate", 4), "DD", "DD", 'D', Content.block("cobbled_deepslate"));
        shaped(event, "deepslate_bricks", count("deepslate_bricks", 4), "DD", "DD", 'D', Content.block("polished_deepslate"));
        shaped(event, "deepslate_tiles", count("deepslate_tiles", 4), "DD", "DD", 'D', Content.block("deepslate_bricks"));
        shaped(event, "chiseled_deepslate", Content.stack("chiseled_deepslate"), "D", "D", 'D', Content.block("cobbled_deepslate_slab"));
        shaped(event, "tuff_bricks", count("tuff_bricks", 4), "TT", "TT", 'T', Content.block("polished_tuff"));
        shaped(event, "chiseled_tuff", Content.stack("chiseled_tuff"), "T", "T", 'T', Content.block("tuff_slab"));
        shaped(event, "chiseled_tuff_bricks", Content.stack("chiseled_tuff_bricks"), "T", "T", 'T', Content.block("tuff_brick_slab"));
        if (Content.available("mud") && Content.available("muddy_mangrove_roots")) event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID, "muddy_mangrove_roots"), Content.stack("muddy_mangrove_roots"), Content.block("mud"), Content.block("mangrove_roots")).setRegistryName(UAB.ID, "muddy_mangrove_roots"));
        for (String name : Content.BLOCKS.keySet()) {
            if (name.endsWith("_door") || name.endsWith("_trapdoor") || name.endsWith("_button") || name.endsWith("_pressure_plate")) {
                String tree = name.substring(0, name.lastIndexOf(name.endsWith("_pressure_plate") ? "_pressure_plate" : name.endsWith("_trapdoor") ? "_trapdoor" : name.endsWith("_button") ? "_button" : "_door"));
                Object planks = Content.BLOCKS.containsKey(tree+"_planks") ? Content.block(tree+"_planks") : null;
                if (planks == null) for (net.minecraft.block.BlockPlanks.EnumType type : net.minecraft.block.BlockPlanks.EnumType.values()) if (type.getName().equals(tree)) planks = new ItemStack(Blocks.PLANKS, 1, type.getMetadata());
                if (planks != null) {
                    if (name.endsWith("_door")) shaped(event, name, count(name, 3), "PP", "PP", "PP", 'P', planks);
                    else if (name.endsWith("_trapdoor")) shaped(event, name, count(name, 2), "PPP", "PPP", 'P', planks);
                    else if (name.endsWith("_pressure_plate")) shaped(event, name, Content.stack(name), "PP", 'P', planks);
                    else shaped(event, name, Content.stack(name), "P", 'P', planks);
                }
            }
            if (name.endsWith("_planks")) {
                String tree = name.substring(0, name.length() - 7), log = tree + ((tree.equals("crimson") || tree.equals("warped")) ? "_stem" : tree.equals("bamboo")?"_block":"_log");
                shaped(event, name, count(name, tree.equals("bamboo")?2:4), "L", 'L', Content.block(log));
                if(tree.equals("bamboo"))shaped(event,"bamboo_planks_stripped",count(name,2),"L",'L',Content.block("stripped_bamboo_block"));
                if (tree.equals("crimson") || tree.equals("warped")) {
                    shaped(event, tree + "_hyphae", count(tree + "_hyphae", 3), "LL", "LL", 'L', Content.block(log));
                    shaped(event, "stripped_" + tree + "_hyphae", count("stripped_" + tree + "_hyphae", 3), "LL", "LL", 'L', Content.block("stripped_" + log));
                    for (String source : new String[]{"stripped_" + log, tree + "_hyphae", "stripped_" + tree + "_hyphae"}) shaped(event, name + "_from_" + source, count(name, 4), "L", 'L', Content.block(source));
                }
                shaped(event, tree + "_fence", count(tree + "_fence", 3), "PSP", "PSP", 'P', Content.block(name), 'S', Items.STICK);
                shaped(event, tree + "_fence_gate", Content.stack(tree + "_fence_gate"), "SPS", "SPS", 'P', Content.block(name), 'S', Items.STICK);
            }
            if (name.endsWith("_stairs") && !Content.SHAPE_BASES.containsKey(name)) shaped(event, name, count(name, 4), "B  ", "BB ", "BBB", 'B', Content.block(name.substring(0, name.length() - 7)));
        }
    }
    public static void initialize() {
        for (Map.Entry<String, Block> entry : Content.BLOCKS.entrySet()) {
            String name = entry.getKey(); if (!Content.available(name) || name.endsWith("_double_slab")) continue;
            String ore = name.endsWith("_log") || name.endsWith("_stem") ? "logWood" : name.endsWith("_planks") ? "plankWood" : name.endsWith("_leaves") ? "treeLeaves" : entry.getValue() instanceof ModernSapling ? "treeSapling" : name.endsWith("_fence") ? "fenceWood" : name.endsWith("_fence_gate") ? "fenceGateWood" : entry.getValue().getDefaultState().getMaterial() == net.minecraft.block.material.Material.WOOD && name.endsWith("_slab") ? "slabWood" : null;
            if (ore != null) OreDictionary.registerOre(ore, new ItemStack(entry.getValue(), 1, OreDictionary.WILDCARD_VALUE));
            if (entry.getValue() instanceof DeepslateOre || name.equals("copper_ore")) {
                String metal = name.replace("deepslate_", "").replace("_ore", "");
                OreDictionary.registerOre("ore" + Character.toUpperCase(metal.charAt(0)) + metal.substring(1), new ItemStack(entry.getValue()));
            }
        }
        smelt("copper_ore", Content.stack("copper_ingot"), .7F);
        smelt("raw_copper", Content.stack("copper_ingot"), .7F);
        smelt("raw_iron", new ItemStack(Items.IRON_INGOT), .7F);
        smelt("raw_gold", new ItemStack(Items.GOLD_INGOT), 1);
        smelt("ancient_debris", Content.stack("netherite_scrap"), 2);
        smelt("kelp", Content.stack("dried_kelp"), .1F);
        smelt("sea_pickle", new ItemStack(Items.DYE,1,10), .1F);
        smelt("cobbled_deepslate", Content.stack("deepslate"), .1F);
        smelt("deepslate_bricks", Content.stack("cracked_deepslate_bricks"), .1F);
        smelt("deepslate_tiles", Content.stack("cracked_deepslate_tiles"), .1F);
        for (Map.Entry<String, Block> entry : Content.BLOCKS.entrySet()) if (entry.getValue() instanceof DeepslateOre && Content.available(entry.getKey())) {
            Block base = ((DeepslateOre) entry.getValue()).base;
            ItemStack input = new ItemStack(base), output = FurnaceRecipes.instance().getSmeltingResult(input);
            if (!output.isEmpty()) GameRegistry.addSmelting(Content.stack(entry.getKey()), output.copy(), FurnaceRecipes.instance().getSmeltingExperience(output));
        }
        smelt("polished_blackstone_bricks", Content.stack("cracked_polished_blackstone_bricks"), .1F);
        if (Content.available("smooth_stone")) GameRegistry.addSmelting(Blocks.STONE, Content.stack("smooth_stone"), .1F);
        if (Content.available("smooth_sandstone")) GameRegistry.addSmelting(new ItemStack(Blocks.SANDSTONE), Content.stack("smooth_sandstone"), .1F);
        if (Content.available("smooth_red_sandstone")) GameRegistry.addSmelting(new ItemStack(Blocks.RED_SANDSTONE), Content.stack("smooth_red_sandstone"), .1F);
        if (Content.available("smooth_quartz")) GameRegistry.addSmelting(new ItemStack(Blocks.QUARTZ_BLOCK), Content.stack("smooth_quartz"), .1F);
        for (String ore : new String[]{"copper", "iron", "gold"}) if (Content.available("raw_" + ore)) OreDictionary.registerOre("rawOre" + Character.toUpperCase(ore.charAt(0)) + ore.substring(1), Content.stack("raw_" + ore));
        if (Content.available("copper_ingot")) OreDictionary.registerOre("ingotCopper", Content.stack("copper_ingot"));
        StonecuttingRecipes.initialize(); Composting.initialize();
    }
    private static void smelt(String input, ItemStack output, float experience) { if (Content.available(input) && enabled(output)) GameRegistry.addSmelting(Content.stack(input), output, experience); }
}
