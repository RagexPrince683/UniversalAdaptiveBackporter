package ganymedes01.etfuturum.client;

import java.util.*;
import ganymedes01.etfuturum.LecternContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public final class LecternGui extends GuiContainer {
    private static final ResourceLocation BOOK=new ResourceLocation("textures/gui/book.png");
    private static final int VISIBLE_LINES=14;
    private final LecternContainer container;private int scroll,lastPage=-1;private List<String> lines=Collections.emptyList();private ItemStack lastBook=ItemStack.EMPTY;
    public LecternGui(LecternContainer container){super(container);this.container=container;xSize=192;ySize=224;}
    @Override public void initGui(){
        super.initGui();
        buttonList.add(new PageButton(0,guiLeft+38,guiTop+156,false));
        buttonList.add(new PageButton(1,guiLeft+120,guiTop+156,true));
        buttonList.add(new GuiButton(2,guiLeft+38,guiTop+196,116,20,I18n.format("gui.lectern.take")));
        buttonList.add(new GuiButton(3,guiLeft+10,guiTop+196,20,20,"^"));
        buttonList.add(new GuiButton(4,guiLeft+162,guiTop+196,20,20,"v"));updateScreen();
    }
    @Override public void updateScreen(){super.updateScreen();ItemStack book=container.tile.getStackInSlot(0);int page=container.tile.page;if(lastPage!=page||!ItemStack.areItemStacksEqual(lastBook,book)){lastPage=page;lastBook=book.copy();scroll=0;String text=book.hasTagCompound()?book.getTagCompound().getTagList("pages",8).getStringTagAt(page):"";if(book.getItem()==Items.WRITTEN_BOOK)try{ITextComponent component=ITextComponent.Serializer.jsonToComponent(text);if(component!=null)text=component.getFormattedText();}catch(RuntimeException ignored){}lines=fontRenderer.listFormattedStringToWidth(text,114);}
        for(GuiButton button:buttonList){if(button.id==0)button.visible=button.enabled=page>0;else if(button.id==1)button.visible=button.enabled=page+1<container.tile.pages();else if(button.id==2)button.enabled=!book.isEmpty()&&mc.player.capabilities.allowEdit;else if(button.id==3){button.visible=lines.size()>VISIBLE_LINES;button.enabled=scroll>0;}else if(button.id==4){button.visible=lines.size()>VISIBLE_LINES;button.enabled=scroll+VISIBLE_LINES<lines.size();}}}
    @Override protected void actionPerformed(GuiButton button){if(button.id==3)scroll=Math.max(0,scroll-VISIBLE_LINES);else if(button.id==4)scroll=Math.min(Math.max(0,lines.size()-VISIBLE_LINES),scroll+VISIBLE_LINES);else if(container.enchantItem(mc.player,button.id))mc.playerController.sendEnchantPacket(container.windowId,button.id);updateScreen();}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);drawTexturedModalRect(guiLeft,guiTop,0,0,192,192);}
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){
        for(int i=0;i<VISIBLE_LINES&&scroll+i<lines.size();i++)fontRenderer.drawString(lines.get(scroll+i),36,34+i*9,0);
        String page=I18n.format("book.pageIndicator",container.tile.page+1,container.tile.pages());fontRenderer.drawString(page,156-fontRenderer.getStringWidth(page),18,0);
    }
    @Override public void handleMouseInput() throws java.io.IOException{super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0&&lines.size()>VISIBLE_LINES){scroll=Math.max(0,Math.min(lines.size()-VISIBLE_LINES,scroll+(wheel>0?-VISIBLE_LINES:VISIBLE_LINES)));updateScreen();}}
    @Override public void drawScreen(int x,int y,float partial){drawDefaultBackground();super.drawScreen(x,y,partial);}
    private static final class PageButton extends GuiButton {
        private final boolean forward;
        PageButton(int id,int x,int y,boolean forward){super(id,x,y,23,13,"");this.forward=forward;}
        @Override public void drawButton(Minecraft mc,int mouseX,int mouseY,float partial){
            if(!visible)return;boolean hover=mouseX>=x&&mouseY>=y&&mouseX<x+width&&mouseY<y+height;
            GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);drawTexturedModalRect(x,y,hover?23:0,forward?192:205,23,13);
        }
    }
}
