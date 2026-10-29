package ganymedes01.etfuturum.client.renderer.item;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
public final class BellItemRenderer implements IItemRenderer {
    @Override public boolean handleRenderType(ItemStack item,ItemRenderType type){return true;}
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack item,ItemRendererHelper helper){return type!=ItemRenderType.INVENTORY;}
    @Override public void renderItem(ItemRenderType type,ItemStack item,Object...data){GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);GL11.glPushMatrix();try{Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/items/bell.png"));GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_ALPHA_TEST);if(type==ItemRenderType.INVENTORY){GL11.glDisable(GL11.GL_LIGHTING);Tessellator tess=Tessellator.instance;tess.startDrawingQuads();tess.addVertexWithUV(0,16,0,0,1);tess.addVertexWithUV(16,16,0,1,1);tess.addVertexWithUV(16,0,0,1,0);tess.addVertexWithUV(0,0,0,0,0);tess.draw();}else ItemRenderer.renderItemIn2D(Tessellator.instance,1,0,0,1,16,16,.0625F);}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
}
