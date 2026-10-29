package ganymedes01.etfuturum.client;

import java.io.IOException;
import ganymedes01.etfuturum.CrafterContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class CrafterGui extends GuiContainer {
    private final CrafterContainer container;
    public CrafterGui(CrafterContainer container){super(container);this.container=container;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/dispenser.png"));drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);for(int slot=0;slot<9;slot++)if(container.tile.disabled(slot)){int x=guiLeft+62+slot%3*18,y=guiTop+17+slot/3*18;drawRect(x,y,x+16,y+16,0xff777777);for(int i=3;i<13;i++){drawRect(x+i,y+i,x+i+1,y+i+1,0xffbcbcbc);drawRect(x+15-i,y+i,x+16-i,y+i+1,0xffbcbcbc);}}}
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){String title=I18n.format("container.crafter");fontRenderer.drawString(title,(xSize-fontRenderer.getStringWidth(title))/2,6,0x404040);fontRenderer.drawString(mc.player.inventory.getDisplayName().getUnformattedText(),8,72,0x404040);}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0&&mc.player.inventory.getItemStack().isEmpty()){int column=(x-guiLeft-62)/18,row=(y-guiTop-17)/18;if(x>=guiLeft+62&&y>=guiTop+17&&column<3&&row<3&&(x-guiLeft-62)%18<16&&(y-guiTop-17)%18<16){int slot=column+row*3;if(container.enchantItem(mc.player,slot)){mc.playerController.sendEnchantPacket(container.windowId,slot);return;}}}super.mouseClicked(x,y,button);}
    @Override public void drawScreen(int x,int y,float partial){drawDefaultBackground();super.drawScreen(x,y,partial);renderHoveredToolTip(x,y);}
}
