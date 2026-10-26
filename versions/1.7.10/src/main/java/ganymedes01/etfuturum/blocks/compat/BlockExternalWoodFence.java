package ganymedes01.etfuturum.blocks.compat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.blocks.BlockModernWoodFence;
import ganymedes01.etfuturum.blocks.BlockWoodFence;
import ganymedes01.etfuturum.blocks.BlockWoodFenceGate;
import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/** Modern fence behavior with a face supplied by an external plank metadata. */
public class BlockExternalWoodFence extends BlockFence {
	private final Block planks;
	private final int plankMeta;

	public BlockExternalWoodFence(Block planks, int plankMeta) {
		super(null, Material.wood);
		this.planks = planks;
		this.plankMeta = plankMeta;
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(Block.soundTypeWood);
		setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ignored) {
		// The source planks own registration. getIcon delegates live, including after resource reloads.
		// BlockFence's implementation would register the unused null constructor texture instead.
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return planks.getIcon(side, plankMeta);
	}

	@Override
	public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return block instanceof BlockWoodFence || block instanceof BlockModernWoodFence
				|| block instanceof BlockWoodFenceGate || super.canConnectFenceTo(world, x, y, z);
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return ConfigFunctions.enableExtraBurnableBlocks;
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return isFlammable(world, x, y, z, side) ? 20 : 0;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return isFlammable(world, x, y, z, side) ? 5 : 0;
	}
}
