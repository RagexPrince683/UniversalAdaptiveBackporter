package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.blocks.BlockCampfire;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

/** Four independent, fuel-free native food recipes; saved input and elapsed cooking are authoritative. */
public final class TileEntityCampfire extends TileEntity {
    private final ItemStack[] items=new ItemStack[4];private final int[] elapsed=new int[4];
    public ItemStack displayed(int slot){return items[slot];}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
    private static ItemStack result(ItemStack input){if(input==null)return null;ItemStack output=FurnaceRecipes.smelting().getSmeltingResult(input);return output!=null&&output.getItem() instanceof ItemFood&&ContentAvailability.available(output)?output:null;}
    public boolean insert(EntityPlayer player,ItemStack stack){if(result(stack)==null||!ContentAvailability.available(getBlockType()))return false;for(int i=0;i<4;i++)if(items[i]==null){if(!worldObj.isRemote){items[i]=stack.copy();items[i].stackSize=1;elapsed[i]=0;if(!player.capabilities.isCreativeMode&&--stack.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}return true;}return false;}
    @Override public void updateEntity(){if(worldObj.isRemote)return;boolean lit=BlockCampfire.lit(getBlockMetadata());for(int i=0;i<4;i++)if(items[i]!=null){if(lit){ItemStack output=result(items[i]);if(output!=null&&++elapsed[i]>=600&&worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.6,zCoord+.5,output.copy()))){items[i]=null;elapsed[i]=0;worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}}else elapsed[i]=Math.max(0,elapsed[i]-2);markDirty();}}
    public void dropContents(){if(worldObj.isRemote)return;for(int i=0;i<4;i++)if(items[i]!=null){worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,items[i]));items[i]=null;}}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList list=new NBTTagList();for(int i=0;i<4;i++)if(items[i]!=null){NBTTagCompound item=items[i].writeToNBT(new NBTTagCompound());item.setInteger("Slot",i);item.setInteger("CookingTime",elapsed[i]);list.appendTag(item);}tag.setTag("Items",list);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);java.util.Arrays.fill(items,null);java.util.Arrays.fill(elapsed,0);NBTTagList list=tag.getTagList("Items",10);for(int j=0;j<list.tagCount();j++){NBTTagCompound item=list.getCompoundTagAt(j);int slot=item.getInteger("Slot");if(slot>=0&&slot<4){items[slot]=ItemStack.loadItemStackFromNBT(item);if(items[slot]!=null)items[slot].stackSize=1;elapsed[slot]=Math.max(0,Math.min(600,item.getInteger("CookingTime")));}}}
}
