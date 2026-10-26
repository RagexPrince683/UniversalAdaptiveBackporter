package ganymedes01.etfuturum.world;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.*;
import java.util.*;
/** Index of loaded chest positions; chunk unload removes its complete bucket. */
public final class MobChestIndex {
	private static final Map<World, Map<Long, Set<ChunkCoordinates>>> WORLDS = new IdentityHashMap<>();
	private MobChestIndex() { }
	public static void add(World world, int x, int y, int z) { if (world.isRemote) return; WORLDS.computeIfAbsent(world, key -> new HashMap<>()).computeIfAbsent(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4), key -> new HashSet<>()).add(new ChunkCoordinates(x, y, z)); }
	public static void remove(World world, int x, int y, int z) { Map<Long, Set<ChunkCoordinates>> map = WORLDS.get(world); if (map != null) { Set<ChunkCoordinates> positions = map.get(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4)); if (positions != null) positions.remove(new ChunkCoordinates(x, y, z)); } }
	public static void unload(World world, int chunkX, int chunkZ) { Map<Long, Set<ChunkCoordinates>> map = WORLDS.get(world); if (map != null) map.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)); }
	public static void unload(World world) { WORLDS.remove(world); }
	public static List<ChunkCoordinates> nearby(World world, double x, double y, double z) { List<ChunkCoordinates> result = new ArrayList<>(); Map<Long, Set<ChunkCoordinates>> map = WORLDS.get(world); if (map == null) return result; int cx = net.minecraft.util.MathHelper.floor_double(x) >> 4, cz = net.minecraft.util.MathHelper.floor_double(z) >> 4; for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) { Set<ChunkCoordinates> positions = map.get(ChunkCoordIntPair.chunkXZ2Int(cx + dx, cz + dz)); if (positions != null) for (ChunkCoordinates position : positions) if (Math.abs(position.posX - x) <= 16 && Math.abs(position.posY - y) <= 8 && Math.abs(position.posZ - z) <= 16) result.add(position); } return result; }
	public static boolean chest(TileEntity tile) { return tile instanceof TileEntityChest || tile instanceof ganymedes01.etfuturum.tileentities.TileEntityCopperChest; }
}
