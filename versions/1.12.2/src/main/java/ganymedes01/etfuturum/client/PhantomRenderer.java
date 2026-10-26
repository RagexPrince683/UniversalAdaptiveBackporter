package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.entities.EntityPhantom;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.opengl.GL11;

public class PhantomRenderer extends RenderLiving<EntityPhantom> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("etfuturum", "textures/entity/phantom.png");
	public PhantomRenderer(RenderManager manager) { super(manager, new Model(), .75F); }
	@Override protected ResourceLocation getEntityTexture(EntityPhantom entity) { return TEXTURE; }
	@Override protected void preRenderCallback(EntityPhantom entity, float partial) { float size = 1 + ((EntityPhantom) entity).getPhantomSize() * .15F; GL11.glScalef(size, size, size); GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial, 1, 0, 0); }
	private static class Model extends ModelBase {
		private final ModelRenderer body, left, right, leftTip, rightTip, tail, tailTip;
		private ModelRenderer box(int u, int v, float x, float y, float z, int w, int h, int d, float px, float py, float pz) { ModelRenderer part = new ModelRenderer(this, u, v); part.addBox(x, y, z, w, h, d); part.setRotationPoint(px, py, pz); return part; }
		Model() {
			textureWidth = textureHeight = 64;
			body = box(0, 8, -3, -2, -8, 5, 3, 9, 0, 20, 0); body.rotateAngleX = -.1F;
			tail = box(3, 20, -2, 0, 0, 3, 2, 6, 0, -2, 1); body.addChild(tail);
			tailTip = box(4, 29, -1, 0, 0, 1, 1, 6, 0, .5F, 6); tail.addChild(tailTip);
			left = box(23, 12, 0, 0, 0, 6, 2, 9, 2, -2, -8); body.addChild(left);
			leftTip = box(16, 24, 0, 0, 0, 13, 1, 9, 6, 0, 0); left.addChild(leftTip);
			right = box(23, 12, -6, 0, 0, 6, 2, 9, -3, -2, -8); body.addChild(right);
			rightTip = box(16, 24, -13, 0, 0, 13, 1, 9, -6, 0, 0); right.addChild(rightTip);
			ModelRenderer head = box(0, 0, -4, -2, -5, 7, 3, 5, 0, 1, -7); head.rotateAngleX = .2F; body.addChild(head);
		}
		@Override public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
			float phase = (age + entity.getEntityId() * 3) * .13F;
			left.rotateAngleZ = leftTip.rotateAngleZ = MathHelper.cos(phase) * .28F; right.rotateAngleZ = rightTip.rotateAngleZ = -left.rotateAngleZ;
			tail.rotateAngleX = tailTip.rotateAngleX = -(.087F + MathHelper.cos(phase * 2) * .087F); body.render(scale);
		}
	}
}
