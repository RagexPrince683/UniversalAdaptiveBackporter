package ganymedes01.etfuturum.world.generate.feature;

import com.google.common.collect.Lists;
import ganymedes01.etfuturum.core.utils.helpers.BlockPos;
import ganymedes01.etfuturum.core.utils.structurenbt.EFRBlockStateConverter;
import ganymedes01.etfuturum.core.utils.structurenbt.NBTStructure;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class WorldGenFossil extends WorldGenerator {

	private final List<Pair<NBTStructure, NBTStructure>> fossils;

	public WorldGenFossil() {

		fossils = Lists.newArrayList();

		for (int i = 1; i <= 4; i++) {
			fossils.add(new ImmutablePair<>(new NBTStructure("/data/structure/fossil/skull_" + i + ".nbt", EFRBlockStateConverter.INSTANCE),
					new NBTStructure("/data/structure/fossil/skull_" + i + "_coal.nbt", EFRBlockStateConverter.INSTANCE)));
			fossils.add(new ImmutablePair<>(new NBTStructure("/data/structure/fossil/spine_" + i + ".nbt", EFRBlockStateConverter.INSTANCE),
					new NBTStructure("/data/structure/fossil/spine_" + i + "_coal.nbt", EFRBlockStateConverter.INSTANCE)));
		}
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		Pair<NBTStructure, NBTStructure> fossilPair = fossils.get(rand.nextInt(fossils.size()));
		ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(4) + 2);
		BlockPos size = fossilPair.getLeft().getSize(dir);
		if (!world.checkChunksExist(x, y, z, x + size.getX(), y + size.getY(), z + size.getZ())) return false;
		if (!canFossilGenerateHere(world, x, y, z, fossilPair.getLeft().getSize(dir))) return false;
		if (ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.reserved(world, new StructureBoundingBox(x, y, z, x + size.getX() - 1, y + size.getY() - 1, z + size.getZ() - 1))) return false;
		for (int dx = 0; dx < size.getX(); dx++) for (int dz = 0; dz < size.getZ(); dz++) for (int dy = 0; dy < size.getY(); dy++) {
			Block existing = world.getBlock(x + dx, y + dy, z + dz);
			if (existing.hasTileEntity(world.getBlockMetadata(x + dx, y + dy, z + dz)) || existing == Blocks.bedrock) return false;
		}
		fossilPair.getLeft().placeStructure(world, rand, x, y, z, dir, 0.9F);
		fossilPair.getRight().placeStructure(world, rand, x, y, z, dir, 0.1F);
		return true;
	}

	protected boolean canFossilGenerateHere(World world, int x, int y, int z, BlockPos corners) {
		int air = 0;
		if (!validCorner(world, x, y, z)) {
			air++;
		}
		if (!validCorner(world, x + corners.getX(), y, z)) {
			air++;
		}
		if (!validCorner(world, x, y, z + corners.getZ())) {
			air++;
		}
		if (!validCorner(world, x + corners.getX(), y, z + corners.getZ())) {
			air++;
		}
		if (!validCorner(world, x, y + corners.getY(), z)) {
			air++;
		}
		if (!validCorner(world, x + corners.getX(), y + corners.getY(), z)) {
			if (air++ >= 5) return false;
		}
		if (!validCorner(world, x, y + corners.getY(), z + corners.getZ())) {
			if (air++ >= 5) return false;
		}
		if (!validCorner(world, x + corners.getX(), y + corners.getY(), z + corners.getZ())) {
			if (air++ >= 5) return false;
		}
		return air < 5;
	}

	protected boolean validCorner(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return block != Blocks.air && block.isOpaqueCube();
	}
}
