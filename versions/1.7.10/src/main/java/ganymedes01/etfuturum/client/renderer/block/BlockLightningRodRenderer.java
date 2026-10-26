package ganymedes01.etfuturum.client.renderer.block;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

@ThreadSafeISBRH(perThread = false)
public class BlockLightningRodRenderer extends BlockModelBase {
	public BlockLightningRodRenderer() { super(RenderIDs.LIGHTNING_ROD); }

	private double[] part(int direction, boolean tip) {
		double min = tip ? 0.25 : 0.375, max = 1 - min;
		double start = tip ? 0.75 : 0, end = tip ? 1 : 0.75;
		if ((direction & 1) == 0) { double old = start; start = 1 - end; end = 1 - old; }
		if (direction / 2 == 1) return new double[]{min, min, start, max, max, end};
		if (direction / 2 == 2) return new double[]{start, min, min, end, max, max};
		return new double[]{min, start, min, max, end, max};
	}
	@Override protected void renderInventoryModel(Block block, int meta, int model, RenderBlocks renderer,
			double x0, double y0, double z0, double x1, double y1, double z1) {
		for (int i = 0; i < 2; i++) {
			double[] box = part(1, i == 1);
			renderStandardInventoryCube(block, 0, model, renderer, box[0], box[1], box[2], box[3], box[4], box[5]);
		}
	}
	@Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int model, RenderBlocks renderer) {
		int direction = (world.getBlockMetadata(x, y, z) & 7) % 6;
		boolean rendered = false;
		for (int i = 0; i < 2; i++) {
			double[] box = part(direction, i == 1);
			rendered |= renderStandardWorldCube(world, x, y, z, block, model, renderer, box[0], box[1], box[2], box[3], box[4], box[5]);
		}
		return rendered;
	}
}
