package ganymedes01.etfuturum.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public class BlockVault extends BlockStructureDecoration {
	public BlockVault() { super("vault", "vault", "vault_ominous", "vault_active", "vault_active_ominous"); setHardness(50); setResistance(2000); }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityVault(); }
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) { return world.isRemote || ((ganymedes01.etfuturum.tileentities.TileEntityVault) world.getTileEntity(x, y, z)).unlock(player); }
	@Override public net.minecraft.item.Item getItemDropped(int meta, java.util.Random random, int fortune) { return null; }
	@Override protected boolean canSilkHarvest() { return false; }
}
