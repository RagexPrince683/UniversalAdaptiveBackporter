package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

final class AmethystBud extends Block {
    static final PropertyDirection FACING = PropertyDirection.create("facing");
    static final String[] STAGES = {"small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud", "amethyst_cluster"};
    final int stage;
    AmethystBud(int stage) {
        super(Material.GLASS); this.stage = stage;
        setHardness(1.5F); setResistance(1.5F); setSoundType(SoundType.GLASS); setHarvestLevel("pickaxe", 0);
        setLightLevel((stage == 0 ? 1 : stage == 1 ? 2 : stage == 2 ? 4 : 5) / 15F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta)); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getIndex(); }
    @Override public IBlockState withRotation(IBlockState state, Rotation rotation) { return state.withProperty(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public IBlockState withMirror(IBlockState state, Mirror mirror) { return withRotation(state, mirror.toRotation(state.getValue(FACING))); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float x, float y, float z, int meta, EntityLivingBase placer) { return getDefaultState().withProperty(FACING, side); }
    @Override public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) { return world.isSideSolid(pos.offset(side.getOpposite()), side); }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos from) {
        if (!canPlaceBlockOnSide(world, pos, state.getValue(FACING))) { dropBlockAsItem(world, pos, state, 0); world.setBlockToAir(pos); }
    }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public EnumPushReaction getPushReaction(IBlockState state) { return EnumPushReaction.DESTROY; }
    @Override public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) { return true; }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return stage == 3 ? Content.item("amethyst_shard") : Items.AIR; }
    @Override public int quantityDroppedWithBonus(int fortune, Random random) {
        if (stage != 3) return 0;
        EntityPlayer player = harvesters.get();
        ItemStack tool = player == null ? ItemStack.EMPTY : player.getHeldItemMainhand();
        boolean pickaxe = !tool.isEmpty() && tool.getItem().getToolClasses(tool).contains("pickaxe");
        int quantity = pickaxe ? 4 : 2;
        if (pickaxe && fortune > 0 && random.nextInt(2 + fortune) == 0) quantity += 4 * fortune;
        return quantity;
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        double inset = stage == 0 ? .25 : .1875, height = new double[]{.1875, .25, .3125, .4375}[stage];
        switch (state.getValue(FACING)) {
            case DOWN: return new AxisAlignedBB(inset, 1-height, inset, 1-inset, 1, 1-inset);
            case NORTH: return new AxisAlignedBB(inset, inset, 1-height, 1-inset, 1-inset, 1);
            case SOUTH: return new AxisAlignedBB(inset, inset, 0, 1-inset, 1-inset, height);
            case WEST: return new AxisAlignedBB(1-height, inset, inset, 1, 1-inset, 1-inset);
            case EAST: return new AxisAlignedBB(0, inset, inset, height, 1-inset, 1-inset);
            default: return new AxisAlignedBB(inset, 0, inset, 1-inset, height, 1-inset);
        }
    }
}
