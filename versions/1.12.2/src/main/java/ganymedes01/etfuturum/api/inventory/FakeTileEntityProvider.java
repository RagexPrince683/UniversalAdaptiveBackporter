package ganymedes01.etfuturum.api.inventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
/** Compatibility boundary for block inventories without persistent tiles. The returned tile is not world-owned. */
public interface FakeTileEntityProvider {TileEntity getFakeTileEntity(World world,int x,int y,int z);}
