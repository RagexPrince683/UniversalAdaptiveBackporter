package ganymedes01.etfuturum.world.generate.feature;

import com.google.common.collect.Lists;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.EFRBlockStateConverter;
import ganymedes01.etfuturum.core.utils.structurenbt.NBTStructure;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class WorldGenNetherFossil extends WorldGenFossil {

	private final List<NBTStructure> fossils;

	public WorldGenNetherFossil() {
		fossils = Lists.newArrayList();
		for (int i = 1; i <= 14; i++) {
			fossils.add(new NBTStructure("/data/structure/nether_fossils/fossil_" + i + ".nbt", EFRBlockStateConverter.INSTANCE));
		}
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		NBTStructure fossil = fossils.get(rand.nextInt(fossils.size()));
		ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(4) + 2);
		BlockPos size = fossil.getSize(dir);
		if (!world.checkChunksExist(x, 1, z, x + size.getX() - 1, world.getActualHeight() - 1, z + size.getZ() - 1)) return false;
		for (int floor = Math.min(y, world.getActualHeight() - size.getY() - 1); floor >= 32; floor--) {
			if (!validCorner(world, x, floor, z) || !canFossilGenerateHere(world, x, floor, z, size)) continue;
			fossil.placeStructure(world, rand, x, floor, z, dir);
			return true;
		}
		return false;
	}

	@Override
	protected boolean canFossilGenerateHere(World world, int x, int y, int z, BlockPos corners) {
		int support = 0;
		for (int dx = 0; dx < corners.getX(); dx++) for (int dz = 0; dz < corners.getZ(); dz++) if (validCorner(world, x + dx, y, z + dz)) support++;
		return support * 2 >= corners.getX() * corners.getZ() && ganymedes01.etfuturum.world.structure.PlacementChecks.clear(world, x, y, z, corners.getX(), corners.getY(), corners.getZ());
	}

	@Override
	protected boolean validCorner(World world, int x, int y, int z) {
		net.minecraft.block.Block floor = world.getBlock(x, y - 1, z);
		return (floor == net.minecraft.init.Blocks.soul_sand || floor == ganymedes01.etfuturum.ModBlocks.SOUL_SOIL.get()) && world.isAirBlock(x, y, z);
	}
}
