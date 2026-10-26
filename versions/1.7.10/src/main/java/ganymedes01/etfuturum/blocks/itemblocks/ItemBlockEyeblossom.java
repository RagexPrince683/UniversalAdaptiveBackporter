package ganymedes01.etfuturum.blocks.itemblocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockEyeblossom extends ItemBlock {
	public ItemBlockEyeblossom(Block block) { super(block); setHasSubtypes(true); }
	@Override public int getMetadata(int meta) { return meta & 1; }
	@Override public String getUnlocalizedName(ItemStack stack) { return super.getUnlocalizedName() + ((stack.getItemDamage() & 1) == 0 ? ".closed" : ".open"); }
}
