package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.SpyglassItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public final class SpyglassView {
    private boolean active(){Minecraft mc=Minecraft.getMinecraft();return mc.player!=null&&mc.gameSettings.thirdPersonView==0&&mc.player.isHandActive()&&mc.player.getActiveItemStack().getItem() instanceof SpyglassItem;}
    @SubscribeEvent public void fov(FOVUpdateEvent event){if(active())event.setNewfov(event.getNewfov()*.1F);}
    @SubscribeEvent public void crosshair(RenderGameOverlayEvent.Pre event){if(event.getType()==RenderGameOverlayEvent.ElementType.CROSSHAIRS&&active())event.setCanceled(true);}
    @SubscribeEvent public void overlay(RenderGameOverlayEvent.Post event){if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL||!active())return;int w=event.getResolution().getScaledWidth(),h=event.getResolution().getScaledHeight(),size=Math.min(w,h),x=(w-size)/2,y=(h-size)/2;Gui.drawRect(0,0,x,h,0xff000000);Gui.drawRect(x+size,0,w,h,0xff000000);Gui.drawRect(x,0,x+size,y,0xff000000);Gui.drawRect(x,y+size,x+size,h,0xff000000);Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/misc/spyglass_scope.png"));GlStateManager.color(1,1,1,1);GlStateManager.enableBlend();GlStateManager.tryBlendFuncSeparate(770,771,1,0);GlStateManager.disableDepth();Tessellator tess=Tessellator.getInstance();BufferBuilder b=tess.getBuffer();b.begin(7,DefaultVertexFormats.POSITION_TEX);b.pos(x,y+size,0).tex(0,1).endVertex();b.pos(x+size,y+size,0).tex(1,1).endVertex();b.pos(x+size,y,0).tex(1,0).endVertex();b.pos(x,y,0).tex(0,0).endVertex();tess.draw();GlStateManager.enableDepth();GlStateManager.disableBlend();}
}
