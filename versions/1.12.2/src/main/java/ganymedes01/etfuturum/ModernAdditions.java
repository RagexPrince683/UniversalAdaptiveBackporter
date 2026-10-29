package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** Additional update content uses the existing availability and native registration owners. */
public final class ModernAdditions {
    private ModernAdditions(){}
    static void define(){
        HangingSigns.define();
        CopperAge.define();
        Content.add("netherite_horse_armor","content.netherite_horse_armor",new NetheriteHorseArmor());
        Content.requires("netherite_horse_armor","blocksitems.enableNetherite");
        SpringPlants.define();
        SpringAnimals.initialize();
        NavigationItems.define();
        RootedSoils.define();
        PowderSnow.define();
        Content.add("respawn_anchor","content.respawn_anchors",new RespawnAnchorBlock());
        RespawnAnchorBlock.dispenser();
        Content.add("bundle","content.bundles",new BundleItem());
        for(String color:BundleItem.COLORS)Content.add(color+"_bundle","content.bundles",new BundleItem());
        Content.add("scaffolding","content.scaffolding",new ScaffoldingBlock());
        Content.add("crafter","content.crafters",new CrafterBlock());
        GameRegistry.registerTileEntity(CrafterTile.class,new ResourceLocation(UAB.ID,"crafter"));
        Content.add("brown_egg","content.spring_to_life",new ColoredEgg(1));Content.add("blue_egg","content.spring_to_life",new ColoredEgg(2));
        Content.add("grindstone","content.grindstones",new GrindstoneBlock());
        Content.add("lectern","content.lecterns",new LecternBlock());
        GameRegistry.registerTileEntity(LecternTile.class,new ResourceLocation(UAB.ID,"lectern"));
        Content.add("bell","content.bells",new BellBlock());
        Content.add("chiseled_bookshelf","content.chiseled_bookshelves",new BookStorage(true,"oak"));
        Content.add("spyglass","content.spyglasses",new SpyglassItem());Content.requires("spyglass","blocksitems.enableCopper","blocksitems.enableAmethyst");
        GameRegistry.registerTileEntity(BookStorage.Storage.class,new ResourceLocation(UAB.ID,"book_storage"));
        GameRegistry.registerTileEntity(BellBlock.BellTile.class,new ResourceLocation(UAB.ID,"bell"));
    }
    static void recipes(RegistryEvent.Register<IRecipe> event){
        Crafting.shaped(event,"respawn_anchor",Content.stack("respawn_anchor"),"CCC","GGG","CCC",'C',Content.block("crying_obsidian"),'G',Blocks.GLOWSTONE);
        Crafting.shaped(event,"bundle",Content.stack("bundle"),"S","L",'S',Items.STRING,'L',Items.LEATHER);
        if(Content.available("bundle"))event.getRegistry().register(new BundleDyeRecipe().setRegistryName(UAB.ID,"bundle_dye"));
        Crafting.shaped(event,"scaffolding",new ItemStack(Content.item("scaffolding"),6),"BSB","B B","B B",'B',Content.item("bamboo"),'S',Items.STRING);
        Crafting.shaped(event,"crafter",Content.stack("crafter"),"III","ICI","RDR",'I',Items.IRON_INGOT,'C',Blocks.CRAFTING_TABLE,'R',Items.REDSTONE,'D',Blocks.DROPPER);
        CopperAge.recipes(event);
        Crafting.shaped(event,"calibrated_sculk_sensor",Content.stack("calibrated_sculk_sensor")," A ","ASA",'A',Content.item("amethyst_shard"),'S',Content.block("sculk_sensor"));
        SpringPlants.recipes(event);
        NavigationItems.recipes(event);
        RootedSoils.recipes();
        for(String wood:HangingSigns.WOODS){String log="stripped_"+wood+(wood.equals("crimson")||wood.equals("warped")?"_stem":wood.equals("bamboo")?"_block":"_log");Crafting.shaped(event,wood+"_hanging_sign",new ItemStack(Content.item(wood+"_hanging_sign"),6),"C C","LLL","LLL",'C',Content.item("chain"),'L',Content.block(log));}
        Crafting.shaped(event,"grindstone",Content.stack("grindstone"),"STS","P P",'S',Items.STICK,'T',new ItemStack(Blocks.STONE_SLAB),'P',"plankWood");
        Crafting.shaped(event,"lectern",Content.stack("lectern"),"SSS"," B "," S ",'S',"slabWood",'B',Blocks.BOOKSHELF);
        Crafting.shaped(event,"bell",Content.stack("bell"),"GGG","G G"," I ",'G',Items.GOLD_INGOT,'I',Items.IRON_INGOT);
        Crafting.shaped(event,"chiseled_bookshelf",Content.stack("chiseled_bookshelf"),"PPP","SSS","PPP",'P',"plankWood",'S',"slabWood");
        Crafting.shaped(event,"spyglass",Content.stack("spyglass"),"A","C","C",'A',Content.item("amethyst_shard"),'C',"ingotCopper");
    }
}
