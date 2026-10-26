package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BarrelBlock extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool OPEN = PropertyBool.create("open");
    BarrelBlock() { super(Material.WOOD); setHardness(2.5F); setSoundType(net.minecraft.block.SoundType.WOOD); setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(OPEN, false)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING, OPEN); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7)).withProperty(OPEN, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getIndex() | (state.getValue(OPEN) ? 8 : 0); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer, EnumHand hand) { return getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite()); }
    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) { return state.withProperty(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) { return withRotation(state, mirror.toRotation(state.getValue(FACING))); }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return new BarrelTile(); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof BarrelTile) player.displayGUIChest((BarrelTile) world.getTileEntity(pos));
        return true;
    }
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.getTileEntity(pos) instanceof BarrelTile) InventoryHelper.dropInventoryItems(world, pos, (BarrelTile) world.getTileEntity(pos));
        super.breakBlock(world, pos, state);
    }
    @Override public boolean hasComparatorInputOverride(IBlockState state) { return true; }
    @Override public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return net.minecraft.inventory.Container.calcRedstone(world.getTileEntity(pos) instanceof BarrelTile ? (BarrelTile) world.getTileEntity(pos) : null);
    }
}
