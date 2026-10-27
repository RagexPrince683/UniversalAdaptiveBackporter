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
    private IBlockState baseState(){IBlockState copper=base.getRegistryName()!=null&&base.getRegistryName().getPath().equals("copper_ore")?OreCompatibility.copperBase():null;return copper==null?base.getDefaultState():copper;}
    @Override public int getHarvestLevel(IBlockState state){return baseState().getBlock().getHarvestLevel(baseState());}
    @Override public String getHarvestTool(IBlockState state){return baseState().getBlock().getHarvestTool(baseState());}
    @Override public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> original = NonNullList.create();
        IBlockState originalState=baseState();Block originalBlock=originalState.getBlock();originalBlock.getDrops(original,world,pos,originalState,fortune);
        for (ItemStack stack : original) {
            if (stack.getItem() == Item.getItemFromBlock(originalBlock)) {
                ItemStack replacement = new ItemStack(this, stack.getCount());
                if (stack.hasTagCompound()) replacement.setTagCompound(stack.getTagCompound().copy());
                drops.add(replacement);
            } else drops.add(stack);
        }
    }
    @Override public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) { IBlockState original=baseState();return original.getBlock().getExpDrop(original,world,pos,fortune); }
}
