package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.SmithingContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SmithingGui extends GuiContainer {
    SmithingGui(EntityPlayer player, World world, BlockPos pos) { super(new SmithingContainer(player.inventory, world, pos)); }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/smithing.png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (inventorySlots.getSlot(1).getHasStack() && inventorySlots.getSlot(2).getHasStack() && !inventorySlots.getSlot(3).getHasStack())
            drawTexturedModalRect(guiLeft + 114, guiTop + 45, 176, 0, 28, 21);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("gui.smithing.upgrade"), 60, 18, 0x404040);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, 72, 0x404040);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) { drawDefaultBackground(); super.drawScreen(mouseX, mouseY, partial); renderHoveredToolTip(mouseX, mouseY); }
}
