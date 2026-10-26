package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/** Retains the native 1.12 wall connection and collision rules. */
final class NativeWall extends BlockWall {
    NativeWall(Block base) { super(base); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState(); }
    @Override public int getMetaFromState(IBlockState state) { return 0; }
    @Override public int damageDropped(IBlockState state) { return 0; }
    @Override public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) { items.add(new ItemStack(this)); }
}
