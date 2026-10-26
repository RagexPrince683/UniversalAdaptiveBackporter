package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

final class CookingGui extends GuiContainer {
    private final CookingTile tile;
    private final InventoryPlayer player;
    CookingGui(InventoryPlayer inventory, CookingTile tile) { super(new CookingContainer(inventory, tile)); this.player = inventory; this.tile = tile; }
    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        String title = tile.getDisplayName().getUnformattedText();
        fontRenderer.drawString(title, xSize/2-fontRenderer.getStringWidth(title)/2, 6, 0x404040);
        fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize-94, 0x404040);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation(UAB.ID, "textures/gui/container/" + tile.kind() + ".png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        // Modern backgrounds use atlas sprites for progress; the native furnace supplies matching indicators.
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/furnace.png"));
        if (tile.isBurning()) {
            int flame = (int) ((long) tile.getField(0) * 13 / Math.max(1, tile.getField(1)));
            drawTexturedModalRect(guiLeft+56, guiTop+48-flame, 176, 12-flame, 14, flame+1);
        }
        int progress = tile.getField(2) * 24 / 100;
        drawTexturedModalRect(guiLeft+79, guiTop+34, 176, 14, progress+1, 16);
    }
    @Override public void drawScreen(int x, int y, float partial) { drawDefaultBackground(); super.drawScreen(x, y, partial); renderHoveredToolTip(x, y); }
}
