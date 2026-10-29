package ganymedes01.etfuturum;

import java.util.*;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.blocks.*;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Dynamic families complement the stable legacy enums without changing existing ordinals or identities. */
public final class ModernAdditions {
    public static final Map<String,Block> BLOCKS=new LinkedHashMap<>();public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
    public static final String[] WOODS={"oak","spruce","birch","jungle","acacia","dark_oak","crimson","warped","mangrove","cherry","bamboo","pale_oak"};
    public static ItemStack planks(String wood){int index=Arrays.asList(WOODS).indexOf(wood);return index<6?new ItemStack(Blocks.planks,1,index):index==11?ModBlocks.PALE_OAK_PLANKS.newItemStack():ModBlocks.WOOD_PLANKS.newItemStack(1,index-6);}
    public static boolean woodEnabled(String wood){int index=Arrays.asList(WOODS).indexOf(wood);switch(index){case 6:return ganymedes01.etfuturum.configuration.configs.ConfigExperiments.enableCrimsonBlocks;case 7:return ganymedes01.etfuturum.configuration.configs.ConfigExperiments.enableWarpedBlocks;case 8:return ganymedes01.etfuturum.configuration.configs.ConfigExperiments.enableMangroveBlocks;case 9:return ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableCherryBlocks;case 10:return ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableBambooBlocks;case 11:return ModBlocks.PALE_OAK_PLANKS.isEnabled();default:return true;}}
    public static ItemStack strippedLog(String wood){int index=Arrays.asList(WOODS).indexOf(wood);if(index<4)return ModBlocks.LOG_STRIPPED.newItemStack(1,index);if(index<6)return ModBlocks.LOG2_STRIPPED.newItemStack(1,index-4);ModBlocks block=index==6?ModBlocks.CRIMSON_STEM:index==7?ModBlocks.WARPED_STEM:index==8?ModBlocks.MANGROVE_LOG:index==9?ModBlocks.CHERRY_LOG:index==10?ModBlocks.BAMBOO_BLOCK:ModBlocks.PALE_OAK_LOG;return block.newItemStack(1,index==10?1:2);}
    private static void hanging(String wood){for(boolean ceiling:new boolean[]{true,false}){String id=wood+(ceiling?"_hanging_sign":"_wall_hanging_sign");ItemStack planks=planks(wood);BlockHangingSign sign=new BlockHangingSign(wood,ceiling,Block.getBlockFromItem(planks.getItem()),planks.getItemDamage());GameRegistry.registerBlock(sign,(Class<? extends ItemBlock>)null,id);BLOCKS.put(id,sign);if(!wood.equals("crimson")&&!wood.equals("warped"))Blocks.fire.setFireInfo(sign,5,20);ContentAvailability.register(sign,ContentAvailability.enabled("hanging_signs")&&woodEnabled(wood));}Item item=add(wood+"_hanging_sign","hanging_signs",new ganymedes01.etfuturum.items.ItemHangingSign(wood));if(!woodEnabled(wood)){item.setCreativeTab(null);ContentAvailability.register(item,false);}}
    public static String copperName(String family,int stage,boolean wax){return (wax?"waxed_":"")+new String[]{"","exposed_","weathered_","oxidized_"}[stage]+family;}
    private ModernAdditions(){}
    public static Block block(String name){return Objects.requireNonNull(BLOCKS.get(name),name);}public static Item item(String name){return Objects.requireNonNull(ITEMS.get(name),name);}public static ItemStack stack(String name){return new ItemStack(item(name));}
    static <T extends Block>T add(String name,String feature,T block){if(block instanceof BlockScaffolding)GameRegistry.registerBlock(block,ganymedes01.etfuturum.items.ItemScaffolding.class,name);else GameRegistry.registerBlock(block,name);if(block instanceof BlockBookStorage){BlockBookStorage storage=(BlockBookStorage)block;if(!storage.wood.equals("crimson")&&!storage.wood.equals("warped"))Blocks.fire.setFireInfo(block,storage.books?30:5,20);}BLOCKS.put(name,block);Item item=Item.getItemFromBlock(block);ITEMS.put(name,item);boolean enabled=ContentAvailability.enabled(feature);ContentAvailability.register(block,enabled);ContentAvailability.register(item,enabled);block.setCreativeTab(enabled?EtFuturum.creativeTabBlocks:null);return block;}
    static <T extends Item>T add(String name,String feature,T item){GameRegistry.registerItem(item,name);ITEMS.put(name,item);boolean enabled=ContentAvailability.enabled(feature);ContentAvailability.register(item,enabled);item.setCreativeTab(enabled?EtFuturum.creativeTabItems:null);return item;}
    public static void initialize(){
        add("grindstone","grindstones",new BlockGrindstone());
        add("shield","shields",new ganymedes01.etfuturum.items.ItemShield());
        add("lectern","lecterns",new BlockLectern());Blocks.fire.setFireInfo(block("lectern"),30,20);
        GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityLectern.class,"etfuturum.lectern");
        add("bell","bells",new BlockBell());add("chiseled_bookshelf","chiseled_bookshelves",new BlockBookStorage(true,"oak"));
        Item spyglass=add("spyglass","spyglasses",new ganymedes01.etfuturum.items.ItemSpyglass());if(!ModItems.AMETHYST_SHARD.isEnabled()||!ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableCopper){spyglass.setCreativeTab(null);ContentAvailability.register(spyglass,false);}
        for(String wood:WOODS)hanging(wood);
        CopperAge.initialize();
        Item horseArmor=add("netherite_horse_armor","netherite_horse_armor",new ganymedes01.etfuturum.items.ItemNetheriteHorseArmor());
        if(!ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableNetherite){ContentAvailability.register(horseArmor,false);horseArmor.setCreativeTab(null);}
        SpringPlants.initialize();
        SpringAnimals.initialize();
        NavigationItems.initialize();
        RootedSoils.initialize();
        PowderSnow.initialize();
        add("respawn_anchor","respawn_anchors",new BlockRespawnAnchor());
        BlockRespawnAnchor.dispenser();
        add("bundle","bundles",new ganymedes01.etfuturum.items.ItemBundle("bundle"));
        for(String color:ganymedes01.etfuturum.items.ItemBundle.COLORS)add(color+"_bundle","bundles",new ganymedes01.etfuturum.items.ItemBundle(color+"_bundle"));
        add("scaffolding","scaffolding",new BlockScaffolding());
        GameRegistry.registerFuelHandler(stack->stack!=null&&stack.getItem() instanceof ganymedes01.etfuturum.items.ItemScaffolding?400:0);
        add("crafter","crafters",new BlockCrafter());
        GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityCrafter.class,"etfuturum.crafter");
        ganymedes01.etfuturum.items.SpearLunge.initialize();
        add("brown_egg","spring_to_life",new ganymedes01.etfuturum.items.ItemColoredEgg("brown_egg",1));add("blue_egg","spring_to_life",new ganymedes01.etfuturum.items.ItemColoredEgg("blue_egg",2));
        GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityHangingSign.class,"etfuturum.hanging_sign");
        GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityBookStorage.class,"etfuturum.book_storage");GameRegistry.registerTileEntity(BlockBell.BellTile.class,"etfuturum.bell");
    }
    static void recipe(String output,int count,Object...input){if(ContentAvailability.available(item(output)))GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(item(output),count),input));}
    public static void recipes(){
        recipe("respawn_anchor",1,"CCC","GGG","CCC",'C',ModBlocks.CRYING_OBSIDIAN.get(),'G',Blocks.glowstone);
        recipe("bundle",1,"S","L",'S',Items.string,'L',Items.leather);
        net.minecraftforge.oredict.RecipeSorter.register("etfuturum:bundle_dye",ganymedes01.etfuturum.recipes.BundleDyeRecipe.class,net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS,"after:minecraft:shapeless");
        if(ContentAvailability.enabled("bundles"))GameRegistry.addRecipe(new ganymedes01.etfuturum.recipes.BundleDyeRecipe());
        recipe("scaffolding",6,"BSB","B B","B B",'B',ModItems.BAMBOO.get(),'S',Items.string);
        recipe("crafter",1,"III","ICI","RDR",'I',Items.iron_ingot,'C',Blocks.crafting_table,'R',Items.redstone,'D',Blocks.dropper);
        CopperAge.recipes();
        if(ModBlocks.CALIBRATED_SCULK_SENSOR.isEnabled()&&ModItems.AMETHYST_SHARD.isEnabled())GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.CALIBRATED_SCULK_SENSOR.newItemStack()," A ","ASA",'A',ModItems.AMETHYST_SHARD.get(),'S',ModBlocks.SCULK_SENSOR.get()));
        SpringPlants.recipes();
        NavigationItems.recipes();
        RootedSoils.recipes();
        if(ModBlocks.CHAIN.isEnabled()&&ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableStrippedLogs)for(String wood:WOODS)if(woodEnabled(wood))recipe(wood+"_hanging_sign",6,"C C","LLL","LLL",'C',ModBlocks.CHAIN.get(),'L',strippedLog(wood));
        recipe("grindstone",1,"STS","P P",'S',Items.stick,'T',new ItemStack(Blocks.stone_slab),'P',"plankWood");
        recipe("shield",1,"PIP","PPP"," P ",'P',"plankWood",'I',Items.iron_ingot);
        recipe("lectern",1,"SSS"," B "," S ",'S',"slabWood",'B',Blocks.bookshelf);
        recipe("bell",1,"GGG","G G"," I ",'G',Items.gold_ingot,'I',Items.iron_ingot);
        recipe("chiseled_bookshelf",1,"PPP","SSS","PPP",'P',"plankWood",'S',"slabWood");
        if(ModItems.AMETHYST_SHARD.isEnabled()&&ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableCopper)recipe("spyglass",1,"A","C","C",'A',ModItems.AMETHYST_SHARD.get(),'C',"ingotCopper");
    }
}
