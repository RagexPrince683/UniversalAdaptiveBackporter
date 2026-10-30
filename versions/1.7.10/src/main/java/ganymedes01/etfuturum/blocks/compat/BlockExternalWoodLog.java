package ganymedes01.etfuturum.blocks.compat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/** A single-species pillar which delegates its artwork to an optional mod's log. */
public class BlockExternalWoodLog extends BlockRotatedPillar {

	private final Block source;
	private final int sourceMeta;
	private final boolean allSides;
	private final String strippedTexture;
	private IIcon strippedSide, strippedTop;

	public BlockExternalWoodLog(Block source, int sourceMeta, boolean allSides) {
		this(source, sourceMeta, allSides, null);
	}

	public BlockExternalWoodLog(Block source, int sourceMeta, boolean allSides, String strippedTexture) {
		super(Material.wood);

		this.source = source;
		this.sourceMeta = sourceMeta;
		this.allSides = allSides;
		this.strippedTexture = strippedTexture;
		setHardness(2.0F);
		setStepSound(Block.soundTypeWood);
		setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	@Override
	protected IIcon getSideIcon(int meta) {
		return strippedTexture == null ? source.getIcon(2, sourceMeta) : strippedSide;
	}

	@Override
	protected IIcon getTopIcon(int meta) {
		return strippedTexture == null ? source.getIcon(allSides ? 2 : 1, sourceMeta) : allSides ? strippedSide : strippedTop;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		// Bark delegates registration to the source mod. Stripped artwork belongs to UAB's compatibility assets.
		if (strippedTexture != null) {
			strippedSide = register.registerIcon(strippedTexture);
			strippedTop = allSides ? strippedSide : register.registerIcon(strippedTexture + "_top");
		}
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return true;
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return 5;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return 5;
	}
}
