package ganymedes01.etfuturum.items;

import net.minecraft.item.ItemStack;

public final class ItemDriedKelp extends BaseFood {
	public ItemDriedKelp() { super(1, .3F, false); setNames("dried_kelp"); }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 16; }
}
