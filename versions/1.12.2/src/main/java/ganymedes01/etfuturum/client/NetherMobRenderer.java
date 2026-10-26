package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.UAB;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

final class NetherMobRenderer extends RenderLiving<EntityLiving> {
    private final ResourceLocation texture;private final ModelModernMob model;
    NetherMobRenderer(RenderManager manager,String kind){super(manager,new ModelModernMob(kind.startsWith("piglin")?"piglin":kind.equals("strider")?"strider":"hoglin"),.6F);model=(ModelModernMob)mainModel;texture=texture(kind);if(kind.startsWith("piglin")){addLayer(new LayerBipedArmor(this));addLayer(new Equipment());}if(kind.equals("strider"))addLayer(new Saddle());}
    private static ResourceLocation texture(String name){return new ResourceLocation(UAB.ID,"textures/entity/"+name+".png");}
    @Override protected ResourceLocation getEntityTexture(EntityLiving entity){return entity instanceof EntityStrider&&((EntityStrider)entity).isCold()?texture("strider_cold"):texture;}
    private final class Equipment implements LayerRenderer<EntityLiving> {
        @Override public void doRenderLayer(EntityLiving living,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(living.getHeldItemMainhand().isEmpty())return;GlStateManager.pushMatrix();if(living.isChild()){GlStateManager.translate(0,.75,0);GlStateManager.scale(.5,.5,.5);}model.poseArm(scale);GlStateManager.translate(-.05,.5,-.1);GlStateManager.rotate(-90,1,0,0);GlStateManager.scale(.65,.65,.65);net.minecraft.client.Minecraft.getMinecraft().getItemRenderer().renderItemSide(living,living.getHeldItemMainhand(),ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,false);GlStateManager.popMatrix();}
        @Override public boolean shouldCombineTextures(){return false;}
    }
    private final class Saddle implements LayerRenderer<EntityLiving> {
        @Override public void doRenderLayer(EntityLiving living,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(!((EntityStrider)living).isSaddled())return;bindTexture(texture("strider_saddle"));model.render(living,limb,amount,age,yaw,pitch,scale);}
        @Override public boolean shouldCombineTextures(){return false;}
    }
}
