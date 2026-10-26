package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.entities.EntityTrident;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class TridentRenderer extends Render<EntityTrident> {
    private final TridentModel model = new TridentModel();
    public TridentRenderer(RenderManager manager) {
        super(manager);
    }
    @Override protected ResourceLocation getEntityTexture(EntityTrident entity) { return TridentModel.TEXTURE; }
    @Override public void doRender(EntityTrident entity, double x, double y, double z, float yaw, float partial) {
        bindEntityTexture(entity); GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial - 90, 0, 1, 0);
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partial + 90, 0, 0, 1);
            model.render();
        } finally { GlStateManager.popMatrix(); }
        super.doRender(entity, x, y, z, yaw, partial);
    }
}
