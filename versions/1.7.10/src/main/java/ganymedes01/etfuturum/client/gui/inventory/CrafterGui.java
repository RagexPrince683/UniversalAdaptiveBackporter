package ganymedes01.etfuturum.client.gui.inventory;

import java.util.Collections;
import ganymedes01.etfuturum.inventory.ContainerCrafter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Vanilla Crafter layout; the server synchronizes the read-only recipe preview. */
public final class CrafterGui extends GuiContainer {
    private static final ResourceLocation BACKGROUND=new ResourceLocation(ganymedes01.etfuturum.Tags.MC_ASSET_VER,"textures/gui/container/crafter.png");
    private static final ResourceLocation DISABLED=new ResourceLocation(ganymedes01.etfuturum.Tags.MC_ASSET_VER,"textures/gui/sprites/container/crafter/disabled_slot.png");
    private static final ResourceLocation POWERED=new ResourceLocation(ganymedes01.etfuturum.Tags.MC_ASSET_VER,"textures/gui/sprites/container/crafter/powered_redstone.png");
    private static final ResourceLocation UNPOWERED=new ResourceLocation(ganymedes01.etfuturum.Tags.MC_ASSET_VER,"textures/gui/sprites/container/crafter/unpowered_redstone.png");
    private final ContainerCrafter container;
    public CrafterGui(ContainerCrafter container){super(container);this.container=container;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BACKGROUND);drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);
        mc.getTextureManager().bindTexture(DISABLED);
        for(int slot=0;slot<9;slot++)if(container.tile.disabled(slot))func_146110_a(guiLeft+25+slot%3*18,guiTop+16+slot/3*18,0,0,18,18,18,18);
        mc.getTextureManager().bindTexture((container.tile.getBlockMetadata()&8)!=0?POWERED:UNPOWERED);
        func_146110_a(guiLeft+97,guiTop+35,0,0,16,16,16,16);
    }
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){String title=I18n.format("container.crafter");fontRendererObj.drawString(title,(xSize-fontRendererObj.getStringWidth(title))/2,6,0x404040);fontRendererObj.drawString(I18n.format("container.inventory"),8,72,0x404040);}
    @Override protected void mouseClicked(int x,int y,int button){
        if(button==0&&mc.thePlayer.inventory.getItemStack()==null){
            int column=(x-guiLeft-26)/18,row=(y-guiTop-17)/18;
            if(x>=guiLeft+26&&y>=guiTop+17&&column<3&&row<3&&(x-guiLeft-26)%18<16&&(y-guiTop-17)%18<16){int slot=column+row*3;if(container.enchantItem(mc.thePlayer,slot)){mc.playerController.sendEnchantPacket(container.windowId,slot);return;}}
        }
        super.mouseClicked(x,y,button);
    }
    @Override public void drawScreen(int x,int y,float partial){
        drawDefaultBackground();super.drawScreen(x,y,partial);
        if(mc.thePlayer.inventory.getItemStack()==null&&x>=guiLeft+26&&x<guiLeft+80&&y>=guiTop+17&&y<guiTop+71){
            int slot=(x-guiLeft-26)/18+(y-guiTop-17)/18*3;
            if(container.tile.getStackInSlot(slot)==null)drawHoveringText(Collections.singletonList(I18n.format(container.tile.disabled(slot)?"gui.etfuturum.crafter.enable":"gui.etfuturum.crafter.disable")),x,y,fontRendererObj);
        }
    }
}
