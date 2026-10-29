package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.BellBlock;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
public final class BellRenderer extends TileEntitySpecialRenderer<BellBlock.BellTile> {
    private final ModelRenderer body;
    public BellRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=32;model.textureHeight=32;body=new ModelRenderer(model,0,0);body.addBox(-3,0,-3,6,7,6);ModelRenderer lip=new ModelRenderer(model,0,13);lip.addBox(-4,7,-4,8,2,8);body.addChild(lip);}
    @Override public void render(BellBlock.BellTile tile,double x,double y,double z,float partial,int destroy,float alpha){GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y+.78,z+.5);GlStateManager.rotate((tile.getBlockMetadata()&3)*90,0,1,0);body.rotateAngleX=tile.ringing>0?(float)Math.sin((50-tile.ringing+partial)*.7)*.3F*tile.ringing/50:0;GlStateManager.scale(1,-1,-1);bindTexture(new ResourceLocation("etfuturum:textures/entity/bell/bell_body.png"));GlStateManager.color(1,1,1,alpha);body.render(1/16F);}finally{GlStateManager.popMatrix();}}
}
