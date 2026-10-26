package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.entities.EntityFrog;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class AmphibianRenderer extends RenderLiving {
	private static final ResourceLocation[] FROGS = {texture("temperate_frog"), texture("warm_frog"), texture("cold_frog")};
	private static final ResourceLocation TADPOLE = texture("tadpole");
	private static ResourceLocation texture(String name) { return new ResourceLocation("etfuturum", "textures/entity/" + name + ".png"); }
	public AmphibianRenderer(boolean tadpole) { super(new Model(tadpole), 0.3F); }
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return entity instanceof EntityFrog ? FROGS[((EntityFrog) entity).getVariant()] : TADPOLE; }
	private static class Model extends ModelBase {
		private final ModelRenderer body;
		Model(boolean tadpole) {
			textureWidth = 48; textureHeight = 48;
			body = new ModelRenderer(this, 3, 1);
			body.addBox(tadpole ? -1.5F : -3.5F, tadpole ? -3 : -5, tadpole ? -3 : -4, tadpole ? 3 : 7, tadpole ? 3 : 5, tadpole ? 6 : 9);
			body.setRotationPoint(0, 24, 0);
			if (!tadpole) for (int side : new int[]{-1, 1}) {
				ModelRenderer leg = new ModelRenderer(this, 0, 20); leg.addBox(side * 4 - 2, -2, 1, 4, 2, 6); body.addChild(leg);
				ModelRenderer eye = new ModelRenderer(this, 0, 0); eye.addBox(side * 2 - 1, -7, -3, 2, 2, 3); body.addChild(eye);
			}
		}
		@Override public void render(Entity entity, float swing, float amount, float age, float yaw, float pitch, float scale) { body.rotateAngleY = yaw * (float)Math.PI / 180; body.render(scale); }
	}
}
