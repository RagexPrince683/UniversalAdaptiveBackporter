package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.EnumHelper;

/** Registrations are retained when acquisition is disabled, so saves never lose identities. */
@Mod.EventBusSubscriber(modid = UAB.ID)
public final class Content {
    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    public static final Map<String, String> FEATURE = new LinkedHashMap<>();
    private static final Map<String, List<String>> REQUIREMENTS = new HashMap<>();
    private static final Map<String, List<String>> ALTERNATIVES = new HashMap<>();
    static final Map<String, net.minecraft.block.state.IBlockState> SHAPE_BASES = new LinkedHashMap<>();
    public static final CreativeTabs TAB = new CreativeTabs("etfuturum") {
        @Override public ItemStack createIcon() { return new ItemStack(Items.IRON_INGOT); }
    };
    public static final Item.ToolMaterial NETHERITE = EnumHelper.addToolMaterial("UAB_NETHERITE", 3, 2031, 9, 4, 15);
    public static final ItemArmor.ArmorMaterial NETHERITE_ARMOR = EnumHelper.addArmorMaterial("UAB_NETHERITE", "etfuturum:netherite", 37, new int[]{3, 6, 8, 3}, 15, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 3);
    private Content() { }
    public static boolean available(String name) {
        if (!FEATURE.containsKey(name) || !Features.enabled(FEATURE.get(name))) return false;
        for (String dependency : REQUIREMENTS.getOrDefault(name, Collections.emptyList())) if (!Features.enabled(dependency)) return false;
        if (ALTERNATIVES.containsKey(name) && ALTERNATIVES.get(name).stream().noneMatch(Features::enabled)) return false;
        return true;
    }
    static void requires(String name, String... dependencies) {
        REQUIREMENTS.put(name, Arrays.asList(dependencies));
        if (!available(name)) { if (BLOCKS.containsKey(name)) block(name).setCreativeTab(null); if (ITEMS.containsKey(name)) item(name).setCreativeTab(null); }
    }
    public static ItemStack stack(String name) { return new ItemStack(ITEMS.get(name)); }
    public static Block block(String name) { return Objects.requireNonNull(BLOCKS.get(name), name); }
    public static Item item(String name) { return Objects.requireNonNull(ITEMS.get(name), name); }

    static <T extends Block> T add(String name, String feature, T block) {
        if (BLOCKS.put(name, block) != null) throw new IllegalStateException("Duplicate block " + name);
        FEATURE.put(name, feature);
        block.setRegistryName(UAB.ID, name).setTranslationKey("etfuturum." + name);
        if (Features.enabled(feature)) block.setCreativeTab(TAB);
        return block;
    }
    static <T extends Item> T add(String name, String feature, T item) {
        if (ITEMS.put(name, item) != null) throw new IllegalStateException("Duplicate item " + name);
        FEATURE.put(name, feature);
        item.setRegistryName(UAB.ID, name).setTranslationKey("etfuturum." + name);
        if (Features.enabled(feature)) item.setCreativeTab(TAB);
        return item;
    }
    private static void stone(String feature, String... names) {
        for (String name : names) add(name, feature, new Block(Material.ROCK).setHardness(1.5F).setResistance(6));
    }
    private static void simple(String feature, String... names) {
        for (String name : names) add(name, feature, new Item());
    }
    static void shapes(String name, String feature, net.minecraft.block.state.IBlockState base, boolean stairs, boolean wall) {
        shapes(name, base, feature, stairs ? feature : null, wall ? feature : null);
    }
    private static void shapes(String name, net.minecraft.block.state.IBlockState base, String slabFeature, String stairFeature, String wallFeature) {
        String shape = name.replace("_planks", "").replace("_bricks", "_brick").replace("_tiles", "_tile");
        if (slabFeature != null) {
            NativeSlab[] slabs = NativeSlab.pair(base);
            add(shape + "_slab", slabFeature, slabs[0]); add(shape + "_double_slab", slabFeature, slabs[1]).setCreativeTab(null);
            SHAPE_BASES.put(shape + "_slab", base);
            if (base.getMaterial() == Material.WOOD && !name.startsWith("crimson_") && !name.startsWith("warped_")) for (NativeSlab slab : slabs) Blocks.FIRE.setFireInfo(slab, 5, 20);
        }
        if (stairFeature != null) {
            String stair = BLOCKS.containsKey(name + "_stairs") ? name + "_stairs" : shape + "_stairs";
            if (!BLOCKS.containsKey(stair)) add(stair, stairFeature, new BlockStairs(base) { @Override public void randomTick(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.block.state.IBlockState state,java.util.Random random){if(CopperAging.copper(this))CopperAging.tick(world,pos,state,random);else super.randomTick(world,pos,state,random);} }).setTickRandomly(base.getBlock().getTickRandomly());
            block(stair).setTranslationKey("etfuturum." + shape + "_stairs");
            SHAPE_BASES.put(stair, base);
        }
        if (wallFeature != null) { add(shape + "_wall", wallFeature, new NativeWall(base.getBlock())); SHAPE_BASES.put(shape + "_wall", base); }
    }
    public static void define() {
        String copper = "blocksitems.enableCopper", netherite = "blocksitems.enableNetherite";
        add("copper_ore", copper, new OreBlock("raw_copper", 1, 1));
        add("ancient_debris", netherite, new Block(Material.ROCK).setHardness(30).setResistance(1200)).setHarvestLevel("pickaxe", 3);
        add("nether_gold_ore", "blocksitems.enableNetherGold", new OreBlock("gold_nugget", 2, 6));
        simple(copper, "copper_ingot");
        simple(netherite, "netherite_scrap", "netherite_ingot");
        simple("blocksitems.enableRawOres", "raw_copper", "raw_iron", "raw_gold");
        stone("blocksitems.enableRawOres", "raw_copper_block", "raw_iron_block", "raw_gold_block");
        for (String kind : new String[]{"copper_block", "cut_copper"}) {
            for (String stage : new String[]{"", "exposed_", "weathered_", "oxidized_"}) {
                String name = stage.isEmpty() ? kind : stage + (kind.equals("copper_block") ? "copper" : kind);
                add(name, copper, new CopperBlock(stage.length() == 0 ? 0 : stage.equals("exposed_") ? 1 : stage.equals("weathered_") ? 2 : 3, false, kind));
                add("waxed_" + name, copper, new CopperBlock(stage.length() == 0 ? 0 : stage.equals("exposed_") ? 1 : stage.equals("weathered_") ? 2 : 3, true, kind));
            }
        }
        stone(netherite, "netherite_block");
        add("netherite_sword", netherite, new ItemSword(NETHERITE));
        add("netherite_pickaxe", netherite, new ItemPickaxe(NETHERITE) { });
        add("netherite_axe", netherite, new ItemAxe(NETHERITE, 8, -3) { });
        add("netherite_shovel", netherite, new ItemSpade(NETHERITE));
        add("netherite_hoe", netherite, new ItemHoe(NETHERITE));
        for (net.minecraft.inventory.EntityEquipmentSlot slot : net.minecraft.inventory.EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == net.minecraft.inventory.EntityEquipmentSlot.Type.ARMOR) {
                String name = slot == net.minecraft.inventory.EntityEquipmentSlot.HEAD ? "helmet" : slot == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? "chestplate" : slot == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? "leggings" : "boots";
                add("netherite_" + name, netherite, new ItemArmor(NETHERITE_ARMOR, 0, slot));
            }
        }
        add("deepslate", "blocksitems.enableDeepslate", new DeepslateBlock());
        stone("blocksitems.enableDeepslate", "cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "cracked_deepslate_bricks", "deepslate_tiles", "cracked_deepslate_tiles", "chiseled_deepslate");
        for (String name : new String[]{"cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "cracked_deepslate_bricks", "deepslate_tiles", "cracked_deepslate_tiles", "chiseled_deepslate"}) {
            float hardness = Features.enabled("functions.useStoneHardnessForDeepslate") ? (name.equals("cobbled_deepslate") || name.equals("polished_deepslate") ? 2 : 3) : 3.5F;
            block(name).setHardness(hardness).setHarvestLevel("pickaxe", 0);
        }
        Block[] ores = {Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, block("copper_ore")};
        for (Block ore : ores) add("deepslate_" + ore.getRegistryName().getPath(), "blocksitems.enableDeepslateOres", ore == Blocks.REDSTONE_ORE ? new DeepslateRedstoneOre() : new DeepslateOre(ore));
        stone("blocksitems.enableTuff", "tuff", "polished_tuff", "tuff_bricks", "chiseled_tuff", "chiseled_tuff_bricks");
        stone("blocksitems.enableCalcite", "calcite");
        block("calcite").setHardness(.75F).setResistance(.75F);
        stone("blocksitems.enableBlackstone", "blackstone", "polished_blackstone", "polished_blackstone_bricks", "cracked_polished_blackstone_bricks", "chiseled_polished_blackstone");
        for(String name:new String[]{"basalt","polished_basalt"})add(name,"blocksitems.enableBasalt",new BlockRotatedPillar(Material.ROCK){{setDefaultState(blockState.getBaseState().withProperty(AXIS,net.minecraft.util.EnumFacing.Axis.Y));setHardness(1.25F);setResistance(4.2F);}});
        stone("blocksitems.enableBasalt", "smooth_basalt");
        block("smooth_basalt").setHardness(1.25F).setResistance(4.2F);
        stone("blocksitems.enableRedNetherBricks", "cracked_nether_bricks", "chiseled_nether_bricks");
        stone("blocksitems.enableQuartzBricks", "quartz_bricks");
        stone("blocksitems.enableSmoothStone", "smooth_stone");
        stone("blocksitems.enableSmoothSandStone", "smooth_sandstone", "smooth_red_sandstone");
        stone("blocksitems.enableSmoothQuartz", "smooth_quartz");
        add("amethyst_block", "blocksitems.enableAmethyst", new MaterialBlock(Material.ROCK, SoundType.GLASS).setHardness(1.5F).setResistance(1.5F));
        add("tinted_glass", "blocksitems.enableAmethyst", new TintedGlass());
        add("budding_amethyst", "blocksitems.enableAmethyst", new BuddingAmethyst());
        for (int stage = 0; stage < AmethystBud.STAGES.length; stage++) add(AmethystBud.STAGES[stage], "blocksitems.enableAmethyst", new AmethystBud(stage));
        simple("blocksitems.enableAmethyst", "amethyst_shard");
        stone("blocksitems.enableMud", "mud", "packed_mud", "mud_bricks");
        add("soul_soil", "blocksitems.enableSoulSoil", new MaterialBlock(Material.GROUND, SoundType.SAND).setHardness(.5F));
        for (boolean crimson : new boolean[]{true, false}) {
            String kind = crimson ? "crimson" : "warped", flag = crimson ? "experiments.enableCrimsonBlocks" : "experiments.enableWarpedBlocks";
            add(kind + "_nylium", flag, new NyliumBlock(crimson));
            add(kind + "_fungus", flag, new NetherPlant(crimson, true));
            add(kind + "_roots", flag, new NetherPlant(crimson, false));
        }
        add("nether_sprouts", "experiments.enableWarpedBlocks", new NetherPlant(false, false));
        add("warped_wart_block", "experiments.enableWarpedBlocks", new MaterialBlock(Material.GRASS, SoundType.WOOD).setHardness(1));
        add("weeping_vines", "experiments.enableCrimsonBlocks", new NetherVines(false));
        add("twisting_vines", "experiments.enableWarpedBlocks", new NetherVines(true));
        add("shroomlight", "blocksitems.masterNetherToggle", new MaterialBlock(Material.GRASS, SoundType.WOOD).setHardness(1).setLightLevel(1));
        ALTERNATIVES.put("shroomlight", Arrays.asList("experiments.enableCrimsonBlocks", "experiments.enableWarpedBlocks"));
        requires("shroomlight");
        add("crying_obsidian", "blocksitems.enableCryingObsidian", new Block(Material.ROCK).setHardness(50).setResistance(1200).setLightLevel(.67F));
        add("blue_ice", "blocksitems.enableBlueIce", new BlockPackedIce() { }).slipperiness = .989F;
        add("lantern", "blocksitems.enableLantern", new LanternBlock().setLightLevel(1));
        add("soul_lantern", "blocksitems.enableSoulLighting", new LanternBlock().setLightLevel(.67F));
        add("chain", "blocksitems.enableChain", new ChainBlock());
        add("fletching_table", "blocksitems.enableFletchingTable", new MaterialBlock(Material.WOOD, SoundType.WOOD).setHardness(2.5F));
        add("barrel", "blocksitems.enableBarrel", new BarrelBlock());
        add("smoker", "blocksitems.enableSmoker", new CookingBlock(false));
        add("blast_furnace", "blocksitems.enableBlastFurnace", new CookingBlock(true));
        add("stonecutter", "blocksitems.enableStonecutter", new WorkstationBlock(2, true));
        add("loom", "blocksitems.enableLoom", new WorkstationBlock(3, false));
        add("cartography_table", "blocksitems.enableCartographyTable", new WorkstationBlock(4, false));
        add("composter", "blocksitems.enableComposter", new ComposterBlock());
        for (net.minecraft.tileentity.BannerPattern pattern : net.minecraft.tileentity.BannerPattern.values()) if (pattern.hasPatternItem()) {
            String name = pattern == net.minecraft.tileentity.BannerPattern.BRICKS ? "field_masoned" : pattern == net.minecraft.tileentity.BannerPattern.CURLY_BORDER ? "bordure_indented" : BannerPatternItem.name(pattern);
            add(name + "_banner_pattern", "blocksitems.enableBanners", new BannerPatternItem(pattern));
        }
        add("smithing_table", "blocksitems.enableSmithingTable", new SmithingBlock());
        simple("content.template_smithing", "netherite_upgrade_smithing_template");
        add("sweet_berry_bush", "blocksitems.enableSweetBerryBushes", new BerryBush());
        add("sweet_berries", "blocksitems.enableSweetBerryBushes", new PlantableFood(2, .1F, "sweet_berry_bush"));
        add("bamboo", "content.bamboo_plants", new BambooBlock()).setCreativeTab(null);
        add("bamboo_sapling", "content.bamboo_plants", new BambooShoot()).setCreativeTab(null);
        add("bamboo", "content.bamboo_plants", new BambooItem());
        add("moss_block", "experiments.enableMossAzalea", new MossBlock());
        add("moss_carpet", "experiments.enableMossAzalea", new MossCarpet());
        add("pale_moss_block", "experiments.enablePaleGarden", new PaleMoss());
        add("pale_moss_carpet", "experiments.enablePaleGarden", new MossCarpet());
        add("pale_hanging_moss", "experiments.enablePaleGarden", new HangingMoss());
        for(String azalea:new String[]{"azalea","flowering_azalea"}){
            add(azalea,"experiments.enableMossAzalea",new AzaleaBlock());
            add(azalea+"_leaves","experiments.enableMossAzalea",new ModernLeaves(azalea));
            Blocks.FIRE.setFireInfo(block(azalea+"_leaves"),30,60);
        }
        add("cave_vines", "blocksitems.enableGlowBerries", new CaveVines(true)).setCreativeTab(null);
        add("cave_vines_plant", "blocksitems.enableGlowBerries", new CaveVines(false)).setCreativeTab(null);
        add("glow_berries", "blocksitems.enableGlowBerries", new GlowBerries());
        add("glow_lichen", "blocksitems.enableGlowLichen", new GlowLichen());
        add("kelp", "content.kelp", new KelpBlock());
        add("seagrass", "content.seagrass", new Seagrass());
        add("turtle_egg", "content.scutes", new TurtleEgg());
        add("sea_pickle", "content.sea_pickles", new SeaPickle(false));
        add("sea_pickle_underwater", "content.sea_pickles", new SeaPickle(true)).setCreativeTab(null);
        for(String color:new String[]{"tube","brain","bubble","fire","horn"})for(boolean dead:new boolean[]{false,true}) {
            String prefix=(dead?"dead_":"")+color+"_coral";
            add(prefix+"_block","content.coral",new CoralBlock(color,dead));
            for(String shape:new String[]{"","_fan","_wall_fan"})for(boolean wet:new boolean[]{false,true}) {
                Block coral=add(prefix+shape+(wet?"_underwater":""),"content.coral",new CoralPlant(color,dead,shape,wet));
                if(wet||shape.equals("_wall_fan"))coral.setCreativeTab(null);
            }
        }
        add("bubble_column","blocksitems.enableBubbleColumns",new BubbleColumn()).setCreativeTab(null);
        add("dried_kelp", "content.kelp", new ItemFood(1, .3F, false) { @Override public int getMaxItemUseDuration(ItemStack s) { return 16; } });
        add("dried_kelp_block", "content.kelp", new Block(Material.GRASS).setHardness(.5F));
        simple("content.nautilus_shells", "nautilus_shell");
        simple("content.conduits", "heart_of_the_sea");
        add("conduit", "content.conduits", new ConduitBlock());
        simple("content.phantom_membranes", "phantom_membrane");
        add("trident", "content.tridents", new TridentItem());
        String[] fish = {"cod", "salmon", "tropical_fish", "pufferfish"};
        for (int i = 0; i < fish.length; i++) add(fish[i] + "_bucket", "content.fish_buckets", new FishBucket(i));
        add("axolotl_bucket","content.axolotl_buckets",new FishBucket(4));
        simple("content.scutes", "turtle_scute");
        add("turtle_helmet", "content.scutes", new TurtleHelmet());
        simple("content.glow_ink", "glow_ink_sac");
        add("crossbow", "content.crossbows", new CrossbowItem());
        add("warped_fungus_on_a_stick", "mobs.strider", new FungusOnStick());
        simple("content.wind_charges", "breeze_rod");
        add("honey_bottle", "blocksitems.enableHoney", new HoneyBottle());
        simple("blocksitems.enableHoney", "honeycomb");
        add("honey_block", "blocksitems.enableHoney", new HoneyBlock());
        add("honeycomb_block", "blocksitems.enableHoney", new Block(Material.CLAY).setHardness(.6F));
        add("mangrove_roots", "experiments.enableMangroveBlocks", new MangroveRoots());
        add("muddy_mangrove_roots", "experiments.enableMangroveBlocks", new BlockRotatedPillar(Material.WOOD) { { setDefaultState(blockState.getBaseState().withProperty(AXIS,net.minecraft.util.EnumFacing.Axis.Y));setHardness(.7F); setSoundType(SoundType.GROUND); } });
        for (String tree : new String[]{"cherry", "mangrove", "crimson", "warped", "pale_oak", "bamboo"}) {
            String feature = tree.equals("bamboo") ? "blocksitems.enableBambooBlocks" : tree.equals("cherry") ? "blocksitems.enableCherryBlocks" : tree.equals("pale_oak") ? "experiments.enablePaleGarden" : "experiments.enable" + Character.toUpperCase(tree.charAt(0)) + tree.substring(1) + "Blocks";
            boolean fungus = tree.equals("crimson") || tree.equals("warped"), bamboo = tree.equals("bamboo");
            String trunk=tree+(fungus?"_stem":bamboo?"_block":"_log");
            add(trunk, feature, new ModernLog());
            add("stripped_" + trunk, feature, new ModernLog());
            if (fungus) { add(tree + "_hyphae", feature, new ModernLog()); add("stripped_" + tree + "_hyphae", feature, new ModernLog()); }
            add(tree + "_planks", feature, new MaterialBlock(Material.WOOD, SoundType.WOOD).setHardness(2).setResistance(3));
            if (!fungus && !bamboo) {
                add(tree + "_leaves", feature, new ModernLeaves(tree));
                add(tree + "_sapling", feature, tree.equals("mangrove") ? new MangroveSapling() : new ModernSapling(tree));
                if (tree.equals("mangrove")) block(tree + "_sapling").setTranslationKey("etfuturum.mangrove_propagule");
            }
            add(tree + "_fence", feature, new BlockFence(Material.WOOD, Material.WOOD.getMaterialMapColor()));
            add(tree + "_fence_gate", feature, new BlockFenceGate(BlockPlanks.EnumType.OAK));
            add(tree + "_door", feature, new WoodDoor());
            add(tree + "_door", feature, new ItemDoor(block(tree + "_door")) { @Override public int getItemBurnTime(ItemStack stack) { return fungus ? 0 : super.getItemBurnTime(stack); } });
            requires(tree + "_door", "blocksitems.enableNewDoors");
            add(tree + "_trapdoor", feature, new BlockTrapDoor(Material.WOOD) { { setHardness(3); setResistance(3); setSoundType(SoundType.WOOD); } });
            requires(tree + "_trapdoor", "blocksitems.enableNewTrapdoors");
            add(tree + "_button", feature, new BlockButtonWood() { });
            requires(tree + "_button", "blocksitems.enableNewWoodRedstone");
            add(tree + "_pressure_plate", feature, new BlockPressurePlate(Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING) { { setHardness(.5F); setSoundType(SoundType.WOOD); } });
            requires(tree + "_pressure_plate", "blocksitems.enableNewWoodRedstone");
            if (!fungus) {
                for (String part : new String[]{bamboo?"_block":"_log", "_planks", "_fence", "_fence_gate"}) Blocks.FIRE.setFireInfo(block(tree + part), 5, part.equals("_log")||part.equals("_block") ? 5 : 20);
                if(!bamboo)Blocks.FIRE.setFireInfo(block(tree + "_leaves"), 30, 60);
                Blocks.FIRE.setFireInfo(block("stripped_" + trunk), 5, 5);
                Blocks.FIRE.setFireInfo(block(tree + "_door"), 5, 20);
                Blocks.FIRE.setFireInfo(block(tree + "_trapdoor"), 5, 20);
            }
        }
        add("bamboo_mosaic", "blocksitems.enableBambooBlocks", new MaterialBlock(Material.WOOD,SoundType.WOOD).setHardness(2).setResistance(3));
        Blocks.FIRE.setFireInfo(block("bamboo_mosaic"),5,20);
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            String tree = type.getName();
            add("stripped_" + tree + "_log", "blocksitems.enableStrippedLogs", new ModernLog());
            if (type != BlockPlanks.EnumType.OAK) {
                add(tree + "_trapdoor", "blocksitems.enableVanillaTrapdoors", new BlockTrapDoor(Material.WOOD) { { setHardness(3); setResistance(3); setSoundType(SoundType.WOOD); } });
                add(tree + "_button", "blocksitems.enableVanillaWoodRedstone", new BlockButtonWood() { });
                add(tree + "_pressure_plate", "blocksitems.enableVanillaWoodRedstone", new BlockPressurePlate(Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING) { { setHardness(.5F); setSoundType(SoundType.WOOD); } });
            }
        }
        // Shape adapters use native stair collision, placement, rotation and neighbor handling.
        for (String name : new ArrayList<>(BLOCKS.keySet())) {
            if (name.endsWith("_planks") || Arrays.asList("cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "deepslate_tiles", "blackstone", "polished_blackstone", "polished_blackstone_bricks", "mud_bricks").contains(name)) {
                add(name + "_stairs", FEATURE.get(name), new BlockStairs(block(name).getDefaultState()) { });
            }
        }
        for (String name : new String[]{"cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "deepslate_tiles", "blackstone", "polished_blackstone", "polished_blackstone_bricks", "mud_bricks", "tuff", "polished_tuff", "tuff_bricks"}) shapes(name, FEATURE.get(name), block(name).getDefaultState(), true, true);
        for (String tree : new String[]{"cherry", "mangrove", "crimson", "warped", "pale_oak", "bamboo"}) shapes(tree + "_planks", FEATURE.get(tree + "_planks"), block(tree + "_planks").getDefaultState(), true, false);
        shapes("bamboo_mosaic","blocksitems.enableBambooBlocks",block("bamboo_mosaic").getDefaultState(),true,false);
        for (String name : new String[]{"smooth_sandstone", "smooth_red_sandstone", "smooth_quartz", "smooth_stone"}) shapes(name, FEATURE.get(name), block(name).getDefaultState(), !name.equals("smooth_stone"), false);
        shapes("prismarine", "blocksitems.enablePrismarine", Blocks.PRISMARINE.getDefaultState(), true, true);
        shapes("prismarine_bricks", "blocksitems.enablePrismarine", Blocks.PRISMARINE.getStateFromMeta(1), true, false);
        shapes("dark_prismarine", "blocksitems.enablePrismarine", Blocks.PRISMARINE.getStateFromMeta(2), true, false);
        shapes("red_nether_bricks", "blocksitems.enableRedNetherBricks", Blocks.RED_NETHER_BRICK.getDefaultState(), true, true);
        String slabFlag = "blocksitems.enableExtraVanillaSlabs", stairFlag = "blocksitems.enableExtraVanillaStairs", wallFlag = "blocksitems.enableExtraVanillaWalls";
        String[] nativeStones = {"stone", "granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite"};
        for (int i = 0; i < nativeStones.length; i++) shapes(nativeStones[i], Blocks.STONE.getStateFromMeta(i), slabFlag, stairFlag, i % 2 == 1 ? wallFlag : null);
        shapes("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE.getDefaultState(), slabFlag, stairFlag, null);
        shapes("mossy_stone_bricks", Blocks.STONEBRICK.getStateFromMeta(1), slabFlag, stairFlag, wallFlag);
        shapes("cut_sandstone", Blocks.SANDSTONE.getStateFromMeta(2), slabFlag, null, null);
        shapes("cut_red_sandstone", Blocks.RED_SANDSTONE.getStateFromMeta(2), slabFlag, null, null);
        shapes("stone_bricks", Blocks.STONEBRICK.getDefaultState(), null, null, wallFlag);
        shapes("brick", Blocks.BRICK_BLOCK.getDefaultState(), null, null, wallFlag);
        shapes("nether_brick", Blocks.NETHER_BRICK.getDefaultState(), null, null, wallFlag);
        shapes("sandstone", Blocks.SANDSTONE.getDefaultState(), null, null, wallFlag);
        shapes("red_sandstone", Blocks.RED_SANDSTONE.getDefaultState(), null, null, wallFlag);
        shapes("end_stone_bricks", Blocks.END_BRICKS.getDefaultState(), slabFlag, stairFlag, wallFlag);
        ModernContent.define();
        GameRegistry.registerTileEntity(BarrelTile.class, new ResourceLocation(UAB.ID, "barrel"));
        GameRegistry.registerTileEntity(CookingTile.Smoker.class, new ResourceLocation(UAB.ID, "smoker"));
        GameRegistry.registerTileEntity(CookingTile.BlastFurnace.class, new ResourceLocation(UAB.ID, "blast_furnace"));
        GameRegistry.registerTileEntity(ComposterTile.class, new ResourceLocation(UAB.ID, "composter"));
        GameRegistry.registerTileEntity(ConduitTile.class, new ResourceLocation(UAB.ID, "conduit"));
        GameRegistry.registerTileEntity(CaveVineTile.class, new ResourceLocation(UAB.ID,"cave_vines"));
        GameRegistry.registerTileEntity(LichenTile.class, new ResourceLocation(UAB.ID,"glow_lichen"));
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(UAB.instance, new WorkstationGuiHandler());
    }
    @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> event) {
        BLOCKS.values().forEach(event.getRegistry()::register);
    }
    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            if(entry.getValue() instanceof AncientCrop || entry.getValue() instanceof FilledCauldron || entry.getValue() instanceof SoulFire)continue;
            if (entry.getValue() instanceof WoodDoor || ITEMS.containsKey(entry.getKey())) continue;
            if(entry.getValue() instanceof BambooBlock||entry.getValue() instanceof BambooShoot||entry.getValue() instanceof CaveVines)continue;
            if(entry.getValue() instanceof BubbleColumn || entry.getValue() instanceof AquaticPlant && ((AquaticPlant)entry.getValue()).submerged && !(entry.getValue() instanceof KelpBlock) && !(entry.getValue() instanceof Seagrass) || entry.getValue() instanceof CoralPlant && ((CoralPlant)entry.getValue()).shape.equals("_wall_fan"))continue;
            if (entry.getValue() instanceof NativeSlab && ((NativeSlab) entry.getValue()).isDouble()) continue;
            boolean fungus = entry.getKey().contains("crimson_") || entry.getKey().contains("warped_");
            ItemBlock item;
            if (entry.getValue() instanceof NativeSlab) {
                NativeSlab slab = (NativeSlab) entry.getValue();
                item = new ItemSlab(slab, slab.single, slab.doubled) { @Override public int getItemBurnTime(ItemStack stack) { return fungus ? 0 : super.getItemBurnTime(stack); } };
            } else if(entry.getValue() instanceof Eyeblossom)item=new ItemBlock(entry.getValue()){{setHasSubtypes(true);}@Override public int getMetadata(int damage){return damage&1;}};
            else if(entry.getValue() instanceof LightBlock)item=new ItemBlock(entry.getValue()){{setHasSubtypes(true);}@Override public int getMetadata(int damage){return damage&15;}};
            else if(entry.getValue() instanceof GlowLichen)item=new GlowLichen.LichenItem(entry.getValue());
            else if(entry.getValue() instanceof SeaPickle || entry.getValue() instanceof CoralPlant || entry.getValue() instanceof Seagrass)item=new AquaticItem(entry.getValue());
            else item = new ItemBlock(entry.getValue()) { @Override public int getItemBurnTime(ItemStack stack) { return fungus ? 0 : super.getItemBurnTime(stack); } };
            item.setRegistryName(entry.getValue().getRegistryName());
            if (available(entry.getKey())) item.setCreativeTab(TAB);
            if (ITEMS.put(entry.getKey(), item) != null) throw new IllegalStateException("Duplicate item " + entry.getKey());
        }
        ITEMS.values().forEach(event.getRegistry()::register);
    }
}
