package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.entities.EntityBee;
import ganymedes01.etfuturum.client.model.ModelBee;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
final class BeeRenderer extends RenderLiving<EntityBee> {
    BeeRenderer(RenderManager manager){super(manager,new ModelBee(),.4F);}
    @Override protected ResourceLocation getEntityTexture(EntityBee bee){return ModernMobRenderer.texture("bee/bee"+(bee.isAngry()?"_angry":"")+(bee.hasNectar()?"_nectar":""));}
}
