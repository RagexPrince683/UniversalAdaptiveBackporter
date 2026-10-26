package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.entities.EntityFallingDripstone;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
final class FallingDripstoneRenderer extends Render<EntityFallingDripstone> {
    private final RenderFallingBlock nativeRenderer;
    FallingDripstoneRenderer(RenderManager manager){super(manager);nativeRenderer=new RenderFallingBlock(manager);}
    @Override public boolean shouldRender(EntityFallingDripstone entity,ICamera camera,double x,double y,double z){return entity.isInRangeToRender3d(x,y,z)&&camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox().expand(0,entity.getCount(),0));}
    @Override public void doRender(EntityFallingDripstone entity,double x,double y,double z,float yaw,float partial){try{for(int i=0;i<entity.getCount();i++){entity.renderSegment(i);nativeRenderer.doRender(entity,x,y+i,z,yaw,partial);}}finally{entity.renderSegment(0);}}
    @Override protected ResourceLocation getEntityTexture(EntityFallingDripstone entity){return net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;}
}
