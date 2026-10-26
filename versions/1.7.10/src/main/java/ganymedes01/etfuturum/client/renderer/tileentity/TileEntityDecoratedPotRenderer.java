package ganymedes01.etfuturum.client.renderer.tileentity;

import ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class TileEntityDecoratedPotRenderer extends TileEntitySpecialRenderer {
	private final ModelRenderer neck, lip, top, bottom;
	public TileEntityDecoratedPotRenderer() {
		ModelBase model = new ModelBase() { }; model.textureWidth = 32; model.textureHeight = 32;
		neck = new ModelRenderer(model, 0, 0); neck.addBox(4, 17, 4, 8, 3, 8, -.1F); neck.setRotationPoint(0, 37, 16); neck.rotateAngleX = (float) Math.PI;
		lip = new ModelRenderer(model, 0, 5); lip.addBox(5, 20, 5, 6, 1, 6, .2F); lip.setRotationPoint(0, 37, 16); lip.rotateAngleX = (float) Math.PI;
		top = new ModelRenderer(model, -14, 13); top.addBox(1, 16, 1, 14, 0, 14);
		bottom = new ModelRenderer(model, -14, 13); bottom.addBox(1, 0, 1, 14, 0, 14);
	}
	@Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
		TileEntityDecoratedPot pot = (TileEntityDecoratedPot) tile;
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glPushMatrix();
		try {
			GL11.glTranslated(x + .5, y, z + .5); GL11.glRotatef(pot.hasWorldObj() ? (pot.getBlockMetadata() & 3) * 90 : 0, 0, 1, 0); GL11.glTranslatef(-.5F, 0, -.5F);
			GL11.glColor4f(1, 1, 1, 1); GL11.glEnable(GL11.GL_CULL_FACE);
			bindTexture(new ResourceLocation("etfuturum", "textures/entity/decorated_pot/decorated_pot_base.png")); neck.render(1F / 16); lip.render(1F / 16); top.render(1F / 16); bottom.render(1F / 16);
			// Each side has its own released design sheet. Opposite sides keep independent identities.
			int[] order = {0, 1, 3, 2};
			for (int side = 0; side < 4; side++) {
				String pattern = pot.designs[order[side]]; String texture = pattern.isEmpty() ? "decorated_pot_side" : pattern + "_pottery_pattern";
				bindTexture(new ResourceLocation("etfuturum", "textures/entity/decorated_pot/" + texture + ".png"));
				GL11.glPushMatrix(); GL11.glTranslatef(.5F, 0, .5F); GL11.glRotatef(side * 90, 0, 1, 0); GL11.glTranslatef(-.5F, 0, -.5F);
				Tessellator t = Tessellator.instance; t.startDrawingQuads(); t.setNormal(0, 0, -1);
				t.addVertexWithUV(.9375, 1, .0625, .9375, 0); t.addVertexWithUV(.9375, 0, .0625, .9375, 1); t.addVertexWithUV(.0625, 0, .0625, .0625, 1); t.addVertexWithUV(.0625, 1, .0625, .0625, 0); t.draw(); GL11.glPopMatrix();
			}
		} finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
	}
}
