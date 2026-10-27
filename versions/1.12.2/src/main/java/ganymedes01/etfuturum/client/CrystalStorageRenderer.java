package ganymedes01.etfuturum.client;
import java.util.*;
import ganymedes01.etfuturum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockableLoot;
/** Bounded item preview inside the maintained transparent storage skins. */
public final class CrystalStorageRenderer extends TileEntitySpecialRenderer<BarrelTile> {
 @Override public void render(BarrelTile tile,double x,double y,double z,float partial,int destroy,float alpha){if(tile.tier()==StorageTier.CRYSTAL&&destroy<0)preview(tile,x,y,z);}
 static void preview(IInventory inventory,double x,double y,double z){
  if(inventory instanceof TileEntityLockableLoot&&((TileEntityLockableLoot)inventory).getLootTable()!=null)return;
  List<ItemStack> top=new ArrayList<>();for(int i=0;i<inventory.getSizeInventory();i++){ItemStack stack=inventory.getStackInSlot(i);if(stack.isEmpty())continue;int index=0;while(index<top.size()&&top.get(index).getCount()>=stack.getCount())index++;if(index<8){top.add(index,stack);if(top.size()>8)top.remove(8);}}
  if(top.isEmpty())return;GlStateManager.pushMatrix();try{GlStateManager.translate(x+.5,y+.5,z+.5);RenderHelper.enableStandardItemLighting();for(int i=0;i<top.size();i++){GlStateManager.pushMatrix();GlStateManager.translate((i%2-.5)*.42,(i/4-.5)*.42,(i/2%2-.5)*.42);GlStateManager.scale(.28,.28,.28);Minecraft.getMinecraft().getRenderItem().renderItem(top.get(i),ItemCameraTransforms.TransformType.FIXED);GlStateManager.popMatrix();}}finally{GlStateManager.popMatrix();GlStateManager.color(1,1,1,1);}
 }
}
