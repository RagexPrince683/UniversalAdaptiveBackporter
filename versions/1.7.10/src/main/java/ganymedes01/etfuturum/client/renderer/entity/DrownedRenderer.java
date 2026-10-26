package ganymedes01.etfuturum.client.renderer.entity;

import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class DrownedRenderer extends RenderBiped {
	private static final ResourceLocation TEXTURE = new ResourceLocation("etfuturum", "textures/entity/zombie/drowned.png");
	private static final ResourceLocation OVERLAY = new ResourceLocation("etfuturum", "textures/entity/zombie/drowned_outer_layer.png");
	private final ModelZombie overlay = new ModelZombie(.25F, false);
	public DrownedRenderer() { super(new ModelZombie(), .5F); }
	@Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
	@Override protected int shouldRenderPass(EntityLivingBase entity, int pass, float partial) { if (pass == 0) { setRenderPassModel(overlay); bindTexture(OVERLAY); return 1; } return super.shouldRenderPass(entity, pass, partial); }
}
