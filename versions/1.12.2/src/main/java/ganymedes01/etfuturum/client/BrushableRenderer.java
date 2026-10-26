package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.BrushableTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
final class BrushableRenderer extends TileEntitySpecialRenderer<BrushableTile> {
    @Override public void render(BrushableTile tile,double x,double y,double z,float partial,int destroyStage,float alpha){ItemStack stack=tile.revealedItem();if(stack.isEmpty())return;double distance=.25+.04*tile.progress;GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5+tile.face.getXOffset()*distance,y+.35+tile.face.getYOffset()*distance,z+.5+tile.face.getZOffset()*distance);GlStateManager.scale(.5,.5,.5);GlStateManager.rotate(tile.face.getHorizontalIndex()<0?0:tile.face.getHorizontalAngle(),0,1,0);Minecraft.getMinecraft().getRenderItem().renderItem(stack,ItemCameraTransforms.TransformType.FIXED);}finally{GlStateManager.popMatrix();}}
}
