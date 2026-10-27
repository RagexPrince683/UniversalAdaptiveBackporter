package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.UAB;
import ganymedes01.etfuturum.entities.EntityGoat;
import ganymedes01.etfuturum.client.model.ModelGoat;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
final class GoatRenderer extends RenderLiving<EntityGoat> {
    GoatRenderer(RenderManager manager){super(manager,new ModelGoat(),.7F);}
    @Override protected ResourceLocation getEntityTexture(EntityGoat goat){return new ResourceLocation(UAB.ID,"textures/entity/goat/goat.png");}
}
