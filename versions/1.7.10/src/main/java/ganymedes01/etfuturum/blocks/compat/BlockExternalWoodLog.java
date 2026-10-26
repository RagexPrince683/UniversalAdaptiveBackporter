package ganymedes01.etfuturum.blocks.compat;

import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/** A single-species pillar which delegates its artwork to an optional mod's log. */
public class BlockExternalWoodLog extends BlockRotatedPillar {

	private final Block source;
	private final int sourceMeta;
	private final boolean allSides;

	public BlockExternalWoodLog(Block source, int sourceMeta, boolean allSides) {
		super(Material.wood);

		this.source = source;
		this.sourceMeta = sourceMeta;
		this.allSides = allSides;
		setHardness(2.0F);
		setStepSound(Block.soundTypeWood);
		setCreativeTab(EtFuturum.creativeTabBlocks);
	}

	@Override
	protected IIcon getSideIcon(int meta) {
		return source.getIcon(2, sourceMeta);
	}

	@Override
	protected IIcon getTopIcon(int meta) {
		return source.getIcon(allSides ? 2 : 1, sourceMeta);
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
