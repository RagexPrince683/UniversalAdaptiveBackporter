package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.items.ItemSpyglass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
public final class SpyglassView {
    private boolean active(){Minecraft mc=Minecraft.getMinecraft();return mc.thePlayer!=null&&mc.gameSettings.thirdPersonView==0&&mc.thePlayer.isUsingItem()&&mc.thePlayer.getItemInUse().getItem() instanceof ItemSpyglass;}
    @SubscribeEvent public void fov(FOVUpdateEvent event){if(active())event.newfov*=.1F;}
    @SubscribeEvent public void crosshair(RenderGameOverlayEvent.Pre event){if(event.type==RenderGameOverlayEvent.ElementType.CROSSHAIRS&&active())event.setCanceled(true);}
    @SubscribeEvent public void overlay(RenderGameOverlayEvent.Post event){if(event.type!=RenderGameOverlayEvent.ElementType.ALL||!active())return;int w=event.resolution.getScaledWidth(),h=event.resolution.getScaledHeight(),size=Math.min(w,h),x=(w-size)/2,y=(h-size)/2;Gui.drawRect(0,0,x,h,0xff000000);Gui.drawRect(x+size,0,w,h,0xff000000);Gui.drawRect(x,0,x+size,y,0xff000000);Gui.drawRect(x,y+size,x+size,h,0xff000000);Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("etfuturum:textures/misc/spyglass_scope.png"));GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);try{GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(770,771);GL11.glDisable(GL11.GL_DEPTH_TEST);Tessellator b=Tessellator.instance;b.startDrawingQuads();b.addVertexWithUV(x,y+size,0,0,1);b.addVertexWithUV(x+size,y+size,0,1,1);b.addVertexWithUV(x+size,y,0,1,0);b.addVertexWithUV(x,y,0,0,0);b.draw();}finally{GL11.glPopAttrib();}}
}
