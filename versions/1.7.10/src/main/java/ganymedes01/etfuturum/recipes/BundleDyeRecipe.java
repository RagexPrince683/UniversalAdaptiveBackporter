package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.ModernAdditions;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.items.ItemBundle;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public final class BundleDyeRecipe implements IRecipe {
    private static int color(ItemStack stack){if(stack.getItem()==Items.dye)return stack.getItemDamage()>=0&&stack.getItemDamage()<16?15-stack.getItemDamage():-1;for(int id:OreDictionary.getOreIDs(stack))for(int i=0;i<ItemBundle.COLORS.length;i++){StringBuilder name=new StringBuilder("dye");for(String part:ItemBundle.COLORS[i].split("_"))name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));if(OreDictionary.getOreName(id).equals(name.toString()))return i;}return -1;}
    @Override public boolean matches(InventoryCrafting grid,World world){if(!ContentAvailability.enabled("bundles"))return false;boolean bundle=false,dye=false;for(int i=0;i<grid.getSizeInventory();i++){ItemStack stack=grid.getStackInSlot(i);if(stack==null||stack.stackSize<=0)continue;if(ItemBundle.bundle(stack)&&!bundle)bundle=true;else if(color(stack)>=0&&!dye)dye=true;else return false;}return bundle&&dye;}
    @Override public ItemStack getCraftingResult(InventoryCrafting grid){ItemStack bundle=null;int color=-1;for(int i=0;i<grid.getSizeInventory();i++){ItemStack stack=grid.getStackInSlot(i);if(ItemBundle.bundle(stack))bundle=stack;else if(stack!=null&&stack.stackSize>0)color=color(stack);}if(bundle==null||color<0)return null;NBTTagCompound tag=(NBTTagCompound)bundle.writeToNBT(new NBTTagCompound()).copy();tag.setShort("id",(short)Item.getIdFromItem(ModernAdditions.item(ItemBundle.COLORS[color]+"_bundle")));tag.setByte("Count",(byte)1);tag.setShort("Damage",(short)0);return ItemStack.loadItemStackFromNBT(tag);}
    @Override public int getRecipeSize(){return 2;}
    @Override public ItemStack getRecipeOutput(){return new ItemStack(ModernAdditions.item("bundle"));}
}
