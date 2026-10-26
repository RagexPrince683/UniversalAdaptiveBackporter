package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.blocks.BlockBrushable;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.*;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.*;

/** The server owns loot, stroke cooldown, decay and conversion. Client packets contain only revealed loot. */
public class TileEntityBrushable extends TileEntity {
	public int progress, face = 1;
	private int idle, cooldown;
	private String lootTable = "";
	private long seed;
	private ItemStack loot;
	public void configure(String table, long seed) { this.lootTable = table; this.seed = seed; loot = null; progress = 0; markDirty(); }
	public ItemStack revealedItem() { return progress == 0 ? null : loot; }
	public boolean brush(int side) {
		if (worldObj == null || worldObj.isRemote || cooldown > 0 || !(getBlockType() instanceof BlockBrushable)) return false;
		if (progress == 0 && loot == null && !lootTable.isEmpty()) {
			List<ItemStack> items = UndergroundLoot.roll(lootTable, new Random(seed));
			loot = items.isEmpty() ? null : items.get(0); lootTable = "";
		}
		face = side; idle = 0; cooldown = 10; progress++;
		worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, "dig.sand", .7F, 1);
		if (progress < 10) { sync(); return false; }
		if (loot != null) {
			ForgeDirection direction = ForgeDirection.getOrientation(face);
			EntityItem item = new EntityItem(worldObj, xCoord + .5 + direction.offsetX * .7, yCoord + .5 + direction.offsetY * .7, zCoord + .5 + direction.offsetZ * .7, loot.copy());
			item.motionX = item.motionY = item.motionZ = 0; worldObj.spawnEntityInWorld(item);
		}
		loot = null; worldObj.setBlock(xCoord, yCoord, zCoord, ((BlockBrushable) getBlockType()).base, 0, 3); return true;
	}
	@Override public void updateEntity() {
		if (worldObj.isRemote) return;
		if (cooldown > 0) cooldown--;
		if (progress > 0 && ++idle >= 40 && idle % 4 == 0) { progress = Math.max(0, progress - 2); sync(); }
	}
	private void sync() { markDirty(); worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }
	@Override public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); tag.setInteger("BrushProgress", progress); tag.setInteger("BrushFace", face); tag.setInteger("BrushIdle", idle); tag.setString("LootTable", lootTable); tag.setLong("LootTableSeed", seed);
		if (loot != null) tag.setTag("Item", loot.writeToNBT(new NBTTagCompound()));
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); progress = Math.max(0, Math.min(9, tag.getInteger("BrushProgress"))); face = Math.max(0, Math.min(5, tag.getInteger("BrushFace"))); idle = tag.getInteger("BrushIdle");
		lootTable = tag.getString("LootTable"); seed = tag.getLong("LootTableSeed"); loot = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Item"));
	}
	@Override public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("BrushProgress", progress); tag.setInteger("BrushFace", face);
		if (revealedItem() != null) tag.setTag("Item", loot.writeToNBT(new NBTTagCompound()));
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}
	@Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) { NBTTagCompound tag = packet.func_148857_g(); progress = tag.getInteger("BrushProgress"); face = tag.getInteger("BrushFace"); loot = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Item")); worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord); }
}
