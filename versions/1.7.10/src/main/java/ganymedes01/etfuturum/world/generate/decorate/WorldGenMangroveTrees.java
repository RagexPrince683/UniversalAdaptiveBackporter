package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

/** Native shallow-water tree. Roots displace water; no waterlogged block states are introduced. */
public class WorldGenMangroveTrees extends WorldGenAbstractTree {
	public WorldGenMangroveTrees(boolean notify) {
		super(notify);
	}

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		int height = 7 + random.nextInt(5);
		if (y < 2 || y + height + 2 >= world.getHeight()
				|| !world.checkChunksExist(x - 5, y - 6, z - 5, x + 5, y + height + 2, z + 5)) return false;
		int ground = y - 1;
		while (ground > y - 6 && world.getBlock(x, ground, z).getMaterial() == Material.water) ground--;
		Block soil = world.getBlock(x, ground, z);
		if (soil != Blocks.grass && soil != Blocks.dirt && soil != Blocks.clay && soil != ModBlocks.MUD.get()) return false;
		// Validate the entire occupied envelope before making any writes.
		for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
			for (int dy = 0; dy <= height + 1; dy++) {
				Block block = world.getBlock(x + dx, y + dy, z + dz);
				if (!replaceable(world, x + dx, y + dy, z + dz, block)) return false;
			}
		}
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			if (Math.abs(dx) + Math.abs(dz) != 1) continue;
			for (int step = 1; step <= 3; step++) {
				int rx = x + dx * step, rz = z + dz * step;
				int ry = y + 4 - step;
				for (; ry >= y - 5 && ry >= 1; ry--) {
					Block old = world.getBlock(rx, ry, rz);
					if (old == ModBlocks.MUD.get() && ModBlocks.MUDDY_MANGROVE_ROOTS.isEnabled()) {
						setBlockAndNotifyAdequately(world, rx, ry, rz, ModBlocks.MUDDY_MANGROVE_ROOTS.get(), 0);
						break;
					}
					if (!replaceable(world, rx, ry, rz, old)) break;
					setBlockAndNotifyAdequately(world, rx, ry, rz, ModBlocks.MANGROVE_ROOTS.get(), 0);
				}
			}
		}
		for (int dy = 3; dy < height; dy++) setBlockAndNotifyAdequately(world, x, y + dy, z, ModBlocks.MANGROVE_LOG.get(), 0);
		for (int dy = height - 3; dy <= height + 1; dy++) {
			int radius = dy >= height ? 2 : 4;
			for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + 1 || (dx == 0 && dz == 0 && dy < height)) continue;
				setBlockAndNotifyAdequately(world, x + dx, y + dy, z + dz, ModBlocks.LEAVES.get(), 0);
				if (dy == height - 3 && random.nextInt(8) == 0 && world.isAirBlock(x + dx, y + dy - 1, z + dz)) {
					setBlockAndNotifyAdequately(world, x + dx, y + dy - 1, z + dz, ModBlocks.SAPLING.get(), 12);
				}
			}
		}
		return true;
	}

	private boolean replaceable(World world, int x, int y, int z, Block block) {
		return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z)
				|| block.getMaterial() == Material.water || block.isReplaceable(world, x, y, z);
	}
}
