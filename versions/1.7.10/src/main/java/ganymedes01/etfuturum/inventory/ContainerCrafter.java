package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.tileentities.TileEntityCrafter;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class ContainerCrafter extends Container {
    public final TileEntityCrafter tile;private int last=-1;
    public ContainerCrafter(InventoryPlayer inventory,TileEntityCrafter tile){this.tile=tile;
        for(int row=0;row<3;row++)for(int column=0;column<3;column++)addSlotToContainer(new Slot(tile,column+row*3,62+column*18,17+row*18){@Override public boolean isItemValid(ItemStack stack){return tile.isItemValidForSlot(getSlotIndex(),stack);}});
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlotToContainer(new Slot(inventory,column+row*9+9,8+column*18,84+row*18));
        for(int column=0;column<9;column++)addSlotToContainer(new Slot(inventory,column,8+column*18,142));
    }
    @Override public boolean canInteractWith(EntityPlayer player){return tile.isUseableByPlayer(player);}
    @Override public boolean enchantItem(EntityPlayer player,int button){if(!canInteractWith(player)||button<0||button>=9||!player.capabilities.allowEdit||player.inventory.getItemStack()!=null||tile.getStackInSlot(button)!=null)return false;return player.worldObj.isRemote||tile.toggle(button);}
    @Override public void addCraftingToCrafters(ICrafting listener){super.addCraftingToCrafters(listener);listener.sendProgressBarUpdate(this,0,tile.disabled);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(last!=tile.disabled){for(Object listener:crafters)((ICrafting)listener).sendProgressBarUpdate(this,0,tile.disabled);last=tile.disabled;}}
    @Override public void updateProgressBar(int id,int value){if(id==0)tile.disabled=value&511;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){if(index<0||index>=inventorySlots.size())return null;Slot slot=(Slot)inventorySlots.get(index);if(!slot.getHasStack())return null;ItemStack stack=slot.getStack(),copy=stack.copy();if(!mergeItemStack(stack,index<9?9:0,index<9?45:9,index<9))return null;if(stack.stackSize<=0)slot.putStack(null);else slot.onSlotChanged();slot.onPickupFromSlot(player,stack);return copy;}
}
