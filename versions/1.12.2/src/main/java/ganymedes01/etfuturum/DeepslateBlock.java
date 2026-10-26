package ganymedes01.etfuturum;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

final class DeepslateBlock extends BlockRotatedPillar {
    DeepslateBlock() {
        super(Material.ROCK);
        setHardness(Features.enabled("functions.useStoneHardnessForDeepslate") ? 1.5F : 3);
        setResistance(6); setHarvestLevel("pickaxe", 0);
    }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return Item.getItemFromBlock(Content.block("cobbled_deepslate")); }
    @Override public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target) {
        return target.apply(Blocks.STONE.getDefaultState()) || super.isReplaceableOreGen(state, world, pos, target);
    }
}
