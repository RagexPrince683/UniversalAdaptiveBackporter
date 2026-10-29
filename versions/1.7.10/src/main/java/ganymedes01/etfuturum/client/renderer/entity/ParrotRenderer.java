package ganymedes01.etfuturum.client.renderer.entity;

import ganymedes01.etfuturum.client.model.ModelParrot;
import ganymedes01.etfuturum.entities.EntityParrot;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public final class ParrotRenderer extends RenderLiving {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[5];
    static { String[] names = {"red_blue", "blue", "green", "yellow_blue", "grey"}; for (int i=0; i<5; i++) TEXTURES[i]=new ResourceLocation("etfuturum", "textures/entity/parrot/parrot_"+names[i]+".png"); }
    public ParrotRenderer() { super(new ModelParrot(), .3F); }
    @Override public void doRender(EntityLiving entity,double x,double y,double z,float yaw,float partial) {
        EntityParrot parrot=(EntityParrot)entity;
        Minecraft client=Minecraft.getMinecraft();
        // Perched parrots remain tracked world entities in this target. Hide only
        // the camera player's own shoulder birds, as the native shoulder layer does.
        if(client.thePlayer!=null&&client.gameSettings.thirdPersonView==0&&parrot.shoulderOwner()==client.thePlayer&&client.renderViewEntity==client.thePlayer)return;
        super.doRender(entity,x,y,z,yaw,partial);
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURES[((EntityParrot)entity).getVariant()]; }
    @Override protected float handleRotationFloat(EntityLivingBase entity, float partial) {
        EntityParrot parrot=(EntityParrot)entity;
        return (MathHelper.sin(parrot.previousFlap+(parrot.flap-parrot.previousFlap)*partial)+1)
                * (parrot.previousFlapSpeed+(parrot.flapSpeed-parrot.previousFlapSpeed)*partial);
    }
}
