package ganymedes01.etfuturum.client;

import java.util.List;
import ganymedes01.etfuturum.WorkstationContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

final class WorkstationGui extends GuiContainer {
    private final WorkstationContainer container; private int page;
    WorkstationGui(WorkstationContainer container) { super(container); this.container = container; xSize = 194; ySize = 228; }
    @Override public void initGui() {
        super.initGui();
        for (int i = 0; i < 5; i++) buttonList.add(new GuiButton(i, guiLeft+9, guiTop+20+i*16, 176, 16, ""));
        buttonList.add(new GuiButton(5, guiLeft+130, guiTop+100, 24, 12, "<")); buttonList.add(new GuiButton(6, guiLeft+160, guiTop+100, 24, 12, ">")); updateButtons();
    }
    @Override public void updateScreen() { super.updateScreen(); updateButtons(); }
    private void updateButtons() {
        List<String> choices = container.choices(); page = Math.min(page, Math.max(0, (choices.size()-1)/5));
        for (GuiButton button : buttonList) if (button.id < 5) {
            int choice = page*5+button.id; button.visible = choice < choices.size();
            if (button.visible) button.displayString = (choice == container.selected ? "> " : "") + fontRenderer.trimStringToWidth(I18n.format(choices.get(choice)), 156);
        } else { button.visible = choices.size()>5; button.enabled = button.id == 5 ? page>0 : (page+1)*5<choices.size(); }
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 5) page--; else if (button.id == 6) page++;
        else { int choice = page*5+button.id; if (container.enchantItem(mc.player, choice)) mc.playerController.sendEnchantPacket(container.windowId, choice); }
        updateButtons();
    }
    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRenderer.drawString(I18n.format(container.title()), 9, 7, 0x404040);
        fontRenderer.drawString(I18n.format("container.inventory"), 16, 135, 0x404040);
        fontRenderer.drawString(fontRenderer.trimStringToWidth(I18n.format(container.title().replace("tile.", "gui.").replace(".name", ".inputs")), 116), 16, 102, 0x404040);
        fontRenderer.drawString(">", 144, 116, 0x404040);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
        drawRect(guiLeft, guiTop, guiLeft+xSize, guiTop+ySize, 0xff555555); drawRect(guiLeft+2, guiTop+2, guiLeft+xSize-2, guiTop+ySize-2, 0xffc6c6c6);
        GlStateManager.color(1,1,1,1); mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/generic_54.png"));
        for (int i = 0; i < container.inputCount; i++) drawTexturedModalRect(guiLeft+15+i*24, guiTop+111, 7, 17, 18, 18);
        drawTexturedModalRect(guiLeft+165, guiTop+111, 7, 17, 18, 18);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawTexturedModalRect(guiLeft+15+col*18, guiTop+145+row*18, 7, 17, 18, 18);
        for (int col = 0; col < 9; col++) drawTexturedModalRect(guiLeft+15+col*18, guiTop+203, 7, 17, 18, 18);
    }
    @Override public void drawScreen(int x, int y, float partial) { drawDefaultBackground(); super.drawScreen(x,y,partial); renderHoveredToolTip(x,y); }
}
