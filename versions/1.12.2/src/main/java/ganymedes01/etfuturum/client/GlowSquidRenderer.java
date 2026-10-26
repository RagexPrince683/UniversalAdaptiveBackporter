package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.UAB;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;

final class GlowSquidRenderer extends RenderSquid {
    GlowSquidRenderer(RenderManager manager){super(manager);}
    @Override protected ResourceLocation getEntityTexture(EntitySquid squid){return new ResourceLocation(UAB.ID,"textures/entity/squid/glow_squid.png");}
}
