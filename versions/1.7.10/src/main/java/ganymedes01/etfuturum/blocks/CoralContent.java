package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class CoralContent {
	public static final String[] TYPES = {"tube", "brain", "bubble", "fire", "horn"};
	private CoralContent() { }
	public static Block get(String color, boolean dead, String shape, boolean wet) {
		return GameRegistry.findBlock("etfuturum", (dead ? "dead_" : "") + color + "_coral" + shape + (wet && !shape.equals("_block") ? "_underwater" : ""));
	}
	public static boolean hasWater(World world, int x, int y, int z) {
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			int nx = x + side.offsetX, ny = y + side.offsetY, nz = z + side.offsetZ;
			// An unavailable neighbor is not evidence that the coral is dry.
			if (!world.blockExists(nx, ny, nz) || world.getBlock(nx, ny, nz).getMaterial() == Material.water) return true;
		}
		return false;
	}
}
