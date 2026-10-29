package ganymedes01.etfuturum;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class BundleDyeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private static int color(ItemStack stack){
        if(stack.getItem()==Items.DYE)return stack.getMetadata()>=0&&stack.getMetadata()<16?15-stack.getMetadata():-1;
        for(int id:OreDictionary.getOreIDs(stack))for(int i=0;i<BundleItem.COLORS.length;i++){StringBuilder name=new StringBuilder("dye");for(String part:BundleItem.COLORS[i].split("_"))name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));if(OreDictionary.getOreName(id).equals(name.toString()))return i;}return -1;
    }
    @Override public boolean matches(InventoryCrafting grid,World world){if(!Content.available("bundle"))return false;boolean bundle=false,dye=false;for(int i=0;i<grid.getSizeInventory();i++){ItemStack stack=grid.getStackInSlot(i);if(stack.isEmpty())continue;if(BundleItem.bundle(stack)&&!bundle)bundle=true;else if(color(stack)>=0&&!dye)dye=true;else return false;}return bundle&&dye;}
    @Override public ItemStack getCraftingResult(InventoryCrafting grid){ItemStack bundle=ItemStack.EMPTY;int color=-1;for(int i=0;i<grid.getSizeInventory();i++){ItemStack stack=grid.getStackInSlot(i);if(BundleItem.bundle(stack))bundle=stack;else if(!stack.isEmpty())color=color(stack);}if(bundle.isEmpty()||color<0)return ItemStack.EMPTY;NBTTagCompound tag=bundle.writeToNBT(new NBTTagCompound()).copy();tag.setString("id",UAB.ID+":"+BundleItem.COLORS[color]+"_bundle");tag.setByte("Count",(byte)1);tag.setShort("Damage",(short)0);return new ItemStack(tag);}
    @Override public ItemStack getRecipeOutput(){return Content.stack("bundle");}
    @Override public boolean canFit(int width,int height){return width*height>=2;}
    @Override public boolean isDynamic(){return true;}
    @Override public NonNullList<ItemStack> getRemainingItems(InventoryCrafting grid){return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(grid);}
}
