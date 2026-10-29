package ganymedes01.etfuturum.client.renderer.block;

import com.gtnewhorizons.angelica.api.ThreadSafeISBRH;
import ganymedes01.etfuturum.blocks.*;
import ganymedes01.etfuturum.lib.RenderIDs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

@ThreadSafeISBRH(perThread = false)
public final class ReleasedBlockRenderer extends BlockModelBase {
	public ReleasedBlockRenderer() { super(RenderIDs.RELEASED_BLOCK); }
	private ReleasedBlockModel model(Block block, int meta) {
        if(block instanceof ganymedes01.etfuturum.PowderSnow.Cauldron cauldron) return cauldron.getReleasedModel(meta);
		if (block instanceof BlockScaffolding scaffolding) return scaffolding.getReleasedModel(meta);
		if (block instanceof BlockCampfire campfire) return campfire.getReleasedModel(meta);
		if (block instanceof BlockLightningRod rod) return rod.getReleasedModel(meta);
		if (block instanceof BlockCoral coral) return coral.getReleasedModel(meta);
		if (block instanceof BlockConduit conduit) return conduit.getReleasedModel();
		if (block instanceof BlockSeaPickle pickle) return pickle.getReleasedModel(meta);
		if (block instanceof BlockKelp kelp) return kelp.getReleasedModel(true);
		if (block instanceof BlockSculkShrieker shrieker) return shrieker.getReleasedModel(meta);
		if (block instanceof BlockTrialSpawner spawner) return spawner.getReleasedModel(meta);
		if (block instanceof BlockTurtleEgg egg) return egg.getReleasedModel(meta);
		if (block instanceof BlockSnifferCrop crop) return crop.getReleasedModel(meta);
		if (block instanceof BlockSnifferEgg egg) return egg.getReleasedModel(meta);
		if (block instanceof BlockStructureDecoration decoration) return decoration.getReleasedModel(meta);
		if (block instanceof BlockDriedGhast ghast) return ghast.getReleasedModel(meta);
		if (block instanceof BlockFrogspawn frogspawn) return frogspawn.getReleasedModel(meta);
		return ((BlockSnifferPlant) block).getReleasedModel(meta);
	}
	@Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int id, RenderBlocks renderer) {
		if ((block instanceof BlockCampfire || block instanceof BlockDriedGhast || block instanceof BlockAquaticPlant) && net.minecraftforge.client.ForgeHooksClient.getWorldRenderPass() == 1) {
			if (block instanceof BlockCampfire ? (world.getBlockMetadata(x, y, z) & 8) == 0 : block instanceof BlockDriedGhast ? (world.getBlockMetadata(x, y, z) & 4) == 0 : !((BlockAquaticPlant) block).submerged) return false;
			IBlockAccess previous = renderer.blockAccess;
			try { renderer.blockAccess = new ContainedWaterAccess(world, x, y, z); return renderer.renderBlockLiquid(net.minecraft.init.Blocks.water, x, y, z); }
			finally { renderer.blockAccess = previous; }
		}
		if(block instanceof BlockBookStorage){((BlockBookStorage)block).render(world,x,y,z,renderer.hasOverrideBlockTexture()?renderer.overrideBlockTexture:null);return true;}
        ReleasedBlockModel selected = block instanceof BlockKelp kelp ? kelp.getReleasedModel(world.getBlock(x, y + 1, z) != block) : model(block, world.getBlockMetadata(x, y, z));
		selected.render(block, world, x, y, z, renderer.hasOverrideBlockTexture() ? renderer.overrideBlockTexture : null);
		return true;
	}
	@Override public void renderInventoryBlock(Block block, int meta, int id, RenderBlocks renderer) {
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glPushMatrix();
		try {
			GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
			GL11.glTranslatef(-.5F, -.5F, -.5F);
			Tessellator.instance.startDrawingQuads();
			model(block, meta).render(block, null, 0, 0, 0, null);
			Tessellator.instance.draw();
		} finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
	}
}
