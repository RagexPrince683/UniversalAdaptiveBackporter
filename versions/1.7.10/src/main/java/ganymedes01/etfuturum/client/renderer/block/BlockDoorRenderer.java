package ganymedes01.etfuturum.client.renderer.block;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

@ThreadSafeISBRH(perThread = false)
public class BlockDoorRenderer extends BlockModelBase {

	public BlockDoorRenderer(int modelID) {
		super(modelID);
		set2DInventory();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);

		if ((meta & 8) != 0) {
			if (world.getBlock(x, y - 1, z) != block) {
				return false;
			}
		} else if (world.getBlock(x, y + 1, z) != block) {
			return false;
		}

		// The native door renderer resolves both halves, hinge UVs and state-dependent bounds.
		// renderStandardBlock uses stale bounds and ordinary neighbor-face culling here.
		return renderer.renderBlockDoor(block, x, y, z);
	}
}
