package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.DecoratedPotTile;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
final class PotStackRenderer extends TileEntityItemStackRenderer {
    private final DecoratedPotTile tile=new DecoratedPotTile();private final DecoratedPotRenderer renderer=new DecoratedPotRenderer();
    @Override public void renderByItem(ItemStack stack,float partialTicks){tile.setDesigns(stack);renderer.render(tile,0,0,0,partialTicks,-1,1);}
}
