package ganymedes01.etfuturum.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
/** Render only actual native percentages and received client chunks. */
public final class TerrainLoading {
 private TerrainLoading(){}
 public static void render(int width,int height,String title,String detail,int progress,boolean chunks){
  Minecraft mc=Minecraft.getMinecraft();Gui.drawRect(0,0,width,height,0xFF202020);int center=width/2,top=height/2-38;
  if(chunks&&mc.world!=null&&mc.player!=null){int radius=MathHelper.clamp(mc.gameSettings.renderDistanceChunks,2,32),size=radius*2+1,scale=Math.max(1,Math.min(4,(height-90)/size)),loaded=0;top=height/2-size*scale/2-24;
   for(int z=-radius;z<=radius;z++)for(int x=-radius;x<=radius;x++){boolean present=mc.world.getChunkProvider().getLoadedChunk(mc.player.chunkCoordX+x,mc.player.chunkCoordZ+z)!=null;if(present)loaded++;int px=center-size*scale/2+(x+radius)*scale,py=top+24+(z+radius)*scale;Gui.drawRect(px,py,px+scale,py+scale,present?0xFFFFFFFF:0xFF545454);}
   progress=loaded*100/(size*size);detail=I18n.format("uab.loading.chunks",loaded,size*size);
  }
  mc.fontRenderer.drawStringWithShadow(title==null?I18n.format("menu.generatingLevel"):title,center-mc.fontRenderer.getStringWidth(title==null?I18n.format("menu.generatingLevel"):title)/2,top,0xFFFFFF);
  int barTop=chunks?height-31:top+22;if(progress>=0){Gui.drawRect(center-100,barTop,center+100,barTop+2,0xFF000000);Gui.drawRect(center-100,barTop,center-100+MathHelper.clamp(progress,0,100)*2,barTop+2,0xFF21C600);}
  if(detail!=null&&!detail.isEmpty())mc.fontRenderer.drawStringWithShadow(detail,center-mc.fontRenderer.getStringWidth(detail)/2,barTop+8,0xFFFFFF);
 }
}
