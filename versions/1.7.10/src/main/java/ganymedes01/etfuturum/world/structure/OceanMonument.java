package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.api.utils.LongIntOpenHashMap;
import ganymedes01.etfuturum.api.utils.PackedBlockCoordinates;
import ganymedes01.etfuturum.configuration.configs.ConfigBlocksItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.io.*;
import java.util.*;

public class OceanMonument {

	private static final List<BiomeGenBase> validBiomes = Arrays.asList(BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.river, BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver);
	private static final LongIntOpenHashMap map = new LongIntOpenHashMap(26000);

	public static void makeMap() {
		try {
			InputStream is = EtFuturum.class.getResourceAsStream("/assets/ocean_monument.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String s;
			while ((s = br.readLine()) != null) {
				String[] data = s.split("-");
				data[0] = data[0].trim();
				data[0] = data[0].substring(1, data[0].length() - 1);

				data[1] = data[1].trim();

				String[] coords = data[0].split(",");

				final int x = Integer.parseInt(coords[0].trim());
				final int y = Integer.parseInt(coords[1].trim());
				final int z = Integer.parseInt(coords[2].trim());
				final long key = PackedBlockCoordinates.pack(x, y, z);
				final int value = Integer.parseInt(data[1]);

				map.put(key, value);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildTemple(World world, int x, int y, int z) {
		if (world.isRemote)
			return;
		// This legacy generator places its entire 58x58 footprint in one call.
		// Reject the whole candidate before any write if it would force neighboring population.
		if (!world.checkChunksExist(x, 0, z, x + 57, world.getActualHeight() - 1, z + 57)) return;
		buildTemple(world, x, y, z, new StructureBoundingBox(x, 0, z, x + 57, world.getActualHeight() - 1, z + 57));
	}

	/** Population writes only its clipped, already-loaded columns. */
	public static void buildTemple(World world, int x, int y, int z, StructureBoundingBox clip) {
		if (world.isRemote) return;

		for (int index = 0; index < map.capacity(); index++) {
			if (!map.isOccupied(index)) continue;
			final long pos = map.keyAt(index);
			final int value = map.valueAt(index);
			final int posX = PackedBlockCoordinates.unpackX(pos);
			final int posY = PackedBlockCoordinates.unpackY(pos);
			final int posZ = PackedBlockCoordinates.unpackZ(pos);

			Block block = null;
			int meta = 0;
			switch (value) {
				case 0:
				case 1:
				case 2:
					block = ModBlocks.PRISMARINE_BLOCK.get();
					meta = value;
					break;
				case 3:
					block = ModBlocks.SEA_LANTERN.get();
					break;
				case 4:
					block = Blocks.gold_block;
					break;
				case 5:
					block = ConfigBlocksItems.enableSponge ? ModBlocks.SPONGE.get() : Blocks.sponge;
					meta = 1;
					break;
				case 6:
					block = Blocks.water;
					break;
			}

			if (block != null && clip.isVecInside(posX + x, posY + y, posZ + z))
				world.setBlock(posX + x, posY + y, posZ + z, block, meta, 2);
		}

		for (int i = 0; i < 7; i++) {
			generatePillar(world, x + 5 * i + 4 * i, y, z, clip);
			generatePillar(world, x, y, z + 5 * i + 4 * i, clip);
			generatePillar(world, x + 54, y, z + 5 * i + 4 * i, clip);
			if (i != 3)
				generatePillar(world, x + 5 * i + 4 * i, y, z + 54, clip);
		}
	}

	private static void generatePillar(World world, int x, int y, int z, StructureBoundingBox clip) {
		for (int dx = 0; dx < 4; dx++) for (int dz = 0; dz < 4; dz++) {
			int px = x + dx, pz = z + dz;
			if (!clip.isVecInside(px, y, pz)) continue;
			int foundation = 2;
			for (int py = y - 1; py > 0; py--) {
				Block existing = world.getBlock(px, py, pz);
				if (existing.getBlockHardness(world, px, py, pz) < 0) break;
				if (py < y - 5 && existing.getMaterial() != Material.water && !world.isAirBlock(px, py, pz) && --foundation < 0) break;
				world.setBlock(px, py, pz, ModBlocks.PRISMARINE_BLOCK.get(), 1, 2);
			}
		}
	}

	public static void generateFile(World world, int x, int y, int z, String path) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

			for (int i = 0; i < 58; i++)
				for (int j = 0; j < 31 - 9; j++)
					for (int k = 0; k < 58; k++) {
						Block b = world.getBlock(x + i, y + j, z + k);
						int meta = world.getBlockMetadata(x + i, y + j, z + k);

						String s = "(" + i + ", " + j + ", " + k + ") - ";
						if (b == ModBlocks.PRISMARINE_BLOCK.get())
							s += meta;
						else if (b == ModBlocks.SEA_LANTERN.get())
							s += 3;
						else if (b == Blocks.gold_block)
							s += 4;
						else if (b == Blocks.sponge)
							s += 5;
						else if (b == Blocks.stained_glass)
							s += 6;
						else
							s = null;

						if (s != null) {
							bw.write(s);
							bw.newLine();
						}
					}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean canSpawnAt(World worldObj, int chunkX, int chunkZ) {
		int spacing = 32;
		int separation = 5;
		int xx = chunkX;
		int zz = chunkZ;

		if (chunkX < 0)
			chunkX -= spacing - 1;

		if (chunkZ < 0)
			chunkZ -= spacing - 1;

		int i1 = chunkX / spacing;
		int j1 = chunkZ / spacing;
		Random random = new Random((long) i1 * 341873128712L + (long) j1 * 132897987541L + worldObj.getSeed() + 10387313L);
		i1 *= spacing;
		j1 *= spacing;
		i1 += (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;
		j1 += (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;

		if (xx == i1 && zz == j1) {
			BiomeGenBase center = worldObj.getWorldChunkManager().getBiomeGenAt(xx * 16 + 8, zz * 16 + 8);
			if (center != BiomeGenBase.deepOcean && !ganymedes01.etfuturum.world.surface.OceanBiomeLayer.isDeepOcean(center))
				return false;
			List<BiomeGenBase> biomes = new ArrayList<>(validBiomes);
			for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) if (biome != null && ganymedes01.etfuturum.world.surface.OceanBiomeLayer.isOcean(biome)) biomes.add(biome);
			return worldObj.getWorldChunkManager().areBiomesViable(xx * 16 + 8, zz * 16 + 8, 29, biomes);
		}

		return false;
	}
}
