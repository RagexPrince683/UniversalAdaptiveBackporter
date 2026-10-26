package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Native redstone activation without ever replacing the deepslate block with a vanilla ore. */
final class DeepslateRedstoneOre extends DeepslateOre {
    private static final PropertyBool LIT = PropertyBool.create("lit");
    DeepslateRedstoneOre() { super(Blocks.REDSTONE_ORE); setDefaultState(blockState.getBaseState().withProperty(LIT, false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, LIT); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(LIT, (meta & 1) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(LIT) ? 1 : 0; }
    @Override public int damageDropped(IBlockState state) { return 0; }
    @Override public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) { return state.getValue(LIT) ? 9 : 0; }
    private void activate(World world, BlockPos pos) {
        particles(world, pos);
        if (!world.isRemote) { world.setBlockState(pos, getDefaultState().withProperty(LIT, true), 3); world.scheduleUpdate(pos, this, 30); }
    }
    private void particles(World world, BlockPos pos) {
        for (EnumFacing face : EnumFacing.values()) if (!world.getBlockState(pos.offset(face)).isOpaqueCube()) world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX()+.5+face.getXOffset()*.5625, pos.getY()+.5+face.getYOffset()*.5625, pos.getZ()+.5+face.getZOffset()*.5625, 0, 0, 0);
    }
    @Override public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) { activate(world, pos); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) { activate(world, pos); return false; }
    @Override public void onEntityWalk(World world, BlockPos pos, Entity entity) { activate(world, pos); super.onEntityWalk(world, pos, entity); }
    @Override public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) { if (state.getValue(LIT)) particles(world, pos); }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) { if (!world.isRemote && state.getValue(LIT)) world.setBlockState(pos, getDefaultState(), 3); }
}
