package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class ComposterBlock extends Block {
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 8);
    private static final AxisAlignedBB[] BOXES = {new AxisAlignedBB(0,0,0,1,.125,1), new AxisAlignedBB(0,0,0,.125,1,1), new AxisAlignedBB(.875,0,0,1,1,1), new AxisAlignedBB(0,0,0,1,1,.125), new AxisAlignedBB(0,0,.875,1,1,1)};
    ComposterBlock() { super(Material.WOOD); Composting.bind(); setHardness(.6F); setResistance(.6F); setSoundType(SoundType.WOOD); setLightOpacity(0); useNeighborBrightness = true; setDefaultState(blockState.getBaseState().withProperty(LEVEL, 0)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, LEVEL); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(LEVEL, Math.min(8, meta)); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(LEVEL); }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return side != EnumFacing.UP; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return side == EnumFacing.UP ? BlockFaceShape.BOWL : BlockFaceShape.SOLID; }
    @Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> boxes, Entity entity, boolean actual) { for (AxisAlignedBB box : BOXES) addCollisionBoxToList(pos, mask, boxes, box); }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return new ComposterTile(); }
    public boolean add(World world, BlockPos pos, ItemStack input) {
        IBlockState state = world.getBlockState(pos); int chance = Composting.chance(input);
        if (world.isRemote || state.getBlock() != this || state.getValue(LEVEL) >= 7 || chance <= 0) return false;
        int amount = chance / 100 + (world.rand.nextInt(100) < chance % 100 ? 1 : 0), level = Math.min(7, state.getValue(LEVEL)+amount);
        if (amount > 0) world.setBlockState(pos, state.withProperty(LEVEL, level), 3);
        if (level == 7) world.scheduleUpdate(pos, this, 10 + world.rand.nextInt(10));
        world.playSound(null, pos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1, 1);
        if (amount > 0 && world instanceof net.minecraft.world.WorldServer) ((net.minecraft.world.WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, 6, .3, .3, .3, 0);
        return true;
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        int level = state.getValue(LEVEL); ItemStack held = player.getHeldItem(hand);
        if (level < 7 && Composting.chance(held) > 0) {
            if (!world.isRemote && add(world, pos, held) && !player.capabilities.isCreativeMode) held.shrink(1); return true;
        }
        if (level == 8) {
            if (!world.isRemote) { spawnAsEntity(world, pos.up(), new ItemStack(Items.DYE, 1, 15)); world.setBlockState(pos, state.withProperty(LEVEL, 0), 3); }
            return true;
        }
        return level == 7;
    }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) { if (!world.isRemote && state.getValue(LEVEL) == 7) world.setBlockState(pos, state.withProperty(LEVEL, 8), 3); }
    @Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }
    @Override public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) { return state.getValue(LEVEL); }
}
