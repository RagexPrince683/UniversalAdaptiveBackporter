package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** Extend the existing copper aging owner and native equipment systems. */
public final class CopperAge {
    public static final Item.ToolMaterial TOOLS=EnumHelper.addToolMaterial("UAB_COPPER",1,190,5,1,13);
    public static final ItemArmor.ArmorMaterial ARMOR=EnumHelper.addArmorMaterial("UAB_COPPER","etfuturum:copper",11,new int[]{1,3,4,2},8,SoundEvents.ITEM_ARMOR_EQUIP_IRON,0);
    private static final net.minecraft.entity.passive.HorseArmorType HORSE=EnumHelper.addHorseArmor("UAB_COPPER","etfuturum:textures/entity/horse/armor/horse_armor_copper.png",4);
    public static boolean repair(ItemStack ingredient){for(int ore:net.minecraftforge.oredict.OreDictionary.getOreIDs(ingredient))if(net.minecraftforge.oredict.OreDictionary.getOreName(ore).equals("ingotCopper"))return true;return false;}
    private CopperAge(){}
    private static void block(String name,Block block){Content.add(name,"content.copper_age",block);Content.requires(name,"blocksitems.enableCopper");}
    private static void item(String name,Item item){Content.add(name,"content.copper_age",item);Content.requires(name,"blocksitems.enableCopper");}
    static void define(){
        TOOLS.setRepairItem(new ItemStack(Content.item("copper_ingot")));ARMOR.setRepairItem(new ItemStack(Content.item("copper_ingot")));
        item("copper_nugget",new Item());item("copper_sword",new ItemSword(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});item("copper_pickaxe",new ItemPickaxe(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});item("copper_axe",new ItemAxe(TOOLS,7,-3.2F){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});item("copper_shovel",new ItemSpade(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});item("copper_hoe",new ItemHoe(TOOLS){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        for(EntityEquipmentSlot slot:EntityEquipmentSlot.values())if(slot.getSlotType()==EntityEquipmentSlot.Type.ARMOR)item("copper_"+(slot==EntityEquipmentSlot.HEAD?"helmet":slot==EntityEquipmentSlot.CHEST?"chestplate":slot==EntityEquipmentSlot.LEGS?"leggings":"boots"),new ItemArmor(ARMOR,0,slot){@Override public boolean getIsRepairable(ItemStack stack,ItemStack ingredient){return repair(ingredient)||super.getIsRepairable(stack,ingredient);}});
        item("copper_horse_armor",new Item(){@Override public net.minecraft.entity.passive.HorseArmorType getHorseArmorType(ItemStack stack){return HORSE;}@Override public String getHorseArmorTexture(net.minecraft.entity.EntityLiving horse,ItemStack stack){return "etfuturum:textures/entity/horse/armor/horse_armor_copper.png";}}.setMaxStackSize(1));
        block("copper_torch",new BlockTorch(){}.setLightLevel(1));
        for(String family:new String[]{"copper_lantern","copper_chain","copper_bars","lightning_rod"})for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true}){
            if(family.equals("lightning_rod")&&stage==0&&!wax)continue;String name=name(family,stage,wax);Block block=family.equals("copper_lantern")?new Lantern():family.equals("copper_chain")?new Chain():family.equals("copper_bars")?new Bars():new Rod();block.setTickRandomly(!wax);block(name,block);if(family.equals("lightning_rod"))Content.requires(name,"blocksitems.enableCopper","experiments.enableLightningRod");
        }
        for(int stage=0;stage<4;stage++)for(boolean wax:new boolean[]{false,true})if(stage!=0||wax){String name=name("copper_chest",stage,wax);Content.add(name,"content.copper_chests",new CopperChestBlock(stage,wax));Content.requires(name,"blocksitems.enableCopper");}
        for(String wood:HangingSigns.WOODS){String name=wood+"_shelf";Content.add(name,"content.copper_age",new BookStorage.Shelf(wood));String prerequisite=Content.FEATURE.get(wood+"_planks");if(prerequisite!=null)Content.requires(name,prerequisite);}
    }
    static boolean fixture(Block block){return block instanceof Lantern||block instanceof Chain||block instanceof Bars||block instanceof LightningRod&&Features.enabled("content.copper_age");}
    static String name(String family,int stage,boolean wax){return (wax?"waxed_":"")+new String[]{"","exposed_","weathered_","oxidized_"}[stage]+family;}
    static void recipes(RegistryEvent.Register<IRecipe> event){
        Crafting.shaped(event,"copper_nuggets",new ItemStack(Content.item("copper_nugget"),9),"C",'C',Content.item("copper_ingot"));Crafting.shaped(event,"copper_from_nuggets",Content.stack("copper_ingot"),"NNN","NNN","NNN",'N',Content.item("copper_nugget"));
        String[] kinds={"sword","pickaxe","axe","shovel","hoe","helmet","chestplate","leggings","boots"};String[][] patterns={{"C","C","S"},{"CCC"," S "," S "},{"CC","CS"," S"},{"C","S","S"},{"CC"," S"," S"},{"CCC","C C"},{"C C","CCC","CCC"},{"CCC","C C","C C"},{"C C","C C"}};
        for(int i=0;i<kinds.length;i++){List<Object> input=new ArrayList<>(Arrays.asList((Object[])patterns[i]));input.add('C');input.add("ingotCopper");if(i<5){input.add('S');input.add(Items.STICK);}Crafting.shaped(event,"copper_"+kinds[i],Content.stack("copper_"+kinds[i]),input.toArray());if(Content.available("copper_"+kinds[i]))GameRegistry.addSmelting(Content.item("copper_"+kinds[i]),Content.stack("copper_nugget"),.1F);}
        Crafting.shaped(event,"copper_horse_armor",Content.stack("copper_horse_armor"),"C C","CCC","C C",'C',"ingotCopper");
        Crafting.shaped(event,"copper_torch",new ItemStack(Content.item("copper_torch"),4),"N","C","S",'N',Content.item("copper_nugget"),'C',new ItemStack(Items.COAL,1,32767),'S',Items.STICK);
        Crafting.shaped(event,"copper_chain",Content.stack("copper_chain"),"N","C","N",'N',Content.item("copper_nugget"),'C',"ingotCopper");Crafting.shaped(event,"copper_bars",new ItemStack(Content.item("copper_bars"),16),"CCC","CCC",'C',"ingotCopper");Crafting.shaped(event,"copper_lantern",Content.stack("copper_lantern"),"NNN","NTN","NNN",'N',Content.item("copper_nugget"),'T',Content.item("copper_torch"));
        for(String family:new String[]{"copper_lantern","copper_chain","copper_bars","lightning_rod","copper_chest"})for(int stage=0;stage<4;stage++){String plain=name(family,stage,false),waxed=name(family,stage,true);ModernRecipes.shapeless(event,waxed,Content.stack(waxed),Content.item(plain),Content.item("honeycomb"));}
        for(String wood:HangingSigns.WOODS){String log="stripped_"+wood+(wood.equals("crimson")||wood.equals("warped")?"_stem":wood.equals("bamboo")?"_block":"_log");Crafting.shaped(event,wood+"_shelf",new ItemStack(Content.item(wood+"_shelf"),6),"LLL","   ","LLL",'L',Content.block(log));}
    }
    private static final class Lantern extends LanternBlock {
        Lantern(){setLightLevel(1);}@Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}@Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){return CopperAging.use(world,pos,state,player,hand,face);}
    }
    private static final class Chain extends ChainBlock {
        @Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}@Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){return CopperAging.use(world,pos,state,player,hand,face);}
    }
    private static final class Bars extends BlockPane {
        Bars(){super(Material.IRON,true);setHardness(3);setResistance(6);setHarvestLevel("pickaxe",0);}@Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){CopperAging.tick(world,pos,state,random);}@Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){return CopperAging.use(world,pos,state,player,hand,face);}
    }
    private static final class Rod extends LightningRod {}
}
