package ganymedes01.etfuturum.world.generate.decorate;

import ganymedes01.etfuturum.blocks.*;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.Random;

/** Native tree, shelf and branching reef formations, fully contained in the decorating chunk. */
public final class CoralReefs {
	private CoralReefs() { }
	public static void generate(World world, Random random, int chunkX, int chunkZ) {
		if (!ContentAvailability.enabled("generate_coral_reefs") || random.nextInt(4) != 0) return;
		int x = (chunkX << 4) + 5 + random.nextInt(6), z = (chunkZ << 4) + 5 + random.nextInt(6);
		if (world.getBiomeGenForCoords(x, z).temperature <= .8F) return;
		int y = Math.min(62, world.getHeightValue(x, z) - 1);
		while (y > 1 && BlockAquaticPlant.sourceWater(world, x, y, z)) y--;
		y++;
		if (y > 57 || !BlockAquaticPlant.sourceWater(world, x, y, z) || !world.getBlock(x, y - 1, z).isSideSolid(world, x, y - 1, z, ForgeDirection.UP)
				|| ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new StructureBoundingBox(x - 4, y - 1, z - 4, x + 4, y + 6, z + 4))) return;
		String color = CoralContent.TYPES[random.nextInt(5)]; int shape = random.nextInt(3), height = 2 + random.nextInt(3);
		for (int dy = 0; dy < height; dy++) for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
			boolean solid = shape == 0 ? dx == 0 && dz == 0 || dy == height - 1 && Math.abs(dx) + Math.abs(dz) <= 2
					: shape == 1 ? dy == 0 && dx == 0 && dz == 0 || dy == height - 1 && dx * dx + dz * dz <= 8
					: dx == 0 && dz == 0 || Math.abs(dx) == dy && dz == 0 || Math.abs(dz) == dy && dx == 0;
			if (solid && BlockAquaticPlant.sourceWater(world, x + dx, y + dy, z + dz)) world.setBlock(x + dx, y + dy, z + dz, CoralContent.get(color, false, "_block", false), 0, 2);
		}
		for (int dy = 0; dy < height; dy++) for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
			int bx = x + dx, by = y + dy, bz = z + dz;
			if (!(world.getBlock(bx, by, bz) instanceof BlockCoralBlock)) continue;
			if (BlockAquaticPlant.sourceWater(world, bx, by + 1, bz)) {
				if (ganymedes01.etfuturum.ModBlocks.SEA_PICKLE.isEnabled() && random.nextInt(8) == 0) world.setBlock(bx, by + 1, bz, ganymedes01.etfuturum.ModBlocks.SEA_PICKLE_UNDERWATER.get(), random.nextInt(4), 2);
				else if (random.nextBoolean()) world.setBlock(bx, by + 1, bz, CoralContent.get(color, false, random.nextBoolean() ? "" : "_fan", true), 0, 2);
			}
			for (int side = 2; side <= 5; side++) if (random.nextInt(5) == 0) {
				ForgeDirection face = ForgeDirection.getOrientation(side);
				if (BlockAquaticPlant.sourceWater(world, bx + face.offsetX, by, bz + face.offsetZ)) world.setBlock(bx + face.offsetX, by, bz + face.offsetZ, CoralContent.get(color, false, "_wall_fan", true), side - 2, 2);
			}
		}
	}
}
