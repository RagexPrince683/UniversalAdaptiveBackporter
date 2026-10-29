package ganymedes01.etfuturum.client.renderer.tileentity;
import ganymedes01.etfuturum.blocks.BlockBell;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public final class BellRenderer extends TileEntitySpecialRenderer {
    private final ModelRenderer body;
    public BellRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=32;model.textureHeight=32;body=new ModelRenderer(model,0,0);body.addBox(-3,0,-3,6,7,6);ModelRenderer lip=new ModelRenderer(model,0,13);lip.addBox(-4,7,-4,8,2,8);body.addChild(lip);}
    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial){GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+.78,z+.5);GL11.glRotatef((tile.getBlockMetadata()&3)*90,0,1,0);int ringing=((BlockBell.BellTile)tile).ringing;body.rotateAngleX=ringing>0?(float)Math.sin((50-ringing+partial)*.7)*.3F*ringing/50:0;GL11.glScalef(1,-1,-1);bindTexture(new ResourceLocation("etfuturum:textures/entity/bell/bell_body.png"));GL11.glColor4f(1,1,1,1);body.render(1/16F);}finally{GL11.glPopMatrix();}}
}
