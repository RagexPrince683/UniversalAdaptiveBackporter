package ganymedes01.etfuturum.client.renderer.tileentity;
import ganymedes01.etfuturum.tileentities.TileEntityLectern;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public final class LecternRenderer extends TileEntitySpecialRenderer {
    private final ModelBook book=new ModelBook();
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){TileEntityLectern tile=(TileEntityLectern)raw;if(tile.getStackInSlot(0)==null)return;GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+1.0625,z+.5);GL11.glRotatef(90-(tile.getBlockMetadata()&3)*90,0,1,0);GL11.glRotatef(67.5F,0,0,1);GL11.glTranslatef(0,-.125F,0);bindTexture(new ResourceLocation("textures/entity/enchanting_table_book.png"));GL11.glColor4f(1,1,1,1);book.render(null,0,.1F,.9F,1.2F,0,1/16F);}finally{GL11.glPopMatrix();}}
}
