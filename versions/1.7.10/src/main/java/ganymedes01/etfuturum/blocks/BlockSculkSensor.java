package ganymedes01.etfuturum.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
public class BlockSculkSensor extends BlockStructureDecoration {
	public BlockSculkSensor() { super("sculk_sensor", "sculk_sensor", "sculk_sensor_active"); setHardness(1.5F); setBlockBounds(0, 0, 0, 1, .5F, 1); }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntitySculkSensor(); }
	@Override public boolean canProvidePower() { return true; }
	@Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) { return (world.getBlockMetadata(x, y, z) & 4) != 0 ? 15 : 0; }
}
