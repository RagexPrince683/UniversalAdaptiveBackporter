package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
public final class WoodSignRenderer extends TileEntitySpecialRenderer<WoodSigns.SignTile> {
 private final ModelSign model=new ModelSign();
 @Override public void render(WoodSigns.SignTile tile,double x,double y,double z,float partial,int destroy,float alpha){if(!(tile.getBlockType() instanceof WoodSigns.Sign))return;WoodSigns.Sign block=(WoodSigns.Sign)tile.getBlockType();GlStateManager.pushMatrix();GlStateManager.translate(x+.5,y+.5,z+.5);int meta=tile.getBlockMetadata();if(block.standing()){GlStateManager.rotate(-meta*360/16F,0,1,0);model.signStick.showModel=true;}else{float angle=meta==2?180:meta==4?90:meta==5?-90:0;GlStateManager.rotate(-angle,0,1,0);GlStateManager.translate(0,-.3125,-.4375);model.signStick.showModel=false;}bindTexture(destroy>=0?DESTROY_STAGES[destroy]:new ResourceLocation(UAB.ID,"textures/entity/signs/"+block.wood()+".png"));GlStateManager.pushMatrix();GlStateManager.scale(2F/3,-2F/3,-2F/3);model.renderSign();GlStateManager.popMatrix();if(destroy<0){FontRenderer font=getFontRenderer();GlStateManager.translate(0,1F/3,.0466667);GlStateManager.scale(1F/90,-1F/90,1F/90);GlStateManager.depthMask(false);GlStateManager.color(1,1,1,alpha);for(int i=0;i<tile.signText.length;i++){java.util.List<ITextComponent> lines=net.minecraft.client.gui.GuiUtilRenderComponents.splitText(tile.signText[i],90,font,false,true);String text=lines.isEmpty()?"":lines.get(0).getFormattedText();if(i==tile.lineBeingEdited)text="> "+text+" <";font.drawString(text,-font.getStringWidth(text)/2,i*10-20,0);}GlStateManager.depthMask(true);}GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();}
}
