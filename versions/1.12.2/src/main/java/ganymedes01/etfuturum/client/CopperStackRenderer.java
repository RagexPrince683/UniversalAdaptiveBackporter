package ganymedes01.etfuturum.client;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
final class CopperStackRenderer extends TileEntityItemStackRenderer {
    private final CopperBlockRenderer renderer;
    CopperStackRenderer(boolean statue){renderer=new CopperBlockRenderer(statue);}
    @Override public void renderByItem(ItemStack stack,float partial){renderer.item(stack);}
}
