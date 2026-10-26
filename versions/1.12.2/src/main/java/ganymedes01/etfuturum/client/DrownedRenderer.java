package ganymedes01.etfuturum.client;

import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

public final class DrownedRenderer extends RenderZombie {
    private static final ResourceLocation TEXTURE = new ResourceLocation("etfuturum", "textures/entity/zombie/drowned.png");
    private static final ResourceLocation OUTER = new ResourceLocation("etfuturum", "textures/entity/zombie/drowned_outer_layer.png");
    public DrownedRenderer(RenderManager manager) {
        super(manager);
        layerRenderers.add(0, new LayerRenderer<EntityZombie>() {
            private final ModelZombie overlay = new ModelZombie(.25F, false);
            @Override public void doRenderLayer(EntityZombie entity, float limb, float amount, float partial, float age, float yaw, float pitch, float scale) {
                if (entity.isInvisible()) return;
                bindTexture(OUTER); overlay.setModelAttributes(getMainModel()); overlay.setLivingAnimations(entity, limb, amount, partial);
                overlay.render(entity, limb, amount, age, yaw, pitch, scale);
            }
            @Override public boolean shouldCombineTextures() { return true; }
        });
    }
    @Override protected ResourceLocation getEntityTexture(EntityZombie entity) { return TEXTURE; }
}
