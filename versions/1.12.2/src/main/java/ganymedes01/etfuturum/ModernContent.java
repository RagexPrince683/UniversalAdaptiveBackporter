package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** The remaining native registrations share Content's authoritative identity and acquisition maps. */
final class ModernContent {
    private ModernContent() { }
    static final String[] SHERDS={"angler","archer","arms_up","blade","brewer","burn","danger","explorer","flow","friend","guster","heart","heartbreak","howl","miner","mourner","plenty","prize","scrape","sheaf","shelter","skull","snort"};
    static final String[] TRIMS={"sentry","dune","coast","wild","ward","eye","vex","tide","snout","rib","spire","wayfinder","shaper","silence","raiser","host","flow","bolt"};
    static void define(){
        Content.add("lily_of_the_valley","blocksitems.enableLilyOfTheValley",new ModernFlower(false));
        Content.add("cornflower","blocksitems.enableCornflower",new ModernFlower(false));
        Content.add("wither_rose","blocksitems.enableWitherRose",new ModernFlower(true));
        for(EnumDyeColor color:new EnumDyeColor[]{EnumDyeColor.WHITE,EnumDyeColor.BLUE,EnumDyeColor.BROWN,EnumDyeColor.BLACK})Content.add(color.getName()+"_dye","blocksitems.enableNewDyes",new ModernDye(color));
        Content.add("suspicious_stew","blocksitems.enableSuspiciousStew",new SuspiciousStew());
        Content.add("beehive","entities.enableBees",new BeeHiveBlock(false));
        Content.add("bee_nest","entities.enableBees",new BeeHiveBlock(true));
        GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityBeeHive.class,new ResourceLocation(UAB.ID,"hive"));
        Content.add("goat_horn","entities.enableGoats",new ganymedes01.etfuturum.items.ItemGoatHorn());
        Content.add("harness","content.harnesses",new VariantItem("white_harness","orange_harness","magenta_harness","light_blue_harness","yellow_harness","lime_harness","pink_harness","gray_harness","light_gray_harness","cyan_harness","purple_harness","blue_harness","brown_harness","green_harness","red_harness","black_harness"));
        Content.add("nautilus_armor","content.nautilus_armor",new VariantItem("copper_nautilus_armor","golden_nautilus_armor","iron_nautilus_armor","diamond_nautilus_armor","netherite_nautilus_armor"));
        Content.add("dried_ghast","content.dried_ghasts",new DriedGhast());
        GameRegistry.registerTileEntity(DriedGhastTile.class,new ResourceLocation(UAB.ID,"dried_ghast"));
        Content.add("frogspawn","entities.enableFrogs",new Frogspawn());
        Content.add("tadpole_bucket","entities.enableFrogs",new FishBucket(5));
        for(String light:new String[]{"ochre_froglight","pearlescent_froglight","verdant_froglight"})Content.add(light,"entities.enableFrogs",new BlockRotatedPillar(Material.GLASS){{setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y));setHardness(.3F);setLightLevel(1);}});
        Content.add("copper_chest","content.copper_chests",new CopperChestBlock());
        Content.add("copper_golem_statue","content.copper_golem_statues",new CopperStatue());
        GameRegistry.registerTileEntity(CopperChestTile.class,new ResourceLocation(UAB.ID,"copper_chest"));
        GameRegistry.registerTileEntity(CopperStatueTile.class,new ResourceLocation(UAB.ID,"copper_statue"));
        Content.add("eyeblossom","experiments.enablePaleGarden",new Eyeblossom());
        Content.add("creaking_heart","experiments.enablePaleGarden",new CreakingHeart());
        Content.add("resin_clump","experiments.enablePaleGarden",new ResinClump());
        Content.add("resin_block","experiments.enablePaleGarden",new Block(Material.CLAY).setHardness(0));
        Content.add("resin_bricks","experiments.enablePaleGarden",new Block(Material.ROCK).setHardness(1.5F).setResistance(6));
        Content.add("chiseled_resin_bricks","experiments.enablePaleGarden",new Block(Material.ROCK).setHardness(1.5F).setResistance(6));
        Content.add("resin_brick","experiments.enablePaleGarden",new Item());
        Content.shapes("resin_bricks","experiments.enablePaleGarden",Content.block("resin_bricks").getDefaultState(),true,true);
        GameRegistry.registerTileEntity(CreakingHeartTile.class,new ResourceLocation(UAB.ID,"creaking_heart"));
        Content.add("sculk","experiments.enableSculk",new SculkBlock(1));
        Content.add("sculk_catalyst","experiments.enableSculk",new SculkCatalyst());
        Content.add("sculk_sensor","experiments.enableSculk",new SculkSensor());
        Content.add("calibrated_sculk_sensor","experiments.enableSculk",new CalibratedSculkSensor());Content.requires("calibrated_sculk_sensor","blocksitems.enableAmethyst");
        GameRegistry.registerTileEntity(SculkCatalystTile.class,new ResourceLocation(UAB.ID,"sculk_catalyst"));
        Content.add("sculk_shrieker","experiments.enableSculk",new SculkShrieker());
        Content.add("reinforced_deepslate","experiments.enableSculk",new Block(Material.ROCK){@Override public Item getItemDropped(net.minecraft.block.state.IBlockState state,java.util.Random random,int fortune){return Items.AIR;}@Override public net.minecraft.block.material.EnumPushReaction getPushReaction(net.minecraft.block.state.IBlockState state){return net.minecraft.block.material.EnumPushReaction.BLOCK;}@Override public boolean canSilkHarvest(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.block.state.IBlockState state,net.minecraft.entity.player.EntityPlayer player){return false;}}.setHardness(55).setResistance(1200));
        GameRegistry.registerTileEntity(SculkSensorTile.class,new ResourceLocation(UAB.ID,"sculk_sensor"));
        GameRegistry.registerTileEntity(SculkShriekerTile.class,new ResourceLocation(UAB.ID,"sculk_shrieker"));
        Content.add("wind_charge","content.wind_charges",new WindChargeItem());
        Content.add("armadillo_scute","content.wolf_armor",new Item());
        Content.add("wolf_armor","content.wolf_armor",new Item().setMaxStackSize(1).setMaxDamage(64));
        Content.add("sniffer_egg","content.sniffer_eggs",new SnifferEgg());
        Content.add("torchflower_crop","content.ancient_plants",new AncientCrop(false)).setCreativeTab(null);
        Content.add("pitcher_crop","content.ancient_plants",new AncientCrop(true)).setCreativeTab(null);
        Content.add("torchflower","content.ancient_plants",new BlockBush(){});
        Content.add("pitcher_plant","content.ancient_plants",new PitcherPlant());
        Content.add("torchflower_seeds","content.ancient_plants",new ItemSeeds(Content.block("torchflower_crop"),Blocks.FARMLAND));
        Content.add("pitcher_pod","content.ancient_plants",new ItemSeeds(Content.block("pitcher_crop"),Blocks.FARMLAND));
        Content.add("target","blocksitems.enableTarget",new TargetBlock());
        Content.add("light","blocksitems.enableLightBlock",new LightBlock());
        Content.add("lightning_rod","experiments.enableLightningRod",new LightningRod());
        Content.add("dripstone_block","experiments.enableDripstone",new Block(Material.ROCK).setHardness(1.5F).setResistance(3));
        Content.add("pointed_dripstone","experiments.enableDripstone",new PointedDripstone());
        Content.add("lava_cauldron","blocksitems.enableLavaCauldrons",new FilledCauldron(true)).setCreativeTab(null);
        Content.add("potion_cauldron","blocksitems.enablePotionCauldron",new FilledCauldron(false)).setCreativeTab(null);
        Content.add("soul_torch","blocksitems.enableSoulLighting",new BlockTorch(){{setLightLevel(10/15F);}});
        Content.add("soul_fire","mixins.soulFire",new SoulFire()).setCreativeTab(null);
        for(String color:new String[]{"","white_","orange_","magenta_","light_blue_","yellow_","lime_","pink_","gray_","light_gray_","cyan_","purple_","blue_","brown_","green_","red_","black_"})Content.add(color+"candle","content.candles",new CandleBlock());
        for(String kind:new String[]{"chiseled_copper","copper_grate","copper_bulb","copper_door","copper_trapdoor"})for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true}){
            String name=(wax?"waxed_":"")+new String[]{"","exposed_","weathered_","oxidized_"}[stage]+kind;Block block=kind.endsWith("_door")?new CopperDecoration.Door(wax):kind.endsWith("_trapdoor")?new CopperDecoration.Trapdoor(wax):kind.endsWith("_grate")?new CopperDecoration.Grate(stage,wax):kind.endsWith("_bulb")?new CopperDecoration.Bulb(stage,wax):new CopperBlock(stage,wax,kind);Content.add(name,"content.decorative_copper",block);
            if(block instanceof BlockDoor)Content.add(name,"content.decorative_copper",new ItemDoor(block){@Override public int getItemBurnTime(ItemStack stack){return 0;}});
        }
        for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true}){String name=(wax?"waxed_":"")+new String[]{"","exposed_","weathered_","oxidized_"}[stage]+"cut_copper";Content.shapes(name,"blocksitems.enableCopper",Content.block(name).getDefaultState(),true,false);}
        for(String tree:new String[]{"oak","spruce","birch","jungle","acacia","dark_oak","cherry","mangrove","pale_oak"}){
            String flag=tree.equals("cherry")?"blocksitems.enableCherryBlocks":tree.equals("mangrove")?"experiments.enableMangroveBlocks":tree.equals("pale_oak")?"experiments.enablePaleGarden":"blocksitems.enableBarkLogs";
            Content.add(tree+"_wood",flag,new ModernLog());Content.add("stripped_"+tree+"_wood",flag,new ModernLog());Content.requires("stripped_"+tree+"_wood","blocksitems.enableStrippedLogs");
        }
        Content.add("gilded_blackstone","blocksitems.enableBlackstone",new GildedBlackstone());
        Content.add("heavy_core","content.heavy_cores",new Block(Material.IRON){@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state){return false;}@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state){return false;}@Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state,net.minecraft.world.IBlockAccess world,net.minecraft.util.math.BlockPos pos){return new net.minecraft.util.math.AxisAlignedBB(.25,0,.25,.75,.5,.75);}}.setHardness(10).setResistance(30));
        String[] spearNames={"wooden","stone","copper","iron","golden","diamond","netherite"};Item.ToolMaterial[] materials={Item.ToolMaterial.WOOD,Item.ToolMaterial.STONE,net.minecraftforge.common.util.EnumHelper.addToolMaterial("UAB_COPPER_SPEAR",1,190,4,1,13),Item.ToolMaterial.IRON,Item.ToolMaterial.GOLD,Item.ToolMaterial.DIAMOND,Content.NETHERITE};for(int i=0;i<spearNames.length;i++){String name=spearNames[i]+"_spear";Content.add(name,"content.spears",new SpearItem(materials[i]));if(i==6)Content.requires(name,"blocksitems.enableNetherite");if(i==2)Content.requires(name,"blocksitems.enableCopper");}
        Content.add("ominous_bottle","content.ominous_bottles",new OminousBottle());
        for(String sherd:SHERDS)Content.add(sherd+"_pottery_sherd","content.archaeology",new Item());
        for(String trim:TRIMS)Content.add(trim+"_armor_trim_smithing_template","content.armor_trims",new ganymedes01.etfuturum.items.ItemSmithingTemplate(trim));
        Content.add("brush","content.brushes",new BrushItem());
        Content.add("suspicious_sand","content.archaeology",new BrushableBlock(Blocks.SAND));
        Content.add("suspicious_gravel","content.archaeology",new BrushableBlock(Blocks.GRAVEL));
        Content.add("decorated_pot","content.decorated_pots",new DecoratedPot());
        GameRegistry.registerTileEntity(BrushableTile.class,new ResourceLocation(UAB.ID,"brushable"));
        GameRegistry.registerTileEntity(DecoratedPotTile.class,new ResourceLocation(UAB.ID,"decorated_pot"));
        GameRegistry.registerTileEntity(PotionCauldronTile.class,new ResourceLocation(UAB.ID,"potion_cauldron"));
        GameRegistry.registerTileEntity(LightningRodTile.class,new ResourceLocation(UAB.ID,"lightning_rod"));
    }
}
