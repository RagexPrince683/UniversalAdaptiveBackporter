package ganymedes01.etfuturum;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
final class PotRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private static final int[] POSITIONS={1,3,5,7};
    PotRecipe(){setRegistryName(UAB.ID,"decorated_pot");}
    @Override public boolean matches(InventoryCrafting inventory,World world){if(inventory.getWidth()!=3||inventory.getHeight()!=3)return false;for(int i=0;i<9;i++){ItemStack stack=inventory.getStackInSlot(i);boolean required=i==1||i==3||i==5||i==7;if(!required){if(!stack.isEmpty())return false;}else if(stack.isEmpty()||stack.getItem()!=Items.BRICK&&(!stack.getItem().getRegistryName().getNamespace().equals(UAB.ID)||!stack.getItem().getRegistryName().getPath().endsWith("_pottery_sherd")))return false;}return true;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory){ItemStack pot=Content.stack("decorated_pot");NBTTagCompound tag=new NBTTagCompound(),tile=new NBTTagCompound();NBTTagList designs=new NBTTagList();for(int position:POSITIONS)designs.appendTag(new NBTTagString(inventory.getStackInSlot(position).getItem().getRegistryName().toString()));tile.setTag("sherds",designs);tag.setTag("BlockEntityTag",tile);pot.setTagCompound(tag);return pot;}
    @Override public ItemStack getRecipeOutput(){return Content.stack("decorated_pot");}
    @Override public boolean canFit(int width,int height){return width>=3&&height>=3;}
    @Override public boolean isDynamic(){return true;}
}
