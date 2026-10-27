package ganymedes01.etfuturum;
import java.util.*;
import java.util.function.Supplier;
import ganymedes01.etfuturum.items.ItemNewBoat;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.dispenser.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
public final class BoatContent {
 public static final String[] WOODS={"oak","spruce","birch","jungle","acacia","dark_oak","mangrove","cherry","bamboo","pale_oak"};
 private BoatContent(){}
 static void define(){for(String wood:WOODS){final BlockPlanks.EnumType old=legacy(wood);Supplier<Item> plank=old!=null?()->Item.getItemFromBlock(Blocks.PLANKS):()->Content.item(wood+"_planks");for(boolean chest:new boolean[]{false,true}){String id=id(wood,chest);ItemNewBoat item=Content.add(id,"blocksitems.enableNewBoats",new ItemNewBoat("minecraft",wood,plank,old==null?0:old.getMetadata(),chest,wood.equals("bamboo")));if(old==null)Content.requires(id,Content.FEATURE.get(wood+"_planks"));if(old!=null&&!chest)item.setCreativeTab(null);}}}
 public static String id(String wood,boolean chest){return (wood.equals("mangrove")&&!chest?"mangrove_oak":wood)+(chest?"_chest":"")+(wood.equals("bamboo")?"_raft":"_boat");}
 static BlockPlanks.EnumType legacy(String wood){for(BlockPlanks.EnumType type:BlockPlanks.EnumType.values())if(type.getName().equals(wood))return type;return null;}
 static ItemStack plank(String wood){BlockPlanks.EnumType old=legacy(wood);return old==null?Content.stack(wood+"_planks"):new ItemStack(Blocks.PLANKS,1,old.getMetadata());}
 static ItemStack nativeBoat(String wood){String name=wood.equals("oak")?"boat":wood+"_boat";ResourceLocation id=new ResourceLocation("minecraft",name);return Item.REGISTRY.containsKey(id)?new ItemStack(Item.REGISTRY.getObject(id)):Content.stack(id(wood,false));}
 static void initialize(){for(String wood:WOODS)for(boolean chest:new boolean[]{false,true}){Item item=Content.item(id(wood,chest));if(!Content.available(id(wood,chest)))continue;BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item,new BehaviorDefaultDispenseItem(){@Override protected ItemStack dispenseStack(IBlockSource source,ItemStack stack){net.minecraft.util.EnumFacing facing=source.getBlockState().getValue(BlockDispenser.FACING);BlockPos front=source.getBlockPos().offset(facing);boolean water=source.getWorld().getBlockState(front).getMaterial()==net.minecraft.block.material.Material.WATER;double y=water?1:source.getWorld().isAirBlock(front)&&source.getWorld().getBlockState(front.down()).getMaterial()==net.minecraft.block.material.Material.WATER?0:-1;if(y<0)return super.dispenseStack(source,stack);ganymedes01.etfuturum.entities.EntityNewBoat boat=chest?new ganymedes01.etfuturum.entities.EntityNewBoatWithChest(source.getWorld()):new ganymedes01.etfuturum.entities.EntityNewBoat(source.getWorld());boat.setBoatType("minecraft",wood);boat.setLocationAndAngles(source.getX()+facing.getXOffset()*1.125,source.getY()+facing.getYOffset()*1.125+y,source.getZ()+facing.getZOffset()*1.125,facing.getHorizontalAngle(),0);if(stack.hasDisplayName())boat.setCustomNameTag(stack.getDisplayName());if(source.getWorld().spawnEntity(boat))stack.shrink(1);return stack;}});}}
}
