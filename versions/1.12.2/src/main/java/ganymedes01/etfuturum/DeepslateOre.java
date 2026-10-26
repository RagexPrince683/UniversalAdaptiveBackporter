package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Native ore loot and experience remain authoritative, including Forge drop hooks. */
class DeepslateOre extends Block {
    final Block base;
    DeepslateOre(Block base) {
        super(Material.ROCK); this.base = base;
        setHardness(Features.enabled("functions.useStoneHardnessForDeepslate") ? 3 : 4.5F);
        setResistance(3); setHarvestLevel("pickaxe", base.getHarvestLevel(base.getDefaultState()));
    }
    @Override public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> original = NonNullList.create();
        base.getDrops(original, world, pos, base.getDefaultState(), fortune);
        for (ItemStack stack : original) {
            if (stack.getItem() == Item.getItemFromBlock(base)) {
                ItemStack replacement = new ItemStack(this, stack.getCount());
                if (stack.hasTagCompound()) replacement.setTagCompound(stack.getTagCompound().copy());
                drops.add(replacement);
            } else drops.add(stack);
        }
    }
    @Override public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) { return base.getExpDrop(base.getDefaultState(), world, pos, fortune); }
}
