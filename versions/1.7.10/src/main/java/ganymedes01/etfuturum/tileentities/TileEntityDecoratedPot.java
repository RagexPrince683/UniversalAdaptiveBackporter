package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.items.ItemPotterySherd;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import java.util.*;

public class TileEntityDecoratedPot extends TileEntity implements IInventory {
	/** Back, left, right, front, relative to the placed pot. Empty names mean brick. */
	public final String[] designs = {"", "", "", ""};
	private ItemStack contents;
	private final List<ItemStack> legacyContents = new ArrayList<>();
	@Override public boolean canUpdate() { return false; }
	public void setDesigns(ItemStack stack) {
		if (stack.hasTagCompound()) readDesigns(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
		markDirty(); if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	public void readDesigns(NBTTagCompound tag) {
		NBTTagList list = tag.getTagList("sherds", 8);
		for (int i = 0; i < 4; i++) {
			String id = i < list.tagCount() ? list.getStringTagAt(i) : "";
			Item item = cpw.mods.fml.common.registry.GameRegistry.findItem("etfuturum", id.replace("minecraft:", "").replace("etfuturum:", ""));
			designs[i] = item instanceof ItemPotterySherd ? ((ItemPotterySherd) item).pattern : "";
		}
	}
	public void writeDesigns(NBTTagCompound tag) {
		NBTTagList list = new NBTTagList();
		for (String design : designs) list.appendTag(new NBTTagString(design.isEmpty() ? "minecraft:brick" : "etfuturum:" + design + "_pottery_sherd"));
		tag.setTag("sherds", list);
	}
	public ItemStack potItem() {
		ItemStack stack = ganymedes01.etfuturum.ModBlocks.DECORATED_POT.newItemStack();
		NBTTagCompound tag = new NBTTagCompound(), block = new NBTTagCompound(); writeDesigns(block); tag.setTag("BlockEntityTag", block); stack.setTagCompound(tag); return stack;
	}
	public List<ItemStack> allContents() { List<ItemStack> list = new ArrayList<>(legacyContents); if (contents != null) list.add(contents.copy()); return list; }
	@Override public int getSizeInventory() { return 1; }
	@Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? contents : null; }
	@Override public ItemStack decrStackSize(int slot, int count) { if (slot != 0 || count <= 0 || contents == null) return null; ItemStack result = contents.splitStack(Math.min(count, contents.stackSize)); if (contents.stackSize <= 0) contents = null; markDirty(); return result; }
	@Override public ItemStack getStackInSlotOnClosing(int slot) { ItemStack stack = getStackInSlot(slot); if (slot == 0) contents = null; return stack; }
	@Override public void setInventorySlotContents(int slot, ItemStack stack) { if (slot != 0) return; contents = stack; if (contents != null) contents.stackSize = Math.min(contents.stackSize, Math.min(64, contents.getMaxStackSize())); markDirty(); }
	@Override public String getInventoryName() { return "tile.etfuturum.decorated_pot.name"; }
	@Override public boolean hasCustomInventoryName() { return false; }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public boolean isUseableByPlayer(EntityPlayer player) { return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64; }
	@Override public void openInventory() { }
	@Override public void closeInventory() { }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot == 0; }
	@Override public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); writeDesigns(tag); NBTTagList items = new NBTTagList();
		for (ItemStack stack : allContents()) items.appendTag(stack.writeToNBT(new NBTTagCompound())); tag.setTag("PotItems", items);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); readDesigns(tag); contents = null; legacyContents.clear();
		NBTTagList list = tag.getTagList(tag.hasKey("PotItems") ? "PotItems" : "Items", 10);
		for (int i = 0; i < list.tagCount(); i++) { ItemStack stack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)); if (stack != null) { if (contents == null) contents = stack; else legacyContents.add(stack); } }
	}
	@Override public Packet getDescriptionPacket() { NBTTagCompound tag = new NBTTagCompound(); writeDesigns(tag); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag); }
	@Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) { readDesigns(packet.func_148857_g()); worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }
}
