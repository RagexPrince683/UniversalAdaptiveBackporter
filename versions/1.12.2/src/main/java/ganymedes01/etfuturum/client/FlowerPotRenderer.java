package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFlowerPot;
/** Native pot geometry stays in the chunk model; only its additional plant is rendered here. */
public final class FlowerPotRenderer extends TileEntitySpecialRenderer<TileEntityFlowerPot> {
 @Override public void render(TileEntityFlowerPot tile,double x,double y,double z,float partial,int destroy,float alpha){if(!Features.enabled("mixins.flowerPotFixes")||tile.getFlowerPotItem()==null)return;ItemStack plant=new ItemStack(tile.getFlowerPotItem(),1,tile.getFlowerPotData());if(!FlowerPots.accepts(plant))return;Block block=Block.getBlockFromItem(plant.getItem());bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);GlStateManager.pushMatrix();try{GlStateManager.translate(x+.25,y+.25,z+.25);GlStateManager.scale(.5,.5,.5);GlStateManager.color(1,1,1,alpha);Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(block.getStateFromMeta(plant.getMetadata()),1);}finally{GlStateManager.popMatrix();GlStateManager.color(1,1,1,1);}}
}
