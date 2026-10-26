package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.item.Item;

final class ModernLeaves extends BlockLeaves {
    private final String tree;
    ModernLeaves(String tree) {
        this.tree = tree;
        setDefaultState(blockState.getBaseState().withProperty(DECAYABLE, true).withProperty(CHECK_DECAY, false));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, DECAYABLE, CHECK_DECAY); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(DECAYABLE, (meta & 4) == 0).withProperty(CHECK_DECAY, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return (state.getValue(DECAYABLE) ? 0 : 4) | (state.getValue(CHECK_DECAY) ? 8 : 0); }
    @Override public BlockPlanks.EnumType getWoodType(int meta) { return BlockPlanks.EnumType.OAK; }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return Content.item(tree.endsWith("azalea") ? tree : tree + "_sapling"); }
    @Override public int damageDropped(IBlockState state) { return 0; }
    @Override public java.util.List<net.minecraft.item.ItemStack> onSheared(net.minecraft.item.ItemStack tool, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, int fortune) {
        return java.util.Collections.singletonList(new net.minecraft.item.ItemStack(this));
    }
}
