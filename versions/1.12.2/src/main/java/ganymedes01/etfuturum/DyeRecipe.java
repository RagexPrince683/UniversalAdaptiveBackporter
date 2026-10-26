package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

/** Native dye recipes receive a translated matrix; the player's real matrix is never changed. */
final class DyeRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    DyeRecipe(){setRegistryName(UAB.ID,"modern_dyes");}
    private InventoryCrafting translate(InventoryCrafting inventory){boolean found=false;InventoryCrafting copy=new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer player){return false;}},inventory.getWidth(),inventory.getHeight());for(int i=0;i<inventory.getSizeInventory();i++){ItemStack stack=inventory.getStackInSlot(i);if(stack.getItem() instanceof ModernDye&&Content.available(stack.getItem().getRegistryName().getPath())){stack=((ModernDye)stack.getItem()).nativeStack(stack);found=true;}copy.setInventorySlotContents(i,stack.copy());}return found?copy:null;}
    @Override public boolean matches(InventoryCrafting inventory,World world){InventoryCrafting translated=translate(inventory);return translated!=null&&recipe(translated)!=null;}
    private IRecipe recipe(InventoryCrafting translated){for(IRecipe recipe:CraftingManager.REGISTRY)if(recipe.getRegistryName()!=null&&recipe.getRegistryName().getNamespace().equals("minecraft")&&recipe.matches(translated,null))return recipe;return null;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory){InventoryCrafting translated=translate(inventory);if(translated==null)return ItemStack.EMPTY;IRecipe recipe=recipe(translated);return recipe==null?ItemStack.EMPTY:recipe.getCraftingResult(translated);}
    @Override public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory){InventoryCrafting translated=translate(inventory);IRecipe recipe=translated==null?null:recipe(translated);return recipe==null?NonNullList.withSize(inventory.getSizeInventory(),ItemStack.EMPTY):recipe.getRemainingItems(translated);}
    @Override public ItemStack getRecipeOutput(){return ItemStack.EMPTY;}
    @Override public boolean canFit(int width,int height){return true;}
    @Override public boolean isDynamic(){return true;}
}
