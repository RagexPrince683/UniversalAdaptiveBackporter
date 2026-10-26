package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.SmithingContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SmithingGui extends GuiContainer {
    SmithingGui(EntityPlayer player, World world, BlockPos pos) { super(new SmithingContainer(player.inventory, world, pos)); }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/generic_54.png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, 71);
        drawTexturedModalRect(guiLeft, guiTop + 71, 0, 126, xSize, 96);
        // Slot frames match the container's actual positions.
        for (int x : new int[]{26, 62, 98, 144}) drawRect(guiLeft + x - 1, guiTop + 34, guiLeft + x + 17, guiTop + 52, 0xff777777);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Smithing Table", 8, 6, 0x404040);
        fontRenderer.drawString("Template", 8, 21, 0x404040);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) { drawDefaultBackground(); super.drawScreen(mouseX, mouseY, partial); renderHoveredToolTip(mouseX, mouseY); }
}
