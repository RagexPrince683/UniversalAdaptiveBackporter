package ganymedes01.etfuturum.client;
import ganymedes01.etfuturum.UndyedShulkerTile;
import net.minecraft.block.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
public final class TieredShulkerStackRenderer extends TileEntityItemStackRenderer {
 private final TileEntityShulkerBox tile;
 public TieredShulkerStackRenderer(Block block){tile=block instanceof ganymedes01.etfuturum.UndyedShulkerBox?new UndyedShulkerTile():new TileEntityShulkerBox(BlockShulkerBox.getColorFromBlock(block));}
 @Override public void renderByItem(ItemStack stack,float partial){tile.loadFromNbt(stack.hasTagCompound()?stack.getTagCompound().getCompoundTag("BlockEntityTag"):new NBTTagCompound());TileEntityRendererDispatcher.instance.render(tile,0,0,0,partial);}
}
