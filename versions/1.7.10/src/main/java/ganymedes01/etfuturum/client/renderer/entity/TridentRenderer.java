package ganymedes01.etfuturum.client.renderer.entity;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TridentRenderer extends Render {
	private static final ResourceLocation TEXTURE = new ResourceLocation("etfuturum", "textures/entity/trident.png");
	private final ModelRenderer model;
	public TridentRenderer() {
		ModelBase base = new ModelBase() { }; base.textureWidth = base.textureHeight = 32;
		model = new ModelRenderer(base, 0, 6); model.addBox(-.5F, 2, -.5F, 1, 25, 1);
		ModelRenderer cross = new ModelRenderer(base, 4, 0); cross.addBox(-1.5F, 0, -.5F, 3, 2, 1); model.addChild(cross);
		for (int x : new int[]{-2, 0, 2}) { ModelRenderer spike = new ModelRenderer(base, x == 0 ? 0 : 4, x == 0 ? 0 : 3); spike.addBox(x - .5F, x == 0 ? -4 : -3, -.5F, 1, 4, 1); model.addChild(spike); }
	}
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
	@Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
		bindEntityTexture(entity); GL11.glPushMatrix();
		try { GL11.glTranslated(x, y, z); GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial - 90, 0, 1, 0); GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial + 90, 0, 0, 1); model.render(.0625F); }
		finally { GL11.glPopMatrix(); }
	}
}
