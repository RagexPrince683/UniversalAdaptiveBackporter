package ganymedes01.etfuturum.client.renderer.tileentity;
import ganymedes01.etfuturum.blocks.BlockBookStorage;
import ganymedes01.etfuturum.tileentities.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
public final class StorageDisplayRenderer extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial){boolean campfire=tile instanceof TileEntityCampfire;if(!campfire&&(!(tile.getBlockType() instanceof BlockBookStorage)||((BlockBookStorage)tile.getBlockType()).books))return;GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y,z+.5);GL11.glRotatef((tile.getBlockMetadata()&3)*90,0,1,0);for(int i=0;i<(campfire?4:3);i++){ItemStack stack=campfire?((TileEntityCampfire)tile).displayed(i):((TileEntityBookStorage)tile).getStackInSlot(i);if(stack==null)continue;GL11.glPushMatrix();GL11.glTranslated(campfire?(i%2==0?-.2:.2):(i-1)/3.0,campfire?.455:.5,campfire?(i<2?-.2:.2):.22);if(campfire)GL11.glRotatef(90,1,0,0);GL11.glScaled(.32,.32,.32);GL11.glColor4f(1,1,1,1);Minecraft.getMinecraft().entityRenderer.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer,stack,0);GL11.glPopMatrix();}}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
}
