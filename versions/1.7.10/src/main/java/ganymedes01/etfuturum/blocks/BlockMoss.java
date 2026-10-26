package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.client.sound.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class BlockMoss extends BaseBlock implements IGrowable {

	public BlockMoss() {
		super(Material.grass);
		setHardness(0.1F);
		setResistance(0.1F);
		setNames("moss_block");
		setHarvestLevel("hoe", 0);
		setBlockSound(ModSounds.soundMoss);
		setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	/**
	 * MCP name: {@code canFertilize}
	 */
	@Override
	public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
		return world.isAirBlock(x, y + 1, z);
	}

	/**
	 * MCP name: {@code shouldFertilize}
	 */
	@Override
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		return true;
	}

	// Adjust the weights as needed
	private static final int AZALEA_BUSH_WEIGHT = 2;
	private static final int FLOWERING_AZALEA_WEIGHT = 1;
	private static final int DOUBLE_TALL_GRASS_WEIGHT = 1;
	private static final int MOSS_CARPET_WEIGHT = 10;
	private static final int TALL_GRASS_WEIGHT = 7;
	private static final int AIR_WEIGHT = 79;

	/**
	 * MCP name: {@code fertilize}
	 */
	@Override
	public void func_149853_b(World world, Random rand, int xCoord, int yCoord, int zCoord) {
		if (world.isRemote || !world.isAirBlock(xCoord, yCoord + 1, zCoord)) return;
		for (int attempt = 0; attempt < 128; attempt++) {
			int x = xCoord + rand.nextInt(7) - 3, z = zCoord + rand.nextInt(7) - 3;
			for (int y = Math.min(world.getHeight() - 2, yCoord + 2); y >= Math.max(1, yCoord - 2); y--) {
				if (!world.blockExists(x, y, z)) break;
				Block ground = world.getBlock(x, y, z), above = world.getBlock(x, y + 1, z);
				if (ground != Blocks.grass && ground != Blocks.dirt && ground != Blocks.stone && ground != this && (!ModBlocks.DEEPSLATE.isEnabled() || ground != ModBlocks.DEEPSLATE.get())) continue;
				if (!above.isAir(world, x, y + 1, z) && !(above instanceof BlockBush)) continue;
				world.setBlock(x, y, z, this, 0, 2);
				if (!world.isAirBlock(x, y + 1, z)) break;
				int choice = rand.nextInt(AZALEA_BUSH_WEIGHT + FLOWERING_AZALEA_WEIGHT + DOUBLE_TALL_GRASS_WEIGHT + MOSS_CARPET_WEIGHT + TALL_GRASS_WEIGHT + AIR_WEIGHT);
				if (choice < 3) world.setBlock(x, y + 1, z, ModBlocks.AZALEA.get(), choice == 2 ? 1 : 0, 2);
				else if (choice == 3 && world.isAirBlock(x, y + 2, z)) Blocks.double_plant.func_149889_c(world, x, y + 1, z, 2, 2);
				else if (choice < 14) world.setBlock(x, y + 1, z, ModBlocks.MOSS_CARPET.get(), 0, 2);
				else if (choice < 21) world.setBlock(x, y + 1, z, Blocks.tallgrass, 1, 2);
				break;
			}
		}
	}

	@Override
	public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plant) {
		return Blocks.dirt.canSustainPlant(world, x, y, z, direction, plant);
	}
}
