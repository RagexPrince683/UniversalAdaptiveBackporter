package ganymedes01.etfuturum.client.gui.inventory;

import java.util.*;
import ganymedes01.etfuturum.inventory.ContainerLectern;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public final class LecternGui extends GuiContainer {
    private static final ResourceLocation BOOK=new ResourceLocation("textures/gui/book.png");
    private static final int VISIBLE_LINES=14;
    private final ContainerLectern container;private int scroll,lastPage=-1;private List<String> lines=Collections.emptyList();private ItemStack lastBook=null;
    public LecternGui(ContainerLectern container){super(container);this.container=container;xSize=192;ySize=224;}
    @Override public void initGui(){
        super.initGui();
        buttonList.add(new PageButton(0,guiLeft+38,guiTop+156,false));
        buttonList.add(new PageButton(1,guiLeft+120,guiTop+156,true));
        buttonList.add(new GuiButton(2,guiLeft+38,guiTop+196,116,20,I18n.format("gui.lectern.take")));
        buttonList.add(new GuiButton(3,guiLeft+10,guiTop+196,20,20,"^"));
        buttonList.add(new GuiButton(4,guiLeft+162,guiTop+196,20,20,"v"));updateScreen();
    }
    @Override public void updateScreen(){super.updateScreen();ItemStack book=container.tile.getStackInSlot(0);int page=container.tile.page;if(lastPage!=page||!ItemStack.areItemStacksEqual(lastBook,book)){lastPage=page;lastBook=book==null?null:book.copy();scroll=0;String text=book!=null&&book.hasTagCompound()?book.getTagCompound().getTagList("pages",8).getStringTagAt(page):"";if(book!=null&&book.getItem()==Items.written_book)try{IChatComponent component=IChatComponent.Serializer.func_150699_a(text);if(component!=null)text=component.getFormattedText();}catch(RuntimeException ignored){}lines=fontRendererObj.listFormattedStringToWidth(text,114);}
        for(Object entry:buttonList){GuiButton button=(GuiButton)entry;if(button.id==0)button.visible=button.enabled=page>0;else if(button.id==1)button.visible=button.enabled=page+1<container.tile.pages();else if(button.id==2)button.enabled=book!=null&&mc.thePlayer.capabilities.allowEdit;else if(button.id==3){button.visible=lines.size()>VISIBLE_LINES;button.enabled=scroll>0;}else if(button.id==4){button.visible=lines.size()>VISIBLE_LINES;button.enabled=scroll+VISIBLE_LINES<lines.size();}}}
    @Override protected void actionPerformed(GuiButton button){if(button.id==3)scroll=Math.max(0,scroll-VISIBLE_LINES);else if(button.id==4)scroll=Math.min(Math.max(0,lines.size()-VISIBLE_LINES),scroll+VISIBLE_LINES);else if(container.enchantItem(mc.thePlayer,button.id))mc.playerController.sendEnchantPacket(container.windowId,button.id);updateScreen();}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);drawTexturedModalRect(guiLeft,guiTop,0,0,192,192);}
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){
        for(int i=0;i<VISIBLE_LINES&&scroll+i<lines.size();i++)fontRendererObj.drawString(lines.get(scroll+i),36,34+i*9,0);
        String page=I18n.format("book.pageIndicator",container.tile.page+1,container.tile.pages());fontRendererObj.drawString(page,156-fontRendererObj.getStringWidth(page),18,0);
    }
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0&&lines.size()>VISIBLE_LINES){scroll=Math.max(0,Math.min(lines.size()-VISIBLE_LINES,scroll+(wheel>0?-VISIBLE_LINES:VISIBLE_LINES)));updateScreen();}}
    @Override public void drawScreen(int x,int y,float partial){drawDefaultBackground();super.drawScreen(x,y,partial);}
    private static final class PageButton extends GuiButton {
        private final boolean forward;
        PageButton(int id,int x,int y,boolean forward){super(id,x,y,23,13,"");this.forward=forward;}
        @Override public void drawButton(Minecraft mc,int mouseX,int mouseY){
            if(!visible)return;boolean hover=mouseX>=xPosition&&mouseY>=yPosition&&mouseX<xPosition+width&&mouseY<yPosition+height;
            GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BOOK);drawTexturedModalRect(xPosition,yPosition,hover?23:0,forward?192:205,23,13);
        }
    }
}
