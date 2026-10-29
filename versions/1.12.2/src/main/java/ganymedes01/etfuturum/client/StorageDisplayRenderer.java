package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
public final class StorageDisplayRenderer extends TileEntitySpecialRenderer<TileEntity> {
    @Override public void render(TileEntity tile,double x,double y,double z,float partial,int destroy,float alpha){boolean campfire=tile instanceof CampfireTile;if(!campfire&&(!(tile.getBlockType() instanceof BookStorage)||((BookStorage)tile.getBlockType()).books))return;GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y,z+.5);int rotation=tile.getBlockMetadata()&3;GlStateManager.rotate((campfire?rotation:rotation+2)*90,0,1,0);for(int i=0;i<(campfire?4:3);i++){ItemStack stack=campfire?((CampfireTile)tile).displayed(i):((BookStorage.Storage)tile).getStackInSlot(i);if(stack.isEmpty())continue;GlStateManager.pushMatrix();GlStateManager.translate(campfire?(i%2==0?-.2:.2):(i-1)/3.0,campfire?.455:.5,campfire?(i<2?-.2:.2):.22);if(campfire)GlStateManager.rotate(90,1,0,0);GlStateManager.scale(.32,.32,.32);GlStateManager.color(1,1,1,alpha);Minecraft.getMinecraft().getRenderItem().renderItem(stack,ItemCameraTransforms.TransformType.FIXED);GlStateManager.popMatrix();}}finally{GlStateManager.popMatrix();GL11.glPopAttrib();}}
}
