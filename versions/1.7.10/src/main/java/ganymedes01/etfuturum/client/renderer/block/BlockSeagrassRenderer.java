package ganymedes01.etfuturum.client.renderer.block;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

/** Keep the native crossed plant and native source-water surfaces in their respective passes. */
@ThreadSafeISBRH(perThread = false)
public final class BlockSeagrassRenderer extends BlockModelBase {
    public BlockSeagrassRenderer() { super(RenderIDs.SEAGRASS); set2DInventory(); }
    @Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int id, RenderBlocks renderer) {
        if (ForgeHooksClient.getWorldRenderPass() == 0) return renderer.renderCrossedSquares(block, x, y, z);
        IBlockAccess previous = renderer.blockAccess;
        try {
            renderer.blockAccess = new ContainedWaterAccess(world, x, y, z);
            return renderer.renderBlockLiquid(Blocks.water, x, y, z);
        } finally { renderer.blockAccess = previous; }
    }
}
