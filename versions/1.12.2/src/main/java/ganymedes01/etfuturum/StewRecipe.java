package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
public final class StewRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    StewRecipe(){setRegistryName(UAB.ID,"suspicious_stew");}
    public static ItemStack flower(ItemStack stack){Block block=Block.getBlockFromItem(stack.getItem());if(block==Blocks.YELLOW_FLOWER)return SuspiciousStew.effect(MobEffects.SATURATION,7);if(block==Blocks.RED_FLOWER){switch(stack.getMetadata()){case 0:return SuspiciousStew.effect(MobEffects.NIGHT_VISION,100);case 1:return SuspiciousStew.effect(MobEffects.SATURATION,7);case 2:return SuspiciousStew.effect(MobEffects.FIRE_RESISTANCE,80);case 3:return SuspiciousStew.effect(MobEffects.BLINDNESS,160);case 8:return SuspiciousStew.effect(MobEffects.REGENERATION,160);default:return SuspiciousStew.effect(MobEffects.WEAKNESS,180);}}if(block instanceof ModernFlower){String name=block.getRegistryName().getPath();return SuspiciousStew.effect(name.equals("cornflower")?MobEffects.JUMP_BOOST:name.equals("wither_rose")?MobEffects.WITHER:MobEffects.POISON,name.equals("cornflower")?120:name.equals("wither_rose")?160:240);}return ItemStack.EMPTY;}
    @Override public boolean matches(InventoryCrafting inventory,World world){boolean red=false,brown=false,bowl=false,flower=false;for(int i=0;i<inventory.getSizeInventory();i++){ItemStack stack=inventory.getStackInSlot(i);if(stack.isEmpty())continue;if(stack.getItem()==Items.BOWL&&!bowl)bowl=true;else if(stack.getItem()==Item.getItemFromBlock(Blocks.RED_MUSHROOM)&&!red)red=true;else if(stack.getItem()==Item.getItemFromBlock(Blocks.BROWN_MUSHROOM)&&!brown)brown=true;else if(!flower&&!flower(stack).isEmpty())flower=true;else return false;}return red&&brown&&bowl&&flower;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory){for(int i=0;i<inventory.getSizeInventory();i++){ItemStack result=flower(inventory.getStackInSlot(i));if(!result.isEmpty())return result;}return ItemStack.EMPTY;}
    @Override public ItemStack getRecipeOutput(){return ItemStack.EMPTY;}
    @Override public boolean canFit(int width,int height){return width*height>=4;}
    @Override public boolean isDynamic(){return true;}
}
