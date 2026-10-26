package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

final class DecoratedPotRenderer extends TileEntitySpecialRenderer<DecoratedPotTile> {
    private final ModelRenderer neck,lip,top,bottom;
    DecoratedPotRenderer(){ModelBase model=new ModelBase(){};model.textureWidth=model.textureHeight=32;neck=new ModelRenderer(model,0,0);neck.addBox(4,17,4,8,3,8,-.1F);neck.setRotationPoint(0,37,16);neck.rotateAngleX=(float)Math.PI;lip=new ModelRenderer(model,0,5);lip.addBox(5,20,5,6,1,6,.2F);lip.setRotationPoint(0,37,16);lip.rotateAngleX=(float)Math.PI;top=new ModelRenderer(model,-14,13);top.addBox(1,16,1,14,0,14);bottom=new ModelRenderer(model,-14,13);bottom.addBox(1,0,1,14,0,14);}
    @Override public void render(DecoratedPotTile pot,double x,double y,double z,float partial,int destroyStage,float alpha){GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y,z+.5);GlStateManager.rotate(pot.hasWorld()?pot.getBlockMetadata()%4*90:0,0,1,0);GlStateManager.translate(-.5,0,-.5);GlStateManager.color(1,1,1,alpha);GlStateManager.enableCull();bindTexture(new ResourceLocation(UAB.ID,"textures/entity/decorated_pot/decorated_pot_base.png"));neck.render(1/16F);lip.render(1/16F);top.render(1/16F);bottom.render(1/16F);int[] order={0,1,3,2};for(int side=0;side<4;side++){String design=pot.designs[order[side]],pattern=design.isEmpty()?"":new ResourceLocation(design).getPath().replace("_pottery_sherd","");if(!Content.ITEMS.containsKey(pattern+"_pottery_sherd"))pattern="";bindTexture(new ResourceLocation(UAB.ID,"textures/entity/decorated_pot/"+(pattern.isEmpty()?"decorated_pot_side":pattern+"_pottery_pattern")+".png"));GlStateManager.pushMatrix();GlStateManager.translate(.5,0,.5);GlStateManager.rotate(side*90,0,1,0);GlStateManager.translate(-.5,0,-.5);BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_NORMAL);b.pos(.9375,1,.0625).tex(.9375,0).normal(0,0,-1).endVertex();b.pos(.9375,0,.0625).tex(.9375,1).normal(0,0,-1).endVertex();b.pos(.0625,0,.0625).tex(.0625,1).normal(0,0,-1).endVertex();b.pos(.0625,1,.0625).tex(.0625,0).normal(0,0,-1).endVertex();Tessellator.getInstance().draw();GlStateManager.popMatrix();}}finally{GlStateManager.popMatrix();GlStateManager.color(1,1,1,1);}}
}
