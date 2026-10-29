package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.oredict.*;

final class ModernRecipes {
    private ModernRecipes() { }
    private static ItemStack count(String name,int count){ItemStack stack=Content.stack(name);stack.setCount(count);return stack;}
    static void shapeless(RegistryEvent.Register<IRecipe> event,String name,ItemStack output,Object...input){if(output.isEmpty()||output.getItem().getRegistryName().getNamespace().equals(UAB.ID)&&!Content.available(output.getItem().getRegistryName().getPath()))return;for(Object item:input){Item value=item instanceof Block?Item.getItemFromBlock((Block)item):item instanceof Item?(Item)item:item instanceof ItemStack?((ItemStack)item).getItem():null;if(value!=null&&value.getRegistryName().getNamespace().equals(UAB.ID)&&!Content.available(value.getRegistryName().getPath()))return;}event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,name),output,input).setRegistryName(UAB.ID,name));}
    private static void copyTemplate(RegistryEvent.Register<IRecipe> event,String name,String substrate){if(!Content.available(name))return;ItemStack material=ModernLoot.item("minecraft:"+substrate);if(material.isEmpty())return;Crafting.shaped(event,name+"_copy_"+substrate,count(name,2),"DTD","DBD","DDD",'D',Items.DIAMOND,'T',Content.item(name),'B',material);}
    static void register(RegistryEvent.Register<IRecipe> event){
        ModernAdditions.recipes(event);
        if(!net.minecraftforge.fml.common.Loader.isModLoaded("dreamcraft"))Crafting.shaped(event,"old_gravel",new ItemStack(Content.block("old_gravel"),4),"DG","GD",'D',new ItemStack(Blocks.DIRT,1,1),'G',Blocks.GRAVEL);
        shapeless(event,"red_dye_from_old_rose",new ItemStack(Items.DYE,1,1),Content.block("rose"));
        Crafting.shaped(event,"rose_bush_from_old_roses",new ItemStack(Blocks.DOUBLE_PLANT,1,4),"RR","RR","RR",'R',Content.block("rose"));
        Crafting.shaped(event,"old_roses_from_bushes",new ItemStack(Content.block("rose"),12),"BB",'B',new ItemStack(Blocks.DOUBLE_PLANT,1,4));
        if(Features.enabled("functions.enablePrismarineRecipes")&&!net.minecraftforge.fml.common.Loader.isModLoaded("botania")){Crafting.shaped(event,"prismarine_shards_from_quartz",new ItemStack(Items.PRISMARINE_SHARD,4),"QB","GQ",'Q',"gemQuartz",'B',"dyeBlue",'G',"dyeGreen");Crafting.shaped(event,"prismarine_crystals_from_quartz",new ItemStack(Items.PRISMARINE_CRYSTALS,4),"QG","GQ",'Q',"gemQuartz",'G',"dustGlowstone");}

        if(Content.available("shulker_box")){ResourceLocation nativeId=new ResourceLocation("minecraft","purple_shulker_box");if(event.getRegistry() instanceof net.minecraftforge.registries.IForgeRegistryModifiable){((net.minecraftforge.registries.IForgeRegistryModifiable<IRecipe>)event.getRegistry()).remove(nativeId);event.getRegistry().register(new ShapedOreRecipe(null,Content.stack("shulker_box"),"S","C","S",'S',Items.SHULKER_SHELL,'C',Blocks.CHEST).setRegistryName(nativeId));}else Crafting.shaped(event,"shulker_box",Content.stack("shulker_box"),"S","C","S",'S',Items.SHULKER_SHELL,'C',Blocks.CHEST);}
        for(String wood:BoatContent.WOODS){ItemStack plank=BoatContent.plank(wood);if(BoatContent.legacy(wood)==null)Crafting.shaped(event,BoatContent.id(wood,false),Content.stack(BoatContent.id(wood,false)),"P P","PPP",'P',plank);net.minecraft.item.crafting.Ingredient boat=net.minecraft.item.crafting.Ingredient.fromStacks(BoatContent.nativeBoat(wood),Content.stack(BoatContent.id(wood,false)));shapeless(event,BoatContent.id(wood,true),Content.stack(BoatContent.id(wood,true)),boat,Blocks.CHEST);}
        for(java.util.Map.Entry<String,WoodSigns.Standing> entry:WoodSigns.SIGNS.entrySet()){String wood=entry.getKey();ItemStack plank;BlockPlanks.EnumType nativeWood=BoatContent.legacy(wood);if(nativeWood!=null)plank=new ItemStack(Blocks.PLANKS,1,nativeWood.getMetadata());else plank=Content.stack(wood+"_planks");Crafting.shaped(event,wood+"_sign",new ItemStack(Content.item(entry.getValue().itemId()),3),"PPP","PPP"," S ",'P',plank,'S',Items.STICK);}

        Crafting.shaped(event,"music_disc_5",Content.stack("five_record"),"FFF","FFF","FFF",'F',Content.item("disc_fragment_5"));
        if(Features.enabled("blocksitems.enableNewDyes"))event.getRegistry().register(new DyeRecipe());if(Content.available("suspicious_stew"))event.getRegistry().register(new StewRecipe());if(Content.available("decorated_pot"))event.getRegistry().register(new PotRecipe());
        String[] substrates={"cobblestone","sandstone","cobblestone","mossy_cobblestone","cobbled_deepslate","end_stone","cobblestone","prismarine","blackstone","netherrack","purpur_block","terracotta","terracotta","cobbled_deepslate","terracotta","terracotta","breeze_rod","copper_block"};
        for(int i=0;i<ModernContent.TRIMS.length;i++)copyTemplate(event,ModernContent.TRIMS[i]+"_armor_trim_smithing_template",substrates[i]);copyTemplate(event,"bolt_armor_trim_smithing_template","waxed_copper_block");copyTemplate(event,"netherite_upgrade_smithing_template","netherrack");
        for(EnumDyeColor color:new EnumDyeColor[]{EnumDyeColor.WHITE,EnumDyeColor.BLUE,EnumDyeColor.BROWN,EnumDyeColor.BLACK}){String name=color.getName()+"_dye";shapeless(event,name,Content.stack(name),new ItemStack(Items.DYE,1,color.getDyeDamage()));if(Content.available(name))OreDictionary.registerOre("dye"+Character.toUpperCase(color.getName().charAt(0))+color.getName().substring(1),Content.stack(name));}
        shapeless(event,"blue_dye_from_cornflower",Content.stack("blue_dye"),Content.block("cornflower"));shapeless(event,"white_dye_from_lily",Content.stack("white_dye"),Content.block("lily_of_the_valley"));shapeless(event,"black_dye_from_rose",Content.stack("black_dye"),Content.block("wither_rose"));
        Crafting.shaped(event,"beehive",Content.stack("beehive"),"PPP","HHH","PPP",'P',"plankWood",'H',Content.item("honeycomb"));
        Crafting.shaped(event,"honeycomb_block",Content.stack("honeycomb_block"),"HH","HH",'H',Content.item("honeycomb"));
        Crafting.shaped(event,"mace",Content.stack("mace"),"H","R",'H',Content.block("heavy_core"),'R',Content.item("breeze_rod"));
        Crafting.shaped(event,"honey_block",Content.stack("honey_block"),"HH","HH",'H',Content.item("honey_bottle"));
        shapeless(event,"honey_bottles_from_block",count("honey_bottle",4),Content.block("honey_block"),Items.GLASS_BOTTLE,Items.GLASS_BOTTLE,Items.GLASS_BOTTLE,Items.GLASS_BOTTLE);
        shapeless(event,"sugar_from_honey",new ItemStack(Items.SUGAR,3),Content.item("honey_bottle"));
        Crafting.shaped(event,"campfire",Content.stack("campfire")," S ","SCS","LLL",'S',"stickWood",'C',new ItemStack(Items.COAL,1,OreDictionary.WILDCARD_VALUE),'L',"logWood");
        Crafting.shaped(event,"soul_campfire",Content.stack("soul_campfire")," S ","SCS","LLL",'S',"stickWood",'C',Blocks.SOUL_SAND,'L',"logWood");
        Crafting.shaped(event,"target",Content.stack("target")," R ","RHR"," R ",'R',Items.REDSTONE,'H',Blocks.HAY_BLOCK);
        Crafting.shaped(event,"lightning_rod",count("lightning_rod",1),"C","C","C",'C',Content.item("copper_ingot"));
        Crafting.shaped(event,"dripstone_block",Content.stack("dripstone_block"),"DD","DD",'D',Content.block("pointed_dripstone"));
        Crafting.shaped(event,"soul_torch",count("soul_torch",4),"C","S","B",'C',"coal",'S',Items.STICK,'B',Blocks.SOUL_SAND);
        Crafting.shaped(event,"soul_torch_from_soil",count("soul_torch",4),"C","S","B",'C',"coal",'S',Items.STICK,'B',Content.block("soul_soil"));
        Crafting.shaped(event,"soul_lantern",Content.stack("soul_lantern"),"NNN","NTN","NNN",'N',Items.IRON_NUGGET,'T',Content.block("soul_torch"));
        Crafting.shaped(event,"candle",Content.stack("candle"),"S","H",'S',Items.STRING,'H',Content.item("honeycomb"));
        for(EnumDyeColor color:EnumDyeColor.values())shapeless(event,color.getName()+"_candle",Content.stack(color.getName()+"_candle"),Content.block("candle"),new ItemStack(Items.DYE,1,color.getDyeDamage()));
        Crafting.shaped(event,"brush",Content.stack("brush"),"F","C","S",'F',Items.FEATHER,'C',Content.item("copper_ingot"),'S',Items.STICK);
        for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true}){String prefix=(wax?"waxed_":"")+new String[]{"","exposed_","weathered_","oxidized_"}[stage],base=prefix+(stage==0?"copper_block":"copper"),cut=prefix+"cut_copper";Crafting.shaped(event,cut,count(cut,4),"CC","CC",'C',Content.block(base));Crafting.shaped(event,prefix+"chiseled_copper",Content.stack(prefix+"chiseled_copper"),"S","S",'S',Content.block(prefix+"cut_copper_slab"));Crafting.shaped(event,prefix+"copper_grate",count(prefix+"copper_grate",4)," C ","C C"," C ",'C',Content.block(base));Crafting.shaped(event,prefix+"copper_bulb",count(prefix+"copper_bulb",4)," C ","CBC"," R ",'C',Content.block(base),'B',Items.BLAZE_ROD,'R',Items.REDSTONE);Crafting.shaped(event,prefix+"copper_door",count(prefix+"copper_door",3),"CC","CC","CC",'C',Content.block(base));Crafting.shaped(event,prefix+"copper_trapdoor",count(prefix+"copper_trapdoor",2),"CCC","CCC",'C',Content.block(base));}
        for(String name:Content.BLOCKS.keySet())if(name.startsWith("waxed_")&&CopperAging.copper(Content.block(name))&&!name.contains("double_slab"))shapeless(event,name+"_waxing",Content.stack(name),Content.item(name.substring(6)),Content.item("honeycomb"));
        for(String wood:new String[]{"oak","spruce","birch","jungle","acacia","dark_oak","cherry","mangrove","pale_oak"}){Object log=Content.BLOCKS.containsKey(wood+"_log")?Content.block(wood+"_log"):null,planks=Content.BLOCKS.containsKey(wood+"_planks")?Content.block(wood+"_planks"):null;for(BlockPlanks.EnumType type:BlockPlanks.EnumType.values())if(type.getName().equals(wood)){log=new ItemStack(type.getMetadata()<4?Blocks.LOG:Blocks.LOG2,1,type.getMetadata()<4?type.getMetadata():type.getMetadata()-4);planks=new ItemStack(Blocks.PLANKS,4,type.getMetadata());}Crafting.shaped(event,wood+"_wood",count(wood+"_wood",3),"LL","LL",'L',log);Crafting.shaped(event,"stripped_"+wood+"_wood",count("stripped_"+wood+"_wood",3),"LL","LL",'L',Content.block("stripped_"+wood+"_log"));for(String input:new String[]{wood+"_wood","stripped_"+wood+"_wood"}){ItemStack output=planks instanceof Block?new ItemStack((Block)planks,4):((ItemStack)planks).copy();Crafting.shaped(event,"planks_from_"+input,output,"L",'L',Content.block(input));}}
        Object[] tips={"plankWood",Blocks.COBBLESTONE,Content.item("copper_ingot"),Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND};String[] names={"wooden","stone","copper","iron","golden","diamond"};for(int i=0;i<names.length;i++)Crafting.shaped(event,names[i]+"_spear",Content.stack(names[i]+"_spear"),"  M"," S ","S  ",'M',tips[i],'S',Items.STICK);
        Crafting.shaped(event,"dried_ghast",Content.stack("dried_ghast"),"GGG","GSG","GGG",'G',Items.GHAST_TEAR,'S',Blocks.SOUL_SAND);
        for(EnumDyeColor color:EnumDyeColor.values())Crafting.shaped(event,color.getName()+"_harness",new ItemStack(Content.item("harness"),1,color.getMetadata()),"LLL","GWG",'L',Items.LEATHER,'G',Blocks.GLASS,'W',new ItemStack(Blocks.WOOL,1,color.getMetadata()));
        Crafting.shaped(event,"copper_chest",Content.stack("copper_chest"),"CCC","CHC","CCC",'C',Content.item("copper_ingot"),'H',net.minecraft.init.Blocks.CHEST);
        Crafting.shaped(event,"resin_block",Content.stack("resin_block"),"RRR","RRR","RRR",'R',Content.block("resin_clump"));
        shapeless(event,"resin_clumps",count("resin_clump",9),Content.block("resin_block"));
        Crafting.shaped(event,"resin_bricks",Content.stack("resin_bricks"),"RR","RR",'R',Content.item("resin_brick"));
        Crafting.shaped(event,"chiseled_resin_bricks",Content.stack("chiseled_resin_bricks"),"S","S",'S',Content.block("resin_brick_slab"));
        Crafting.shaped(event,"creaking_heart",Content.stack("creaking_heart"),"L","R","L",'L',Content.block("pale_oak_log"),'R',Content.block("resin_block"));
        shapeless(event,"gray_dye_from_eyeblossom",new ItemStack(Items.DYE,1,EnumDyeColor.GRAY.getDyeDamage()),new ItemStack(Content.block("eyeblossom"),1,0));
        shapeless(event,"orange_dye_from_eyeblossom",new ItemStack(Items.DYE,1,EnumDyeColor.ORANGE.getDyeDamage()),new ItemStack(Content.block("eyeblossom"),1,1));
        if(Content.available("resin_brick"))net.minecraftforge.fml.common.registry.GameRegistry.addSmelting(Content.block("resin_clump"),Content.stack("resin_brick"),.1F);
        shapeless(event,"wind_charges",count("wind_charge",4),Content.item("breeze_rod"));
        Crafting.shaped(event,"wolf_armor",Content.stack("wolf_armor"),"S  ","SSS","S S",'S',Content.item("armadillo_scute"));
        shapeless(event,"orange_dye_from_torchflower",new ItemStack(Items.DYE,1,EnumDyeColor.ORANGE.getDyeDamage()),Content.block("torchflower"));
        shapeless(event,"cyan_dye_from_pitcher",new ItemStack(Items.DYE,2,EnumDyeColor.CYAN.getDyeDamage()),Content.block("pitcher_plant"));
        if(Features.enabled("functions.enableRecipeForTotem"))Crafting.shaped(event,"totem",new ItemStack(Items.TOTEM_OF_UNDYING),"EBE","GBG"," G ",'E',Items.EMERALD,'G',Items.GOLD_INGOT,'B',Blocks.GOLD_BLOCK);
    }
}
