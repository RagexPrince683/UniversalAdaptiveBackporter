package ganymedes01.etfuturum;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;

/** One server inventory, using the maintained wide layout rather than a second storage array. */
public final class StorageContainer extends Container {
 public final IInventory storage; public final StorageTier tier;
 public StorageContainer(InventoryPlayer inventory,IInventory storage,EntityPlayer player){
  this.storage=storage;this.tier=StorageTier.of(storage);storage.openInventory(player);
  int columns=tier.columns,rows=storage.getSizeInventory()/columns;
  for(int row=0;row<rows;row++)for(int col=0;col<columns;col++)addSlotToContainer(storage instanceof TileEntityShulkerBox?new SlotShulkerBox(storage,col+row*columns,12+col*18,8+row*18):new Slot(storage,col+row*columns,12+col*18,8+row*18));
  int left=12+(columns-9)*9,top=12+rows*18;
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(inventory,col+row*9+9,left+col*18,top+row*18));
  for(int col=0;col<9;col++)addSlotToContainer(new Slot(inventory,col,left+col*18,top+58));
 }
 @Override public boolean canInteractWith(EntityPlayer player){return storage.isUsableByPlayer(player);}
 @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
  if(index<0||index>=inventorySlots.size())return ItemStack.EMPTY;Slot slot=inventorySlots.get(index);if(!slot.getHasStack())return ItemStack.EMPTY;
  ItemStack held=slot.getStack(),copy=held.copy();int size=storage.getSizeInventory();
  if(!mergeItemStack(held,index<size?size:0,index<size?inventorySlots.size():size,index<size))return ItemStack.EMPTY;
  if(held.isEmpty())slot.putStack(ItemStack.EMPTY);else slot.onSlotChanged();if(held.getCount()==copy.getCount())return ItemStack.EMPTY;slot.onTake(player,held);return copy;
 }
 @Override public void onContainerClosed(EntityPlayer player){super.onContainerClosed(player);storage.closeInventory(player);}
}
