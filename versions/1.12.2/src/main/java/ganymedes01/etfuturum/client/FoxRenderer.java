package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.entities.EntityFox;
import ganymedes01.etfuturum.client.model.ModelFox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
final class FoxRenderer extends RenderLiving<EntityFox> {
    FoxRenderer(RenderManager manager){super(manager,new ModelFox(),.4F);addLayer(new Mouth());}
    @Override protected ResourceLocation getEntityTexture(EntityFox fox){return ModernMobRenderer.texture("fox/"+(fox.getFoxType()==EntityFox.Type.SNOW?"snow_fox":"fox")+(fox.isPlayerSleeping()?"_sleep":""));}
    @Override protected void applyRotations(EntityFox fox,float age,float yaw,float partial){super.applyRotations(fox,age,yaw,partial);if(fox.isChasing()||fox.isWalking())GlStateManager.rotate(-(fox.prevRotationPitch+partial*(fox.rotationPitch-fox.prevRotationPitch)),1,0,0);}
    private final class Mouth implements LayerRenderer<EntityFox> {
        @Override public void doRenderLayer(EntityFox fox,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(fox.getHeldItemMainhand().isEmpty())return;ModelFox model=(ModelFox)mainModel;GlStateManager.pushMatrix();if(fox.isChild()){GlStateManager.scale(.75,.75,.75);GlStateManager.translate(0,.5,.209375);}model.head.postRender(scale);GlStateManager.translate(0,.3,-.35);GlStateManager.rotate(90,1,0,0);GlStateManager.scale(.5,.5,.5);Minecraft.getMinecraft().getItemRenderer().renderItem(fox,fox.getHeldItemMainhand(),ItemCameraTransforms.TransformType.GROUND);GlStateManager.popMatrix();}
        @Override public boolean shouldCombineTextures(){return false;}
    }
}
