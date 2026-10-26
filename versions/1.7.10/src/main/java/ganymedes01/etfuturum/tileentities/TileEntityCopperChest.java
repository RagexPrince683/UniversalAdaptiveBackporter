package ganymedes01.etfuturum.tileentities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
public class TileEntityCopperChest extends TileEntity implements IInventory {
	private final InventoryBasic inventory = new InventoryBasic("Copper Chest", false, 27);
	public float lidAngle, prevLidAngle;
	private int users;
	@Override public void validate() { super.validate(); if (worldObj != null) ganymedes01.etfuturum.world.MobChestIndex.add(worldObj, xCoord, yCoord, zCoord); }
	@Override public void invalidate() { if (worldObj != null) ganymedes01.etfuturum.world.MobChestIndex.remove(worldObj, xCoord, yCoord, zCoord); super.invalidate(); }
	@Override public void updateEntity() {
		prevLidAngle = lidAngle;
		if (!worldObj.isRemote && users > 0 && lidAngle == 0) sound("open");
		lidAngle = Math.max(0, Math.min(1, lidAngle + (users > 0 ? .1F : -.1F)));
		if (!worldObj.isRemote && users == 0 && prevLidAngle >= .5F && lidAngle < .5F) sound("close");
	}
	private void sound(String event) { worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.copper_chest." + event, .5F, worldObj.rand.nextFloat() * .1F + .9F); }
	@Override public boolean receiveClientEvent(int id, int value) { if (id == 1) { users = value; return true; } return super.receiveClientEvent(id, value); }
	@Override public int getSizeInventory() { return 27; }
	@Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
	@Override public ItemStack decrStackSize(int slot, int count) { ItemStack stack = inventory.decrStackSize(slot, count); markDirty(); return stack; }
	@Override public ItemStack getStackInSlotOnClosing(int slot) { return inventory.getStackInSlotOnClosing(slot); }
	@Override public void setInventorySlotContents(int slot, ItemStack stack) { inventory.setInventorySlotContents(slot, stack); markDirty(); }
	@Override public String getInventoryName() { return "Copper Chest"; }
	@Override public boolean hasCustomInventoryName() { return true; }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64; }
	@Override public void openInventory() { if (!worldObj.isRemote) { users++; worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, users); } }
	@Override public void closeInventory() { if (!worldObj.isRemote) { users = Math.max(0, users - 1); worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, users); } }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot >= 0 && slot < 27; }
	@Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); NBTTagList list = new NBTTagList(); for (int i = 0; i < 27; i++) if (inventory.getStackInSlot(i) != null) { NBTTagCompound entry = inventory.getStackInSlot(i).writeToNBT(new NBTTagCompound()); entry.setByte("Slot", (byte) i); list.appendTag(entry); } tag.setTag("Items", list); }
	@Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); NBTTagList list = tag.getTagList("Items", 10); for (int i = 0; i < Math.min(27, list.tagCount()); i++) { NBTTagCompound entry = list.getCompoundTagAt(i); int slot = entry.getByte("Slot") & 255; if (slot < 27) inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(entry)); } }
}
