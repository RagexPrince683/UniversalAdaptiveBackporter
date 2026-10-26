package ganymedes01.etfuturum.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
final class BrownMooshroomRenderer extends RenderMooshroom {
    BrownMooshroomRenderer(RenderManager manager){super(manager);layerRenderers.removeIf(layer->layer instanceof LayerMooshroomMushroom);addLayer(new LayerRenderer<EntityMooshroom>(){
        @Override public void doRenderLayer(EntityMooshroom entity,float limb,float amount,float partial,float age,float yaw,float pitch,float scale){if(entity.isChild()||entity.isInvisible())return;bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);GlStateManager.enableCull();for(int i=0;i<3;i++){GlStateManager.pushMatrix();if(i==2){((ModelQuadruped)getMainModel()).head.postRender(scale);GlStateManager.translate(0,-.7,-.1);}else GlStateManager.translate(i==0?.2:-.2,-.35,i==0?.5:0);GlStateManager.rotate(42,0,1,0);GlStateManager.scale(.5,-.5,.5);GlStateManager.translate(-.5,0,-.5);Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(Blocks.BROWN_MUSHROOM.getDefaultState(),1);GlStateManager.popMatrix();}GlStateManager.disableCull();}
        @Override public boolean shouldCombineTextures(){return false;}
    });}
    @Override protected ResourceLocation getEntityTexture(EntityMooshroom entity){return ModernMobRenderer.texture("cow/brown_mooshroom");}
}
