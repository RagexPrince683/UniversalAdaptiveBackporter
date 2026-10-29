package ganymedes01.etfuturum.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
public class BlockSculkSensor extends BlockStructureDecoration {
	public BlockSculkSensor(){this("sculk_sensor","sculk_sensor","sculk_sensor_active");}
    protected BlockSculkSensor(String name,String inactive,String active) { super(name,inactive,active); setHardness(1.5F); setBlockBounds(0, 0, 0, 1, .5F, 1); }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntitySculkSensor(); }
	@Override public boolean canProvidePower() { return true; }
	@Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) { TileEntity tile=world.getTileEntity(x,y,z);return (world.getBlockMetadata(x,y,z)&4)!=0&&tile instanceof ganymedes01.etfuturum.tileentities.TileEntitySculkSensor?((ganymedes01.etfuturum.tileentities.TileEntitySculkSensor)tile).power:0; }
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){TileEntity tile=world.getTileEntity(x,y,z);return (world.getBlockMetadata(x,y,z)&4)!=0&&tile instanceof ganymedes01.etfuturum.tileentities.TileEntitySculkSensor?((ganymedes01.etfuturum.tileentities.TileEntitySculkSensor)tile).frequency:0;}
}
