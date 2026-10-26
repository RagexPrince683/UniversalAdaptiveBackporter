package ganymedes01.etfuturum.client.renderer.tileentity;

import ganymedes01.etfuturum.tileentities.TileEntityBrushable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public final class TileEntityBrushableRenderer extends TileEntitySpecialRenderer {
	private final RenderItem renderer = new RenderItem();
	@Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
		TileEntityBrushable brushable = (TileEntityBrushable) tile; ItemStack stack = brushable.revealedItem(); if (stack == null) return;
		ForgeDirection side = ForgeDirection.getOrientation(brushable.face); double distance = .25 + .04 * brushable.progress;
		EntityItem item = new EntityItem(tile.getWorldObj(), 0, 0, 0, stack.copy()); item.hoverStart = 0;
		GL11.glPushMatrix();
		try { GL11.glTranslated(x + .5 + side.offsetX * distance, y + .35 + side.offsetY * distance, z + .5 + side.offsetZ * distance); GL11.glScalef(.5F, .5F, .5F); renderer.doRender(item, 0, 0, 0, 0, 0); }
		finally { GL11.glPopMatrix(); }
	}
}
