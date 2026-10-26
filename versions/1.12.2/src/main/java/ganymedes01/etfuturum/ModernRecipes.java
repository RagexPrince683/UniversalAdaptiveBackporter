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
    private static void shapeless(RegistryEvent.Register<IRecipe> event,String name,ItemStack output,Object...input){if(output.isEmpty()||output.getItem().getRegistryName().getNamespace().equals(UAB.ID)&&!Content.available(output.getItem().getRegistryName().getPath()))return;for(Object item:input){Item value=item instanceof Block?Item.getItemFromBlock((Block)item):item instanceof Item?(Item)item:item instanceof ItemStack?((ItemStack)item).getItem():null;if(value!=null&&value.getRegistryName().getNamespace().equals(UAB.ID)&&!Content.available(value.getRegistryName().getPath()))return;}event.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation(UAB.ID,name),output,input).setRegistryName(UAB.ID,name));}
    static void register(RegistryEvent.Register<IRecipe> event){
        if(Features.enabled("blocksitems.enableNewDyes"))event.getRegistry().register(new DyeRecipe());if(Content.available("suspicious_stew"))event.getRegistry().register(new StewRecipe());if(Content.available("decorated_pot"))event.getRegistry().register(new PotRecipe());
        for(EnumDyeColor color:new EnumDyeColor[]{EnumDyeColor.WHITE,EnumDyeColor.BLUE,EnumDyeColor.BROWN,EnumDyeColor.BLACK}){String name=color.getName()+"_dye";shapeless(event,name,Content.stack(name),new ItemStack(Items.DYE,1,color.getDyeDamage()));if(Content.available(name))OreDictionary.registerOre("dye"+Character.toUpperCase(color.getName().charAt(0))+color.getName().substring(1),Content.stack(name));}
        shapeless(event,"blue_dye_from_cornflower",Content.stack("blue_dye"),Content.block("cornflower"));shapeless(event,"white_dye_from_lily",Content.stack("white_dye"),Content.block("lily_of_the_valley"));shapeless(event,"black_dye_from_rose",Content.stack("black_dye"),Content.block("wither_rose"));
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
        shapeless(event,"orange_dye_from_torchflower",new ItemStack(Items.DYE,2,EnumDyeColor.ORANGE.getDyeDamage()),Content.block("torchflower"));
        shapeless(event,"cyan_dye_from_pitcher",new ItemStack(Items.DYE,2,EnumDyeColor.CYAN.getDyeDamage()),Content.block("pitcher_plant"));
        if(Features.enabled("functions.enableRecipeForTotem"))Crafting.shaped(event,"totem",new ItemStack(Items.TOTEM_OF_UNDYING),"EBE","GBG"," G ",'E',Items.EMERALD,'G',Items.GOLD_INGOT,'B',Blocks.GOLD_BLOCK);
    }
}
