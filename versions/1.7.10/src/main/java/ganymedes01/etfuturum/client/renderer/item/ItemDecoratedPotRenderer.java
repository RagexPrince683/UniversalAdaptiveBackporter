package ganymedes01.etfuturum.client.renderer.item;

import ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public final class ItemDecoratedPotRenderer implements IItemRenderer {
	@Override public boolean handleRenderType(ItemStack item, ItemRenderType type) { return type != ItemRenderType.FIRST_PERSON_MAP; }
	@Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) { return true; }
	@Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		TileEntityDecoratedPot pot = new TileEntityDecoratedPot(); pot.setDesigns(stack);
		boolean centered = type == ItemRenderType.ENTITY || type == ItemRenderType.INVENTORY;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(pot, centered ? -.5 : 0, centered ? -.5 : 0, centered ? -.5 : 0, 0);
	}
}
