package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.tileentities.TileEntityBrushable;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import java.util.*;

public final class ArchaeologySites {
	private ArchaeologySites() { }
	public static void suspicious(World world, int x, int y, int z, String table, long seed) {
		world.setBlock(x, y, z, ModBlocks.SUSPICIOUS_SAND.get(), 0, 2);
		if (world.getTileEntity(x, y, z) instanceof TileEntityBrushable) ((TileEntityBrushable) world.getTileEntity(x, y, z)).configure("minecraft:archaeology/" + table, seed);
	}
	/** A buried excavation pocket beside the native treasure chamber; TNT and all four chests are retained. */
	public static void pyramid(World world, StructureBoundingBox box, StructureBoundingBox clip) {
		if (world.isRemote || !ContentAvailability.enabled("archaeology")) return;
		Random random = new Random(world.getSeed() ^ box.minX * 341873128712L ^ box.minZ * 132897987541L);
		List<Integer> cells = new ArrayList<>(); for (int i = 0; i < 25; i++) cells.add(i); Collections.shuffle(cells, random);
		Set<Integer> suspicious = new HashSet<>(cells.subList(0, 4));
		for (int dx = 0; dx < 5; dx++) for (int dz = 0; dz < 5; dz++) {
			int x = box.minX + 3 + dx, z = box.minZ + 12 + dz, y = box.minY - 3;
			if (!clip.isVecInside(x, y, z)) continue;
			world.setBlock(x, y - 1, z, Blocks.sandstone, 2, 2);
			world.setBlock(x, y, z, Blocks.sand, 0, 2); world.setBlock(x, y + 1, z, Blocks.sand, 0, 2);
			if (suspicious.contains(dx * 5 + dz)) suspicious(world, x, y + 1, z, "desert_pyramid", random.nextLong());
		}
	}
	public static void well(World world, int x, int y, int z) {
		if (world.isRemote || !ContentAvailability.enabled("archaeology")) return;
		while (y > 2 && world.getBlock(x, y, z) != Blocks.flowing_water && world.getBlock(x, y, z) != Blocks.water) y--;
		if (y <= 2) return;
		Random random = new Random(world.getSeed() ^ x * 341873128712L ^ z * 132897987541L);
		for (int dx : new int[]{-1, 1}) { world.setBlock(x + dx, y - 2, z, Blocks.sandstone, 0, 2); suspicious(world, x + dx, y - 1, z, "desert_well", random.nextLong()); }
	}
}
