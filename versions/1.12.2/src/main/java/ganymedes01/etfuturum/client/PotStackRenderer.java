package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.DecoratedPotTile;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
final class PotStackRenderer extends TileEntityItemStackRenderer {
    private final DecoratedPotTile tile=new DecoratedPotTile();
    @Override public void renderByItem(ItemStack stack,float partialTicks){tile.setDesigns(stack);TileEntityRendererDispatcher.instance.render(tile,0,0,0,partialTicks);}
}
