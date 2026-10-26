package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.world.structure.underground.UndergroundLoot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import java.util.*;

/** Native once-per-player vault with persisted reward queue and the released 128-player history bound. */
public class TileEntityVault extends TileEntity {
	private final LinkedHashSet<UUID> rewarded = new LinkedHashSet<>();
	private final ArrayDeque<ItemStack> pending = new ArrayDeque<>();
	public boolean unlock(EntityPlayer player) {
		if (worldObj.isRemote) return true;
		ItemStack held = player.getHeldItem(); boolean ominous = (getBlockMetadata() & 4) != 0;
		if (held == null || held.getItem() != (ominous ? ModItems.OMINOUS_TRIAL_KEY : ModItems.TRIAL_KEY).get() || rewarded.contains(player.getUniqueID()) || !pending.isEmpty()) return false;
		List<ItemStack> loot = UndergroundLoot.roll("minecraft:chests/trial_chambers/reward" + (ominous ? "_ominous" : ""), worldObj.rand);
		if (loot.isEmpty()) return false;
		pending.addAll(loot); rewarded.add(player.getUniqueID()); if (rewarded.size() > 128) rewarded.remove(rewarded.iterator().next());
		if (!player.capabilities.isCreativeMode && --held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		markDirty(); sound("insert_item"); return true;
	}
	@Override public void updateEntity() {
		if (worldObj.isRemote || worldObj.getTotalWorldTime() % 20 != 0) return;
		boolean eligible = false;
		for (Object value : worldObj.playerEntities) { EntityPlayer player = (EntityPlayer) value; if (!player.capabilities.disableDamage && !rewarded.contains(player.getUniqueID()) && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) < 16) { eligible = true; break; } }
		int meta = getBlockMetadata(), next = (meta & 7) | (eligible || !pending.isEmpty() ? 8 : 0);
		if (meta != next) worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, next, 2);
		if (!pending.isEmpty()) {
			EntityItem item = new EntityItem(worldObj, xCoord + .5, yCoord + 1.2, zCoord + .5, pending.peek().copy());
			if (worldObj.spawnEntityInWorld(item)) { pending.remove(); markDirty(); sound("eject_item"); }
		}
	}
	private void sound(String event) { worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, Tags.MC_ASSET_VER + ":block.vault." + event, 1, 1); }
	@Override public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); NBTTagList players = new NBTTagList(), queue = new NBTTagList();
		for (UUID id : rewarded) players.appendTag(new NBTTagString(id.toString())); for (ItemStack stack : pending) queue.appendTag(stack.writeToNBT(new NBTTagCompound())); tag.setTag("Rewarded", players); tag.setTag("Pending", queue);
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); rewarded.clear(); pending.clear(); NBTTagList players = tag.getTagList("Rewarded", 8), queue = tag.getTagList("Pending", 10);
		for (int i = 0; i < Math.min(128, players.tagCount()); i++) try { rewarded.add(UUID.fromString(players.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(64, queue.tagCount()); i++) { ItemStack stack = ItemStack.loadItemStackFromNBT(queue.getCompoundTagAt(i)); if (stack != null) pending.add(stack); }
	}
}
