package ganymedes01.etfuturum.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

final class TridentStackRenderer extends TileEntityItemStackRenderer {
    private final TridentModel model = new TridentModel();
    @Override public void renderByItem(ItemStack stack) { renderByItem(stack, 0); }
    @Override public void renderByItem(ItemStack stack, float partial) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TridentModel.TEXTURE);
        GlStateManager.pushMatrix();
        try { GlStateManager.scale(1, -1, -1); model.render(); }
        finally { GlStateManager.popMatrix(); }
    }
}
