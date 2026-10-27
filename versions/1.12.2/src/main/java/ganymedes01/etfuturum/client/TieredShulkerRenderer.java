package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.tileentity.TileEntityShulkerBoxRenderer;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.ResourceLocation;
/** Native animated geometry and orientation; only the maintained tier skin changes. */
public final class TieredShulkerRenderer extends TileEntityShulkerBoxRenderer {
 private TileEntityShulkerBox rendering;
 public TieredShulkerRenderer(){super(new ModelShulker());}
 @Override public void render(TileEntityShulkerBox tile,double x,double y,double z,float partial,int destroy,float alpha){TileEntityShulkerBox previous=rendering;rendering=tile;try{boolean crystal=StorageTier.of(tile)==StorageTier.CRYSTAL;if(crystal){net.minecraft.client.renderer.GlStateManager.enableBlend();net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770,771,1,0);}super.render(tile,x,y,z,partial,destroy,alpha);if(crystal&&destroy<0)CrystalStorageRenderer.preview(tile,x,y,z);if(crystal)net.minecraft.client.renderer.GlStateManager.disableBlend();}finally{rendering=previous;}}
 @Override protected void bindTexture(ResourceLocation texture){
  if(rendering!=null&&!texture.getPath().contains("destroy_stage")){
   StorageTier tier=StorageTier.of(rendering);boolean undyed=rendering instanceof UndyedShulkerTile;
   if(tier!=StorageTier.WOOD){String color=undyed?"vanilla":rendering.getColor()==net.minecraft.item.EnumDyeColor.SILVER?"light_gray":rendering.getColor().getName();texture=new ResourceLocation("ironshulkerbox","textures/model/"+color+"/shulker_"+(undyed?"":color+"_")+tier.id()+".png");}
   else if(undyed)texture=new ResourceLocation(UAB.ID,"textures/entity/shulker/shulker.png");
  }
  super.bindTexture(texture);
 }
}
