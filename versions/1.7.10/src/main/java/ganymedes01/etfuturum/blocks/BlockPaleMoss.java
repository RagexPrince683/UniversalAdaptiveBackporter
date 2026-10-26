package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;

public class BlockPaleMoss extends BaseBlock implements IGrowable {
	public BlockPaleMoss() { super(Material.grass); setNames("pale_moss_block"); setHardness(0.1F); setStepSound(soundTypeGrass); }
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return world.isAirBlock(x, y + 1, z); }
	@Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
	@Override public void func_149853_b(World world, Random random, int x, int y, int z) {
		for (int tries = 0; tries < 64; tries++) {
			int px = x + random.nextInt(7) - 3, pz = z + random.nextInt(7) - 3, py = y + random.nextInt(3) - 1;
			if (!world.blockExists(px, py, pz) || !world.isAirBlock(px, py + 1, pz)) continue;
			net.minecraft.block.Block old = world.getBlock(px, py, pz);
			if (old != Blocks.dirt && old != Blocks.grass && old != Blocks.stone && old != this) continue;
			world.setBlock(px, py, pz, this, 0, 2);
			if (random.nextInt(3) == 0) world.setBlock(px, py + 1, pz, ModBlocks.PALE_MOSS_CARPET.get(), 0, 2);
		}
	}
}
