package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.entities.EntityFrog;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class AmphibianRenderer extends RenderLiving {
	private static final ResourceLocation[] FROGS = {texture("temperate_frog"), texture("warm_frog"), texture("cold_frog")};
	private static final ResourceLocation TADPOLE = texture("tadpole");
	private static ResourceLocation texture(String name) { return new ResourceLocation("etfuturum", "textures/entity/" + name + ".png"); }
	public AmphibianRenderer(boolean tadpole) { super(new ganymedes01.etfuturum.client.model.ModelModernMob(tadpole ? "tadpole" : "frog"), 0.3F); }
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return entity instanceof EntityFrog ? FROGS[Math.min(2, ((EntityFrog) entity).getVariant())] : TADPOLE; }
}
