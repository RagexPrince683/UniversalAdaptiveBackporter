package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
/** Maintained 9/12/15-column layout, with the original metal-storage textures. */
public final class StorageGui extends GuiContainer {
 private final ResourceLocation texture;
 public StorageGui(EntityPlayer player,IInventory inventory){super(new StorageContainer(player.inventory,inventory,player));StorageTier tier=StorageTier.of(inventory);xSize=22+tier.columns*18;ySize=94+tier.size/tier.columns*18;texture=new ResourceLocation(UAB.ID,"textures/gui/container/ironbarrels/"+tier.gui+"container.png");}
 @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){GlStateManager.color(1,1,1,1);mc.getTextureManager().bindTexture(texture);drawModalRectWithCustomSizedTexture(guiLeft,guiTop,0,0,xSize,ySize,xSize>256?xSize:256,256);}
 @Override public void drawScreen(int mouseX,int mouseY,float partial){drawDefaultBackground();super.drawScreen(mouseX,mouseY,partial);renderHoveredToolTip(mouseX,mouseY);}
}
