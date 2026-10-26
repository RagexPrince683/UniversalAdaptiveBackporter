package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.entities.EntityGuardian;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuardianRenderer extends RenderLiving {
	private static final ResourceLocation NORMAL = texture("guardian"), ELDER = texture("elder_guardian");
	private static ResourceLocation texture(String name) { return new ResourceLocation("etfuturum", "textures/entity/" + name + ".png"); }
	public GuardianRenderer() { super(new Model(), .5F); }
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return ((EntityGuardian) entity).isElder() ? ELDER : NORMAL; }
	@Override protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
		if (((EntityGuardian) entity).isElder()) GL11.glScalef(2.35F, 2.35F, 2.35F);
	}
	@Override public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		super.doRender(entity, x, y, z, yaw, partialTicks);
		EntityGuardian guardian = (EntityGuardian) entity;
		Entity target = guardian.worldObj.getEntityByID(guardian.beamTarget());
		if (guardian.beamTarget() == 0 || target == null) return;
		double dx = target.prevPosX + (target.posX - target.prevPosX) * partialTicks - (guardian.prevPosX + (guardian.posX - guardian.prevPosX) * partialTicks);
		double dy = target.prevPosY + (target.posY - target.prevPosY) * partialTicks + target.height * .5 - (guardian.prevPosY + (guardian.posY - guardian.prevPosY) * partialTicks + guardian.getEyeHeight());
		double dz = target.prevPosZ + (target.posZ - target.prevPosZ) * partialTicks - (guardian.prevPosZ + (guardian.posZ - guardian.prevPosZ) * partialTicks);
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (length < .001) return;
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y + guardian.getEyeHeight(), z);
		GL11.glRotated(Math.atan2(dx, dz) * 180 / Math.PI, 0, 1, 0);
		GL11.glRotated(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI, 1, 0, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING); GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads(); tessellator.setColorRGBA(180, 200, 75, 210);
		for (int face = 0; face < 4; face++) {
			double a = face * Math.PI / 2, b = a + Math.PI / 2;
			double ax = Math.cos(a) * .08, ay = Math.sin(a) * .08, bx = Math.cos(b) * .08, by = Math.sin(b) * .08;
			tessellator.addVertex(ax, ay, 0); tessellator.addVertex(bx, by, 0); tessellator.addVertex(bx, by, length); tessellator.addVertex(ax, ay, length);
		}
		tessellator.draw(); GL11.glPopMatrix(); GL11.glPopAttrib();
	}
	private static class Model extends ModelBase {
		private final ModelRenderer body, eye;
		private final ModelRenderer[] tail = new ModelRenderer[3];
		private final ModelRenderer[] spikes = new ModelRenderer[12];
		private static final float[] SPIKE_X = {0, 0, 8, -8, -8, 8, 8, -8, 0, 0, 8, -8};
		private static final float[] SPIKE_Y = {-8, -8, -8, -8, 0, 0, 0, 0, 8, 8, 8, 8};
		private static final float[] SPIKE_Z = {8, -8, 0, 0, -8, -8, 8, 8, 8, -8, 0, 0};
		Model() {
			textureWidth = 64; textureHeight = 64;
			body = new ModelRenderer(this, 0, 0); body.addBox(-6, -6, -8, 12, 12, 16); body.setRotationPoint(0, 16, 0);
			body.setTextureOffset(0, 28).addBox(-8, -6, -6, 2, 12, 12);
			body.mirror = true;
			body.setTextureOffset(0, 28).addBox(6, -6, -6, 2, 12, 12);
			body.mirror = false;
			body.setTextureOffset(16, 40).addBox(-6, -8, -6, 12, 2, 12);
			body.setTextureOffset(16, 40).addBox(-6, 6, -6, 12, 2, 12);
			eye = new ModelRenderer(this, 8, 0); eye.addBox(-1, -1, 0, 2, 2, 1); eye.setRotationPoint(0, 0, -8.25F); body.addChild(eye);
			float[] rx = {1.75F, .25F, 0, 0, .5F, .5F, .5F, .5F, 1.25F, .75F, 0, 0};
			float[] ry = {0, 0, 0, 0, .25F, 1.75F, 1.25F, .75F, 0, 0, 0, 0};
			float[] rz = {0, 0, .25F, 1.75F, 0, 0, 0, 0, 0, 0, .75F, 1.25F};
			for (int i = 0; i < spikes.length; i++) {
				spikes[i] = new ModelRenderer(this, 0, 0); spikes[i].addBox(-1, -4.5F, -1, 2, 9, 2);
				spikes[i].rotateAngleX = rx[i] * (float) Math.PI;
				spikes[i].rotateAngleY = ry[i] * (float) Math.PI;
				spikes[i].rotateAngleZ = rz[i] * (float) Math.PI;
				body.addChild(spikes[i]);
			}
			tail[0] = new ModelRenderer(this, 40, 0); tail[0].addBox(-2, -2, 0, 4, 4, 8); tail[0].setRotationPoint(0, 0, 7); body.addChild(tail[0]);
			tail[1] = new ModelRenderer(this, 0, 54); tail[1].addBox(0, 0, 0, 3, 3, 7); tail[1].setRotationPoint(-1.5F, -1.5F, 7); tail[0].addChild(tail[1]);
			tail[2] = new ModelRenderer(this, 41, 32); tail[2].addBox(0, 0, 0, 2, 2, 6); tail[2].setRotationPoint(.5F, .5F, 6); tail[1].addChild(tail[2]);
			tail[2].setTextureOffset(25, 19).addBox(1, -3.5F, 3, 1, 9, 9);
		}
		@Override public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) {
			body.rotateAngleY = yaw * (float) Math.PI / 180; body.rotateAngleX = pitch * (float) Math.PI / 180;
			float retract = ((EntityGuardian) entity).isSwimming() ? .25F : 0;
			for (int i = 0; i < spikes.length; i++) {
				float extension = 1 + (float) Math.cos(age * 1.5F + i) * .01F - retract;
				spikes[i].setRotationPoint(SPIKE_X[i] * extension, SPIKE_Y[i] * extension, SPIKE_Z[i] * extension);
			}
			float wave = (float) Math.sin(age * (((EntityGuardian) entity).isSwimming() ? .5 : .15));
			for (int i = 0; i < tail.length; i++) tail[i].rotateAngleY = wave * (i + 1) * .15F;
			body.render(scale);
		}
	}
}
