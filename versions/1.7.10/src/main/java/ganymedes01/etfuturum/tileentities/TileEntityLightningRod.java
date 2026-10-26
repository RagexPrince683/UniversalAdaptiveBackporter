package ganymedes01.etfuturum.tileentities;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;

/** Loaded rods indexed through the native tile lifecycle, queried only when lightning strikes. */
public class TileEntityLightningRod extends TileEntity {
	private static final Map<World, Set<TileEntityLightningRod>> LOADED = new IdentityHashMap<>();
	private long poweredUntil;
	@Override public boolean canUpdate() { return false; }
	@Override public void validate() {
		super.validate();
		if (worldObj != null && !worldObj.isRemote) LOADED.computeIfAbsent(worldObj, w -> new HashSet<>()).add(this);
	}
	public void powerForEightTicks() { poweredUntil = worldObj.getTotalWorldTime() + 8; markDirty(); }
	public boolean powerExpired() { return worldObj.getTotalWorldTime() >= poweredUntil; }
	public int powerTicksRemaining() { return (int) Math.min(8, Math.max(1, poweredUntil - worldObj.getTotalWorldTime())); }
	@Override public boolean shouldRefresh(net.minecraft.block.Block oldBlock, net.minecraft.block.Block newBlock,
			int oldMeta, int newMeta, World world, int x, int y, int z) { return oldBlock != newBlock; }
	@Override public void writeToNBT(NBTTagCompound nbt) { super.writeToNBT(nbt); nbt.setLong("PoweredUntil", poweredUntil); }
	@Override public void readFromNBT(NBTTagCompound nbt) { super.readFromNBT(nbt); poweredUntil = nbt.getLong("PoweredUntil"); }
	private void remove() {
		Set<TileEntityLightningRod> rods = LOADED.get(worldObj);
		if (rods != null) {
			rods.remove(this);
			if (rods.isEmpty()) LOADED.remove(worldObj);
		}
	}
	@Override public void invalidate() { remove(); super.invalidate(); }
	@Override public void onChunkUnload() { remove(); super.onChunkUnload(); }
	public static void clear(World world) { LOADED.remove(world); }
	public static void clearAll() { LOADED.clear(); }

	public static TileEntityLightningRod nearest(World world, double x, double y, double z) {
		TileEntityLightningRod nearest = null;
		double best = 128 * 128;
		for (TileEntityLightningRod rod : LOADED.getOrDefault(world, Collections.emptySet())) {
			if (rod.isInvalid() || !world.blockExists(rod.xCoord, rod.yCoord, rod.zCoord)
					|| world.getBlock(rod.xCoord, rod.yCoord, rod.zCoord) != ModBlocks.LIGHTNING_ROD.get()) continue;
			double distance = rod.getDistanceFrom(x, y, z);
			if (distance <= best && world.canBlockSeeTheSky(rod.xCoord, rod.yCoord + 1, rod.zCoord)) {
				nearest = rod;
				best = distance;
			}
		}
		return nearest;
	}
}
