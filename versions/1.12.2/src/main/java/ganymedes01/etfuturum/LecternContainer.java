package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

/** Hidden native slot synchronizes the entire book; native button packets request server-owned page turns. */
public final class LecternContainer extends Container {
    public final LecternTile tile;private int lastPage=-1;
    public LecternContainer(LecternTile tile){this.tile=tile;addSlotToContainer(new Slot(tile,0,-1000,-1000){@Override public boolean isItemValid(ItemStack stack){return false;}@Override public boolean canTakeStack(EntityPlayer player){return false;}});}
    @Override public boolean canInteractWith(EntityPlayer player){return tile.isUsableByPlayer(player)&&!tile.isEmpty();}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int slot){return ItemStack.EMPTY;}
    @Override public void addListener(IContainerListener listener){super.addListener(listener);listener.sendWindowProperty(this,0,tile.page);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(lastPage!=tile.page){for(IContainerListener listener:listeners)listener.sendWindowProperty(this,0,tile.page);lastPage=tile.page;}}
    @Override public void updateProgressBar(int id,int value){tile.setField(id,value);}
    @Override public boolean enchantItem(EntityPlayer player,int button){
        if(!canInteractWith(player)||button<0||button>2)return false;if(player.world.isRemote)return true;
        if(button<2)return tile.turn(tile.page+(button==0?-1:1));
        if(!player.capabilities.allowEdit)return false;ItemStack copy=tile.getStackInSlot(0).copy();
        if(!player.inventory.addItemStackToInventory(copy))return false;tile.removeStackFromSlot(0);player.inventory.markDirty();detectAndSendChanges();return true;
    }
}
