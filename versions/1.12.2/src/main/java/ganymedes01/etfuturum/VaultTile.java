package ganymedes01.etfuturum;

import net.minecraft.world.WorldServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import java.util.*;

/** Native once-per-player vault with persisted reward queue and the released 128-player history bound. */
public class VaultTile extends TileEntity implements ITickable {
	private final LinkedHashSet<UUID> rewarded = new LinkedHashSet<>();
	private final ArrayDeque<ItemStack> pending = new ArrayDeque<>();
	public boolean unlock(EntityPlayer player,EnumHand hand) {
		if (world.isRemote) return true;
		ItemStack held = player.getHeldItem(hand); boolean ominous = (getBlockMetadata() & 4) != 0;
		if (held.isEmpty() || held.getItem() != Content.item(ominous?"ominous_trial_key":"trial_key") || rewarded.contains(player.getUniqueID()) || !pending.isEmpty()) return false;
		List<ItemStack> loot = ModernLoot.roll("chests/trial_chambers/reward" + (ominous ? "_ominous" : ""), world.rand,(WorldServer)world);
		if (loot.isEmpty()) return false;
		pending.addAll(loot); rewarded.add(player.getUniqueID()); if (rewarded.size() > 128) rewarded.remove(rewarded.iterator().next());
		if(!player.capabilities.isCreativeMode)held.shrink(1);
		markDirty(); sound("insert_item"); return true;
	}
	@Override public void update() {
		if (world.isRemote || world.getTotalWorldTime() % 20 != 0) return;
		boolean eligible = false;
		for (Object value : world.playerEntities) { EntityPlayer player = (EntityPlayer) value; if (!player.capabilities.disableDamage && !rewarded.contains(player.getUniqueID()) && player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) < 16) { eligible = true; break; } }
		int meta = getBlockMetadata(), next = (meta & 7) | (eligible || !pending.isEmpty() ? 8 : 0);
		if (meta != next) world.setBlockState(pos,world.getBlockState(pos).getBlock().getStateFromMeta(next),2);
		if (!pending.isEmpty()) {
			EntityItem item = new EntityItem(world, pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, pending.peek().copy());
			if (world.spawnEntity(item)) { pending.remove(); markDirty(); sound("eject_item"); }
		}
	}
	private void sound(String event){world.playSound(null,pos,ModernSounds.get("block.vault."+event),SoundCategory.BLOCKS,1,1);}
    @Override public boolean shouldRefresh(net.minecraft.world.World world,BlockPos pos,net.minecraft.block.state.IBlockState before,net.minecraft.block.state.IBlockState after){return before.getBlock()!=after.getBlock();}
	@Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag); NBTTagList players = new NBTTagList(), queue = new NBTTagList();
		for (UUID id : rewarded) players.appendTag(new NBTTagString(id.toString())); for (ItemStack stack : pending) queue.appendTag(stack.writeToNBT(new NBTTagCompound())); tag.setTag("Rewarded", players); tag.setTag("Pending", queue);
        return tag;
	}
	@Override public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag); rewarded.clear(); pending.clear(); NBTTagList players = tag.getTagList("Rewarded", 8), queue = tag.getTagList("Pending", 10);
		for (int i = 0; i < Math.min(128, players.tagCount()); i++) try { rewarded.add(UUID.fromString(players.getStringTagAt(i))); } catch (IllegalArgumentException ignored) { }
		for (int i = 0; i < Math.min(64, queue.tagCount()); i++) { ItemStack stack = new ItemStack(queue.getCompoundTagAt(i)); if (!stack.isEmpty()) pending.add(stack); }
	}
}
