package ganymedes01.etfuturum.tileentities;

import java.util.*;
import ganymedes01.etfuturum.blocks.BlockBookStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityBookStorage extends TileEntity implements IInventory {
    private final ItemStack[] items=new ItemStack[6];public int last=-1;
    @Override public boolean shouldRefresh(net.minecraft.block.Block previous,net.minecraft.block.Block next,int previousMeta,int nextMeta,net.minecraft.world.World world,int x,int y,int z){return previous!=next;}
    private boolean books(){return worldObj==null||!(getBlockType() instanceof BlockBookStorage)||((BlockBookStorage)getBlockType()).books;}
    @Override public int getSizeInventory(){return books()?6:3;}
    @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<getSizeInventory()?items[slot]:null;}
    @Override public ItemStack decrStackSize(int slot,int count){ItemStack stack=getStackInSlot(slot);if(stack==null)return null;ItemStack result=stack.splitStack(count);if(stack.stackSize==0)items[slot]=null;changed(slot);return result;}
    @Override public ItemStack getStackInSlotOnClosing(int slot){ItemStack result=getStackInSlot(slot);if(result!=null){items[slot]=null;changed(slot);}return result;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot<0||slot>=getSizeInventory()||stack!=null&&!isItemValidForSlot(slot,stack))return;items[slot]=stack;if(stack!=null)stack.stackSize=Math.min(getInventoryStackLimit(),stack.stackSize);changed(slot);}
    private void changed(int slot){last=slot;markDirty();if(worldObj!=null&&!worldObj.isRemote){worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());}}
    @Override public int getInventoryStackLimit(){return books()?1:64;}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){Item item=stack.getItem();return !books()||item==Items.book||item==Items.enchanted_book||item==Items.writable_book||item==Items.written_book;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return worldObj!=null&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&player.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;}
    @Override public String getInventoryName(){return books()?"container.chiseled_bookshelf":"container.shelf";}@Override public boolean hasCustomInventoryName(){return false;}@Override public void openInventory(){}@Override public void closeInventory(){}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);NBTTagList list=new NBTTagList();for(int i=0;i<6;i++)if(items[i]!=null){NBTTagCompound entry=items[i].writeToNBT(new NBTTagCompound());entry.setByte("Slot",(byte)i);list.appendTag(entry);}tag.setTag("Items",list);tag.setInteger("Last",last);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);Arrays.fill(items,null);NBTTagList list=tag.getTagList("Items",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound entry=list.getCompoundTagAt(i);int slot=entry.getByte("Slot");if(slot>=0&&slot<6)items[slot]=ItemStack.loadItemStackFromNBT(entry);}last=Math.max(-1,Math.min(5,tag.getInteger("Last")));}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());worldObj.markBlockRangeForRenderUpdate(xCoord,yCoord,zCoord,xCoord,yCoord,zCoord);}
}
