package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.item.*;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Native slab placement/merging, with separate stable single and double block identities. */
abstract class NativeSlab extends BlockSlab {
    private enum Variant implements IStringSerializable { DEFAULT; @Override public String getName() { return "default"; } }
    private static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
    NativeSlab single, doubled;
    private NativeSlab(IBlockState base) {
        super(base.getMaterial());setTickRandomly(base.getBlock().getTickRandomly()); setHardness(base.getBlockHardness(null, BlockPos.ORIGIN)); setSoundType(base.getBlock().getSoundType());
        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        setDefaultState(isDouble() ? state : state.withProperty(HALF, EnumBlockHalf.BOTTOM));
    }
    static NativeSlab[] pair(IBlockState base) {
        NativeSlab single = new Half(base), doubled = new Double(base);
        single.single = doubled.single = single; single.doubled = doubled.doubled = doubled;
        return new NativeSlab[]{single, doubled};
    }
    @Override protected BlockStateContainer createBlockState() { return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, VARIANT, HALF); }
    @Override public IProperty<?> getVariantProperty() { return VARIANT; }
    @Override public Comparable<?> getTypeForItem(ItemStack stack) { return Variant.DEFAULT; }
    @Override public String getTranslationKey(int meta) { return getTranslationKey(); }
    @Override public IBlockState getStateFromMeta(int meta) { return isDouble() ? getDefaultState() : getDefaultState().withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP); }
    @Override public int getMetaFromState(IBlockState state) { return !isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0; }
    @Override public void randomTick(World world,BlockPos pos,IBlockState state,Random random){if(CopperAging.copper(this))CopperAging.tick(world,pos,state,random);}
    @Override public int damageDropped(IBlockState state) { return 0; }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return Item.getItemFromBlock(single); }
    @Override public ItemStack getItem(World world, BlockPos pos, IBlockState state) { return new ItemStack(single); }
    private static final class Half extends NativeSlab { private Half(IBlockState base) { super(base); } @Override public boolean isDouble() { return false; } }
    private static final class Double extends NativeSlab { private Double(IBlockState base) { super(base); } @Override public boolean isDouble() { return true; } }
}
