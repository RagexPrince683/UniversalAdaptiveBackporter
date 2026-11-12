package ganymedes01.etfuturum;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class CrafterContainer extends Container {
    public final CrafterTile tile;private int last=-1,previewMask=-1;
    private final InventoryCraftResult preview=new InventoryCraftResult();
    private final ItemStack[] previousInputs={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
    public CrafterContainer(InventoryPlayer inventory,CrafterTile tile){
        this.tile=tile;
        for(int row=0;row<3;row++)for(int column=0;column<3;column++)addSlotToContainer(new Slot(tile,column+row*3,26+column*18,17+row*18){@Override public boolean isItemValid(ItemStack stack){return tile.isItemValidForSlot(getSlotIndex(),stack);}});
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlotToContainer(new Slot(inventory,column+row*9+9,8+column*18,84+row*18));
        for(int column=0;column<9;column++)addSlotToContainer(new Slot(inventory,column,8+column*18,142));
        // Native slot synchronization carries a server-computed, non-extractable recipe preview.
        addSlotToContainer(new Slot(preview,0,134,35){@Override public boolean isItemValid(ItemStack stack){return false;}@Override public boolean canTakeStack(EntityPlayer player){return false;}});
    }
    @Override public boolean canInteractWith(EntityPlayer player){return tile.isUsableByPlayer(player);}
    @Override public boolean enchantItem(EntityPlayer player,int button){if(!canInteractWith(player)||button<0||button>=9||!player.capabilities.allowEdit||!player.inventory.getItemStack().isEmpty()||!tile.getStackInSlot(button).isEmpty())return false;return player.world.isRemote||tile.toggle(button);}
    private void refreshPreview(){
        if(tile.getWorld().isRemote)return;
        boolean changed=previewMask!=tile.disabled;
        for(int i=0;i<9;i++)changed|=!ItemStack.areItemStacksEqual(previousInputs[i],tile.getStackInSlot(i));
        if(changed){previewMask=tile.disabled;for(int i=0;i<9;i++)previousInputs[i]=tile.getStackInSlot(i).copy();preview.setInventorySlotContents(0,tile.preview());}
    }
    @Override public void addListener(IContainerListener listener){refreshPreview();super.addListener(listener);listener.sendWindowProperty(this,0,tile.disabled);}
    @Override public void detectAndSendChanges(){refreshPreview();super.detectAndSendChanges();if(last!=tile.disabled){for(IContainerListener listener:listeners)listener.sendWindowProperty(this,0,tile.disabled);last=tile.disabled;}}
    @Override public void updateProgressBar(int id,int value){if(id==0)tile.disabled=value&511;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){if(index<0||index>=45)return ItemStack.EMPTY;Slot slot=inventorySlots.get(index);if(!slot.getHasStack())return ItemStack.EMPTY;ItemStack stack=slot.getStack(),copy=stack.copy();if(!mergeItemStack(stack,index<9?9:0,index<9?45:9,index<9))return ItemStack.EMPTY;if(stack.isEmpty())slot.putStack(ItemStack.EMPTY);else slot.onSlotChanged();slot.onTake(player,stack);return copy;}
}
