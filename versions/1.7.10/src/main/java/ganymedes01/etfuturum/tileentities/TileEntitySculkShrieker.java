package ganymedes01.etfuturum.tileentities;

import net.minecraft.world.World;
import java.util.*;

/** Loaded shrieker lifecycle index; warning state remains on players for save compatibility. */
public class TileEntitySculkShrieker extends net.minecraft.tileentity.TileEntity {
	private static final Map<World, Set<TileEntitySculkShrieker>> LOADED = new IdentityHashMap<>();
	@Override public boolean canUpdate() { return false; }
	@Override public void validate() { super.validate(); if (worldObj != null && !worldObj.isRemote) LOADED.computeIfAbsent(worldObj, w -> Collections.newSetFromMap(new IdentityHashMap<>())).add(this); }
	private void remove() { Set<TileEntitySculkShrieker> set = LOADED.get(worldObj); if (set != null) { set.remove(this); if (set.isEmpty()) LOADED.remove(worldObj); } }
	@Override public void invalidate() { remove(); super.invalidate(); }
	@Override public void onChunkUnload() { remove(); super.onChunkUnload(); }
	public static List<TileEntitySculkShrieker> nearby(World world, int x, int y, int z) {
		List<TileEntitySculkShrieker> result = new ArrayList<>(); Set<TileEntitySculkShrieker> set = LOADED.get(world);
		if (set != null) for (TileEntitySculkShrieker tile : set) if (!tile.isInvalid() && tile.getDistanceFrom(x, y, z) <= 64) result.add(tile);
		return result;
	}
	public static void unload(World world) { LOADED.remove(world); }
	public static void clear() { LOADED.clear(); }
}
