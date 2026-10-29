package ganymedes01.etfuturum.client.gui.inventory;

import java.util.*;
import ganymedes01.etfuturum.inventory.ContainerLectern;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public final class LecternGui extends GuiContainer {
    private final ContainerLectern container;private int scroll,lastPage=-1;private List<String> lines=Collections.emptyList();private ItemStack lastBook=null;
    public LecternGui(ContainerLectern container){super(container);this.container=container;xSize=236;ySize=230;}
    @Override public void initGui(){super.initGui();buttonList.add(new GuiButton(0,guiLeft+12,guiTop+181,25,18,"<"));buttonList.add(new GuiButton(1,guiLeft+199,guiTop+181,25,18,">"));buttonList.add(new GuiButton(2,guiLeft+62,guiTop+205,112,18,I18n.format("gui.lectern.take")));buttonList.add(new GuiButton(3,guiLeft+206,guiTop+28,18,18,"^"));buttonList.add(new GuiButton(4,guiLeft+206,guiTop+151,18,18,"v"));updateScreen();}
    @Override public void updateScreen(){super.updateScreen();ItemStack book=container.tile.getStackInSlot(0);int page=container.tile.page;if(lastPage!=page||!ItemStack.areItemStacksEqual(lastBook,book)){lastPage=page;lastBook=book==null?null:book.copy();scroll=0;String text=book!=null&&book.hasTagCompound()?book.getTagCompound().getTagList("pages",8).getStringTagAt(page):"";if(book!=null&&book.getItem()==Items.written_book)try{IChatComponent component=IChatComponent.Serializer.func_150699_a(text);if(component!=null)text=component.getFormattedText();}catch(RuntimeException ignored){}lines=fontRendererObj.listFormattedStringToWidth(text,184);}
        for(Object entry:buttonList){GuiButton button=(GuiButton)entry;if(button.id==0)button.enabled=page>0;else if(button.id==1)button.enabled=page+1<container.tile.pages();else if(button.id==2)button.enabled=book!=null&&mc.thePlayer.capabilities.allowEdit;else if(button.id==3){button.visible=lines.size()>15;button.enabled=scroll>0;}else if(button.id==4){button.visible=lines.size()>15;button.enabled=scroll+15<lines.size();}}}
    @Override protected void actionPerformed(GuiButton button){if(button.id==3)scroll=Math.max(0,scroll-15);else if(button.id==4)scroll=Math.min(Math.max(0,lines.size()-15),scroll+15);else if(container.enchantItem(mc.thePlayer,button.id))mc.playerController.sendEnchantPacket(container.windowId,button.id);updateScreen();}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xff60442e);drawRect(guiLeft+3,guiTop+3,guiLeft+xSize-3,guiTop+ySize-3,0xfff4e5bd);}
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){fontRendererObj.drawString(I18n.format("container.lectern"),12,10,0x403020);for(int i=0;i<15&&scroll+i<lines.size();i++)fontRendererObj.drawString(lines.get(scroll+i),12,28+i*9,0x403020);String page=I18n.format("book.pageIndicator",container.tile.page+1,container.tile.pages());fontRendererObj.drawString(page,(xSize-fontRendererObj.getStringWidth(page))/2,186,0x403020);}
    @Override public void drawScreen(int x,int y,float partial){drawDefaultBackground();super.drawScreen(x,y,partial);}
}
