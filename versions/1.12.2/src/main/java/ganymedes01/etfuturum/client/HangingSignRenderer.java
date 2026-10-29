package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
public final class HangingSignRenderer extends TileEntitySpecialRenderer<HangingSigns.HangingTile> {
    private final ModelRenderer board,left,right;
    public HangingSignRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=64;model.textureHeight=32;board=new ModelRenderer(model,0,12);board.addBox(-7,-5,-1,14,10,2);left=new ModelRenderer(model,0,0);left.addBox(-6,-9,0,2,4,0);right=new ModelRenderer(model,6,0);right.addBox(4,-9,0,2,4,0);}
    @Override public void render(HangingSigns.HangingTile tile,double x,double y,double z,float partial,int destroy,float alpha){if(!(tile.getBlockType() instanceof WoodSigns.Sign))return;WoodSigns.Sign block=(WoodSigns.Sign)tile.getBlockType();GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y+.5625,z+.5);int meta=tile.getBlockMetadata();float angle=block.standing()?meta*360/16F:meta==2?180:meta==4?90:meta==5?-90:0;GlStateManager.rotate(-angle,0,1,0);if(!block.standing())GlStateManager.translate(0,0,-.375);bindTexture(new ResourceLocation(UAB.ID,"textures/entity/signs/hanging/"+block.wood()+".png"));GlStateManager.pushMatrix();GlStateManager.scale(1,-1,-1);GlStateManager.color(1,1,1,alpha);GlStateManager.disableCull();board.render(1/16F);left.render(1/16F);right.render(1/16F);GlStateManager.enableCull();GlStateManager.popMatrix();GlStateManager.translate(0,.12,.063);GlStateManager.scale(1F/120,-1F/120,1F/120);for(int i=0;i<4;i++){String text=getFontRenderer().trimStringToWidth(tile.signText[i].getUnformattedText(),90);if(i==tile.lineBeingEdited)text="> "+text+" <";getFontRenderer().drawString(text,-getFontRenderer().getStringWidth(text)/2,i*10-20,0);}}finally{GlStateManager.popMatrix();}}
}
