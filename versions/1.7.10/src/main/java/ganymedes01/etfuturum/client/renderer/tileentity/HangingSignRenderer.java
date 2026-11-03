package ganymedes01.etfuturum.client.renderer.tileentity;
import ganymedes01.etfuturum.blocks.BlockHangingSign;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public final class HangingSignRenderer extends TileEntitySpecialRenderer {
    private final ModelRenderer board,left,right,leftCross,rightCross,beam,bracket;
    public HangingSignRenderer(){
        ModelBase model=new ModelBase(){};model.textureWidth=64;model.textureHeight=32;
        board=new ModelRenderer(model,0,12);board.addBox(-7,-5,-1,14,10,2);
        left=chain(model,-5,.785398F);leftCross=chain(model,-5,-.785398F);
        right=chain(model,5,.785398F);rightCross=chain(model,5,-.785398F);
        beam=new ModelRenderer(model,0,0);beam.addBox(-8,-10,-2,16,2,4);
        bracket=new ModelRenderer(model,0,0);bracket.addBox(-2,-10,0,4,2,8);
    }
    private static ModelRenderer chain(ModelBase model,float x,float angle){ModelRenderer chain=new ModelRenderer(model,0,6);chain.setRotationPoint(x,-10,0);chain.addBox(-1.5F,0,0,3,5,0);chain.rotateAngleY=angle;return chain;}

    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){if(!(raw.getBlockType() instanceof BlockHangingSign))return;BlockHangingSign block=(BlockHangingSign)raw.getBlockType();TileEntitySign tile=(TileEntitySign)raw;GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_COLOR_BUFFER_BIT);GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+.375,z+.5);int meta=tile.getBlockMetadata();float angle=block.ceiling?meta*360/16F:meta==2?180:meta==4?90:meta==5?-90:0;GL11.glRotatef(-angle,0,1,0);bindTexture(new ResourceLocation("etfuturum:textures/entity/signs/hanging/"+block.wood+".png"));GL11.glPushMatrix();GL11.glScalef(1,-1,-1);GL11.glColor4f(1,1,1,1);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);board.render(1/16F);left.render(1/16F);leftCross.render(1/16F);right.render(1/16F);rightCross.render(1/16F);if(!block.ceiling){beam.render(1/16F);bracket.render(1/16F);}GL11.glPopMatrix();GL11.glTranslatef(0,.12F,.063F);GL11.glScalef(1F/120,-1F/120,1F/120);for(int i=0;i<4;i++){String text=func_147498_b().trimStringToWidth(tile.signText[i],90);if(i==tile.lineBeingEdited)text="> "+text+" <";func_147498_b().drawString(text,-func_147498_b().getStringWidth(text)/2,i*10-20,0);}}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
}
