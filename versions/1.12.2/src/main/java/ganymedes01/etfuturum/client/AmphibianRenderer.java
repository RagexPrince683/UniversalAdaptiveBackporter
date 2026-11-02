package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.UAB;
import ganymedes01.etfuturum.entities.EntityFrog;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
/** The maintained amphibian geometry and textures, baked once into native model parts. */
final class AmphibianRenderer extends RenderLiving<EntityLiving> {
    private static final ResourceLocation[] FROGS={texture("temperate_frog"),texture("warm_frog"),texture("cold_frog")};
    private static final ResourceLocation TADPOLE=texture("tadpole");
    private static ResourceLocation texture(String name){return new ResourceLocation(UAB.ID,"textures/entity/"+name+".png");}
    AmphibianRenderer(RenderManager manager,boolean tadpole){super(manager,new ganymedes01.etfuturum.client.model.ModelModernMob(tadpole ? "tadpole" : "frog"),.3F);}
    @Override protected ResourceLocation getEntityTexture(EntityLiving entity){return entity instanceof EntityFrog?FROGS[Math.min(2,((EntityFrog)entity).getVariant())]:TADPOLE;}
}
