package ganymedes01.etfuturum.blocks;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public class BlockCopperChest extends BlockContainer {
	public BlockCopperChest() { super(Material.iron); setBlockName("etfuturum.copper_chest"); setBlockTextureName("copper_block"); setHardness(2.5F); setStepSound(ganymedes01.etfuturum.client.sound.ModSounds.soundCopper); setBlockBounds(.0625F, 0, .0625F, .9375F, .875F, .9375F); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabBlocks); }
	@Override public int getRenderType() { return -1; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public TileEntity createNewTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityCopperChest(); }
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) { if (!world.isRemote && !world.getBlock(x, y + 1, z).isNormalCube()) player.displayGUIChest((IInventory) world.getTileEntity(x, y, z)); return true; }
	@Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) { TileEntity tile = world.getTileEntity(x, y, z); if (tile instanceof IInventory inventory && !world.isRemote) for (int i = 0; i < inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) != null) { net.minecraft.entity.item.EntityItem item = new net.minecraft.entity.item.EntityItem(world, x + .5, y + .5, z + .5, inventory.getStackInSlot(i)); world.spawnEntityInWorld(item); inventory.setInventorySlotContents(i, null); } ganymedes01.etfuturum.world.MobChestIndex.remove(world, x, y, z); super.breakBlock(world, x, y, z, block, meta); }
}
