package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

final class WorkstationBlock extends Block {
    private static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final AxisAlignedBB SAW = new AxisAlignedBB(0, 0, 0, 1, .5625, 1);
    private final int gui;
    private final boolean cutter, hurts;
    WorkstationBlock(int gui, boolean cutter) {
        super(cutter ? Material.ROCK : Material.WOOD); this.gui = gui; this.cutter = cutter;
        hurts = cutter && Features.enabled("tweaks.stonecutterSawHurts");
        setHardness(cutter ? 3.5F : 2.5F); setResistance(cutter ? 3.5F : 2.5F); setSoundType(cutter ? SoundType.STONE : SoundType.WOOD);
        setHarvestLevel(cutter ? "pickaxe" : "axe", 0);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        if (cutter) { setLightOpacity(0); useNeighborBrightness = true; }
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta)); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getHorizontalIndex(); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer, EnumHand hand) { return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()); }
    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) { return state.withProperty(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) { return withRotation(state, mirror.toRotation(state.getValue(FACING))); }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return cutter ? SAW : FULL_BLOCK_AABB; }
    @Override public boolean isFullCube(IBlockState state) { return !cutter; }
    @Override public boolean isOpaqueCube(IBlockState state) { return !cutter; }
    @Override public BlockRenderLayer getRenderLayer() { return cutter ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID; }
    @Override public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) { if (!world.isRemote && hurts) entity.attackEntityFrom(new DamageSource("stonecutter"), 1); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) { if (!world.isRemote) player.openGui(UAB.instance, gui, world, pos.getX(), pos.getY(), pos.getZ()); return true; }
}
