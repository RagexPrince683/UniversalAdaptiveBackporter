package ganymedes01.etfuturum;

import java.util.*;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.blocks.*;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class CopperAge {
    public static final Item.ToolMaterial TOOLS=EnumHelper.addToolMaterial("UAB_COPPER",1,190,5,1,13);
    public static final ItemArmor.ArmorMaterial ARMOR=EnumHelper.addArmorMaterial("UAB_COPPER",11,new int[]{2,4,3,1},8);
    private CopperAge(){}
    public static boolean repair(ItemStack ingredient){return EtFuturum.hasDictTag(ingredient,"ingotCopper");}
    private static void item(String name,Item item){item.setUnlocalizedName("etfuturum."+name);item.setTextureName(name);ModernAdditions.add(name,"copper_age",item);if(!ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableCopper){item.setCreativeTab(null);ContentAvailability.register(item,false);}}
    private static void block(String name,Block block){ModernAdditions.add(name,"copper_age",block);if(!ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableCopper){block.setCreativeTab(null);ContentAvailability.register(block,false);ContentAvailability.register(Item.getItemFromBlock(block),false);}}
    public static void initialize(){
        item("copper_nugget",new ganymedes01.etfuturum.items.BaseItem("copper_nugget"));
        item("copper_sword",new ItemSword(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        item("copper_pickaxe",new ItemPickaxe(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        item("copper_axe",new ItemAxe(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        item("copper_shovel",new ItemSpade(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        item("copper_hoe",new ItemHoe(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        for(int slot=0;slot<4;slot++){final int armorSlot=slot;item("copper_"+new String[]{"helmet","chestplate","leggings","boots"}[slot],new ItemArmor(ARMOR,0,slot){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}@Override public String getArmorTexture(ItemStack stack,Entity entity,int slot,String type){return "etfuturum:textures/models/armor/copper_layer_"+(armorSlot==2?2:1)+".png";}});}
        item("copper_horse_armor",new ganymedes01.etfuturum.items.ItemCopperHorseArmor());
        block("copper_torch",new net.minecraft.block.BlockTorch(){}.setBlockName("etfuturum.copper_torch").setBlockTextureName("copper_torch").setLightLevel(1));
        for(String family:new String[]{"copper_lantern","copper_chain","copper_bars","lightning_rod"})for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true}){if(family.equals("lightning_rod")&&stage==0&&!wax)continue;String name=ModernAdditions.copperName(family,stage,wax);Block block=family.equals("copper_lantern")?new CopperFixtures.Lantern(name,stage,wax):family.equals("copper_chain")?new CopperFixtures.Chain(name,stage,wax):family.equals("copper_bars")?new CopperFixtures.Bars(name,stage,wax):new CopperFixtures.Rod(name,stage,wax);block(name,block);if(family.equals("lightning_rod")&&!ModBlocks.LIGHTNING_ROD.isEnabled()){block.setCreativeTab(null);ContentAvailability.register(block,false);ContentAvailability.register(Item.getItemFromBlock(block),false);}}
        for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true})if(stage!=0||wax){String name=ModernAdditions.copperName("copper_chest",stage,wax);Block chest=ModernAdditions.add(name,"copper_chests",new BlockCopperChest(name,stage,wax));if(!ModBlocks.COPPER_CHEST.isEnabled()){chest.setCreativeTab(null);ContentAvailability.register(chest,false);ContentAvailability.register(Item.getItemFromBlock(chest),false);}}
        for(String wood:ModernAdditions.WOODS){String name=wood+"_shelf";Block shelf=ModernAdditions.add(name,"copper_age",new BlockBookStorage(false,wood));if(!ModernAdditions.woodEnabled(wood)){shelf.setCreativeTab(null);ContentAvailability.register(shelf,false);ContentAvailability.register(Item.getItemFromBlock(shelf),false);}}
        net.minecraftforge.oredict.OreDictionary.registerOre("nuggetCopper",ModernAdditions.item("copper_nugget"));
    }
    public static void recipes(){
        ModernAdditions.recipe("copper_nugget",9,"C",'C',"ingotCopper");if(ContentAvailability.available(ModernAdditions.item("copper_nugget")))for(ItemStack ingot:net.minecraftforge.oredict.OreDictionary.getOres("ingotCopper"))if(ContentAvailability.available(ingot)){ItemStack result=ingot.copy();result.stackSize=1;GameRegistry.addRecipe(new net.minecraftforge.oredict.ShapedOreRecipe(result,"NNN","NNN","NNN",'N',ModernAdditions.item("copper_nugget")));break;}
        String[] kinds={"sword","pickaxe","axe","shovel","hoe","helmet","chestplate","leggings","boots"};String[][] patterns={{"C","C","S"},{"CCC"," S "," S "},{"CC","CS"," S"},{"C","S","S"},{"CC"," S"," S"},{"CCC","C C"},{"C C","CCC","CCC"},{"CCC","C C","C C"},{"C C","C C"}};
        for(int i=0;i<kinds.length;i++){List<Object> input=new ArrayList<>(Arrays.asList((Object[])patterns[i]));input.add('C');input.add("ingotCopper");input.add('S');input.add(Items.stick);ModernAdditions.recipe("copper_"+kinds[i],1,input.toArray());if(ContentAvailability.available(ModernAdditions.item("copper_"+kinds[i])))GameRegistry.addSmelting(ModernAdditions.item("copper_"+kinds[i]),ModernAdditions.stack("copper_nugget"),.1F);}
        ModernAdditions.recipe("copper_horse_armor",1,"C C","CCC","C C",'C',"ingotCopper");
        ModernAdditions.recipe("copper_torch",4,"N","C","S",'N',ModernAdditions.item("copper_nugget"),'C',new ItemStack(Items.coal,1,32767),'S',Items.stick);
        ModernAdditions.recipe("copper_chain",1,"N","C","N",'N',ModernAdditions.item("copper_nugget"),'C',"ingotCopper");ModernAdditions.recipe("copper_bars",16,"CCC","CCC",'C',"ingotCopper");ModernAdditions.recipe("copper_lantern",1,"NNN","NTN","NNN",'N',ModernAdditions.item("copper_nugget"),'T',ModernAdditions.item("copper_torch"));
        for(String family:new String[]{"copper_lantern","copper_chain","copper_bars","lightning_rod","copper_chest"})for(int stage=0;stage<4;stage++){String plain=ModernAdditions.copperName(family,stage,false),waxed=ModernAdditions.copperName(family,stage,true);if(ContentAvailability.available(ModernAdditions.item(waxed))&&ModItems.HONEYCOMB.isEnabled())GameRegistry.addRecipe(new ShapelessOreRecipe(ModernAdditions.stack(waxed),plain.equals("lightning_rod")?ModBlocks.LIGHTNING_ROD.get():plain.equals("copper_chest")?ModBlocks.COPPER_CHEST.get():ModernAdditions.item(plain),ModItems.HONEYCOMB.get()));}
        if(ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems.enableStrippedLogs)for(String wood:ModernAdditions.WOODS)if(ModernAdditions.woodEnabled(wood))ModernAdditions.recipe(wood+"_shelf",6,"LLL","   ","LLL",'L',ModernAdditions.strippedLog(wood));
    }
}
