package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.LecternTile;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
public final class LecternRenderer extends TileEntitySpecialRenderer<LecternTile> {
    private final ModelBook book=new ModelBook();
    @Override public void render(LecternTile tile,double x,double y,double z,float partial,int destroy,float alpha){if(tile.isEmpty())return;GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y+1.0625,z+.5);GlStateManager.rotate(270-(tile.getBlockMetadata()&3)*90,0,1,0);GlStateManager.rotate(67.5F,0,0,1);GlStateManager.translate(0,-.125,0);bindTexture(new ResourceLocation("textures/entity/enchanting_table_book.png"));GlStateManager.color(1,1,1,alpha);book.render(null,0,.1F,.9F,1.2F,0,1/16F);}finally{GlStateManager.popMatrix();}}
}
