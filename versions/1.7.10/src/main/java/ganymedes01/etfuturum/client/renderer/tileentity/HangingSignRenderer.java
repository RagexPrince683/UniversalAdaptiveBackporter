package ganymedes01.etfuturum.client.renderer.tileentity;
import ganymedes01.etfuturum.blocks.BlockHangingSign;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public final class HangingSignRenderer extends TileEntitySpecialRenderer {
    private final ModelRenderer board,left,right;
    public HangingSignRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=64;model.textureHeight=32;board=new ModelRenderer(model,0,12);board.addBox(-7,-5,-1,14,10,2);left=new ModelRenderer(model,0,0);left.addBox(-6,-9,0,2,4,0);right=new ModelRenderer(model,6,0);right.addBox(4,-9,0,2,4,0);}
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){if(!(raw.getBlockType() instanceof BlockHangingSign))return;BlockHangingSign block=(BlockHangingSign)raw.getBlockType();TileEntitySign tile=(TileEntitySign)raw;GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT);GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+.5625,z+.5);int meta=tile.getBlockMetadata();float angle=block.ceiling?meta*360/16F:meta==2?180:meta==4?90:meta==5?-90:0;GL11.glRotatef(-angle,0,1,0);if(!block.ceiling)GL11.glTranslatef(0,0,-.375F);bindTexture(new ResourceLocation("etfuturum:textures/entity/signs/hanging/"+block.wood+".png"));GL11.glPushMatrix();GL11.glScalef(1,-1,-1);GL11.glColor4f(1,1,1,1);GL11.glDisable(GL11.GL_CULL_FACE);board.render(1/16F);left.render(1/16F);right.render(1/16F);GL11.glPopMatrix();GL11.glTranslatef(0,.12F,.063F);GL11.glScalef(1F/120,-1F/120,1F/120);for(int i=0;i<4;i++){String text=func_147498_b().trimStringToWidth(tile.signText[i],90);if(i==tile.lineBeingEdited)text="> "+text+" <";func_147498_b().drawString(text,-func_147498_b().getStringWidth(text)/2,i*10-20,0);}}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
}
