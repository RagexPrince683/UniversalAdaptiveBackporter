package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class BerryBush extends BlockBush implements IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    BerryBush() { setDefaultState(blockState.getBaseState().withProperty(AGE, 0)); setTickRandomly(true); setSoundType(SoundType.PLANT); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, AGE); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(AGE, Math.min(3, meta & 3)); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(AGE); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return new AxisAlignedBB(.1, 0, .1, .9, .3 + state.getValue(AGE) * .2, .9); }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return Content.item("sweet_berries"); }
    @Override public int quantityDropped(IBlockState state, int fortune, Random random) { return state.getValue(AGE) == 3 ? 2 + random.nextInt(2) : 1; }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (canGrow(world, pos, state, false) && world.getLight(pos.up()) >= 9 && random.nextInt(5) == 0) grow(world, random, pos, state);
    }
    @Override public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean client) { return state.getValue(AGE) < 3; }
    @Override public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) { return true; }
    @Override public void grow(World world, Random random, BlockPos pos, IBlockState state) { world.setBlockState(pos, state.withProperty(AGE, Math.min(3, state.getValue(AGE) + 1)), 2); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (state.getValue(AGE) < 2) return false;
        if (!world.isRemote) { spawnAsEntity(world, pos, new ItemStack(Content.item("sweet_berries"), 1 + world.rand.nextInt(2) + (state.getValue(AGE) == 3 ? 1 : 0))); world.setBlockState(pos, state.withProperty(AGE, 1), 2); }
        return true;
    }
    @Override public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return;
        entity.motionX *= .3405; entity.motionZ *= .3405; entity.fallDistance = 0;
        if (!world.isRemote && state.getValue(AGE) > 0 && (Math.abs(entity.posX - entity.lastTickPosX) >= .003 || Math.abs(entity.posZ - entity.lastTickPosZ) >= .003)) entity.attackEntityFrom(new DamageSource("sweetBerryBush"), 1);
    }
}
