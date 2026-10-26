package ganymedes01.etfuturum.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/** Per-call source-water view; never mutates shared block bounds or the underlying chunk. */
final class ContainedWaterAccess implements IBlockAccess {
	private final IBlockAccess delegate; private final int x, y, z;
	ContainedWaterAccess(IBlockAccess delegate, int x, int y, int z) { this.delegate = delegate; this.x = x; this.y = y; this.z = z; }
	private boolean own(int x, int y, int z) { return this.x == x && this.y == y && this.z == z; }
	private boolean water(int x, int y, int z) { return own(x, y, z) || delegate.getBlock(x, y, z) instanceof ganymedes01.etfuturum.blocks.BlockAquaticPlant plant && plant.submerged; }
	@Override public Block getBlock(int x, int y, int z) { return water(x, y, z) ? Blocks.water : delegate.getBlock(x, y, z); }
	@Override public int getBlockMetadata(int x, int y, int z) { return water(x, y, z) ? 0 : delegate.getBlockMetadata(x, y, z); }
	@Override public TileEntity getTileEntity(int x, int y, int z) { return delegate.getTileEntity(x, y, z); }
	@Override public int getLightBrightnessForSkyBlocks(int x, int y, int z, int light) { return delegate.getLightBrightnessForSkyBlocks(x, y, z, light); }
	@Override public boolean isAirBlock(int x, int y, int z) { return !own(x, y, z) && delegate.isAirBlock(x, y, z); }
	@Override public BiomeGenBase getBiomeGenForCoords(int x, int z) { return delegate.getBiomeGenForCoords(x, z); }
	@Override public int getHeight() { return delegate.getHeight(); }
	@Override public boolean extendedLevelsInChunkCache() { return delegate.extendedLevelsInChunkCache(); }
	@Override public int isBlockProvidingPowerTo(int x, int y, int z, int side) { return delegate.isBlockProvidingPowerTo(x, y, z, side); }
	@Override public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean fallback) { return !own(x, y, z) && delegate.isSideSolid(x, y, z, side, fallback); }
}
