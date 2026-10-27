package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
public final class NewBoatRenderer extends Render<EntityNewBoat> {
 private final ModelNewBoat boat=new ModelNewBoat(),raft=new ModelRaft();private final TileEntityChest chest=new TileEntityChest();
 public NewBoatRenderer(RenderManager manager){super(manager);shadowSize=.5F;}
 private void transform(EntityNewBoat entity,double x,double y,double z,float yaw,float partial){GlStateManager.translate(x,y+.375,z);GlStateManager.rotate(180-yaw,0,1,0);float hurt=entity.getTimeSinceHit()-partial,damage=Math.max(0,entity.getDamageTaken()-partial);if(hurt>0)GlStateManager.rotate(MathHelper.sin(hurt)*hurt*damage/10*entity.getForwardDirection(),1,0,0);GlStateManager.scale(-1,-1,1);}
 @Override public void doRender(EntityNewBoat entity,double x,double y,double z,float yaw,float partial){GlStateManager.pushMatrix();transform(entity,x,y,z,yaw,partial);bindEntityTexture(entity);(entity.isRaft()?raft:boat).render(entity,partial,0,-.1F,0,0,.0625F);if(entity instanceof EntityNewBoatWithChest){EntityNewBoatWithChest contents=(EntityNewBoatWithChest)entity;GlStateManager.scale(-1,-1,1);GlStateManager.rotate(180,0,1,0);GlStateManager.scale(.8,.8,.8);GlStateManager.translate(contents.getChestXOffset(),contents.getChestHeight(),contents.getChestZOffset());TileEntityRendererDispatcher.instance.render(chest,0,0,0,partial);}GlStateManager.popMatrix();super.doRender(entity,x,y,z,yaw,partial);}
 @Override public boolean isMultipass(){return true;}
 @Override public void renderMultipass(EntityNewBoat entity,double x,double y,double z,float yaw,float partial){if(entity.isRaft())return;GlStateManager.pushMatrix();transform(entity,x,y,z,yaw,partial);bindEntityTexture(entity);boat.renderMultipass(entity,partial,0,-.1F,0,0,.0625F);GlStateManager.popMatrix();}
 @Override protected ResourceLocation getEntityTexture(EntityNewBoat entity){return entity.getResourceLocation();}
}
