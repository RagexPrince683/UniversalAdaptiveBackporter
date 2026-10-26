package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import java.util.Random;

public class WorldGenPaleOak extends WorldGenAbstractTree {
	private final boolean natural;
	public WorldGenPaleOak(boolean notify, boolean natural) { super(notify); this.natural = natural; }
	@Override public boolean generate(World world, Random random, int x, int y, int z) {
		int height = 6 + random.nextInt(4);
		if (y < 1 || y + height + 2 >= world.getHeight() || !world.checkChunksExist(x - 4, y - 1, z - 4, x + 5, y + height + 2, z + 5)) return false;
		for (int i = 0; i < 4; i++) {
			Block soil = world.getBlock(x + i % 2, y - 1, z + i / 2);
			if (soil != Blocks.grass && soil != Blocks.dirt && soil != ModBlocks.PALE_MOSS.get()) return false;
		}
		for (int dx = -4; dx <= 5; dx++) for (int dz = -4; dz <= 5; dz++) for (int dy = 0; dy <= height + 1; dy++) {
			if (dy < height - 3 && (dx < 0 || dx > 1 || dz < 0 || dz > 1)) continue;
			Block block = world.getBlock(x + dx, y + dy, z + dz);
			if (!block.isAir(world, x + dx, y + dy, z + dz) && !block.isLeaves(world, x + dx, y + dy, z + dz) && !block.isReplaceable(world, x + dx, y + dy, z + dz)) return false;
		}
		for (int i = 0; i < 4; i++) {
			setBlockAndNotifyAdequately(world, x + i % 2, y - 1, z + i / 2, Blocks.dirt, 0);
			for (int dy = 0; dy < height; dy++) setBlockAndNotifyAdequately(world, x + i % 2, y + dy, z + i / 2, ModBlocks.PALE_OAK_LOG.get(), 0);
		}
		for (int dy = height - 3; dy <= height + 1; dy++) {
			int radius = dy > height ? 2 : dy == height ? 3 : 4;
			for (int dx = -radius; dx <= radius + 1; dx++) for (int dz = -radius; dz <= radius + 1; dz++) {
				if (dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1 && dy < height) continue;
				if ((dx == -radius || dx == radius + 1) && (dz == -radius || dz == radius + 1)) continue;
				setBlockAndNotifyAdequately(world, x + dx, y + dy, z + dz, ModBlocks.PALE_OAK_LEAVES.get(), 0);
				if (dy == height - 3 && random.nextInt(6) == 0) {
					for (int length = 1, max = 1 + random.nextInt(4); length <= max && world.isAirBlock(x + dx, y + dy - length, z + dz); length++) setBlockAndNotifyAdequately(world, x + dx, y + dy - length, z + dz, ModBlocks.PALE_HANGING_MOSS.get(), 0);
				}
			}
		}
		if (natural && random.nextInt(10) == 0) setBlockAndNotifyAdequately(world, x, y + height - 3, z, ModBlocks.CREAKING_HEART.get(), 1);
		if (natural) for (int i = 0; i < 24; i++) {
			int mx = x + random.nextInt(9) - 4, mz = z + random.nextInt(9) - 4;
			int my = world.getHeightValue(mx, mz);
			if (world.getBlock(mx, my - 1, mz) == Blocks.grass && world.isAirBlock(mx, my, mz)) {
				world.setBlock(mx, my - 1, mz, ModBlocks.PALE_MOSS.get(), 0, 2);
				world.setBlock(mx, my, mz, random.nextInt(5) == 0 ? ModBlocks.EYEBLOSSOM.get() : ModBlocks.PALE_MOSS_CARPET.get(), 0, 2);
			}
		}
		return true;
	}
}
