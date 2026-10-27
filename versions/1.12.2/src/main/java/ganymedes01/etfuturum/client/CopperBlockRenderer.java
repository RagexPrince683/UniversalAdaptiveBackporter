package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.client.model.ModelModernMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
final class CopperBlockRenderer extends TileEntitySpecialRenderer<TileEntity> {
    private final boolean statue;
    private final ModelChest chest=new ModelChest();
    private final ModelModernMob[] poses;
    CopperBlockRenderer(boolean statue){this.statue=statue;poses=statue?new ModelModernMob[]{new ModelModernMob("copper_golem"),new ModelModernMob("copper_statue_running"),new ModelModernMob("copper_statue_sitting"),new ModelModernMob("copper_statue_star")}:null;}
    @Override public void render(TileEntity tile,double x,double y,double z,float partial,int destroyStage,float alpha){int meta=tile.hasWorld()?tile.getBlockMetadata():0;float lid=tile instanceof CopperChestTile?((CopperChestTile)tile).prevLidAngle+(((CopperChestTile)tile).lidAngle-((CopperChestTile)tile).prevLidAngle)*partial:0;draw(x,y,z,meta,lid);}
    void item(ItemStack stack){draw(0,0,0,stack.getMetadata(),0);}
    private void draw(double x,double y,double z,int meta,float lid){GlStateManager.pushMatrix();GlStateManager.enableRescaleNormal();try{GlStateManager.translate(x+.5,y+(statue?1.5:1),z+.5);GlStateManager.scale(1,-1,-1);GlStateManager.color(1,1,1,1);if(statue){Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/entity/copper_golem/oxidized_copper_golem.png"));poses[meta&3].render(null,0,0,0,0,0,.0625F);}else{Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(UAB.ID,"textures/entity/chest/copper.png"));GlStateManager.rotate((meta&3)*90,0,1,0);GlStateManager.translate(-.5,0,-.5);lid=1-lid;chest.chestLid.rotateAngleX=-(1-lid*lid*lid)*(float)Math.PI/2;chest.renderAll();}}finally{GlStateManager.disableRescaleNormal();GlStateManager.popMatrix();}}
}
