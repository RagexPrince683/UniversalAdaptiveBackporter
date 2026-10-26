package ganymedes01.etfuturum.blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;
public class BlockCopperGolemStatue extends net.minecraft.block.BlockContainer {
	public BlockCopperGolemStatue() { super(Material.iron); setBlockName("etfuturum.copper_golem_statue"); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabBlocks); setBlockTextureName("oxidized_copper"); setHardness(3); setBlockBounds(.2F, 0, .2F, .8F, 1, .8F); }
	@Override public net.minecraft.tileentity.TileEntity createNewTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityCopperStatue(); }
	@Override public int getRenderType() { return -1; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int damageDropped(int meta) { return meta; }
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) { ItemStack held = player.getHeldItem(); int meta = world.getBlockMetadata(x, y, z); if (held != null && held.getItem() instanceof ItemAxe) { if (!world.isRemote) { if ((meta & 4) != 0) world.setBlockMetadataWithNotify(x, y, z, meta & ~4, 3); else { ganymedes01.etfuturum.entities.EntityCopperGolem golem = new ganymedes01.etfuturum.entities.EntityCopperGolem(world); golem.setLocationAndAngles(x + .5, y, z + .5, 0, 0); golem.setOxidation(2); if (world.spawnEntityInWorld(golem)) world.setBlockToAir(x, y, z); } held.damageItem(1, player); } return true; } if (held != null && held.getItem() == ganymedes01.etfuturum.ModItems.HONEYCOMB.get() && (meta & 4) == 0) { if (!world.isRemote) { world.setBlockMetadataWithNotify(x, y, z, meta | 4, 3); if (!player.capabilities.isCreativeMode) held.stackSize--; } return true; } if (!world.isRemote) world.setBlockMetadataWithNotify(x, y, z, (meta & 4) | ((meta + 1) & 3), 3); return true; }
}
