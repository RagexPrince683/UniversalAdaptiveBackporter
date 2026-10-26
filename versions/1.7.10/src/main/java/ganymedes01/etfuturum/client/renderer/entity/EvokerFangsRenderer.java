package ganymedes01.etfuturum.client.renderer.entity;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public class EvokerFangsRenderer extends Render {
	private final ModelRenderer base, upper, lower;
	public EvokerFangsRenderer() { ModelBase model = new ModelBase() { }; model.textureWidth = 64; model.textureHeight = 32; base = new ModelRenderer(model, 0, 0); base.addBox(0, 0, 0, 10, 12, 10); base.setRotationPoint(-5, 24, -5); upper = new ModelRenderer(model, 40, 0); upper.addBox(0, 0, 0, 4, 14, 8); upper.setRotationPoint(6.5F, 0, 1); lower = new ModelRenderer(model, 40, 0); lower.addBox(0, 0, 0, 4, 14, 8); lower.setRotationPoint(3.5F, 0, 9); lower.rotateAngleY = (float) Math.PI; base.addChild(upper); base.addChild(lower); }
	@Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) { if (!((ganymedes01.etfuturum.entities.EntityEvokerFangs) entity).isActive()) return; float progress = ((ganymedes01.etfuturum.entities.EntityEvokerFangs) entity).animationProgress(partial), open = 1 - (float) Math.pow(Math.min(1, progress * 2), 3); upper.rotateAngleZ = (float) Math.PI - open * .35F * (float) Math.PI; lower.rotateAngleZ = (float) Math.PI + open * .35F * (float) Math.PI; float size = progress > .9 ? (1 - progress) * 10 : 1; bindEntityTexture(entity); GL11.glPushMatrix(); GL11.glTranslated(x, y, z); GL11.glRotatef(90 - yaw, 0, 1, 0); GL11.glScalef(-size, -size, size); GL11.glTranslatef(0, -1.5F - (progress + MathHelperSin(progress * 2.7F)) * .45F, 0); base.render(.0625F); GL11.glPopMatrix(); }
	private static float MathHelperSin(float value) { return net.minecraft.util.MathHelper.sin(value); }
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return new ResourceLocation("etfuturum:textures/entity/illager/evoker_fangs.png"); }
}
