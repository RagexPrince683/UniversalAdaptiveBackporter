package ganymedes01.etfuturum.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import java.util.Random;

/** Bits 0..1 are hydration, bit 2 is contained source water. Items retain neither. */
public class BlockDriedGhast extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[4]; for (int i = 0; i < 4; i++) models[i] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "dried_ghast_hydration_" + i); blockIcon = models[0].particle; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[meta & 3]; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	public BlockDriedGhast() { super(Material.rock); setNames("dried_ghast"); setTickRandomly(true); setHardness(.5F); setLightOpacity(0); setBlockBounds(.25F, 0, .25F, .75F, .625F, .75F); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundDriedGhast); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int damageDropped(int meta) { return 0; }
	@Override public void updateTick(World world, int x, int y, int z, Random random) { if (!world.isRemote) world.getTileEntity(x, y, z); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, net.minecraft.block.Block neighbor) { if (!world.isRemote) world.getTileEntity(x, y, z); }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public boolean canRenderInPass(int pass) { return pass == 0 || pass == 1; }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public int getRenderBlockPass() { return 1; }
	@Override public int getMobilityFlag() { return 1; }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityDriedGhast(); }
	@Override public int onBlockPlaced(World world, int x, int y, int z, int side, float hx, float hy, float hz, int meta) { return world.getBlock(x, y, z).getMaterial() == Material.water && world.getBlockMetadata(x, y, z) == 0 ? 4 : 0; }
	@Override public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean harvest) { return world.setBlock(x, y, z, (world.getBlockMetadata(x, y, z) & 4) != 0 ? Blocks.water : Blocks.air, 0, 3); }
	@Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
		ItemStack held = player.getHeldItem();
		if (held == null || !player.canPlayerEdit(x, y, z, side, held)) return false;
		boolean wet = (world.getBlockMetadata(x, y, z) & 4) != 0;
		boolean fill = held.getItem() == Items.water_bucket && !wet && !world.provider.isHellWorld;
		boolean drain = held.getItem() == Items.bucket && wet;
		if (!fill && !drain) return false;
		if (!world.isRemote) {
			world.setBlockMetadataWithNotify(x, y, z, (world.getBlockMetadata(x, y, z) & 3) | (fill ? 4 : 0), 3);
			if (!player.capabilities.isCreativeMode) {
				ItemStack result = new ItemStack(fill ? Items.bucket : Items.water_bucket);
				if (--held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
				else if (!player.inventory.addItemStackToInventory(result)) player.dropPlayerItemWithRandomChoice(result, false);
			}
		}
		return true;
	}
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		if ((world.getBlockMetadata(x, y, z) & 4) != 0 && random.nextInt(4) == 0) world.spawnParticle("happyVillager", x + random.nextDouble(), y + .7, z + random.nextDouble(), 0, 0, 0);
	}
}
