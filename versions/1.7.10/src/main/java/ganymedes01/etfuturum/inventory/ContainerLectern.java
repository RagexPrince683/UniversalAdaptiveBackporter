package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.tileentities.TileEntityLectern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class ContainerLectern extends Container {
    public final TileEntityLectern tile;private int lastPage=-1;
    public ContainerLectern(TileEntityLectern tile){this.tile=tile;addSlotToContainer(new Slot(tile,0,-1000,-1000){@Override public boolean isItemValid(ItemStack stack){return false;}@Override public boolean canTakeStack(EntityPlayer player){return false;}});}
    @Override public boolean canInteractWith(EntityPlayer player){return tile.isUseableByPlayer(player)&&tile.getStackInSlot(0)!=null;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int slot){return null;}
    @Override public void addCraftingToCrafters(ICrafting listener){super.addCraftingToCrafters(listener);listener.sendProgressBarUpdate(this,0,tile.page);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(lastPage!=tile.page){for(Object listener:crafters)((ICrafting)listener).sendProgressBarUpdate(this,0,tile.page);lastPage=tile.page;}}
    @Override public void updateProgressBar(int id,int value){if(id==0)tile.page=Math.max(0,Math.min(tile.pages()-1,value));}
    @Override public boolean enchantItem(EntityPlayer player,int button){
        if(!canInteractWith(player)||button<0||button>2)return false;if(player.worldObj.isRemote)return true;
        if(button<2)return tile.turn(tile.page+(button==0?-1:1));
        if(!player.capabilities.allowEdit)return false;ItemStack copy=tile.getStackInSlot(0).copy();
        if(!player.inventory.addItemStackToInventory(copy))return false;tile.getStackInSlotOnClosing(0);player.inventory.markDirty();detectAndSendChanges();return true;
    }
}
